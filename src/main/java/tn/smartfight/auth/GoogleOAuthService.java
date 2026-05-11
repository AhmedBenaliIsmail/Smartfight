package tn.smartfight.auth;

import com.sun.net.httpserver.HttpServer;
import org.mindrot.jbcrypt.BCrypt;
import tn.smartfight.config.AppConfig;
import tn.smartfight.config.DBConnection;
import tn.smartfight.dao.UserDao;
import tn.smartfight.model.User;

import javax.sql.DataSource;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OAuth2 Authorization Code + PKCE flow for desktop (JavaFX).
 *
 * Flow:
 *  1. Generate PKCE verifier/challenge
 *  2. Start a local HttpServer on localhost:8484 to catch the redirect
 *  3. Open the system browser at the Google auth URL
 *  4. Google redirects to http://localhost:8484/callback?code=...
 *  5. Exchange code for id_token via token endpoint
 *  6. Decode JWT payload → email + name
 *  7. findByEmail → return existing user, or auto-register (mirrors Symfony GoogleAuthenticator)
 *
 * IMPORTANT: http://localhost:8484/callback must be added to the list of
 * Authorised redirect URIs in the Google Cloud Console for this OAuth client.
 */
public class GoogleOAuthService {
    private static final Logger LOG = Logger.getLogger(GoogleOAuthService.class.getName());

    static final int    CALLBACK_PORT = 8484;
    static final String REDIRECT_URI  = "http://localhost:" + CALLBACK_PORT + "/callback";

    private final UserDao userDao;

    public GoogleOAuthService() { this.userDao = new UserDao(); }

    /** Performs the full OAuth flow. Returns the authenticated User or throws on failure. */
    public User authenticate() throws Exception {
        AppConfig cfg          = AppConfig.get();
        String    clientId     = cfg.googleClientId;
        String    clientSecret = cfg.googleClientSecret;

        if (clientId.isBlank() || clientSecret.isBlank()) {
            throw new IllegalStateException("Google OAuth credentials not configured in config.properties");
        }

        /* ── PKCE ────────────────────────────────────────────── */
        byte[] verifierBytes = new byte[64];
        new SecureRandom().nextBytes(verifierBytes);
        String codeVerifier = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(verifierBytes);

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] challengeBytes = md.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
        String codeChallenge = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(challengeBytes);

        String state = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(new SecureRandom().generateSeed(16));

        /* ── Local callback server ───────────────────────────── */
        CompletableFuture<String> codeFuture = new CompletableFuture<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", CALLBACK_PORT), 0);
        server.createContext("/callback", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            String code  = extractQueryParam(query, "code");
            String html  = "<html><body style='font-family:sans-serif;background:#09090b;color:#fafafa;" +
                    "display:flex;justify-content:center;align-items:center;height:100vh;margin:0'>" +
                    "<div style='text-align:center'>" +
                    "<div style='font-size:48px'>✓</div>" +
                    "<h2 style='color:#16a34a'>Signed in with Google</h2>" +
                    "<p style='color:#a1a1aa'>You can close this tab and return to SmartFight.</p>" +
                    "</div></body></html>";
            byte[] body = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.getResponseBody().close();
            exchange.close();
            server.stop(0);
            if (code != null) codeFuture.complete(code);
            else codeFuture.completeExceptionally(new Exception("No 'code' parameter in callback"));
        });
        server.start();

        /* ── Open system browser ─────────────────────────────── */
        String authUrl = "https://accounts.google.com/o/oauth2/v2/auth" +
                "?client_id="             + enc(clientId) +
                "&redirect_uri="          + enc(REDIRECT_URI) +
                "&response_type=code" +
                "&scope="                 + enc("openid email profile") +
                "&code_challenge="        + codeChallenge +
                "&code_challenge_method=S256" +
                "&state="                 + state +
                "&access_type=offline" +
                "&prompt=select_account";

        java.awt.Desktop.getDesktop().browse(new URI(authUrl));

        /* ── Wait for the auth code (30 s) ──────────────────── */
        String authCode;
        try {
            authCode = codeFuture.get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            server.stop(0);
            throw new Exception("Google sign-in timed out or was cancelled.", e);
        }

        /* ── Token exchange ──────────────────────────────────── */
        String tokenBody =
                "code="          + enc(authCode) +
                "&client_id="    + enc(clientId) +
                "&client_secret="+ enc(clientSecret) +
                "&redirect_uri=" + enc(REDIRECT_URI) +
                "&grant_type=authorization_code" +
                "&code_verifier="+ enc(codeVerifier);

        HttpClient http = HttpClient.newHttpClient();
        HttpRequest tokenReq = HttpRequest.newBuilder()
                .uri(URI.create("https://oauth2.googleapis.com/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(tokenBody))
                .build();

        HttpResponse<String> tokenResp = http.send(tokenReq, HttpResponse.BodyHandlers.ofString());
        String tokenJson = tokenResp.body();
        LOG.fine("Token response: " + tokenJson);

        String idToken = extractJsonString(tokenJson, "id_token");
        if (idToken == null) {
            throw new Exception("No id_token in Google response. " +
                    "Ensure http://localhost:" + CALLBACK_PORT + "/callback is in " +
                    "Authorised redirect URIs in Google Cloud Console.");
        }

        /* ── Decode JWT payload ──────────────────────────────── */
        String[] parts = idToken.split("\\.");
        if (parts.length < 2) throw new Exception("Malformed id_token");
        String padded = parts[1];
        int rem = padded.length() % 4;
        if (rem != 0) padded += "=".repeat(4 - rem);
        String payload = new String(Base64.getUrlDecoder().decode(padded), StandardCharsets.UTF_8);

        String email = extractJsonString(payload, "email");
        String name  = extractJsonString(payload, "name");
        if (email == null) throw new Exception("No email in Google id_token");

        /* ── Find or create user ─────────────────────────────── */
        return findOrCreate(email, name);
    }

    /* ── User lookup / auto-registration ────────────────────── */

    private User findOrCreate(String email, String name) throws Exception {
        User existing = userDao.findByEmail(email);
        if (existing != null) return existing;

        DataSource ds = DBConnection.getDataSource();

        // Derive a safe username from the display name or email prefix
        String base = (name != null && !name.isBlank())
                ? name.replaceAll("[^a-zA-Z0-9_]", "")
                : email.split("@")[0].replaceAll("[^a-zA-Z0-9_]", "");
        if (base.isBlank()) base = "fan";
        String username = uniqueUsername(base, ds);

        // Random unhashable password — user logs in via Google only
        String randomPw = BCrypt.hashpw(
                Base64.getEncoder().encodeToString(new SecureRandom().generateSeed(20)),
                BCrypt.gensalt(13));

        int newUserId;
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO users (username, password, email, createdDate, " +
                "predictionPoints, is_verified, verification_token) " +
                "VALUES (?, ?, ?, NOW(), 0, 1, NULL)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, randomPw);
            ps.setString(3, email);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new Exception("User insert returned no key");
                newUserId = keys.getInt(1);
            }
        }

        // Assign FAN role (mirrors Symfony's GoogleAuthenticator)
        int roleId = userDao.findRoleId("FAN");
        if (roleId < 0) roleId = userDao.findRoleId("ROLE_USER");
        if (roleId > 0) userDao.assignRole(newUserId, roleId);

        return userDao.findByEmail(email);
    }

    private String uniqueUsername(String base, DataSource ds) {
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM users WHERE username = ?")) {
            ps.setString(1, base);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) return base;
            }
            for (int i = 1; i <= 9999; i++) {
                String candidate = base + i;
                ps.setString(1, candidate);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) return candidate;
                }
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "uniqueUsername check failed", e);
        }
        return base + System.currentTimeMillis();
    }

    /* ── Helpers ─────────────────────────────────────────────── */

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static String extractQueryParam(String query, String key) {
        if (query == null) return null;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && kv[0].equals(key)) return kv[1];
        }
        return null;
    }

    private static String extractJsonString(String json, String key) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
        return m.find() ? m.group(1) : null;
    }
}
