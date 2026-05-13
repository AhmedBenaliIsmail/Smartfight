# SmartFight Java Desktop App — Localhost Implementation Plan

**Scope:** This document is the consolidated, fact-checked implementation plan for porting the SmartFight Symfony app to a JavaFX desktop client. **It assumes a single-machine localhost deployment**: Symfony, MySQL, and the JavaFX app all run on the same Windows PC. Cross-PC (TCP, firewall, network share, IP-based QR codes) is deferred to §20 "Future: multi-PC deployment" at the end.

**Source artifacts:**
- Original feature plan: `JavaPlan.md` (1241 lines)
- Ultra-review: `C:\Users\ahmed\.claude\plans\c-users-ahmed-onedrive-desktop-pr-smartf-snazzy-hinton.md`

This file applies the three corrections from the review (token length, mixed bcrypt costs, snake_case verification) and integrates the per-template UI port plan.

---

## 0. Pre-flight checks (do these BEFORE writing Java code)

These two checks de-risk the entire port. Run them in the first hour. (Cross-PC connectivity check from the original plan is removed — everything runs on localhost.)

### 0.1 Verify password hash formats in the live DB

Symfony's `security.yaml` uses `password_hashers: 'auto'`, which resolves to **Argon2id** if PHP's sodium extension is available, otherwise **bcrypt**. The actual `users` table contains a **mix** of formats — confirmed in `smartfight.sql`: both `$2y$13$...` (cost 13) and `$2y$10$...` (cost 10) bcrypt hashes coexist.

```sql
SELECT userId, username, password FROM users LIMIT 10;
```

Possible prefixes:
- `$2y$10$` / `$2y$13$` / `$2a$..$` / `$2b$..$` → **bcrypt, any cost**
- `$argon2id$...` → **Argon2id**

The Java `PasswordVerifier` must support **any** bcrypt cost (jBCrypt reads cost from the hash prefix automatically) plus Argon2id. Unit-test against at least one cost-10 and one cost-13 sample from the real DB.

### 0.2 Dump the current Doctrine schema and verify column names

```bash
php bin/console doctrine:schema:create --dump-sql > docs/current-schema.sql
```

**Critical:** in `User.php`, the properties `isVerified`, `resetToken`, `webauthnCredentialId`, `facePhoto` have **no** `#[ORM\Column(name: '...')]` overrides. The actual column names depend on whether `config/packages/doctrine.yaml` sets `naming_strategy: doctrine.orm.naming_strategy.underscore`. If it doesn't, the columns are camelCase: `isVerified`, `resetToken`, etc. — NOT snake_case.

**Run before any DAO is written:**
```sql
SHOW COLUMNS FROM users;
SHOW COLUMNS FROM notifications;
SHOW COLUMNS FROM predictions;
```

Take the literal column names from the output and update §2.3 below before any INSERT/UPDATE/SELECT is coded.

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
| Password hashing | bcrypt + Argon2id | `org.mindrot:jbcrypt` + `de.mkammerer:argon2-jvm` |
| QR codes | ZXing | `com.google.zxing:core` + `com.google.zxing:javase` |
| PDF | OpenPDF + Flying Saucer | `org.xhtmlrenderer:flying-saucer-pdf-openpdf:9.x` |
| Templates (email/PDF) | Pebble | `io.pebbletemplates:pebble:3.x` |
| HTTP (DeepSeek) | Java 17 `HttpClient` | built-in |
| JSON | Jackson | `com.fasterxml.jackson.core:jackson-databind` |
| Email | Jakarta Mail | `jakarta.mail:jakarta.mail-api` + `org.eclipse.angus:jakarta.mail` |
| Webcam (Face ID) | Webcam-Capture | `com.github.sarxos:webcam-capture:0.3.12` |
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
│   ├── widget/                             # custom JavaFX widgets
│   ├── auth/
│   │   ├── PasswordVerifier.java
│   │   └── FaceIdRecognizer.java
│   ├── integration/
│   │   ├── DeepSeekClient.java
│   │   ├── GmailMailer.java
│   │   ├── PebbleRenderer.java
│   │   ├── PdfRenderer.java                # Flying Saucer wrapper
│   │   └── QrCodeGenerator.java
│   └── util/
│       ├── CsvWriter.java
│       └── AlertHelper.java
├── src/main/resources/
│   ├── config.properties
│   ├── styles/smartfight.css               # pinned global CSS
│   ├── tn/smartfight/views/                # FXML files
│   ├── email-templates/                    # Pebble (Twig)
│   ├── pdf-templates/                      # Pebble→Flying Saucer
│   └── venue_data.json
└── src/test/java/...
```

### 1.3 `config.properties` — localhost configuration

```properties
# Database — same MySQL instance Symfony uses
db.url=jdbc:mysql://localhost:3306/smartfight?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8&useUnicode=true&useLegacyDatetimeCode=false&zeroDateTimeBehavior=CONVERT_TO_NULL
db.user=root
db.password=
db.pool.maxSize=8
db.pool.leakDetectionThresholdMs=5000

# SMTP — Gmail App Password from existing Symfony .env
mail.smtp.host=smtp.gmail.com
mail.smtp.port=587
mail.smtp.user=teamsmartfight@gmail.com
mail.smtp.password=uucxnpkizyclppsr
mail.from=teamsmartfight@gmail.com

# DeepSeek
deepseek.api.key=sk-23d74dc6f4f14f5d8020ee22ff1939b4
deepseek.api.url=https://api.deepseek.com/chat/completions

# Local file storage — ALL uploads go here. This is the same directory
# Symfony's public/uploads/ writes to. On localhost both apps share the
# same filesystem, so we just write directly.
upload.dir=C:/Users/ahmed/OneDrive/Desktop/pr/Smartfight/public/uploads

# Symfony web app URL (for QR codes and email links)
symfony.base.url=http://localhost:8001
```

**Critical localhost simplification:** since both Symfony and JavaFX run on the same machine, the JavaFX app writes uploaded files **directly to Symfony's `public/uploads/` directory**. No HTTP upload endpoint, no SMB share, no Vich-Java translation layer. Filesystem is the contract.

JVM startup flag (parity with PHP):
```
-Duser.timezone=UTC
```

---

## 2. Database schema reference (the contract)

The Symfony schema mixes naming conventions. **This is the single most common source of silent JDBC failures.** Always run `SHOW COLUMNS FROM <table>` before writing a DAO.

### 2.1 Tables with **camelCase** PK columns (verified)

| Table | PK column |
|---|---|
| `events` | `eventId` |
| `fighters` | `fighterId` |
| `users` | `userId` |
| `roles` | `roleId` |
| `notifications` | `notificationId` |
| `predictions` | `predictionId` |
| `fight_results` | `resultId` |

### 2.2 Tables with **snake_case** PK columns (`id`, verified)
`blog_article`, `blog_category`, `discipline`, `event_booking`, `fan_preference`, `fan_profile`, `fan_reaction`, `fan_vote`, `fight_statistic`, `fighter_contract`, `match_proposal`, `performance_score`, `ranking`, `weight_division`.

### 2.3 camelCase data columns (verified)
- `fighters`: `koWins`, `decisionWins`, `eloRating`, `performanceScore`, `winStreak`, `strengthOfSchedule`, `titleDefenses`
- `fight_results`: `fightNumber`, `fighter1Id`, `fighter2Id`, `winnerId`, `methodOfVictory`, `roundNumber`, `fightDate`, `eventId`
- `events`: `eventName`, `eventDate`
- `users`: `username`, `password` (camelCase). **Other user fields (`isVerified`, `resetToken`, `webauthnCredentialId`, `facePhoto`, `verificationToken`, `resetTokenExpiresAt`, `createdDate`) — VERIFY with `SHOW COLUMNS FROM users` before writing the DAO.** Doctrine entity properties are camelCase with no explicit column name override; actual column casing depends on `doctrine.yaml`'s `naming_strategy`.
- `notifications`: `userId` (camelCase). `created_at`, `is_read`, `type` per entity — verify.
- `predictions`: `userId`, `fightId`, `predictedWinnerId` (camelCase) + `predicted_method`, `predicted_round` (snake_case, verified).

### 2.4 Join tables
- `user_roles` (M2M) → `userId`, `roleId` (camelCase, verified User.php:48-49).

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
- `User.createdDate = now`, `isVerified = false`

**Failure mode:** forgetting these causes `NOT NULL` constraint violations OR records with stale timestamps that Symfony's queries depend on.

### 2.6 Implicit defaults Java must respect
- `Event.organization = 'INDEPENDENT'`, `status = 'SCHEDULED'`, `visibility = 'PUBLIC'`
- `Fighter.eloRating = 1500.0`, `strengthOfSchedule = 1500.0`, `performanceScore = 0.0`
- `Ranking.organization = 'MEDIA'`, `rank_position = 999`
- `EventBooking.booking_status = 'CONFIRMED'`, `ticket_quantity = 1`, `total_price = '0.00'`, `ticket_type = 'REGULAR_SEATING'`

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
- `BlogArticle.status` ∈ {DRAFT, PUBLISHED, ARCHIVED}

### 2.8 Foreign key cascades
**Only ONE cascade is configured** in the schema: `fan_vote.match_proposal_id` → `match_proposal.id` ON DELETE CASCADE. Every other delete will fail with FK violations unless rows are deleted in the right order. The Java app **must** delete dependent rows first.

### 2.9 Money / decimal columns

Doctrine `decimal(10,2)` columns (`EventBooking.total_price`, `FighterContract.basePay`, `winBonus`, `calculatedPayout`) MUST map to `java.math.BigDecimal`, NOT `double`. Round with `HALF_UP` to 2 decimals to match PHP's `round()`. Using `double` causes cents-level drift on multi-fight events.

---

## 3. Authentication features

### 3.1 Username + password login

**Symfony source:** `SecurityController::login`. Login is by `username` (NOT email). Hashes are bcrypt cost 10 OR 13 (mixed in DB), possibly Argon2id.

**Java implementation:**
```java
public class PasswordVerifier {
    private final Argon2 argon2 = Argon2Factory.create();
    public boolean verify(String rawPassword, String storedHash) {
        if (storedHash.startsWith("$argon2")) {
            return argon2.verify(storedHash, rawPassword.toCharArray());
        }
        if (storedHash.startsWith("$2y$") || storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$")) {
            // jBCrypt rejects $2y$ in some versions — convert to $2a$ for compat
            String compat = storedHash.startsWith("$2y$") ? "$2a$" + storedHash.substring(4) : storedHash;
            return BCrypt.checkpw(rawPassword, compat);
        }
        return false;
    }
}
```

Test before building login UI:
```java
@Test void verifyAgainstSymfonyHashCost10() {
    // pull a real $2y$10$ hash from smartfight.sql
    assertTrue(new PasswordVerifier().verify("knownPassword", realHashCost10));
}
@Test void verifyAgainstSymfonyHashCost13() {
    assertTrue(new PasswordVerifier().verify("knownPassword", realHashCost13));
}
```

### 3.2 Registration

**Symfony source:** `SecurityController::register` + `RegistrationType`. On submit:
1. Generate `verificationToken` via `bin2hex(random_bytes(32))` → **64 hex chars** (corrected from original plan)
2. `isVerified = false`
3. Hash password with `$passwordHasher->hashPassword()`
4. Attach `Role` with `roleName = 'USER'`
5. Send `emails/verification.html.twig` via Symfony Mailer with link `/verify-email/{token}` from sender `mahdidaly24@gmail.com`

**Java implementation:**
- FXML form with three fields + confirm.
- Hash with bcrypt cost 13: `BCrypt.hashpw(password, BCrypt.gensalt(13))`. Symfony's verifier accepts `$2a$/$2b$/$2y$` interchangeably.
- Generate token: 32 bytes via `SecureRandom` → hex-encode → 64 chars.
- INSERT into `users`: `userId` auto, `username`, `password`, `email`, `createdDate=now`, `predictionPoints=0`, `isVerified=0`, `verificationToken=<64 hex>`, plus null webauthn fields.
- INSERT into `user_roles`: `(userId, roleId WHERE roleName='USER')`.
- Send verification email via SMTP through Pebble-rendered template.

**Risk:** the Role with `roleName='USER'` must exist. Run a one-time bootstrap to seed roles `USER`, `ADMIN`, `SUPER_ADMIN`, `FAN` if missing.

### 3.3 Forgot password / reset password

**Symfony source:** `SecurityController::forgotPassword` + `resetPassword`. Look up user by email OR username. Set `resetToken` (64 hex from `random_bytes(32)`), `resetTokenExpiresAt = now + 1 hour`. Email link `/reset-password/{token}`.

**Recommendation:** show a "Forgot password? Use the web app" hyperlink inside JavaFX login that opens `http://localhost:8001/forgot-password` in the system browser via `Desktop.getDesktop().browse(URI)`. Implementing a desktop reset flow with email-clickable links is more work than it's worth.

### 3.4 Email verification (post-registration)

The verification link in email goes to `/verify-email/{token}` on the **Symfony web app at localhost:8001**. Java sets the token and sends the email; user clicks the link in their browser; Symfony marks them verified. Java just queries `isVerified = 1` before allowing login.

### 3.5 Gmail SMTP setup

**Symfony source:** `mailer.yaml` reads `MAILER_DSN`. Current DSN:
```
smtp://teamsmartfight%40gmail.com:uucxnpkizyclppsr@smtp.gmail.com:587
```
That's a Gmail App Password (16 chars). 2FA is enabled on `teamsmartfight@gmail.com`.

**Java implementation with Jakarta Mail:**
```java
public class GmailMailer {
    private final Properties props = new Properties();
    public GmailMailer(AppConfig cfg) {
        props.put("mail.smtp.host", cfg.smtpHost);
        props.put("mail.smtp.port", cfg.smtpPort);
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

**Demo-day risk:** if the demo PC has no internet OR Gmail's outbound IP rate-limits, emails fail. Always log SMTP errors loudly.

### 3.6 Google OAuth login

**Symfony source:** `GoogleAuthenticator.php` + `knpu_oauth2_client.yaml`. Note: `knpu_oauth2_client.yaml` has NO explicit `scopes:` key — the bundle uses Google's defaults (openid, email, profile). The authenticator looks up `FAN` role first, falls back to `USER` if FAN missing.

**Recommendation: skip in JavaFX.** State explicitly during the demo: "Google login is on the web app at `http://localhost:8001`; the desktop app uses local credentials." If the marker insists on it, see the original `JavaPlan.md` §3.6 Option B for the embedded WebView + loopback callback approach.

### 3.7 Face ID login

**Symfony source:** `FaceIdController.php`. Four endpoints (`/face-id/register|login|save-photo|verify-visual`). The `verify-visual` route requires `photo ≥500 chars`, then logs in **the FIRST user with any face_photo, ordered by userId ASC**. **This is a demo prop, not a real biometric system.**

**Java implementation strategy — two options:**

**Option A (matches Symfony — simple).** Use `webcam-capture` API. Capture image → save base64 in `User.facePhoto` → "login" reads first user with any `facePhoto` and logs them in. Identical behavior to Symfony's `verify-visual`.

**Option B (real face matching, more impressive).** OpenCV with LBPH face recognizer (`org.openpnp:opencv:4.7.0-0`). Train per-user model, classify on login.

**Recommendation:** Option A for parity + reliability. Decide before Phase 11 of the build.

### 3.8 Logout
`Session.clear()` and swap content-host back to `Login.fxml` (single-stage architecture — see §6.1). No DB action.

---

## 4. Admin back-office features

### 4.1 Dashboard (`/`)

**Symfony source:** `DashboardController.php`. Aggregates: counts (fighters/events/fights/users/articles), 7-day revenue, 7-day registrations, last 5 fight results, 5 upcoming events, financial summary, top 3 voted match proposals.

**Java:** `DashboardController.java` with multiple `Label`s, two `LineChart`s (revenue + registrations), `TableView` for upcoming events. Single `DashboardService.loadStats()` fires all queries in one shot via `Task<>`.

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

**Symfony source:** `FighterController.php`. List (paginated, search), new, edit, delete, recalc-rankings, generate-AI-profile.

**Photo upload:** controller-managed `UploadedFile::move()` to `public/uploads/boxers/<uniqid>.<ext>` — NOT Vich.

**Java implementation:**
- `FighterListController` with `TableView<Fighter>`, search, refresh, "Recalc Rankings" button.
- `FighterFormController` with: firstName, lastName, nickname, weightDivision, nationality, wins/losses/draws/koWins/technicalWins/decisionWins, koLosses, height, reach, weight, age, manager, photo.
- **Photo upload (localhost simplified):** `FileChooser` → copy file directly to `<upload.dir>/boxers/<UUID>.<ext>` → store filename only in DB. Both Symfony (also localhost) and JavaFX read from the same directory.
- "Generate AI profile" button → `AIService.generateFighterProfile()` → updates `aiStyleTag`, `aiDescription`.

### 4.3 Event management (`/events`)

**Symfony source:** `EventController.php`. List (KnpPaginator 6/page), autocomplete, champions filter, new, champions-event-new (auto-schedules 3 fights), edit, delete, fights-tab, add-fight, ai-apply, fight-delete, ai-matchmake, fan-favorite-create.

**Champions event auto-schedule:** picks 3 distinct WeightDivisions, top-2 ranked per division, calls `FightResultService.addScheduledFight` × 3.

**Fan Favorite Night:** picks top 3 PENDING `MatchProposal` by voteCount, creates Event "Fan Favorite Night — <date>" at "Community Arena", dated +14 days, organization=`FAN CHOICE`, schedules 3 fights, marks proposals APPROVED, links `proposal.event = event`.

**Java:** `EventListController` + `EventFormController` + `EventFightsController`. AI Matchmake button uses `MatchmakingService.computeScoreIA` (local 8-vector). Hard cap 3 fights/event enforced in `FightResultService.addScheduledFight`.

### 4.4 Fight result management (`/results`)

**Symfony source:** `ResultController.php`. List, schedule, enter (with optional video upload), stats (per-round), cancel, delete, show, export-csv, manage-card, generate-AI-stats, generate-round-commentary, export-pdf.

**Result entry triggers `RankingService.processCompletedFight()`** which:
1. Updates fighter records (wins/losses/draws/koWins/decisionWins)
2. Recomputes ELO via `calculateElo`
3. Updates `lastFightDate`, `winStreak`, `performanceScore`, `strengthOfSchedule`
4. Triggers `PredictionService.processPredictionsForFight`
5. Wipes and rebuilds the entire `ranking` table for all 5 organizations
6. Broadcasts notifications to all users with ROLE_USER

**This is the highest-value, highest-risk feature.** The math must match exactly (see §5.6 and §5.10).

**Java:** `ResultEnterController` form, on submit → `FightResultService.enterResult()` → triggers full ranking pipeline. Per-round stats (`FightStatisticController`) with strict CompuBox validation.

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

After save, `FightStatisticService.recalculateForFighter` triggers `AnalyticsEngine.calculatePerformanceScore` (§5.2).

### 4.6 Rankings (`/rankings`)

**Symfony source:** `RankingController.php`. Public list, mobile view, PDF export, recalc, CSV export, QR-download.

**Java:** `RankingListController` table grouped by weight division. "Generate QR" button produces ZXing QR pointing to `http://localhost:8001/rankings/mobile` (the Symfony server URL — phones on the same Wi-Fi can scan it; cross-PC concerns deferred to §20). "Export PDF" via Flying Saucer rendering `ranking/pdf.html.twig`. "Recalc" → `RankingService.recomputeAllRankings()`.

### 4.7 Performance / injury predictions (`/performance`)

**Symfony source:** `PerformanceController.php`. List, injury-predictions (per-fighter HTTP calls — slow!), injury-pdf, recalc, API endpoint, fighter detail.

**Java:** `PerformanceListController` sortable table. Injury Predictions tab runs in background `Task` with progress bar. Each AI call returns either real DeepSeek or heuristic — heuristic is deterministic and fast (§5.1.2).

### 4.8 User management (`/users`, `/users/profile`)

**Symfony source:** `UserController.php`. Admin sees user list + new/update/password/delete. Non-admin sees `/profile` only. Username uniqueness enforced.

### 4.9 Blog (`/admin/blog/*` and `/blog`)

**Symfony source:** `Admin/BlogController.php` + `Front/BlogController.php`. Form uses VichUploader for image (`blog_image` → `public/uploads/blog/images`) and video (`blog_video` → `public/uploads/blog/videos`). Files use `UniqidNamer`.

**Java:**
- `BlogListController` (admin): table with category/status filter.
- `BlogFormController`: title, summary, content (`HTMLEditor`), category, status, image upload, video upload.
- **Localhost simplification:** copy uploaded files directly to `<upload.dir>/blog/images/` and `<upload.dir>/blog/videos/` with `UUID.randomUUID()` prefix. Store filename only in DB.
- Public view: `BlogPublicController` increments `view_count`.

### 4.10 Bookings (`/admin/bookings` and `/booking`)

**Symfony source:** `Admin/BookingAdminController.php` + `Front/BookingController.php`.

**Booking creation:** `BookingService.createBooking()` validates capacity and quantity (1–4), generates `bookingReference = 'SF-' + 8 hex chars`, sets total_price from dynamic pricing, sends email with embedded QR PNG, dispatches notification.

**Dynamic pricing formula** (see §5.3 for constants):
```
final = base × eloMultiplier × championsMultiplier × fanDiscountMultiplier × loyaltyMultiplier
```

**Java:**
- `BookingFormController` for fans.
- `MyBookingsController`: shows bookings, embedded QR preview (ZXing → `ImageView`), cancel button.
- Admin variants: `BookingAdminController` with all-bookings table, manual create, QR view.
- Email + QR via Pebble rendering of `booking_confirmation.html.twig`. QR encodes the multiline string in §5.13.

### 4.11 Finance (`/admin/finance`)
**Symfony source:** `Admin/FinanceAdminController.php`. SQL aggregation per event, daily revenue 30d, by ticket type. **Java:** straightforward SQL + `BarChart`/`PieChart`/`LineChart`.

### 4.12 Reactions (`/admin/reactions` + `/reactions`)
Fans post reactions to fights ∈ {FIRE, SHOCK, RESPECT, DOMINANT, CONTROVERSIAL} with comment ≤140 chars. Admin can pin/soft-delete.

### 4.13 Contracts (`/admin/contracts`)

**Symfony source:** `Admin/AdminContractController.php`.

**Calculation conflict (verified bug):** `ContractService.calculateFinalPurse` applies `basePay *= 0.8` if `missed_weight=true`; but `FightResultService.enterResult` does NOT apply this when auto-paying (`FightResultService.php:139-162` uses `basePay` directly). The Java port must **always go through `ContractService.calculateFinalPurse`**.

**Java:** `ContractListController` with create/pay/delete/toggle-weight. PDF via Flying Saucer rendering `admin/contract/pdf.html.twig`.

### 4.14 Match proposals (`/admin/match-proposals` and `/admin/proposals`)

**Symfony has TWO duplicate controllers** for the same feature. **Java: consolidate to one screen.** `MatchProposalController` lists by voteCount, allows manual create, AI-generate (`MatchmakingService.generateProposalsLocal` → 8-vector algorithm), approve, reject, delete.

**Skip the Symfony AI matchmaking path** — `MatchmakingService::suggestMatches()` calls non-existent `AIService::suggestMatches()` (verified bug). Use only the local algorithm.

---

## 5. Service-layer algorithms (the math you cannot get wrong)

Both apps must produce **identical** results for the same input or rankings desync.

### 5.1 AIService

Wraps `https://api.deepseek.com/chat/completions` (model `deepseek-chat`, 30s timeout). All AI methods compute a deterministic heuristic baseline first; if API fails OR key missing, return heuristic with `is_fallback: true`.

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
Body: {"model":"deepseek-chat","messages":[{"role":"system",...},{"role":"user",...}],"max_tokens":N,"temperature":T}
Response: {"choices":[{"message":{"content":"<json string>"}}]}
```
Strip ```` ```json ```` fences from content before `JSON.parse`.

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

### 5.3 BookingService — dynamic pricing
```
basePrice (USD, BigDecimal):
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

final = round(base * eloMult * championsMult * fanDiscountMult * loyaltyMult, 2, HALF_UP)
```
Validations: ticket type valid; quantity 1–4; user has no existing CONFIRMED booking for this event (PENDING ones reused); quantity ≤ remaining capacity.

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
Output rounded to 1 decimal.

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
- `addScheduledFight(eventId, fightNumber, f1Id, f2Id, ...)` → max 3 fights/event, fighters distinct, must share weight division if both have one, must not already be scheduled in this event.
- `enterResult(...)` → updates status to `COMPLETED`; on first completion only, calls `RankingService.processCompletedFight` and updates fighter contracts (basePay + winBonus, **but auto-payout omits missed-weight penalty — verified bug; Java port should call `ContractService.calculateFinalPurse` for consistency**).

### 5.7 MatchmakingService — 8-vector Score IA

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

Total clamped `[0, 100]`. **The Symfony docblock says `25/20/15/15/10/10/5` (7 vectors). The CODE uses `25/20/15/8/7/10/10/5` (8 vectors). Port the code, not the docblock.** Excitement: ≥75 high, ≥50 medium, else low.

`generateProposalsLocal(count)` greedy-randomized: build all valid pairs, score, sort top-50, loop greedily picking random from top-5 (excluding pairs touching already-used fighters and already-used divisions).

### 5.8 NotificationService
DB-only inserts. Methods: `notifyUser`, `notifyAllFans`, `notifyPredictionResult`, `notifyRankingUpdate`, `notifyNewArticle`, `notifyFanFavoriteEvent`. No email, no push.

### 5.9 PredictionService

#### Win probability
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

#### Points scoring
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
  else:      totalVector += oppRating * 0.85    // NO qualityFactor on loss — verified
sos = round(totalVector / count, 2)
default if no fights: 1000.0
```

#### `getBoxingRankingPoints(fighter)`
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
5. Recompute `winStreak`, `performanceScore`, `strengthOfSchedule`.
6. Call `PredictionService.processPredictionsForFight`.
7. Call `updateGlobalRankings`.
8. Broadcast notification to all users.

**Wrap steps 1–8 in a single JDBC transaction** (`conn.setAutoCommit(false); ... conn.commit();` in try-with-resources). On failure, rollback.

#### `updateGlobalRankings()`
- `DELETE FROM ranking`.
- For each weight division → sort fighters by `getBoxingRankingPoints` desc → for each org in `['WBC','WBA','IBF','WBO','MEDIA']` → top 16 fighters → INSERT ranking rows. Skip fighters with `points <= 0`.

#### `recomputeAllRankings()`
Resets `elo=800, performanceScore=0, winStreak=0, strengthOfSchedule=1000, decisionWins=0, technicalWins=0, titleDefenses=0`. **Verified Symfony bug: does NOT reset `wins/losses/draws/koWins`** — replaying then re-increments them, inflating counts. **The Java port MUST explicitly zero those four fields too.**

### 5.11 RoundCommentaryService
Generates 3–4 sentence per-round commentary using random selection from variant pools. Ports cleanly with `ThreadLocalRandom`. Templates are English strings — keep verbatim for parity. **Tie-break note:** Symfony's Sentence 1 ties report `f1l` apiece but Sentence 2's winner/loser is derived from `f1l >= f2l` — implicit f1 favoritism on ties. Keep this behavior.

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

**Fix this Symfony typo on port:** `getStyleMatchupAnalysis` has `$style1 === 'SHARPSHOOTER' && $style1 === $style2` (the second `$style1` should be `$style2`; it's currently a no-op). Fix when porting.

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

## 6. UI architecture and per-template port plan

### 6.1 Window strategy: single stage (CONFIRMED)

One `Stage`, one `Scene`. Center of `AdminShell.fxml` / `FanShell.fxml` is a `StackPane` content-host. A `RouterService` swaps the child node when the user navigates. No multi-window juggling, no stage parenting. Modal popups use `Stage.initOwner(...)` against the single primary stage.
- Closing the window kills the app — same as Symfony closing a tab. Add an "Are you sure?" guard if there's an unsaved form.
- Logout = clear `Session` + swap content-host back to `Login.fxml`. Same stage, same scene.

### 6.2 Pinned global stylesheet: `src/main/resources/styles/smartfight.css`

One stylesheet, attached to the Scene at startup:
```java
scene.getStylesheets().add(getClass().getResource("/styles/smartfight.css").toExternalForm());
```

Built once before Phase 1 with these contents:

**Color tokens** (define once via JavaFX lookup vars):
```css
.root {
    -accent-gold: #d4af37;
    -corner-red: #e63946;
    -corner-blue: #1d4ed8;
    -bg-app: linear-gradient(...);  /* extract from base.html.twig */
    -bg-surface: #1a1a1a;
}
.h1 { -fx-text-fill: -accent-gold; -fx-font-size: 28px; }
```

**Status pill classes:** `.status-pill-scheduled`, `.status-pill-live`, `.status-pill-completed`, `.status-pill-cancelled`, `.status-pill-pending`, `.status-pill-approved`, `.status-pill-rejected`, `.status-pill-confirmed`.

**Typography:** `.h1` / `.h2` / `.h3` / `.body` / `.caption` with explicit `-fx-font-size`/`-fx-font-weight`. Font family: `'Inter', 'Segoe UI', sans-serif`.

**Layout primitives:** `.card`, `.card-elevated` (drop shadow), `.divider`, `.hex-photo` (rounded-rect approximation; true hex requires programmatic `Polygon` clip on `ImageView`).

**Rule:** no inline `style="..."` in FXML. Add a class, put the rule in the CSS.

### 6.3 Reuse strategy — three template tiers

| Tier | Templates | Strategy |
|---|---|---|
| **Reused verbatim** | `emails/*.twig`, all `*pdf.html.twig` | Pebble for emails (~95% Twig-compatible), Flying Saucer for HTML→PDF. Zero UI rework. |
| **Spec only — rebuild as FXML** | All admin/front/security/dashboard/etc. | Read the Twig to extract field lists, table columns, validations, button labels, and CSS colors; ship FXML from scratch. |
| **WebView fallback** | `ranking/mobile.html.twig` | Render via JavaFX `WebView` against `http://localhost:8001/rankings/mobile` — phones scan the QR, JavaFX never serves it. |

### 6.4 Shared widgets to build before any screen (Phase 0.5)

| Widget | Replaces | Implementation |
|---|---|---|
| `AdminShell.fxml` + `FanShell.fxml` | `base.html.twig`, `admin/base_admin.html.twig`, `front/base_front.html.twig` | `BorderPane` with sidebar (240px, collapsible) + topbar (72px) + center content `StackPane`. |
| `Sidebar.fxml` (parametrized) | `admin/partials/sidebar.html.twig`, `front/partials/sidebar.html.twig` | Two configs: ADMIN vs FAN nav items. |
| `Topbar.fxml` | `admin/partials/navbar.html.twig` | Brand + breadcrumb + user-menu + global-search + notification bell. |
| `GlobalSearchPopup.java` | `front/partials/global_search.html.twig` | `TextField` + `ContextMenu` populated from `SearchController`. Debounced 300ms. |
| `NotificationPanel.fxml` | `notification/_list.html.twig` | `Popup` over Topbar bell. `ListView<Notification>` with mark-read + clear-all. Polled every 5s. |
| `PaginatedTable<T>` | KnpPaginator (6/page) | `VBox` wrapping `TableView<T>` + `Pagination`. DAO returns `PageResult<T>`. |
| `AutocompleteSearchField<T>` | Live-search dropdowns | `TextField` + popup `ListView`. Configurable renderer + filter. |
| `PhotoUploadPane.fxml` | Fighter photo + blog featured image | `FileChooser` + `ImageView` preview + drag-drop. Writes via `UploadService` (local copy). |
| `QrCodeImageView.java` | QR codes everywhere | Wraps ZXing → `WritableImage`. |
| `StatusBadge.java` | Status pills | `Label` with derived CSS class. |
| `FlashAlertStack.fxml` | Symfony flash messages | `VBox` of dismissable alerts driven by a `FlashBus`. |
| `LoadingOverlay.java` | XHR upload progress, AI spinners | `StackPane` overlay with `ProgressIndicator` + cancel. |
| `RoundTabsPane.fxml` | `result/stats.html.twig` round tabs | Dynamic `TabPane`, content is a stats matrix. |

### 6.5 Per-screen mapping (consolidated)

**Auth (Phase 1):**
- `Login.fxml` ← `security/login.html.twig`
- `Register.fxml` ← `security/register.html.twig`
- `ForgotPassword.fxml` ← `security/forgot_password.html.twig`
- `security/reset_password.html.twig` — **skipped in JavaFX**, opens `http://localhost:8001/forgot-password` in system browser.

**Dashboard (Phase 1.5):**
- `Dashboard.fxml` ← `dashboard/index.html.twig` — 5 sections: quick actions, financial stats, AI injury panel, fan-favorite voting, charts + recent activity.

**Fighter (Phase 3):**
- `FighterList.fxml` ← `fighter/index.html.twig` — `FlowPane` of `FighterCard`s, filter bar (search/country/weight/style/winRate/ELO/sort), AI injury collapsible.
- `FighterForm.fxml` ← `fighter/form.html.twig` — 8/4 grid with Profile + Record cards left, Photo + AI Profile + Calculated Ratings right.

**Event (Phase 4):**
- `EventList.fxml` ← `event/index.html.twig`
- `EventForm.fxml` ← `event/form.html.twig` — eventName, eventDate (DatePicker+Spinner), organization, dependent city→venue ComboBoxes.
- `EventFights.fxml` ← `event/fights.html.twig` — 8/4 SplitPane: scheduled bouts list + add-fight panel + simulation modal.
- `ChampionsEventForm.fxml` ← `event/champions_form.html.twig` — 3 mutually-exclusive division ComboBoxes.

**Result (Phase 4–5):**
- `ResultList.fxml` ← `result/index.html.twig` — Grid/Gallery view toggle.
- `ResultEnter.fxml` ← `result/enter.html.twig` — hero matchup + outcome card (radio + method + decisionType + round) + media upload.
- `ResultSchedule.fxml` ← `result/schedule.html.twig`
- `ResultShow.fxml` ← `result/show.html.twig` — analytics card, efficiency scores, total-punches grid, round-by-round table, "Inside the Numbers".
- `ResultEventCenter.fxml` ← `result/event_center.html.twig` — TilePane of FightCards.
- `ResultStats.fxml` ← `result/stats.html.twig` — `RoundTabsPane` with red/blue corner stat matrix per round.

**Stats wizard (Phase 7):**
- `StatsHub.fxml` ← `statistic/hub.html.twig` + `statistic/index.html.twig`
- `StatsWizard.fxml` (3 steps via `StackPane` swap) ← `statistic/form_select_event.html.twig` + `form_select_fight.html.twig` + `form.html.twig`
- `StatsShow.fxml` ← `statistic/show.html.twig`

**Rankings / Performance / Predictions (Phase 5–8):**
- `RankingsList.fxml` ← `ranking/index.html.twig` — hero + per-division Champion + table.
- `RankingsMobileQr.fxml` (NEW) — modal showing ZXing QR encoding `http://localhost:8001/rankings/mobile`.
- `Performance.fxml` ← `performance/index.html.twig`
- `PerformanceShow.fxml` ← `performance/show.html.twig`
- `InjuryPredictions.fxml` ← `performance/injury_predictions.html.twig`
- `Predictions.fxml` ← `prediction/index.html.twig`
- `PredictionLeaderboard.fxml` ← `prediction/leaderboard.html.twig`

**Bookings / Contracts / Proposals / Reactions / Blog / Finance (Phase 6, 7, 10):**
- `AdminBookings.fxml` ← `admin/booking/index.html.twig`
- `AdminBookingCreate.fxml` ← `admin/booking/create.html.twig` — live price preview using `BookingService` formula.
- `AdminContracts.fxml` ← `admin/contract/index.html.twig` — PDF via Flying Saucer.
- `MatchProposals.fxml` ← `admin/match_proposal/index.html.twig` + `match_proposal/create.html.twig` (consolidated).
- `Reactions.fxml` ← `admin/reaction/index.html.twig`
- `Finance.fxml` ← `admin/finance/index.html.twig` — BarChart + LineChart + PieChart + table.
- `AdminBlogList.fxml` ← `admin/blog/index.html.twig`
- `AdminBlogForm.fxml` ← `admin/blog/form.html.twig` — uses `HTMLEditor` for content.
- `AdminBlogShow.fxml` ← `admin/blog/show.html.twig`
- `Users.fxml` + `UserForm.fxml` ← `user/index.html.twig` + `user/form.html.twig`
- `UserProfile.fxml` ← `user/profile.html.twig`

**Fan front (Phase 6–8):**
- `FanDashboard.fxml` ← `front/fan_dashboard.html.twig`
- `FanBookingForm.fxml` ← `front/booking/book.html.twig`
- `MyBookings.fxml` ← `front/booking/my_bookings.html.twig` — embedded QR per row.
- `FanBlog.fxml` + `FanBlogShow.fxml` ← `front/blog/index.html.twig` + `front/blog/show.html.twig`
- `FanReactions.fxml` ← `front/reaction/index.html.twig`

**Explicitly NOT ported:**
- Bootstrap/Chart.js/Flatpickr/jsPDF/Quill — JavaFX has native equivalents.
- Symfony flash/CSRF infrastructure.
- `base.html.twig` — replaced by `AdminShell.fxml` / `FanShell.fxml`.
- `partials/_injury_ai_js.html.twig` — JS-only, re-implemented in `InjuryPredictionsController.java`.
- `front/footer.html.twig` / `front/partials/footer.html.twig` — desktop apps don't need footers.
- `ranking/mobile.html.twig` — kept as Twig, served by Symfony at localhost:8001.

---

## 7. Pebble + Flying Saucer pipeline

### 7.1 Emails (Pebble)

`io.pebbletemplates:pebble:3.x` — drop-in Twig replacement for Java. Compatible with `{{ var }}`, `{% if %}`, `{% for %}`, `{{ var|upper }}`, `{{ var|date('Y-m-d') }}`. Replace these unsupported filters: `path(...)`, `asset(...)`, `trans(...)` → hardcoded URLs or remove.

Copy to `src/main/resources/email-templates/`:
- `verification.html.twig` → vars `{userName, verificationUrl}`. URL = `http://localhost:8001/verify-email/<token>`.
- `password_reset.html.twig` → `{userName, resetUrl, expirationTime}`.
- `booking_confirmation.html.twig` → `{user, booking, event, qrCodeBase64}`.

```java
PebbleEngine engine = new PebbleEngine.Builder().build();
PebbleTemplate t = engine.getTemplate("email-templates/verification.html.twig");
StringWriter w = new StringWriter();
t.evaluate(w, Map.of("userName", u.getUsername(), "verificationUrl", url));
mailer.send(u.getEmail(), "Verify your account", w.toString());
```

### 7.2 PDFs (Flying Saucer + OpenPDF)

`org.xhtmlrenderer:flying-saucer-pdf-openpdf:9.x`. Pipeline: Pebble renders Twig → resulting HTML must be **valid XHTML** (self-closed tags, properly nested) — Flying Saucer is strict.

Copy to `src/main/resources/pdf-templates/`:
- `result/pdf.html.twig`, `result/hub_pdf.html.twig`
- `ranking/pdf.html.twig`
- `admin/contract/pdf.html.twig`, `admin/contract/pdf_list.html.twig`
- `performance/injury_pdf.html.twig`

One-time XHTML cleanup pass per template: self-close `<br/>`, `<img/>`, `<hr/>`; replace `{{ asset('logo.png') }}` with `file:///C:/Users/ahmed/.../logo.png` or inline base64 data URIs.

```java
String html = pebbleRenderer.render("pdf-templates/result/pdf.html.twig", vars);
ITextRenderer r = new ITextRenderer();
r.setDocumentFromString(html);
r.layout();
try (FileOutputStream fos = new FileOutputStream(out)) { r.createPDF(fos); }
```

### 7.3 Phase-0.5 smoke test

Before Phase 1 ships, a passing test must:
1. Render `verification.html.twig` to a string and assert it contains the verification URL.
2. Render `result/pdf.html.twig` to `target/test-output/result.pdf` and assert the file is non-empty.

If either fails, Pebble syntax or XHTML validity is wrong — fix before building screens.

---

## 8. File uploads (localhost simplified)

**Three Symfony upload locations are mirrored in localhost mode by writing directly to `<upload.dir>` from JavaFX:**

| DB field | Subdirectory under `<upload.dir>` |
|---|---|
| `Fighter.photo_filename` | `boxers/` |
| `BlogArticle.image_filename` (Vich `blog_image`) | `blog/images/` |
| `BlogArticle.video_filename` (Vich `blog_video`) | `blog/videos/` |
| `FightResult.highlightVideoFilename` (Vich `fight_highlight`) | `fights/highlights/` |
| `Event.poster_filename` | `events/` (verify Symfony's actual location) |

**JavaFX `UploadService`:**
```java
public class UploadService {
    private final Path baseDir;  // from config.properties upload.dir
    public String upload(String subdir, File source) {
        String name = UUID.randomUUID() + "." + ext(source);
        Path target = baseDir.resolve(subdir).resolve(name);
        Files.createDirectories(target.getParent());
        Files.copy(source.toPath(), target);
        return name;  // store in DB
    }
}
```

That's it. No HTTP endpoint, no SMB, no auth token. Both apps share the filesystem.

---

## 9. PDF generation

Already covered in §7.2. PDF entry points used by these features:
- Rankings PDF — Phase 5 (`RankingsList` "Export PDF" button).
- Contract PDF (single + list) — Phase 10 (`AdminContracts`).
- Injury report PDF — Phase 9 (`InjuryPredictions`).
- Result PDF — Phase 5 (`ResultShow` "Export Official Report").

---

## 10. Data import

The DB is already populated by Symfony. **The Java app does NOT seed data.** First boot just connects to the DB and shows what's there. For test isolation:
```bash
mysqldump -u root smartfight > smartfight-snapshot.sql
mysql -u root smartfight_test < smartfight-snapshot.sql
```

---

## 11. AI integration (DeepSeek)

Reuse the existing key from Symfony's `.env`: `DEEPSEEK_API_KEY=sk-23d74dc6f4f14f5d8020ee22ff1939b4`. Endpoint: `https://api.deepseek.com/chat/completions`. Model: `deepseek-chat`.

**Java client:**
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

**Risks:** DeepSeek rate-limits; outbound HTTPS may be restricted. All AI methods have heuristic fallbacks (§5.1).

---

## 12. Same-machine "live sync"

Both apps run on the same PC against the same MySQL instance. The "sync" is just polling — there's no network. When the JavaFX app inserts a row, Symfony's next page load sees it; when Symfony writes a notification, JavaFX's poll picks it up within 5s.

### 12.1 Refresh strategy

| Screen | Strategy | Cadence |
|---|---|---|
| Fighter list | Polling | 5s |
| Event list | Polling | 5s |
| Fight results | Polling | 3s |
| Notifications | Polling | 5s |
| Bookings | On-demand refresh | manual button |
| Rankings | On-demand | manual button (recompute is heavy) |

```java
Timeline poll = new Timeline(new KeyFrame(Duration.seconds(5), e -> refresh()));
poll.setCycleCount(Animation.INDEFINITE);
poll.play();
```

**Polling task must run in `javafx.concurrent.Task`** and update UI via `Platform.runLater` — never block the FX thread on a JDBC call.

### 12.2 Side-effect ordering

When JavaFX submits a fight result:
1. INSERT/UPDATE `fight_results`.
2. Java `RankingService` runs the same algorithm Symfony's would.
3. **All within one DB transaction** (`conn.setAutoCommit(false); ... conn.commit();`).
4. Symfony's next page load (or the JavaFX poll) sees the new state.

**Concurrent ranking recalc:** if both apps simultaneously process the same fight, they double-process. For uni demo with one operator, this is unlikely. Document as known limitation; do not over-engineer.

---

## 13. Demo-day setup checklist (single PC)

### 13.1 MySQL
- [ ] MySQL running, version ≥ 8.0
- [ ] `smartfight` database exists, `smartfight.sql` imported
- [ ] Local `root` (or app user) can connect via `mysql -u root -p smartfight`

### 13.2 Symfony
- [ ] `.env.dev` `DATABASE_URL` points to `localhost:3306`
- [ ] `php bin/console cache:clear`
- [ ] `symfony server:start` (or `php -S localhost:8001 -t public/`)
- [ ] Verify `http://localhost:8001/` loads

### 13.3 JavaFX
- [ ] JDK 17 installed
- [ ] `config.properties` `db.url` points to `localhost:3306`
- [ ] `config.properties` `upload.dir` points to Symfony's `public/uploads/`
- [ ] `mvn javafx:run`
- [ ] First launch shows green DB status (Phase 1 health check)

### 13.4 Pre-demo smoke test (5 min)
1. Symfony: log in, add a fighter "Test Demo".
2. JavaFX: refresh fighter list → "Test Demo" appears (within 5s polling cycle).
3. JavaFX: enter a fight result for an existing scheduled fight.
4. Symfony: view rankings page → updated.
5. Symfony: notification panel shows the broadcast.
6. JavaFX: notification toast appears within 5s.

If any step fails, debug now, not in front of the teacher.

---

## 14. Build sequence

Each phase ends with a demoable checkpoint.

### Phase 0 — Foundations (½ day)
- Pre-flight checks (§0).
- Maven skeleton, JavaFX hello-world.
- Empty `model/`, `dao/`, `service/`, `controller/`, `widget/` packages.
- Externalized `config.properties`.

### Phase 0.5 — Shared infrastructure (½ day)
- Pinned `smartfight.css` (§6.2).
- Shared widgets (§6.4): AdminShell, Sidebar, Topbar, PaginatedTable, AutocompleteSearchField, PhotoUploadPane, QrCodeImageView, FlashAlertStack, LoadingOverlay.
- Pebble + Flying Saucer smoke test passing (§7.3).

### Phase 1 — DB + Auth core (1 day)
- HikariCP `DBConnection` (pool size 8, leak detection 5000ms).
- Health-check screen on startup.
- `User` model + `UserDAO`.
- `PasswordVerifier` with bcrypt (any cost) + Argon2id detection.
- `Login.fxml` + controller.
- **Checkpoint:** log in with a Symfony-created user.

### Phase 2 — Registration + Email (½ day)
- `RegistrationController` → INSERT user, attach USER role, generate 64-hex verification token (`SecureRandom` 32 bytes → hex).
- `GmailMailer` + Pebble-rendered `verification.html.twig`.
- **Checkpoint:** register from JavaFX, see user in Symfony's user list, click email link, verify in browser.

### Phase 3 — Fighter CRUD (1 day)
- `FighterDAO` (full CRUD with all camelCase columns).
- `FighterListController` + `FighterFormController`.
- Photo upload (direct copy to `<upload.dir>/boxers/`).
- "Generate AI profile" button → `AIService.generateFighterProfile`.
- **Checkpoint:** add a fighter in JavaFX, see it in Symfony with photo. **SHIP-IT MOMENT.**

### Phase 4 — Event + FightResult CRUD (1 day)
- All event screens (list, new, edit, fight card editor).
- Fight result entry — **scheduled/completed only, no ranking math yet**.
- **Checkpoint:** schedule a fight in JavaFX, see it on Symfony's `EventFights`.

### Phase 5 — Ranking algorithm (½ day)
- Port `calculateElo`, `calculateSOS`, `getBoxingRankingPoints`, `processCompletedFight`, `updateGlobalRankings`, `recomputeAllRankings`.
- **Java `recomputeAllRankings` MUST also reset `wins/losses/draws/koWins`** (verified Symfony bug fix).
- Wire into `FightResultService.enterResult` so result entry triggers full pipeline.
- Unit tests against known inputs from Symfony.
- **Checkpoint:** Java rankings match Symfony rankings byte-for-byte after `recomputeAll` (modulo the wins/losses fix).

### Phase 6 — Bookings + QR + dynamic pricing (1 day)
- `BookingService.createBooking` with all 5 multipliers (BigDecimal everywhere).
- ZXing QR generation.
- Booking confirmation email (Pebble) with embedded QR PNG.
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
- Injury predictions screen + PDF (Flying Saucer).

### Phase 10 — Notifications + Rankings PDF + Search + Admin remainders (1 day)
- Polling notification panel.
- Rankings PDF (Flying Saucer).
- Global search popup.
- AdminBlog (list/form/show), MatchProposals, Reactions, Finance, AdminContracts, Users, UserProfile.

### Phase 11 — Face ID (½ day or 1.5 days)
- See §3.7. Decide Option A (parity-fake) or Option B (real OpenCV).

### Phase 12 — Polish (½ day)
- Cross-screen QA: tab order, keyboard shortcuts, error toasts.
- Build distributable JAR (`mvn javafx:jlink`).

**Total: ~9–11 working days** for a single dev. Demo-grade trim path (skip Phases 9/10/11) → ~6 days.

---

## 15. Risk register

| Risk | Severity | Mitigation |
|---|---|---|
| Symfony schema drifts mid-port | High | Lock schema after Phase 0; treat changes as coordinated events |
| Mixed bcrypt costs (10 + 13) | High | `PasswordVerifier` reads cost from hash prefix; test against both |
| camelCase column names mistyped | High | Always `SHOW COLUMNS FROM <table>` first |
| Doctrine snake_case naming-strategy assumption | High | Verify §2.3 actual column names before any DAO |
| Doctrine `decimal` → `double` precision loss | High | Use `BigDecimal` everywhere money flows |
| MySQL/Java charset mismatch | High | Pin `characterEncoding=UTF-8` in JDBC URL; verify with `Saúl Álvarez` |
| MySQL/Java timezone drift | Medium | `serverTimezone=UTC` + JVM `-Duser.timezone=UTC` |
| Concurrent ranking recalc | Low | Single-operator demo; documented as known limitation |
| DeepSeek rate limit / outage | Medium | All AI methods have heuristic fallbacks |
| Gmail App Password rotated/revoked | Low | Test SMTP at Phase 2 start; have backup app-password |
| FK constraint violations on delete | Medium | Document deletion order per entity |
| `recomputeAllRankings` wins/losses inflation | Mitigated | Java port resets all four counters explicitly (§5.10) |
| HikariCP connection leak | Medium | try-with-resources + leakDetectionThreshold=5000 |
| FX thread blocked by DB | Medium | Polling/queries via `Task<>` + `Platform.runLater` |
| Webcam not available on demo PC | Medium | Test webcam in Phase 11; have "skip face ID" path |

---

## 16. Symfony bugs/inconsistencies to fix on port (verified)

1. **`AIService::suggestMatches()` does not exist** but is called by `MatchmakingService::suggestMatches`. Java port: skip AI matchmaking; use only `generateProposalsLocal`.
2. **Missed-weight penalty divergence:** `ContractService::calculateFinalPurse` applies 20% basePay penalty when `missed_weight=true`; `FightResultService::enterResult` does NOT on auto-payout. Java port: always go through `ContractService.calculateFinalPurse`.
3. **`MatchmakingService::computeScoreIA` weight mismatch:** docblock says `25/20/15/15/10/10/5` (7 vectors), code uses `25/20/15/8/7/10/10/5` (8 vectors). Match the code.
4. **`recomputeAllRankings` does NOT reset `wins/losses/draws/koWins`** before replaying completed fights → counts inflate. Java port: zero those four fields explicitly.
5. **Two duplicate match-proposal admin controllers** (`/admin/match-proposals` and `/admin/proposals`). Java port: consolidate into one screen.
6. **`FaceIdController::verifyVisual` always logs in `userId ASC` first user with a face_photo** — not a real biometric match. Java can replicate or implement real LBPH.
7. **`PerformanceAnalyzer::getStyleMatchupAnalysis` typo:** `$style1 === 'SHARPSHOOTER' && $style1 === $style2` (the second should be `$style2` semantically; it's a no-op). Fix when porting.
8. **`RoundCommentaryService` Sentence 1 ties** report `f1l` apiece but Sentence 2's winner/loser is derived from `f1l >= f2l` — implicit f1 favoritism on ties. Keep behavior or break tie randomly.

---

## 17. What to do TODAY (first 4 hours)

1. Run §0.1 — verify hash formats on actual current DB. Pull two real hashes (one cost 10, one cost 13) for unit tests.
2. Run §0.2 — dump current schema; **run `SHOW COLUMNS FROM users; SHOW COLUMNS FROM notifications; SHOW COLUMNS FROM predictions;`** and update §2.3 with literal column names.
3. Maven skeleton + JavaFX hello-world.
4. HikariCP DB config; prove connection to `localhost:3306/smartfight`.
5. `PasswordVerifier` + tests against both real hashes.
6. Pinned `smartfight.css` skeleton with color tokens (§6.2).

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
| SoS multipliers | win ×1.3×qualityFactor, loss ×0.85 (no qualityFactor on loss) | RankingService |
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
| Verification token | 64 hex chars (random_bytes(32)) | SecurityController |
| Reset token expiry | 1 hour | SecurityController |
| Min password length | 6 | RegistrationType + reset |
| FaceID min photo length | 500 chars (base64) | FaceIdController |

---

## 19. Verification protocol

After Phase 0:
```bash
# §0.1 verification token length
grep -n "random_bytes" Smartfight/src/Controller/SecurityController.php
# expect: lines 45 (verify), 108-110 (reset) → both random_bytes(32) → 64 hex chars

# §0.1 mixed bcrypt costs in DB
grep -E '\$2y\$1[0-9]\$' Smartfight/smartfight.sql | sed -E 's/.*(\$2y\$1[0-9]\$).*/\1/' | sort -u
# expect: $2y$10$ AND $2y$13$

# §0.2 actual column names
mysql -u root smartfight -e "SHOW COLUMNS FROM users;" \
  | awk '{print $1}' | grep -iE 'verified|reset|webauthn|face'
# read back the literal column names — those are what the Java DAO must use
```

---

## 20. Future: multi-PC deployment (deferred to end of project)

Everything in this section is **out of scope** for the localhost build but documented here so it's not lost. Address only after the full localhost feature set works end-to-end.

### 20.1 Pre-flight cross-PC connectivity check
Before any cross-PC code, prove from the JavaFX PC against the DB PC:
1. `mysql -h <DB_PC_IP> -u smartfight -p smartfight` connects.
2. A user inserted via Symfony (PC #2) is visible from `mysql` on PC #3.
3. Port 3306 is open in firewall on the DB PC.

### 20.2 MySQL configuration changes
- `bind-address = 0.0.0.0` in `my.cnf` (not `127.0.0.1`).
- Restart MySQL after editing.
- Create user `'smartfight'@'%' IDENTIFIED BY '<pwd>'; GRANT ALL ON smartfight.* TO 'smartfight'@'%'; FLUSH PRIVILEGES;`.
- Firewall: allow inbound TCP 3306.
- Note the DB PC's LAN IP (`ipconfig`).

### 20.3 JavaFX `config.properties` changes
```properties
db.url=jdbc:mysql://<DB_PC_IP>:3306/smartfight?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8&useUnicode=true
symfony.base.url=http://<SYMFONY_PC_IP>:8001
```

### 20.4 File uploads — three options to choose from

| Option | Pros | Cons |
|---|---|---|
| **A. Network share (SMB)** | Symfony PC shares `public/uploads/` over SMB; JavaFX writes via UNC `\\<symfonyPC>\uploads\boxers\<file>`. | SMB access, firewall config, may be slow over Wi-Fi. |
| **B. NFS / shared mount** | Linux-friendly. | Less common on Windows lab PCs. |
| **C. HTTP upload endpoint in Symfony** | Cleanest. JavaFX POSTs to `/api/upload/{type}`; Symfony writes locally. | Requires new Symfony endpoint + auth token. |

**Recommended: Option C.** Add this Symfony endpoint:
```php
#[Route('/api/upload/{type}', methods: ['POST'])]
public function upload(string $type, Request $request) {
  // auth via X-API-Key header against an env-var token
  // accept type ∈ {boxers, blog/images, blog/videos, fights/highlights, events}
  // move uploaded file to public/uploads/<type>/, return JSON {filename}
}
```
JavaFX POSTs as `multipart/form-data` to this endpoint and writes only the returned filename to the DB. The Java `UploadService` must be swapped from local-copy to HTTP-POST.

### 20.5 QR code targets
The `RankingsMobileQr.fxml` modal must encode `http://<SYMFONY_PC_IP>:8001/rankings/mobile` instead of localhost. Phones scanning the QR must be on the same Wi-Fi network.

### 20.6 Live-sync transactional safety
With two operators on two PCs, concurrent ranking recalc becomes a real risk. Add either:
- An `is_processed` flag on `fight_results` (coordinated migration), OR
- `SELECT ... FOR UPDATE` to serialize processing in `RankingService.processCompletedFight`.

### 20.7 Three-PC demo-day checklist

**MySQL PC:**
- [ ] MySQL ≥ 8.0
- [ ] `bind-address = 0.0.0.0`
- [ ] Firewall: inbound TCP 3306
- [ ] Note LAN IP

**Symfony PC:**
- [ ] `.env.dev` `DATABASE_URL` → `<MYSQL_PC_IP>:3306`
- [ ] `php -S 0.0.0.0:8001 -t public/`
- [ ] Note LAN IP (for QR codes)
- [ ] `http://<symfony-pc-ip>:8001/` reachable from another PC

**JavaFX PC:**
- [ ] JDK 17 installed
- [ ] `config.properties` updated with `<MYSQL_PC_IP>:3306`
- [ ] `config.properties` updated with `symfony.base.url=http://<SYMFONY_PC_IP>:8001`
- [ ] Upload service swapped to HTTP-POST to `/api/upload/{type}`
- [ ] First launch shows green DB status

End of plan.
