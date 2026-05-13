# SmartFight Java Desktop App — Feature-by-Feature Port Plan

**Source of truth:** This plan is built from a direct read of every entity (22), every controller (25 across `src/Controller/` + `Admin/` + `Front/`), every service (15) in `src/Service/`, plus `config/packages/security.yaml`, `knpu_oauth2_client.yaml`, `mailer.yaml`, `vich_uploader.yaml`, `RegistrationType.php`, `GoogleAuthenticator.php`, `FaceIdController.php`, and `.env.example`.

**Architecture recap:** Symfony app owns the schema. Java desktop app is a second client to the same MySQL DB. Live-sync between the two is achieved by both clients reading/writing the same tables — no direct app-to-app channel. See the live-sync section in `java.md` for the network model.

---

## 0. Critical pre-flight checks (do these BEFORE writing Java code)

These three checks de-risk the entire port. Run them in the first hour.

### 0.1 Verify the current password hash format

Symfony's `security.yaml` uses `password_hashers: 'auto'`. The "auto" hasher resolves at runtime to **Argon2id** if PHP's sodium extension is available, otherwise **bcrypt**. Inspecting the legacy `smartfight.sql` dump shows existing hashes in the `$2y$13$...` format (bcrypt, cost 13). **But the current Doctrine-managed `users` table may have produced different hashes** depending on the PHP environment used when each user registered.

**Action:**
```sql
SELECT userId, username, password FROM users LIMIT 3;
```
- Hashes starting with `$2y$13$` → **bcrypt cost 13**
- Hashes starting with `$argon2id$` → **Argon2id**
- Mixed → support both in Java (check prefix, dispatch to right verifier)

### 0.2 Dump the current Doctrine schema

```bash
php bin/console doctrine:schema:create --dump-sql > docs/current-schema.sql
```
This is the contract Java codes against. **Lock it.** Any further Symfony schema migration is a coordinated event that requires updating Java DAOs.

### 0.3 Test cross-PC DB connectivity early

Before any Java is written, prove three things from the JavaFX PC against the DB PC:
1. `mysql -h <DB_PC_IP> -u smartfight -p smartfight` connects.
2. A user inserted via Symfony (PC #2) is visible from `mysql` on PC #3.
3. Port 3306 is open in firewall on the DB PC.

If any of these fails, no Java code matters yet.

---

## 1. Stack and project layout

### 1.1 Locked stack

| Layer | Choice | Library / Version |
|---|---|---|
| Language | Java 17 | OpenJDK 17 LTS |
| UI | JavaFX 21 | `javafx-controls`, `javafx-fxml`, `javafx-media`, `javafx-web` |
| Build | Maven | `org.openjfx:javafx-maven-plugin` |
| DB driver | MySQL Connector/J | `com.mysql:mysql-connector-j:8.x` |
| Connection pool | HikariCP | `com.zaxxer:HikariCP` |
| Password hashing | bcrypt + Argon2id | `org.mindrot:jbcrypt` + `de.mkammerer:argon2-jvm` (or BouncyCastle) |
| QR codes | ZXing | `com.google.zxing:core` + `com.google.zxing:javase` |
| PDF | OpenPDF (LGPL) | `com.github.librepdf:openpdf` (or Flying Saucer + iText) |
| HTTP (DeepSeek) | Java 17 `HttpClient` | built-in |
| JSON | Jackson | `com.fasterxml.jackson.core:jackson-databind` |
| Email (optional) | Jakarta Mail | `jakarta.mail:jakarta.mail-api` + `org.eclipse.angus:jakarta.mail` |
| Logging | SLF4J + Logback | `ch.qos.logback:logback-classic` |
| Testing | JUnit 5 | `org.junit.jupiter:junit-jupiter` |

### 1.2 Project structure

```
smartfight-desktop/
├── pom.xml
├── src/main/java/tn/smartfight/
│   ├── App.java                            # JavaFX entry point
│   ├── config/
│   │   ├── DBConnection.java               # HikariCP DataSource
│   │   ├── AppConfig.java                  # loads config.properties
│   │   └── Session.java                    # current logged-in user
│   ├── model/                              # POJOs (one per Doctrine entity)
│   ├── dao/                                # JDBC repositories
│   ├── service/                            # business logic & algorithms
│   ├── controller/                         # FXML controllers
│   ├── auth/
│   │   ├── PasswordVerifier.java           # bcrypt + argon2 dispatch
│   │   └── FaceIdRecognizer.java           # webcam capture + match
│   ├── integration/
│   │   ├── DeepSeekClient.java             # HTTP wrapper
│   │   ├── GmailMailer.java                # SMTP sender
│   │   └── QrCodeGenerator.java            # ZXing wrapper
│   └── util/
│       ├── PdfBuilder.java
│       ├── CsvWriter.java
│       └── AlertHelper.java
├── src/main/resources/
│   ├── config.properties                   # externalized DB / SMTP / API key
│   ├── tn/smartfight/views/                # FXML files
│   └── tn/smartfight/styles/smartfight.css
└── src/test/java/...
```

`config.properties` skeleton:
```properties
db.url=jdbc:mysql://192.168.1.10:3306/smartfight?useSSL=false&serverTimezone=UTC
db.user=smartfight
db.password=changeme
mail.smtp.host=smtp.gmail.com
mail.smtp.port=587
mail.smtp.user=teamsmartfight@gmail.com
mail.smtp.password=uucxnpkizyclppsr
mail.from=teamsmartfight@gmail.com
deepseek.api.key=sk-23d74dc6f4f14f5d8020ee22ff1939b4
deepseek.api.url=https://api.deepseek.com/chat/completions
upload.dir=C:/SmartFightShared/uploads
```

---

## 2. Database schema reference (the contract)

The Symfony schema mixes naming conventions in one project. **This is the single most common source of silent JDBC failures.** Always run `SHOW COLUMNS FROM <table>` before writing a DAO.

### 2.1 Tables with **camelCase** PK columns
| Table | PK column |
|---|---|
| `events` | `eventId` |
| `fighters` | `fighterId` |
| `users` | `userId` |
| `roles` | `roleId` |
| `notifications` | `notificationId` |
| `predictions` | `predictionId` |
| `fight_results` | `resultId` |

### 2.2 Tables with **snake_case** PK columns
All others (`blog_article`, `blog_category`, `discipline`, `event_booking`, `fan_preference`, `fan_profile`, `fan_reaction`, `fan_vote`, `fight_statistic`, `fighter_contract`, `match_proposal`, `performance_score`, `ranking`, `weight_division`) → PK is `id`.

### 2.3 camelCase data columns to watch
- `fighters`: `koWins`, `decisionWins`, `eloRating`, `performanceScore`, `winStreak`, `strengthOfSchedule`, `titleDefenses`
- `fight_results`: `fightNumber`, `fighter1Id`, `fighter2Id`, `winnerId`, `methodOfVictory`, `roundNumber`, `fightDate`, `eventId`
- `events`: `eventName`, `eventDate`
- `users`: `username`, `password` (snake_case fallback for newer fields like `is_verified`, `reset_token`, `webauthn_credential_id`, `face_photo`)
- `notifications`: `userId`, `created_at`, `is_read`, `type`
- `predictions`: `userId`, `fightId`, `predictedWinnerId`, `predicted_method`, `predicted_round`

### 2.4 Join tables
- `user_roles` (M2M) → columns `userId`, `roleId`.

### 2.5 Lifecycle behavior Java must replicate manually (Doctrine PrePersist/PreUpdate)
| Entity | On INSERT | On UPDATE |
|---|---|---|
| BlogArticle | set `created_at`, `updated_at` | set `updated_at` |
| BlogCategory | same | same |
| EventBooking | set `created_at`, `updated_at`, fallback `booking_date` if null | set `updated_at` |
| FightResult | set `updated_at` | set `updated_at` |

Constructor-set defaults Java must replicate:
- `FanReaction.reacted_at = now`
- `FanVote.voted_at = now`
- `Notification.created_at = now`
- `Prediction.created_at = now`
- `User.createdDate = now, is_verified = false`

**Failure mode:** Forgetting these will cause `NOT NULL` constraint violations OR records with stale timestamps that Symfony's queries depend on.

### 2.6 Implicit defaults Java must respect
- `Event.organization` default `'INDEPENDENT'`, `status` default `'SCHEDULED'`, `visibility` default `'PUBLIC'`.
- `Fighter.eloRating` default `1500.0`, `strengthOfSchedule` default `1500.0`, `performanceScore` default `0.0`.
- `Ranking.organization` default `'MEDIA'`, `rank_position` default `999`.
- `EventBooking.booking_status` default `'CONFIRMED'`, `ticket_quantity` default `1`, `total_price` default `'0.00'`, `ticket_type` default `'REGULAR_SEATING'`.

### 2.7 Enum-like VARCHAR columns (DB doesn't enforce — Java service layer must)
- `Event.organization` ∈ {WBC, WBA, IBF, WBO, INDEPENDENT, FAN CHOICE}
- `Event.status` ∈ {SCHEDULED, LIVE, COMPLETED, CANCELLED}
- `FightResult.methodOfVictory` ∈ {KO, DQ, DRAW, DECISION}
- `FightResult.decision_type` ∈ {UD, SD, MD}
- `FightResult.belt_organization` ∈ {WBC, WBA, IBF, WBO, UNDISPUTED}
- `EventBooking.ticket_type` ∈ {VIP_RINGSIDE, PREMIUM_LOWER, REGULAR_SEATING, BALCONY, STANDING_ROOM}
- `EventBooking.booking_status` ∈ {CONFIRMED, CANCELLED}
- `FanReaction.reaction_type` ∈ {FIRE, SHOCK, RESPECT, DOMINANT, CONTROVERSIAL}
- `MatchProposal.status` ∈ {PENDING, APPROVED, REJECTED}
- `Ranking.organization` ∈ {WBC, WBA, IBF, WBO, MEDIA}
- `BlogArticle.status` ∈ {DRAFT, PUBLISHED, ARCHIVED} (only DRAFT confirmed in entity; other values used by Symfony admin)

### 2.8 Foreign key cascades
**Only ONE cascade is configured** in the schema:
- `fan_vote.match_proposal_id` → `match_proposal.id` ON DELETE CASCADE.

Every other delete will fail with FK violations unless rows are deleted in the right order. The Java app **must** delete dependent rows first or use `ON DELETE SET NULL` in code (not in schema).

---

## 3. Authentication features

### 3.1 Username + password login

**Symfony source:** `SecurityController::login`, firewall config in `security.yaml`. Login is by `username` (NOT email — email is for notifications only). Hashes are bcrypt cost 13 (`$2y$13$...`) per the existing DB; **may be Argon2id** in some accounts depending on PHP environment when registered.

**Java implementation:**
```java
public class PasswordVerifier {
    private final Argon2 argon2 = Argon2Factory.create();
    public boolean verify(String rawPassword, String storedHash) {
        if (storedHash.startsWith("$argon2")) {
            return argon2.verify(storedHash, rawPassword.toCharArray());
        }
        if (storedHash.startsWith("$2y$") || storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$")) {
            String compat = storedHash.startsWith("$2y$") ? "$2a$" + storedHash.substring(4) : storedHash;
            return BCrypt.checkpw(rawPassword, compat);
        }
        return false;
    }
}
```

**Test before building login UI:**
```java
@Test void verifyAgainstSymfonyHash() {
    String hash = "$2y$13$7LbxaT3vbDsRHy0Dhvqu/uFhcO2BrVBRgfO/p2nx3oq6AaCY8ePdC"; // from DB
    assertTrue(new PasswordVerifier().verify("knownPassword", hash));
}
```

**Risk:** jBCrypt rejects `$2y$` prefix outright in some versions. The compat conversion above is essential.

### 3.2 Registration

**Symfony source:** `SecurityController::register` + `RegistrationType` form (fields: `username`, `email`, `password` as RepeatedType — confirm field). On submit:
1. Generate 32-byte hex `verificationToken` (`bin2hex(random_bytes(16))`)
2. `is_verified = false`
3. Hash password (`$passwordHasher->hashPassword()`)
4. Attach `Role` with `roleName = 'USER'`
5. Send `emails/verification.html.twig` via Symfony Mailer with link `/verify-email/{token}` from sender `mahdidaly24@gmail.com`

**Java implementation:**
- FXML form with three fields + confirm.
- Hash with bcrypt cost 13: `BCrypt.hashpw(password, BCrypt.gensalt(13))`. Symfony's verifier accepts `$2a$`/`$2b$`/`$2y$` interchangeably.
- INSERT into `users`: `userId` (auto), `username`, `password`, `email`, `createdDate=now`, `predictionPoints=0`, `is_verified=0`, `verification_token=<32 hex>`, `face_photo=null`, `webauthn_credential_id=null`, `webauthn_public_key=null`.
- INSERT into `user_roles`: `(userId, roleId WHERE roleName='USER')`.
- Send verification email via SMTP (see §3.5).

**Risk:** the Role with `roleName='USER'` must exist. Run a one-time bootstrap to seed roles `USER`, `ADMIN`, `SUPER_ADMIN` (and `FAN` per `GoogleAuthenticator`'s lookup) if missing.

### 3.3 Forgot password / reset password

**Symfony source:** `SecurityController::forgotPassword` + `resetPassword`. Look up user by email OR username. Set `reset_token` (32 hex bytes), `reset_token_expires_at = now + 1 hour`. Email link `/reset-password/{token}`. Template `emails/password_reset.html.twig`.

**Recommendation:** show a "Forgot password? Use the web app" link inside JavaFX login. Implementing a desktop reset flow with email-clickable links is more work than it's worth for the demo.

### 3.4 Email verification (post-registration)

The verification link in email goes to `/verify-email/{token}` on the **Symfony web app**. Java sets the token and sends the email; user clicks the link in their browser; Symfony marks them verified. Java just queries `is_verified = 1` before allowing login.

### 3.5 Gmail SMTP setup (for verification & reset emails)

**Symfony source:** `mailer.yaml` reads `MAILER_DSN`. Current DSN:
```
smtp://teamsmartfight%40gmail.com:uucxnpkizyclppsr@smtp.gmail.com:587
```
That is a Gmail App Password (16 chars). The user `teamsmartfight@gmail.com` has 2FA enabled and an App Password was generated for SMTP.

**Java implementation with Jakarta Mail:**
```java
public class GmailMailer {
    private final Properties props = new Properties();
    public GmailMailer(AppConfig cfg) {
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
    }
    public void send(String to, String subject, String htmlBody) throws MessagingException {
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(cfg.smtpUser, cfg.smtpAppPassword);
            }
        });
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(cfg.mailFrom));
        msg.setRecipients(Message.RecipientType.TO, to);
        msg.setSubject(subject);
        msg.setContent(htmlBody, "text/html; charset=UTF-8");
        Transport.send(msg);
    }
}
```

**Setup steps for demo:**
1. The Gmail App Password from `.env` (`uucxnpkizyclppsr`) is reusable from Java — no new account needed.
2. **CRITICAL:** Gmail blocks SMTP from "less secure apps" by default. The existing `.env` proves this app password works because Symfony has been sending emails with it. Java will too.
3. If the app password ever stops working: Google Account → Security → 2-Step Verification → App passwords → generate new 16-char password → update `config.properties`.
4. Demo-day risk: if the demo PC has no internet OR Gmail's outbound IP rate-limits, emails fail silently. Always log SMTP errors loudly.

### 3.6 Google OAuth login (`/connect/google`)

**Symfony source:** `GoogleAuthenticator.php` + `knpu_oauth2_client.yaml`. Flow:
1. User clicks "Login with Google" → redirect to Google OAuth consent (`scope: email, profile`)
2. Google redirects to `/connect/google/check?code=...`
3. Symfony exchanges code for access token, fetches Google user profile
4. Look up User by email; if exists → log in; else create with random password, `is_verified = true`, role `FAN` (fallback `USER`)
5. Redirect to `app_dashboard`

**Java implementation considerations:**

This is **awkward in a desktop app** because OAuth requires a browser redirect to a public callback URL. Two viable approaches:

**Option A — Skip in JavaFX (recommended for uni demo).** Justification: the user already has Google OAuth working in Symfony. JavaFX is for staff/admin desktop use; they'd log in with their username + password. **State this explicitly in the demo: "Google login is on the web app; the desktop app uses local credentials."**

**Option B — Implement with embedded browser + loopback callback.** Steps:
1. Use `javafx.scene.web.WebView` to load `https://accounts.google.com/o/oauth2/v2/auth?client_id=...&redirect_uri=http://localhost:53682/callback&scope=email+profile&response_type=code`
2. Start a tiny HTTP server (`com.sun.net.httpserver.HttpServer`) on `localhost:53682` to capture the `?code=...` callback.
3. Exchange code for token via direct HTTP POST to `https://oauth2.googleapis.com/token`.
4. Fetch profile from `https://www.googleapis.com/oauth2/v2/userinfo`.
5. Look up / create user as in `GoogleAuthenticator.php`.

**You must add `http://localhost:53682/callback` as an Authorized Redirect URI in the Google Cloud Console** under the OAuth client. The current redirect URI is `http://localhost:8001/connect/google/check` — you'd add the loopback one alongside.

**Risk:** brittle. Recommend Option A unless the teacher specifically asks for Google login in the desktop client.

### 3.7 Face ID login

**Symfony source:** `FaceIdController.php`. Three modes:
1. **Register** (`/face-id/register`) — POST `{credentialId, publicKey?}`. Stores on `User.webauthn_credential_id` + `webauthn_public_key`.
2. **Login** (`/face-id/login`) — POST `{credentialId}`. Look up user; if found, `Security::login(...)`.
3. **Save photo** (`/face-id/save-photo`) — POST `{photo}` (base64 image). Stores in `User.face_photo`.
4. **Verify visual** (`/face-id/verify-visual`) — DEMO: requires `photo` ≥500 chars, then logs in **the FIRST user with any face_photo, ordered by userId ASC**. **This is not real face matching.**

**Important truth:** the existing Symfony "face ID" is a **demo prop**, not a biometric system. The web app uses face-api.js or similar in the browser to fake a credential ID, then logs in by string comparison.

**Java implementation strategy:**

For a uni demo, you have two choices:

**Option A (matches current Symfony behavior — simple).**
- Use JavaFX webcam capture (via Webcam Capture API or OpenCV).
- Save the captured image as base64 in `User.face_photo`.
- "Login" = capture a photo, then read the first user with any `face_photo` and log them in. This is identical to Symfony's `verify-visual` route — both apps are "fake" but consistent.

**Option B (real face matching, more impressive).**
- Use OpenCV with Haar cascade or LBPH face recognizer (`org.openpnp:opencv` in Maven).
- On register: capture N photos, train an LBPH model, store the model file + label.
- On login: capture, classify, find user with matching label.
- Far more code, but actually demonstrates biometrics.

**Recommendation:** Option A for parity with Symfony + reliability, OR Option B if you want a "wow" moment for the teacher. **Decide before Phase 11 of the build.**

**Webcam access in JavaFX:** JavaFX has no native webcam API. Use:
- [Webcam Capture API](https://github.com/sarxos/webcam-capture) — `com.github.sarxos:webcam-capture:0.3.12`
- Or OpenCV: `org.openpnp:opencv:4.7.0-0`

**Setup steps:**
1. Add `webcam-capture` to `pom.xml`.
2. On Windows, no driver setup needed — uses DirectShow.
3. Test capture in a 10-line standalone main before integrating.

### 3.8 Logout

**Symfony source:** `app_logout` route, handled by firewall.
**Java equivalent:** `Session.clear()` and switch back to `Login.fxml`. No DB action required.

---

## 4. Admin back-office features

Below is the port plan for each, grouped logically.

### 4.1 Dashboard (`/`)

**Symfony source:** `DashboardController.php`. If `!ROLE_ADMIN` → redirect to fan dashboard.

Aggregates:
- Counts: fighters, events, fights, users, articles
- Last 7 days revenue (from `EventBookingRepository::getDailyRevenueLast7Days`)
- Last 7 days user registrations (`UserRepository::getDailyRegistrationsLast7Days`)
- Last 5 fight results
- 5 upcoming events
- Financial summary (`SUM total_price WHERE booking_status='CONFIRMED'`, contracts paid/pending, etc.)
- Top 3 voted match proposals

**Java implementation:**
- `DashboardController.java` with multiple `Label`s for counts, two `LineChart`s (revenue + registrations), a `TableView` for upcoming events.
- Use a single `DashboardService.loadStats()` method that fires all queries in one shot (or in a `Task<>` background thread).

**SQL examples (verify column names against your DB first):**
```sql
SELECT DATE(booking_date) AS day, COALESCE(SUM(total_price),0) AS revenue
FROM event_booking
WHERE booking_status='CONFIRMED' AND booking_date >= CURDATE() - INTERVAL 7 DAY
GROUP BY DATE(booking_date) ORDER BY day;

SELECT DATE(createdDate) AS day, COUNT(*) AS n
FROM users
WHERE createdDate >= CURDATE() - INTERVAL 7 DAY
GROUP BY DATE(createdDate) ORDER BY day;
```

### 4.2 Fighter management (`/fighters`)

**Symfony source:** `FighterController.php`. Actions: list (paginated, search), new, edit, delete, recalc-rankings, generate-AI-profile.

**Photo upload:** `FighterController` uses **manual** `UploadedFile::move()` to `public/uploads/boxers/<uniqid>.<ext>` — NOT Vich.

**Java implementation:**
- `FighterListController` with `TableView<Fighter>`, search field, refresh button, "Recalc Rankings" button (calls `RankingService.recomputeAllRankings`).
- `FighterFormController` with all fields:
  - firstName, lastName, nickname, weightDivision (ComboBox of WeightDivision), nationality (ISO-2 country code dropdown), wins/losses/draws/koWins/technicalWins/decisionWins, koLosses, height, reach, weight, age, manager (ComboBox of User), photo (FileChooser).
- Photo upload: copy to **shared upload directory** (see §8) so both apps see the same file. Filename pattern: `<uniqid>.<ext>`.
- "Generate AI profile" button → `AIService.generateFighterProfile()` → updates `ai_style_tag` and `ai_description`.

**Risk:** the photo path. If JavaFX writes to `C:/SmartFightShared/uploads/boxers/` and Symfony reads from `public/uploads/boxers/` on a different PC, the image won't display in Symfony. See §8 for the shared-storage solution.

### 4.3 Event management (`/events`)

**Symfony source:** `EventController.php`. Actions: list (KnpPaginator 6/page, search), autocomplete (JSON), champions filter, new, champions-event-new (auto-schedules 3 fights), edit, delete, fights-tab, add-fight, ai-apply, fight-delete, ai-matchmake, fan-favorite-create.

**Champions event auto-schedule:** picks 3 distinct WeightDivisions, fetches top-2 ranked fighters per division, calls `FightResultService.addScheduledFight` for each.

**Fan Favorite Night:** picks top 3 PENDING `MatchProposal` by voteCount, creates Event "Fan Favorite Night — <date>" at `Community Arena`, dated +14 days, organization=`FAN CHOICE`, schedules 3 fights from those proposals, marks them APPROVED, links `proposal.event = event`.

**Java implementation:**
- `EventListController` + `EventFormController` + `EventFightsController` (fight card editor).
- "AI Matchmake" button on fight card screen — calls local 8-vector algorithm (no DeepSeek, deterministic) — the `MatchmakingService.computeScoreIA` formula (see §5.7).
- "Create Fan Favorite Night" button on dashboard.
- Hard cap of 3 fights per event (enforced in `FightResultService.addScheduledFight`).

### 4.4 Fight result management (`/results`)

**Symfony source:** `ResultController.php`. Actions: list, schedule, enter (with optional video upload), stats (per-round detailed), cancel, delete, show, export-csv, manage-card, generate-AI-stats, generate-round-commentary, export-pdf.

**Result entry triggers RankingService.processCompletedFight()** which:
1. Updates fighter records (wins/losses/draws/koWins/decisionWins)
2. Recomputes ELO via `calculateElo`
3. Updates `lastFightDate`, `winStreak`, `performanceScore`, `strengthOfSchedule`
4. Triggers `PredictionService.processPredictionsForFight` (awards points to users who predicted correctly)
5. Wipes and rebuilds the entire `ranking` table for all 5 organizations
6. Broadcasts notifications to all users with ROLE_USER

**This is the highest-value, highest-risk feature.** The math must match exactly (see §5.6 and §5.10 for formulas).

**Java implementation:**
- `ResultEnterController` form: fighter1/fighter2 (read-only), winner radio (or "Draw"), method (KO/DECISION/DQ/DRAW), round (numeric), decision_type (UD/SD/MD if DECISION), highlightVideoUrl, videoFile (optional upload).
- On submit → `FightResultService.enterResult()` → triggers full ranking pipeline.
- Per-round stats screen (`FightStatisticController`): pair entry of CompuBox-shaped stats per round per fighter, with strict validation (`landed <= thrown`, `right + left = total thrown`, etc.).

### 4.5 Fight statistics (`/stats`)

**Symfony source:** `FightStatisticController.php`. 3-phase wizard: select event → select fight → enter round-by-round stats.

**Validation rules (port literally):**
- `punchesLanded ≤ punchesThrown`
- `powerPunchesLanded ≤ powerPunchesThrown`
- `jabsLanded ≤ jabsThrown`
- `uppercutsLanded ≤ uppercutsThrown`
- `rightHandLanded + leftHandLanded == punchesLanded`
- `rightHandThrown + leftHandThrown == punchesThrown`
- `uppercutsThrown ≤ punchesThrown`

After save, `FightStatisticService.recalculateForFighter` triggers `AnalyticsEngine.calculatePerformanceScore` for each fighter (see §5.2).

### 4.6 Rankings (`/rankings`)

**Symfony source:** `RankingController.php`. Public list, mobile view, PDF export, recalc, CSV export, QR-download.

**The `/rankings/mobile` and `/rankings/pdf` routes are PUBLIC** — they're meant to be accessed by phones scanning a QR code from a printed ranking sheet.

**Java implementation:**
- `RankingListController`: table grouped by weight division, sortable.
- "Generate QR" button: produces ZXing QR pointing to `http://<symfony-pc-ip>:8001/rankings/mobile`. **The QR must point to the Symfony server**, NOT the Java desktop, since phones scan it.
- "Export PDF" button: locally render PDF via OpenPDF/Flying Saucer.
- "Recalc" button: `RankingService.recomputeAllRankings()` (full schema-wide rebuild — see §5.10).

### 4.7 Performance / injury predictions (`/performance`)

**Symfony source:** `PerformanceController.php`. Actions: list, injury-predictions (calls `AIService.predictInjuryRisk` for every fighter — slow!), injury-pdf, recalc, API endpoint, fighter detail.

**Java implementation:**
- `PerformanceListController`: sortable table.
- "Injury Predictions" tab: runs in background `Task` (it's slow due to per-fighter HTTP calls), shows progress bar.
- AI fallback: each call returns either real DeepSeek result or heuristic — the heuristic is deterministic and fast (see §5.1.7).

### 4.8 User management (`/users`, `/users/profile`)

**Symfony source:** `UserController.php`.

**Java implementation:**
- Admin sees user list + new/update/password/delete actions.
- Non-admin sees `/profile` only — can update own info + password.
- Username uniqueness check before INSERT/UPDATE.

### 4.9 Blog (`/admin/blog/*` and `/blog`)

**Symfony source:** `Admin/BlogController.php` (CRUD) + `Front/BlogController.php` (public list/show + view increment).

**Form:** `BlogArticleType` uses VichUploader for image (mapping `blog_image` → `public/uploads/blog/images`) and video (mapping `blog_video` → `public/uploads/blog/videos`). Files use `UniqidNamer`.

**Java implementation:**
- `BlogListController` (admin): table with filter by category, status.
- `BlogFormController`: title, summary, content (TextArea or HTMLEditor), category (ComboBox), status (ComboBox: DRAFT/PUBLISHED/ARCHIVED), image upload, video upload.
- File uploads: copy to shared `uploads/blog/images/` with `UUID.randomUUID().toString()` prefix to mimic Vich's `UniqidNamer`. Store filename only in DB.
- Public view (front-office in JavaFX): `BlogPublicController` increments `view_count`.

### 4.10 Bookings (`/admin/bookings` and `/booking`)

**Symfony source:** `Admin/BookingAdminController.php` (admin: list, cancel, manual create, QR), `Front/BookingController.php` (fan: list events, book, my bookings, cancel, QR).

**Booking creation:** `BookingService.createBooking()` validates capacity and quantity (1–4), generates `booking_reference = 'SF-' + 8 hex chars`, sets total_price from dynamic pricing, sends email with embedded QR PNG, dispatches a notification.

**Dynamic pricing formula** (see §5.3 for exact constants):
```
final = base × eloMultiplier × championsMultiplier × fanDiscountMultiplier × loyaltyMultiplier
```

**Java implementation:**
- `BookingFormController` for fans: select event, ticket type, quantity → calls `BookingService.createBooking`.
- `MyBookingsController`: shows bookings, embedded QR preview (ZXing → ImageView), cancel button.
- Admin variants: `BookingAdminController` with all-bookings table, manual create form, QR view.

**Email + QR:** the booking confirmation email matches `templates/emails/booking_confirmation.html.twig`. The QR encodes a fixed multiline string (see §5.13).

### 4.11 Finance (`/admin/finance`)

**Symfony source:** `Admin/FinanceAdminController.php`. Aggregates revenue per event, daily revenue last 30 days, revenue by ticket type — all SQL aggregation.

**Java:** straightforward SQL + bar/pie charts with JavaFX Charts.

### 4.12 Reactions (`/admin/reactions` + `/reactions`)

**Symfony source:** `Admin/ReactionController.php` + `Front/ReactionController.php`. Fans post reactions to fights with type ∈ {FIRE, SHOCK, RESPECT, DOMINANT, CONTROVERSIAL} and a comment ≤140 chars. Admin can pin/soft-delete.

**Java:**
- `FanReactionController`: list last 10 COMPLETED fights + recent reactions; form to post a reaction.
- Admin tab adds pin and delete buttons.

### 4.13 Contracts (`/admin/contracts`)

**Symfony source:** `Admin/AdminContractController.php`. List, create, pay, delete, toggle-weight, PDF (single + list).

**Calculation conflict — flagged for resolution:** `ContractService.calculateFinalPurse` applies `basePay *= 0.8` if `missed_weight=true`; but `FightResultService.enterResult` does NOT apply this when auto-paying. The Java port should **always use `ContractService.calculateFinalPurse`** for consistency.

**Java implementation:**
- `ContractListController` with create/pay/delete/toggle-weight buttons.
- "Generate PDF" button per contract → uses `templates/admin/contract/pdf.html.twig` shape; in Java, hand-build the PDF with OpenPDF.

### 4.14 Match proposals (`/admin/match-proposals` and `/admin/proposals`)

**Symfony has TWO duplicate controllers** for proposals: `Admin/MatchProposalController.php` (`/admin/match-proposals`) and `Admin/MatchProposalAdminController.php` (`/admin/proposals`). They render the same template and largely overlap.

**Java:** consolidate to one screen. `MatchProposalController` lists by voteCount, allows manual create, AI-generate (calls `MatchmakingService.generateProposalsLocal` → 8-vector algorithm), approve, reject, delete.

---

## 5. Service-layer algorithms (the math you cannot get wrong)

These formulas drive ranking, predictions, payouts, matchmaking. Both apps must produce **identical** results for the same input or the demo desyncs.

### 5.1 AIService

- Wraps `https://api.deepseek.com/chat/completions` over HTTPS, model `deepseek-chat`.
- 30-second timeout.
- All AI methods compute a deterministic heuristic baseline first; if the API fails OR the API key is missing, return the heuristic with `is_fallback: true`.

#### 5.1.1 `analyzeFightDynamics(f1, f2)` heuristic
```
weights: elo 0.45, phys (reach+height) 0.15, lethality (ko_rate) 0.20, momentum (wins-losses) 0.20
For each vector v:
  diff = v.f1 − v.f2
  norm = 50 + (diff / max(1, |diff|*0.1))
  norm_clamped = clamp(norm, 0, 100)
  f1Score += norm_clamped * weight
f2Score = 100 − f1Score
edge: >55 "Technical Superiority", <45 "Underdog Momentum", else "Kinetic Parity"
```

#### 5.1.2 `predictInjuryRisk(data)` heuristic
```
ageFactor    = max(0, (age − 30) * 3)
loadFactor   = total_fights * 0.8
traumaFactor = ko_losses * 15
totalRisk    = min(95, 10 + ageFactor + loadFactor + traumaFactor)
risk_level   = >70 High, >35 Medium, else Low
zones        = ["Hand/Wrist", "Rib Cage", "Orbital Bone", "Ankle"]
zone         = zones[(age + total_fights) % 4]
days_to_alert       = max(5, 30 − round(totalRisk / 3))
mitigation_strategy = totalRisk > 50 ? "Rest and PT 2x/day" : "Standard maintenance"
accuracy = 92 + (random(0..50) / 10)
roi      = 300 + total_fights * 10
```

#### 5.1.3 `generateFighterProfile` fallback
```
ko_rate > 70  → "Power Puncher"
wins > 15     → "Veteran Technician"
else          → "Rising Contender"
```

#### 5.1.4 `generateText` fallback (verbatim string)
> "Neural Insight: Statistical patterns indicate a high-level tactical exchange. The winner demonstrated superior range control and kinetic efficiency throughout the encounter."

#### 5.1.5 Other fallbacks
`generateHeuristicRecap`, `generateHeuristicScouting`, `generateHeuristicComparison`, `generateHeuristicStats` — string templates with light math. Port verbatim.

#### 5.1.6 DeepSeek wire format
```
POST https://api.deepseek.com/chat/completions
Headers: Authorization: Bearer <key>; Content-Type: application/json
Body: {"model":"deepseek-chat","messages":[{"role":"system","content":"..."},{"role":"user","content":"..."}],"max_tokens":N,"temperature":T}
Response: {"choices":[{"message":{"content":"<json string>"}}]}
```
Strip ```` ```json ```` fences from the content before `JSON.parse`.

#### 5.1.7 Bug to avoid replicating
`MatchmakingService::suggestMatches()` calls `$this->aiService->suggestMatches(...)` which **does not exist on AIService**. Symfony throws at runtime if AI matchmaking is invoked. The Java port should use `MatchmakingService.generateProposalsLocal` (the local 8-vector algorithm) for matchmaking; do not implement the AI matchmaking path.

### 5.2 AnalyticsEngine — performance score
```
For each completed fight by fighter:
  totalStats = stat row with round IS NULL (or first row)
  fightScore = (punchAccuracy * 0.5) + (powerAccuracy * 0.3) + (kdBonus * 0.2)
  kdBonus    = clamp(knockdowns * 15.0, 0, 45.0)
  if !winner && method=="DQ": fightScore −= 50.0
  if isBeltFight: fightScore *= isWinner ? 1.5 : 1.25
  accumulator += fightScore

avgScore = accumulator / fightCount
performanceScore = round(avgScore, 2)
```
Side effects: writes `fighters.performanceScore` and a new `performance_score` row.

### 5.3 BookingService — dynamic pricing
```
basePrice (USD):
  VIP_RINGSIDE     200.00
  PREMIUM_LOWER    120.00
  REGULAR_SEATING   75.00
  BALCONY           50.00
  STANDING_ROOM     30.00

avgElo            = average elo of all fighters in event's fights (default 1500)
eloMultiplier     = avgElo / 1500
championsMult     = (event.organization != 'INDEPENDENT') ? 1.5 : 1.0
fanDiscountMult   = (any MatchProposal for either fighter pair on event has voteCount > 0) ? 0.9 : 1.0
loyaltyMult       = (user.pastConfirmedCount > 0 AND (count + 1) % 4 == 0) ? 0.9 : 1.0

final = round(base * eloMult * championsMult * fanDiscountMult * loyaltyMult, 2)
```
Validations: ticket type valid; quantity 1–4; user has no existing confirmed booking for this event (PENDING ones reused); quantity ≤ remaining capacity.

Booking reference: `'SF-' + uppercase 8-hex chars` from secure random.

### 5.4 BoutAnalysisService — dominance score
```
totalLanded  = s1.punchesLanded + s2.punchesLanded
totalThrown  = s1.punchesThrown + s2.punchesThrown
overallAcc   = totalThrown>0 ? totalLanded/totalThrown*100 : 0

For decided fights:
  efficiencyIndex = loser.punchesLanded > 0 ? winner.punchesLanded / loser.punchesLanded : 2.5
  kdVector        = winner.knockdowns * 2.0
  accVector       = (winner.punchAccuracy − loser.punchAccuracy) * 0.1
  volVector       = winner.punchesThrown > loser.punchesThrown ? 0.5 : -0.5
  domScore        = min(10.0, 5.0 + efficiencyIndex*1.5 + kdVector + accVector + volVector)
```
Output rounded to 1 decimal. Insight thresholds: connectivity dominance >3 round diff, late surge ≥3 consecutive wins after round 6, power dominance |power-landed diff|>12, defensive efficiency |accuracy diff|>15.

### 5.5 ContractService — purse
```
basePay  = contract.basePay
winBonus = (winner && winner.id == contract.fighter.id) ? contract.winBonus : 0
if (contract.missedWeight) basePay *= 0.8
total    = basePay + winBonus
contract.calculatedPayout = total

netToFighter = calculatedPayout * (1 − managerFeePercent)
managerFee   = calculatedPayout * managerFeePercent
```

### 5.6 FightResultService
- `addScheduledFight(eventId, fightNumber, f1Id, f2Id, ...)` → max 3 fights/event, fighters must be distinct, must share weight division if both have one, must not already be scheduled in this event.
- `enterResult(...)` → updates status to `COMPLETED`; on first completion only, calls `RankingService.processCompletedFight` and updates fighter contracts (basePay + winBonus if winner, **without** missed-weight penalty — see §4.13 conflict).

### 5.7 MatchmakingService — 8-vector Score IA
**The exact formula** (port verbatim — values total 100):

| # | Vector | Max | Formula |
|---|---|---|---|
| 1 | Weight Integrity | 25 | `sameDiv ? 25 : 0` |
| 2 | ELO Parity | 20 | `max(0, 20 * (1 − abs(e1-e2)/400))` |
| 3 | Record Parity | 15 | `wr_i = (w_i+l_i)>0 ? w_i/(w_i+l_i) : 0.5; max(0, 15*(1−|wr1-wr2|))` |
| 4 | Height Balance | **8** | both heights present: `max(0, 8*(1−|h1-h2|/30))`; else neutral 4 |
| 5 | Reach Balance | **7** | both reaches present: `max(0, 7*(1−|r1-r2|/30))`; else neutral 3.5 |
| 6 | Lethality (KO ratio) | 10 | `ko_i = w_i>0 ? koWins_i/w_i : 0; max(0, 10*(1−|ko1-ko2|))` |
| 7 | Precision (accuracy) | 10 | `max(0, 10*(1−|acc1-acc2|/100))` |
| 8 | Diversity | 5 | `(divId == null OR divId NOT IN usedDivisionIds) ? 5 : 0` |

Total clamped `[0, 100]`.

**The Symfony docblock claims `25/20/15/15/10/10/5` (7 vectors). The CODE uses `25/20/15/8/7/10/10/5` (8 vectors).** Port the code, not the docblock.

Excitement level: ≥75 high, ≥50 medium, else low.

`generateProposalsLocal(count)` greedy-randomized: build all valid pairs, score, sort top-50, loop greedily picking random from top-5 (excluding pairs touching already-used fighters and already-used divisions).

### 5.8 NotificationService
DB-only inserts. Methods: `notifyUser`, `notifyAllFans`, `notifyPredictionResult`, `notifyRankingUpdate`, `notifyNewArticle`, `notifyFanFavoriteEvent`. No email, no push.

### 5.9 PredictionService

#### Win probability (`calculateWinProbability(f1, f2)`)
```
totalFights_i normalized to ≥1
eloNorm_i  = (clamp(elo_i, 800, 2500) − 800) / 1700
winRate_i  = wins_i / totalFights_i
koRate_i   = koWins_i / totalFights_i
sa_i       = strikesThrown_i>0 ? min(landed/thrown, 1.0) : 0.5
form_i     = min(winStreak_i, 10) / 10
perf_i     = clamp(performanceScore_i, 0, 100) / 100
physDiff   = clamp((h1-h2)+(r1-r2), -60, 60); h/r default 175 if missing
physScore_1 = 0.5 + physDiff/120; physScore_2 = 1 − physScore_1

score_i = 0.30*eloNorm + 0.20*winRate + 0.10*koRate + 0.10*sa + 0.10*form + 0.10*perf + 0.10*phys
drawProb = 4.0 + (eloDiff < 50 ? 2.0 : 0.0)
total    = max(score1+score2, 0.001)
f1Prob   = round((score1/total) * (100 − drawProb), 1)
f2Prob   = round(100 − drawProb − f1Prob, 1)
```

#### Points scoring (`calculatePoints(prediction, result)`)
```
1. correctWinner: predictedWinner==null ↔ DRAW; else predictedWinner.id == winner.id
   if !correctWinner → return 0
2. points = 10
3. if upper(method) == upper(predictedMethod): points += 10
4. if method == "KO" AND predictedRound == result.roundNumber: points += 20
5. if step 3 true AND (method == "DECISION" OR step 4 true): points += 10
Max: 50
```

### 5.10 RankingService — the heart of the app

#### `calculateElo(fightResult)`
```
K_i = totalFights_i < 12 ? 60 : 32
expectedWinner = 1 / (1 + 10^((loser.elo − winner.elo)/400))

domBonus:
  KO        → 1.5 + max(0, 12 − roundNumber) * 0.05
  UD        → 1.2
  else      → 1.0

winnerChange = K_winner * (1 − expectedWinner) * domBonus
loserChange  = K_loser  * (0 − expectedLoser)  * (1 / domBonus)

if isBeltFight:
  winnerChange *= 1.25
  loserChange  *= 0.75

winner.elo = round(winner.elo + winnerChange, 2)
loser.elo  = round(max(loser.elo + loserChange, 100), 2)
```

#### `calculateSOS(fighterId)`
```
For each completed fight:
  oppRating  = opp.elo
  qualityFactor = oppRating / 1200
  if won:    totalVector += oppRating * 1.3 * qualityFactor
  else:      totalVector += oppRating * 0.85
sos = round(totalVector / count, 2)
default if no fights: 1000.0
```

#### `getBoxingRankingPoints(fighter)` — **the 0.45/0.35/0.20 formula**
```
daysInactive = days(now − lastFightDate)
if days > 730: return 0.0
decay = days > 180 ? 1 − (days − 180)/550 : 1.0
if lastFightDate is null: decay = 0.5

R = elo/15 + performanceScore * 1.5
S = (sos/1200) * 80
L = titleDefenses*20 + winStreak*2

total = R*0.45 + S*0.35 + L*0.20
return round(total * max(0.1, decay), 2)
```

#### `processCompletedFight(fight)`
1. Skip if status != COMPLETED.
2. Update both fighters' `last_fight_date`.
3. Increment counts (wins/losses/draws/koWins/decisionWins/titleDefenses).
4. Call `calculateElo` for both, write back.
5. Recompute `winStreak` (count consecutive wins from latest), `performanceScore`, `strengthOfSchedule`.
6. Call `PredictionService.processPredictionsForFight` (awards user points + sends notifications).
7. Call `updateGlobalRankings` (wipes & rebuilds `ranking` table for all 5 orgs × all weight divisions, top 16 each).
8. Broadcast notification to all users.

#### `updateGlobalRankings()`
- `DELETE FROM ranking`.
- For each weight division → sort fighters by `getBoxingRankingPoints` desc → for each org in `['WBC','WBA','IBF','WBO','MEDIA']` → top 16 fighters → INSERT ranking rows with `rank_position`, `is_champion = (rank == 0)`, `points`, `last_fight_date`, `updated_at = now`. Skip fighters with `points <= 0`.

#### `recomputeAllRankings()`
Resets `elo=800, performanceScore=0, winStreak=0, strengthOfSchedule=1000, decisionWins=0, technicalWins=0, titleDefenses=0`. **Note: does NOT reset `wins/losses/draws/koWins`** — replaying then re-increments them, inflating counts. **The Java port should explicitly zero those four fields too** to fix this latent Symfony bug.

### 5.11 RoundCommentaryService
Generates a 3–4 sentence per-round commentary using random selection from variant pools. Ports cleanly to Java with `ThreadLocalRandom`. Templates are English strings — keep verbatim for parity.

### 5.12 PerformanceAnalyzer
```
calculateEfficiencyScore(fighter):
  accuracy    = strikeAccuracy
  winRate     = totalFights>0 ? wins/totalFights*100 : 0
  streakBonus = min(winStreak * 5, 25)
  score       = accuracy*0.4 + winRate*0.4 + streakBonus
  return round(min(score, 100), 2)

getMomentum(fighter):
  streak >= 3                  → 'SCORCHING'
  streak >= 1                  → 'RISING'
  losses>0 AND wins==0         → 'STRUGGLING'
  default                      → 'STABLE'
```

### 5.13 QrCodeService
**Fan ticket QR (multiline plain text):**
```
SMARTFIGHT FAN TICKET
-------------------
Event: <name>
Date: <dd MMM yyyy>
Venue: <venue>, <city>

Ticket Details
-------------------
Fan: <fullName>
Type: <type with _ replaced by space>
Qty: <qty>
Ref: <bookingReference>
Status: <bookingStatus>
```

**Admin QR adds:** `Email: ...` and `Total: $0.00`.

**ZXing in Java:**
```java
QRCodeWriter writer = new QRCodeWriter();
Map<EncodeHintType,Object> hints = Map.of(
    EncodeHintType.CHARACTER_SET, "UTF-8",
    EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L,
    EncodeHintType.MARGIN, 10);
BitMatrix m = writer.encode(text, BarcodeFormat.QR_CODE, 250, 250, hints);
BufferedImage img = MatrixToImageWriter.toBufferedImage(m);
```

---

## 6. Notifications system

**Symfony source:** `NotificationController.php` + `NotificationService.php`.

**Java implementation:**
- Background `Timeline` polling `notifications` table every 5 seconds for current user's unread:
  ```sql
  SELECT * FROM notifications WHERE userId = ? AND is_read = 0 ORDER BY created_at DESC LIMIT 10
  ```
- Show toast in UI for each new one.
- "Mark all read" button updates `is_read = 1`.

**Cross-process visibility:** when Symfony writes a notification (after a fight result is processed), the JavaFX app's poll picks it up within 5s. This is the demo's "live sync" moment.

---

## 7. Search

**Symfony source:** `SearchController.php` returns `{results: [...]}` from queries on Fighter/Event/BlogArticle (LIKE searches, top 5/3/3).

**Java:** a `TextField` in the top bar with autocomplete (`ContextMenu` populated as user types). Run query in background `Task`.

---

## 8. File uploads — **the trickiest non-obvious part**

The whole point of the demo is that JavaFX-uploaded files (a fighter photo, a fight highlight video) appear in Symfony's UI on a different PC, and vice versa. **MySQL stores filenames only, not file bytes.** This means file storage must be reachable from both apps.

### 8.1 Vich-managed paths (canonical)
| Mapping | Path |
|---|---|
| `blog_image` | `<symfony>/public/uploads/blog/images` |
| `blog_video` | `<symfony>/public/uploads/blog/videos` |
| `fight_highlight` | `<symfony>/public/uploads/fights/highlights` |

### 8.2 Manual paths (controller-handled)
| Field | Path |
|---|---|
| `Fighter.photo_filename` | `<symfony>/public/uploads/boxers` |
| `Event.poster_filename` | `<symfony>/public/uploads/events` (presumed) |

### 8.3 Three options for cross-PC file sharing

| Option | Pros | Cons |
|---|---|---|
| **A. Network share (SMB)** | Symfony PC shares `public/uploads/` over SMB; JavaFX writes via UNC `\\<symfonyPC>\uploads\boxers\<file>`. | Requires SMB access, firewall config, may be slow over Wi-Fi. |
| **B. NFS / shared mount** | Same idea, Linux-friendly. | Less common on Windows university lab PCs. |
| **C. Add an HTTP upload endpoint to Symfony** | Cleanest. JavaFX POSTs the file to a new `/api/upload/{type}` endpoint; Symfony writes locally. | Requires new Symfony endpoint + auth token. |

**Recommendation: Option C** for clean separation. Add this Symfony endpoint:
```php
#[Route('/api/upload/{type}', methods: ['POST'])]
public function upload(string $type, Request $request) {
  // auth via X-API-Key header against an env-var token
  // accept type ∈ {boxers, blog/images, blog/videos, fights/highlights, events}
  // move uploaded file to public/uploads/<type>/, return JSON {filename}
}
```
JavaFX POSTs as `multipart/form-data` to this endpoint and writes only the returned filename to the DB.

**If you skip the HTTP option**, place a shared SMB folder (e.g., `\\server\smartfight-uploads\`) and configure both:
- Symfony: change `vich_uploader.yaml` `upload_destination` to that path (or a symlink).
- JavaFX: read `upload.dir` from `config.properties`.

---

## 9. PDF generation

**Symfony source:** `PdfService.php` wraps DomPDF; renders Twig templates to PDF.

**Used for:** rankings PDF, contracts PDF (single + list), injury reports.

**Java implementation:** OpenPDF (LGPL fork of iText 4). Build PDFs programmatically — don't try to port Twig templates 1:1. Match the visual layout from screenshots of the Symfony output.

```java
Document doc = new Document(PageSize.A4);
PdfWriter.getInstance(doc, new FileOutputStream(out));
doc.open();
doc.add(new Paragraph("SMARTFIGHT WORLD RANKINGS"));
PdfPTable table = new PdfPTable(5);
table.addCell("Rank"); table.addCell("Fighter");
doc.close();
```

---

## 10. Data import (existing users, fighters, events)

The DB is already populated by Symfony. **The Java app does NOT need to seed data.** First boot just connects to the DB and shows what's there.

If you want to test in isolation, snapshot the DB:
```bash
mysqldump -u root -p smartfight > smartfight-snapshot.sql
mysql -u root -p smartfight_test < smartfight-snapshot.sql
```

---

## 11. AI integration (DeepSeek)

### 11.1 Setup
- Key already present in `.env`: `DEEPSEEK_API_KEY=sk-23d74dc6f4f14f5d8020ee22ff1939b4`
- Reuse the same key in Java's `config.properties`.
- API endpoint: `https://api.deepseek.com/chat/completions`.
- Model: `deepseek-chat`.

### 11.2 Java client skeleton
```java
public class DeepSeekClient {
    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper json = new ObjectMapper();
    
    public Optional<JsonNode> chat(String system, String user, int maxTokens, double temperature) {
        try {
            var body = json.writeValueAsString(Map.of(
                "model", "deepseek-chat",
                "messages", List.of(
                    Map.of("role", "system", "content", system),
                    Map.of("role", "user", "content", user)),
                "max_tokens", maxTokens,
                "temperature", temperature));
            var req = HttpRequest.newBuilder(URI.create(API_URL))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
            var resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) return Optional.empty();
            var content = json.readTree(resp.body())
                .path("choices").get(0).path("message").path("content").asText();
            content = content.replaceAll("```[a-zA-Z]*\\s*|\\s*```", "").trim();
            return Optional.of(json.readTree(content));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
```

### 11.3 Risk
- DeepSeek API key is public-ish (it's in `.env.example`). For demo day, plan for it being rate-limited or rotated. **Always have heuristic fallback paths**.
- DeepSeek requires outbound HTTPS. If the demo PC is on a restricted network, AI features fall back gracefully — but verify in advance.

---

## 12. Live-sync between apps (the demo moment)

The teacher will:
1. Add a fighter via Symfony (PC #2) → expect it on JavaFX (PC #3).
2. Submit a result via JavaFX → expect ranking + notification update on Symfony.

### 12.1 Refresh strategy

| Screen | Strategy | Cadence |
|---|---|---|
| Fighter list | Polling | 5s |
| Event list | Polling | 5s |
| Fight results | Polling | 3s |
| Notifications | Polling | 5s |
| Bookings | On-demand refresh | manual button |
| Rankings | On-demand | manual button (recompute is heavy) |

JavaFX implementation:
```java
Timeline poll = new Timeline(new KeyFrame(Duration.seconds(5), e -> refresh()));
poll.setCycleCount(Animation.INDEFINITE);
poll.play();
```

### 12.2 Side-effect ordering

When JavaFX submits a fight result:
1. INSERT/UPDATE `fight_results`.
2. The Java `RankingService` runs the same algorithm Symfony's would: updates fighter records, ELO, recomputes everything, INSERTs notifications. **All within one DB transaction.**
3. Symfony's next page load (or the JavaFX poll) sees the new state.

**CRITICAL:** if both apps run their RankingService simultaneously on the same fight, they could double-process it. Mitigate by:
- Adding a `is_processed` flag to `fight_results` (or repurposing existing fields).
- OR using `SELECT ... FOR UPDATE` to serialize processing.

For the uni demo with two operators, this is unlikely to happen. Add a comment in code; don't over-engineer.

---

## 13. Demo-day setup checklist

### 13.1 MySQL PC
- [ ] MySQL running, version ≥ 8.0
- [ ] `bind-address = 0.0.0.0` in `my.cnf` (not `127.0.0.1`)
- [ ] Restart MySQL after editing
- [ ] Create user `'smartfight'@'%' IDENTIFIED BY '<pwd>'; GRANT ALL ON smartfight.* TO 'smartfight'@'%'; FLUSH PRIVILEGES;`
- [ ] Firewall: allow inbound TCP 3306
- [ ] Note this PC's LAN IP (`ipconfig`)

### 13.2 Symfony PC
- [ ] Update `.env.dev` `DATABASE_URL` to use `<MYSQL_PC_IP>:3306`
- [ ] Update `vich_uploader.yaml` if using shared folder for uploads
- [ ] Run `php bin/console cache:clear`
- [ ] Run `symfony server:start` (or `php -S 0.0.0.0:8001 -t public/`)
- [ ] Note this PC's LAN IP (for QR codes pointing to `/rankings/mobile`)
- [ ] Test from another PC: `http://<symfony-pc-ip>:8001/`

### 13.3 JavaFX PC
- [ ] JDK 17 installed
- [ ] Update `config.properties` with `<MYSQL_PC_IP>:3306`
- [ ] Update `config.properties` with shared upload path if Option A
- [ ] Run `mvn javafx:run` or `java -jar smartfight-desktop.jar`
- [ ] On first launch, app shows green DB status (Phase 1 health check)

### 13.4 Pre-demo smoke test (5 min)
1. Symfony: log in, add a fighter "Test Demo".
2. JavaFX: refresh fighter list → "Test Demo" appears.
3. JavaFX: enter a fight result for an existing scheduled fight.
4. Symfony: view rankings page → updated.
5. Symfony: notification panel shows the broadcast.
6. JavaFX: notification toast appears within 5s.

If any step fails, you have a problem to debug now, not in front of the teacher.

---

## 14. Build sequence (revised — feature-aware)

Replaces the rough sequence in `java.md`. Each phase ends with a demoable checkpoint.

### Phase 0 — Foundations (½ day)
- Pre-flight checks (§0).
- Maven skeleton, JavaFX hello-world.
- Empty `model/`, `dao/`, `service/`, `controller/` packages.
- Externalized `config.properties`.

### Phase 1 — DB + Auth core (1 day)
- HikariCP `DBConnection`.
- Health-check screen on startup.
- `User` model + `UserDAO`.
- `PasswordVerifier` with bcrypt + Argon2id detection.
- `Login.fxml` + controller.
- **Checkpoint:** log in with a Symfony-created user.

### Phase 2 — Registration + Email (½ day)
- `RegistrationController` → INSERT user, attach USER role, generate verification token.
- `GmailMailer` integration, send verification email.
- **Checkpoint:** register from JavaFX, see user in Symfony's user list, verify by clicking email link in browser.

### Phase 3 — Fighter CRUD (1 day)
- `FighterDAO` (full CRUD with all camelCase columns).
- `FighterListController` + `FighterFormController`.
- Photo upload (decide Option A/B/C from §8).
- "Generate AI profile" button → `AIService.generateFighterProfile`.
- **Checkpoint:** add a fighter in JavaFX, see it in Symfony with photo. THIS IS THE SHIP-IT MOMENT.

### Phase 4 — Event + FightResult CRUD (1 day)
- All event screens (list, new, edit, fight card editor).
- Fight result entry → triggers RankingService.
- **Checkpoint:** enter a result in JavaFX, see ELO + rankings update on Symfony.

### Phase 5 — Ranking algorithm (½ day)
- Port `calculateElo`, `calculateSOS`, `getBoxingRankingPoints`, `processCompletedFight`, `updateGlobalRankings`, `recomputeAllRankings`.
- Unit tests against known inputs from Symfony.
- **Checkpoint:** Java rankings match Symfony rankings byte-for-byte after `recomputeAll`.

### Phase 6 — Bookings + QR + dynamic pricing (1 day)
- `BookingService.createBooking` with all 5 multipliers.
- ZXing QR generation.
- Booking confirmation email with embedded QR.
- **Checkpoint:** book in JavaFX, see booking in Symfony admin, receive email.

### Phase 7 — Stats + analytics (1 day)
- Per-round stat entry wizard with full validation.
- `AnalyticsEngine.calculatePerformanceScore`.
- `BoutAnalysisService.analyzeBout`.
- `RoundCommentaryService` (port templates verbatim).

### Phase 8 — Predictions + Leaderboard (½ day)
- `PredictionService.calculateWinProbability` + `calculatePoints`.
- Leaderboard view.

### Phase 9 — AI features (½ day)
- `DeepSeekClient`.
- All 8 AIService methods with heuristic fallbacks.
- Injury predictions screen.

### Phase 10 — Notifications + Rankings PDF + Search (½ day)
- Polling notification panel.
- Rankings PDF via OpenPDF.
- Global search.

### Phase 11 — Face ID (½ day or 1.5 days depending on Option)
- See §3.7. Decide Option A (fake) or Option B (real OpenCV).

### Phase 12 — Cross-PC hardening (½ day)
- Test on second PC against remote DB.
- Test demo flow end-to-end.
- Build distributable JAR (`mvn javafx:jlink`).

**Total: ~10 working days for a single dev**, or ~6 days if Phases 9 + 10 + 11 are trimmed.

---

## 15. Risk register

| Risk | Severity | Mitigation |
|---|---|---|
| Symfony schema drifts mid-port | High | Lock schema after Phase 0; treat changes as coordinated events |
| bcrypt vs Argon2id mismatch | High | Verify in §0.1; support both in `PasswordVerifier` |
| camelCase column names mistyped | High | Always `SHOW COLUMNS FROM <table>` first; treat the §2 reference as source-of-truth |
| File uploads don't appear cross-PC | High | Implement Option C (HTTP upload endpoint) before Phase 3 |
| Concurrent ranking recalc | Medium | Document as known limitation; rely on demo-day operator discipline |
| DeepSeek rate limit / outage | Medium | All AI methods have heuristic fallbacks (see §5.1) |
| Gmail App Password rotated/revoked | Low | Test SMTP at Phase 2 start; have backup app-password |
| MySQL `bind-address = 127.0.0.1` | High | Day-zero check (§0.3) |
| Firewall blocks 3306 | High | Day-zero check (§0.3) |
| FK constraint violations on delete | Medium | Document deletion order per entity; or add ON DELETE CASCADE in code |
| `recomputeAllRankings` doesn't reset wins/losses | Low | Java port should reset all four counters explicitly (§5.10) |
| Google OAuth not working in JavaFX | Low | Skip in JavaFX; tell teacher OAuth is web-only (§3.6 Option A) |
| Webcam not available on demo PC | Medium | Test webcam in Phase 11; have fallback "skip face ID" path |

---

## 16. Symfony bugs/inconsistencies inherited from the codebase

These are bugs in the existing Symfony code. The Java port should avoid replicating them.

1. **`AIService::suggestMatches()` does not exist** but is called by `MatchmakingService::suggestMatches`. Symfony throws at runtime when AI matchmaking endpoint is hit. **Java port: skip AI matchmaking; use only the local 8-vector algorithm.**
2. **Missed-weight penalty divergence:** `ContractService::calculateFinalPurse` applies a 20% basePay penalty when `missed_weight=true`; `FightResultService::enterResult` does NOT apply it on auto-payout. **Java port: always go through `ContractService.calculateFinalPurse`.**
3. **`MatchmakingService::computeScoreIA` weight mismatch:** docblock says `25/20/15/15/10/10/5` (7 vectors), code uses `25/20/15/8/7/10/10/5` (8 vectors). **Java port: match the code, with height=8 and reach=7.**
4. **`recomputeAllRankings` does NOT reset `wins/losses/draws/koWins`** before replaying completed fights → counts inflate. **Java port: zero those four fields explicitly.**
5. **Two duplicate match-proposal admin controllers** (`/admin/match-proposals` and `/admin/proposals`). **Java port: consolidate into one screen.**
6. **`FaceIdController::verifyVisual` always logs in `userId ASC` first user with a face_photo** — not a real biometric match. **Java port can either (a) replicate this fake behavior for parity, or (b) implement real LBPH face recognition for a "wow" demo.**
7. **`PerformanceAnalyzer::getStyleMatchupAnalysis` typo:** `$style1 === 'SHARPSHOOTER' && $style1 === $style2` (the second should be `$style2` semantically; it's a no-op). **Java port: fix the typo when porting.**
8. **`RoundCommentaryService` Sentence 1 ties** report `f1l` apiece but Sentence 2's winner/loser is derived from `f1l >= f2l` — implicit f1 favoritism on ties. **Java port: keep Symfony's behavior or break the tie randomly.**

---

## 17. What to do TODAY (first 4 hours)

1. Run §0.1 — verify hash format on actual current DB.
2. Run §0.2 — dump current schema into `docs/current-schema.sql`.
3. Run §0.3 — prove cross-PC MySQL connectivity from the JavaFX-target PC.
4. Set up Maven skeleton + JavaFX hello-world.
5. Add HikariCP DB config.
6. Build `PasswordVerifier` + write a test against a real hash.

That's the entire day-one critical path. After that, every subsequent feature is mechanical and incremental.

---

## 18. Reference cheat-sheet (consolidated constants)

| Constant | Value | Source |
|---|---|---|
| ELO baseline | 800 | RankingService |
| ELO floor | 100 | RankingService |
| K-factor | 60 (<12 fights) / 32 | RankingService |
| ELO logistic divisor | 400 | RankingService |
| KO domBonus | `1.5 + max(0, 12 − round) * 0.05` | RankingService |
| UD domBonus | 1.2 | RankingService |
| Belt multipliers | winner ×1.25, loser ×0.75 | RankingService |
| Ranking formula weights | 0.45 R + 0.35 S + 0.20 L | RankingService |
| R | `elo/15 + perf*1.5` | RankingService |
| S | `(sos/1200) * 80` | RankingService |
| L | `titleDefenses*20 + winStreak*2` | RankingService |
| Inactivity decay | 0–180d full, 180–730d linear, >730d zero | RankingService |
| Decay formula | `1 − (days−180)/550` | RankingService |
| SoS default | 1000 | RankingService |
| SoS benchmark | 1200 | RankingService |
| SoS multipliers | win ×1.3×qualityFactor, loss ×0.85 | RankingService |
| Performance score weights | 0.5 punchAcc + 0.3 powerAcc + 0.2 kdBonus | AnalyticsEngine |
| KD bonus cap | `clamp(kd*15, 0, 45)` | AnalyticsEngine |
| DQ penalty | −50 | AnalyticsEngine |
| Belt fight performance multiplier | 1.5 winner / 1.25 loser | AnalyticsEngine |
| 8-vector matchmaking | 25/20/15/8/7/10/10/5 = 100 | MatchmakingService |
| AI heuristic weights | elo 0.45 / phys 0.15 / lethality 0.20 / momentum 0.20 | AIService |
| Prediction points | winner 10 + method 10 + KO+round 20 + perfect 10 = 50 max | PredictionService |
| Win-prob weights | 0.30 elo / 0.20 wr / 0.10 ko / 0.10 acc / 0.10 form / 0.10 perf / 0.10 phys | PredictionService |
| Booking prices (USD) | VIP 200 / Premium 120 / Regular 75 / Balcony 50 / Standing 30 | BookingService |
| Loyalty trigger | every 4th confirmed booking | BookingService |
| Champion price multiplier | 1.5 | BookingService |
| Fan-vote discount | 0.9 | BookingService |
| Loyalty discount | 0.9 | BookingService |
| Booking quantity | 1–4 per booking | BookingService |
| Max fights per event | 3 | FightResultService |
| Booking ref format | `SF-` + 8 hex chars (uppercase) | BookingService |
| Top fighters per division per org | 16 | RankingService.updateGlobalRankings |
| Verification token | 32 hex chars | SecurityController |
| Reset token expiry | 1 hour | SecurityController |
| Min password length | 6 | RegistrationType + reset |
| FaceID min photo length | 500 chars (base64) | FaceIdController |

---

End of plan. This document, together with `java.md`, is the complete contract for the Java desktop port. Cross-reference both before each phase.
