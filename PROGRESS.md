# SmartFight JavaFX – Progress Log

> **Status: ALL PHASES COMPLETE** — `mvn compile` clean, `mvn test` all pass (2026-05-09)
>
> Run the app: `mvn javafx:run`

---

## Complete Feature Summary

### Infrastructure & Configuration
| Component | Details |
|---|---|
| Build | Maven, Java 17, JavaFX 21.0.2, `javafx-maven-plugin` 0.0.8 |
| Database | MariaDB/MySQL via HikariCP connection pool (`DBConnection`) |
| Config | `AppConfig` singleton reads `config.properties` (db, mail, deepseek, upload.dir, symfony.base.url) |
| Auth config | bcrypt-13 for new users; `PasswordVerifier` supports legacy `$2y$`/`$2a$`/`$2b$` jBCrypt + `$argon2…` argon2-jvm |
| Email | `GmailMailer` via Jakarta Mail SMTP — host: smtp.gmail.com, port: 587 (STARTTLS), user: teamsmartfight@gmail.com, app-password configured in `config.properties` |
| Templates | `PebbleRenderer` (Pebble 3.2.2, Twig-compatible, ClasspathLoader) |
| PDF | `PdfRenderer` via Flying Saucer 9.1.22 + OpenPDF |
| AI | `DeepSeekClient` — calls `https://api.deepseek.com/v1/chat/completions`; API key configured in `config.properties`; `chatRaw(prompt, maxTokens)` public method; all AIService methods try DeepSeek first, fall back to heuristics |
| Webcam | `webcam-capture 0.3.12` (Face ID capture) |
| QR | ZXing 3.5.3 — `QrCodeGenerator` (BufferedImage, WritableImage, PNG bytes/base64) |
| Session | `Session` singleton (logged-in `User`); `AccessGuard.isAdmin()` |
| CSS | `styles/smartfight.css` — UFC black/red/white theme (`#0a0a0a`, `#d20a0a`, `#f0f0f0`) |

---

### Authentication & User Management
| Feature | Implementation |
|---|---|
| Login | `Login.fxml` + `LoginController` — username or email, bcrypt/argon2 verify, routes to AdminShell or FanShell |
| Face ID Login | "Sign In with Face ID" button — `FaceIdRecognizer.recognize()` via webcam; finds first user with `face_photo` (demo) |
| Register | `Register.fxml` + `RegisterController` — bcrypt-13 hash, email verification link sent via Pebble + GmailMailer |
| Register Face ID | AdminShell sidebar → `FaceIdRecognizer.registerFaceId()` captures webcam frame, stores base64 in `users.face_photo` |
| Email verify | Token-based verification; `UserDao.setVerified()` |

---

### Shell & Navigation
| Component | Details |
|---|---|
| `BaseShellController` | StackPane `contentHost` swap-in pattern; `loadContent(fxmlPath)`; `onLogout()` clears session + reloads Login; **notification polling** (5s `Timeline`, stops on logout) |
| `AdminShell.fxml` | Top-bar: SmartFight Admin label, welcome, 🔔 bell + unread badge, Logout. Sidebar: Fighters, Events, Results, Rankings, Blog, Stats, Performance, Injury AI, Finance, Contracts, Proposals, Register Face ID |
| `FanShell.fxml` | Top-bar: SmartFight label, welcome, 🔔 bell + unread badge, Logout. Sidebar: Events, My Bookings, Blog, Predictions, Leaderboard |
| Access guard | `AdminShellController.initialize()` calls `AccessGuard.isAdmin()` — redirects to login if not admin |

---

### Notification System
| Component | Details |
|---|---|
| `Notification` model | notificationId, message, createdAt, isRead, type, userId |
| `NotificationDao` | `findRecentByUser(userId, limit)`, `countUnread(userId)`, `markAsRead(id)`, `markAllReadForUser(userId)`, `create(n)`, `findAllUserIds()` |
| `NotificationService` | `notifyUser`, `notifyAllFans`, `notifyRankingUpdate`, `notifyNewArticle`, `notifyPredictionResult`, `notifyFanFavoriteEvent`, `notifyBookingConfirmed` |
| Polling UI | 5-second `javafx.animation.Timeline` in `BaseShellController.startNotificationPolling()` — off-thread `countUnread()`, updates badge label; both shells call it in `initialize()` |
| Popup | `onBellClick()` loads recent 20 notifications off-thread, shows `Popup` with `ListView`; click item marks read; "Mark all read" button; auto-hide |

---

### Admin Features

#### Fighters
| Feature | Implementation |
|---|---|
| Fighters CRUD | `FighterDao` full CRUD; `FightersList.fxml` + `FightersListController` — table + New/Edit/Delete |
| Fighter form | `FighterForm.fxml` + `FighterFormController` — all fields, photo upload stub, AI style tag via DeepSeek |

#### Events
| Feature | Implementation |
|---|---|
| Events CRUD | `EventDao` full CRUD; `EventsList.fxml` + `EventsListController` — table + New/Edit/Delete |
| Event form | `EventForm.fxml` + `EventFormController` — ComboBox for status and type enums |

#### Fight Results
| Feature | Implementation |
|---|---|
| Results CRUD | `FightResultDao` (create returns generated ID); `ResultsList.fxml` + `ResultsListController` |
| Result form | `ResultForm.fxml` + `ResultFormController` — 21-field form; on status → COMPLETED: triggers `RankingService.processCompletedFight()` in background task |

#### Rankings
| Feature | Implementation |
|---|---|
| Rankings list | `RankingsList.fxml` + `RankingsListController` — table (pos, org, fighter), org filter ComboBox (ALL/WBC/WBA/IBF/WBO/MEDIA), Refresh, Recompute All (confirmation dialog) |
| Rankings PDF | "Export PDF" → builds `groupedRankings` map → `PebbleRenderer` + `PdfRenderer` → `FileChooser` save; template: `templates/ranking/pdf.html.twig` |
| Ranking algorithm | `RankingService.processCompletedFight(resultId)` — single JDBC transaction: increment wins/losses/draws/koWins/decisionWins/titleDefenses, ELO update, winStreak, SOS recompute, `updateGlobalRankings()` |
| Recompute all | Resets ALL fighter stats (elo=800, perf=0, wins/losses/draws/koWins=0, etc.), replays all COMPLETED fights chronologically |

#### Blog Admin
| Feature | Implementation |
|---|---|
| Blog CRUD | `BlogDao` extended with `create`, `update`, `deleteById`; `AdminBlogList.fxml` + `AdminBlogListController` |
| Blog form | `AdminBlogForm.fxml` + `AdminBlogFormController` — title, summary, content, status |

#### Fight Statistics
| Feature | Implementation |
|---|---|
| Stats wizard | `StatsWizard.fxml` + `StatsWizardController` — 3-step: select event → select fight → enter round stats |
| Validation | landed ≤ thrown, right+left = total, uppercuts ≤ total; §4.5 rules |
| Persistence | `FightStatistic` model + `FightStatisticDao` — save/upsert by fight+fighter+round; `findSummedByFighter()` |
| Analytics | `AnalyticsEngine.calculatePerformanceScore()` §5.2 — punchAcc×0.5 + powerAcc×0.3 + kdBonus×0.2, DQ penalty −50, belt multiplier; updates `fighters.performanceScore` |

#### Performance Screen
| Feature | Implementation |
|---|---|
| Performance list | `Performance.fxml` + `PerformanceListController` — sortable table (ELO, perf score, efficiency §5.12, momentum §5.12, win streak, record, title defenses) |
| Recalculate All | Runs `AnalyticsEngine.calculatePerformanceScore()` per fighter in background Task |

#### Injury AI
| Feature | Implementation |
|---|---|
| Injury predictions | `InjuryPredictions.fxml` + `InjuryPredictionsController` — fighter ComboBox, Analyze (single), Analyze All (ProgressBar) |
| Result pane | Displays riskLevel, totalRisk %, zone, daysToAlert, mitigation, AI accuracy, ROI |
| All-fighters table | `TableView<InjuryRow>` typed rows (fighter name + InjuryRiskResult) |
| Injury PDF | "Export PDF Report" → `PebbleRenderer` + `PdfRenderer` → `FileChooser` save; template: `templates/performance/injury_pdf.html.twig` |

#### Finance
| Feature | Implementation |
|---|---|
| Finance charts | `Finance.fxml` + `FinanceController` — `LineChart` (daily revenue 30d), `PieChart` (by ticket type), `BarChart` (by event top-10); total revenue/bookings labels |

#### Contracts
| Feature | Implementation |
|---|---|
| Fighter Contracts | `FighterContract` model + `ContractDao` (findAll with LEFT JOIN fighters+events, create, update, deleteById) |
| Purse calculator | `ContractService.calculateFinalPurse(contract, isWinner)` §5.5 — missedWeight×0.8, winBonus if winner, managerFee deducted; returns `PurseBreakdown` record |
| Contracts UI | `ContractList.fxml` + `ContractListController` — table + New Contract dialog, Pay (shows breakdown), Toggle Missed Weight, Delete |
| Contract PDF | "Export PDF" → flat variables → `PebbleRenderer` + `PdfRenderer` → `FileChooser` save; template: `templates/admin/contract/pdf.html.twig` |

#### Match Proposals
| Feature | Implementation |
|---|---|
| Match proposals | `MatchProposal` model + `MatchProposalDao` (findAll/findByStatus with JOINs, create, updateStatus, deleteById) |
| Matchmaking AI | `MatchmakingService.computeScoreIA(f1,f2,usedDivs)` §5.7 — 8-vector: weight(25)+ELO parity(20)+record parity(15)+height(8)+reach(7)+lethality(10)+precision(10)+diversity(5) |
| Generate proposals | `generateProposalsLocal(count)` — builds top-50 scored pairs, greedy-picks eligible, persists via `MatchProposalDao` |
| Proposals UI | `MatchProposals.fxml` + `MatchProposalsController` — table with status filter, Generate AI Proposals dialog, Approve/Reject/Delete |

---

### Fan Features

#### Bookings
| Feature | Implementation |
|---|---|
| Event booking | `BookingForm.fxml` + `BookingFormController` — event/type selection, real-time price calc in background Task |
| Dynamic pricing | `BookingService.calculateUnitPrice()` — base × eloMult × championsMult × fanMult × loyaltyMult (5 multipliers) |
| Ticket types | VIP_RINGSIDE($200), PREMIUM_LOWER($120), REGULAR_SEATING($75), BALCONY($50), STANDING_ROOM($30) |
| Booking flow | qty 1–4 validation, `SF-XXXXXXXX` reference generated, persisted, confirmation email with QR code |
| My Bookings | `Bookings.fxml` + `BookingsController` — list of user's bookings |

#### Blog (Fan Read-Only)
| Feature | Implementation |
|---|---|
| Blog feed | `Blog.fxml` + `BlogController` — article list, read-only |

#### Predictions
| Feature | Implementation |
|---|---|
| Win probability | `PredictionService.calculateWinProbability(f1Id,f2Id)` §5.9 — 7-vector: eloNorm×0.30, winRate×0.20, koRate×0.10, strikeAcc×0.10, form×0.10, perf×0.10, phys×0.10; drawProb 4–6% |
| Predictions UI | `Predictions.fxml` + `PredictionsController` — fight selection, AI probability display, prediction form (winner/method/round), my-predictions table |
| Points scoring | `calculatePoints(prediction,result)` §5.9 — winner(10)+method(10)+early_finish(20)+round(10), max 50 |
| Leaderboard | `Leaderboard.fxml` + `LeaderboardController` — top-50 users ranked by `predictionPoints` |

---

### AI Service (§5.1)
| Method | Implementation |
|---|---|
| `analyzeFightDynamics(f1,f2)` | §5.1.1 — 4-vector heuristic (elo×0.45, phys×0.15, lethality×0.20, momentum×0.20); normVector = `clamp(50+diff/(max(1,|diff|×0.1)),0,100)`; edge labels (Technical Superiority / Kinetic Parity / Underdog Momentum); DeepSeek enrichment optional |
| `predictInjuryRisk(fighter)` | §5.1.2 — ageFactor=max(0,(age-30)×3), loadFactor=totalFights×0.8, traumaFactor=koLosses×15, totalRisk=min(95,10+sum); risk zones by (age+fights)%4 |
| `generateFighterProfile(f)` | §5.1.3 — tries DeepSeek; fallback: koRate>70→"Power Puncher", wins>15→"Veteran Technician", else→"Rising Contender" |
| `generateText(prompt)` | §5.1.4 — tries DeepSeek; deterministic fallback string |
| `generateHeuristicRecap/Scouting/Comparison/Stats` | §5.1.5 — DeepSeek + string template fallbacks |
| `calculateEfficiencyScore(f)` | §5.12 — accuracy×0.4 + winRate×0.4 + streakBonus; capped 100 |
| `getMomentum(f)` | §5.12 — SCORCHING / RISING / STRUGGLING / STABLE |

---

### Ranking Algorithm (§5.10)
| Formula | Value |
|---|---|
| K-factor | 60 if totalFights < 12, else 32 |
| ELO expected | `1 / (1 + 10^((loserElo − winnerElo)/400))` |
| KO domBonus | `1.5 + max(0, 12 − roundNumber) × 0.05` |
| UD domBonus | 1.2 |
| Belt multipliers | winner ×1.25, loser ×0.75 |
| ELO floor | 100 |
| SOS (win) | `oppElo × 1.3 × (oppElo/1200)` |
| SOS (loss) | `oppElo × 0.85` |
| SOS default | 1000.0 (no fights) |
| Points formula | `(R×0.45 + S×0.35 + L×0.20) × decay` |
| R component | `elo/15 + perfScore×1.5` |
| S component | `(sos/1200)×80` |
| L component | `titleDefenses×20 + winStreak×2` |
| Inactivity decay | 0–180d: 1.0 · 180–730d: `1−(days−180)/550` · >730d: 0 |
| Org count | 5 orgs (WBC, WBA, IBF, WBO, MEDIA) × top 16 per division |

---

### PDF Templates (Pebble-rendered → Flying Saucer)
| Template | Variables |
|---|---|
| `templates/ranking/pdf.html.twig` | `groupedRankings` (Map<division,List<{fullName,org,pts}>>), `lastUpdated` (pre-formatted string), `year` (string), `qrSvg` |
| `templates/admin/contract/pdf.html.twig` | `fighterName`, `eventName`, `eventDate`, `divisionName`, `basePay`, `winBonus`, `hasPayout` (boolean), `calculatedPayout`, `maxPurse`, `today` |
| `templates/performance/injury_pdf.html.twig` | `fighterName`, `division`, `wins/losses/draws`, `age`, `totalFights`, `koLosses`, `fightingStyle`, `eloRating`, `riskLevel`, `riskClass`, `riskPct`, `zone`, `daysToAlert`, `mitigation`, `accuracy`, `roi`, `generatedAt` |
| `templates/emails/verification.html.twig` | `user`, `verifyUrl`, `now` |
| `templates/emails/booking_confirmation.html.twig` | `booking`, `qrDataUri`, `eventDate` (pre-formatted), `unitPrice`/`totalPrice` (pre-formatted) |

---

### Full File Map

```
src/main/java/tn/smartfight/
├── App.java                          JavaFX entry point
├── config/
│   ├── AppConfig.java                singleton, config.properties
│   ├── DBConnection.java             lazy HikariCP DataSource
│   └── Session.java                  logged-in User holder
├── auth/
│   ├── PasswordVerifier.java         bcrypt + argon2id
│   └── FaceIdRecognizer.java         webcam-capture: captureBase64, recognize, registerFaceId
├── util/
│   ├── AccessGuard.java
│   ├── TokenGenerator.java
│   └── ReferenceGenerator.java
├── model/
│   ├── User.java
│   ├── Fighter.java
│   ├── FighterDetails.java
│   ├── Event.java
│   ├── EventDetails.java
│   ├── Booking.java
│   ├── BlogArticle.java
│   ├── FightResult.java
│   ├── FightStatistic.java
│   ├── Ranking.java
│   ├── Prediction.java
│   ├── Notification.java
│   ├── FighterContract.java
│   └── MatchProposal.java
├── dao/
│   ├── UserDao.java                  + findFirstWithFacePhoto, saveFacePhoto
│   ├── FighterDao.java
│   ├── EventDao.java
│   ├── BookingDao.java
│   ├── BlogDao.java                  full CRUD
│   ├── FightResultDao.java           create → int
│   ├── FightStatisticDao.java
│   ├── RankingDao.java
│   ├── PredictionDao.java
│   ├── NotificationDao.java
│   ├── ContractDao.java
│   └── MatchProposalDao.java
├── service/
│   ├── RankingService.java           ELO/SOS/points pipeline + notifications
│   ├── BookingService.java           dynamic pricing + QR + email
│   ├── AnalyticsEngine.java          performanceScore §5.2
│   ├── PredictionService.java        win probability §5.9 + points
│   ├── AIService.java                8 methods + heuristic fallbacks §5.1
│   ├── NotificationService.java
│   ├── ContractService.java          purse calc §5.5
│   └── MatchmakingService.java       8-vector score §5.7
├── integration/
│   ├── GmailMailer.java
│   ├── PebbleRenderer.java
│   ├── PdfRenderer.java
│   ├── DeepSeekClient.java           chatRaw() public
│   └── QrCodeGenerator.java
└── controller/
    ├── BaseShellController.java      swap-in + logout + notification polling
    ├── LoginController.java          + onFaceIdLogin
    ├── RegisterController.java
    ├── DashboardController.java
    ├── AdminShellController.java     + onRegisterFaceId
    ├── FanShellController.java
    ├── FightersListController.java
    ├── FighterFormController.java
    ├── EventsListController.java
    ├── EventFormController.java
    ├── BookingsController.java
    ├── BookingFormController.java
    ├── BlogController.java
    ├── ResultsListController.java
    ├── ResultFormController.java
    ├── RankingsListController.java   + onExportPdf
    ├── AdminBlogListController.java
    ├── AdminBlogFormController.java
    ├── StatsWizardController.java
    ├── PerformanceListController.java
    ├── InjuryPredictionsController.java  + onExportPdf
    ├── FinanceController.java
    ├── ContractListController.java   + onExportPdf
    ├── MatchProposalsController.java
    ├── PredictionsController.java
    └── LeaderboardController.java

src/main/resources/
├── config.properties
├── styles/smartfight.css
├── templates/
│   ├── emails/
│   │   ├── verification.html.twig
│   │   └── booking_confirmation.html.twig
│   ├── ranking/pdf.html.twig
│   ├── admin/contract/pdf.html.twig
│   └── performance/injury_pdf.html.twig
└── tn/smartfight/views/
    ├── Login.fxml                    + Face ID button
    ├── Register.fxml
    ├── Dashboard.fxml
    ├── AdminShell.fxml               full sidebar + bell badge
    ├── FanShell.fxml                 sidebar + bell badge
    └── admin/
        ├── AdminDashboard.fxml
        ├── FightersList.fxml / FighterForm.fxml
        ├── EventsList.fxml / EventForm.fxml
        ├── ResultsList.fxml / ResultForm.fxml
        ├── RankingsList.fxml
        ├── AdminBlogList.fxml / AdminBlogForm.fxml
        ├── StatsWizard.fxml
        ├── Performance.fxml
        ├── InjuryPredictions.fxml
        ├── Finance.fxml
        ├── ContractList.fxml
        └── MatchProposals.fxml
    └── fan/
        ├── FanDashboard.fxml
        ├── Bookings.fxml / BookingForm.fxml
        ├── Blog.fxml
        ├── Predictions.fxml
        └── Leaderboard.fxml
```

---

## Session Log (brief)

| Session | Phase | Key additions |
|---|---|---|
| 1 | Phase 0 | Scaffold, auth (login/register/bcrypt/argon2), shells, email, Pebble, PDF, tests |
| 2 | Phase 1 | Admin CRUD: Fighters, Events |
| 3 | Phase 2 | Fan: Bookings, Blog read-only |
| 4 | Phase 3+4 | Fight Results CRUD, DeepSeek client stub |
| 5 | CSS | UFC black/red/white theme rewrite |
| 6 | Phase 5 | RankingService (ELO/SOS/points pipeline), Rankings UI, Blog admin CRUD, ResultForm → RankingService wired |
| 7 | Phase 6+7+8 | BookingService (dynamic pricing + QR + email), FightStatistic wizard + AnalyticsEngine, PredictionService + Predictions + Leaderboard |
| 8 | Phase 9+10 | AIService (8 methods + heuristics), Performance, InjuryPredictions, NotificationService, Finance charts, ContractService, MatchmakingService, Match Proposals |
| 9 | Phase 11 | Notification polling UI (bell/badge/popup), Rankings PDF + Contract PDF + Injury PDF exports, FaceIdRecognizer (webcam), Login Face ID button, Register Face ID in admin |

| 10 | Admin UI Redesign | AdminShell full rebuild (navbar + 17-item sidebar), Dashboard redesign, Users page, Fighters card grid, Events card grid + ManageCard, AdminLeaderboard (podium + table), ResultsList → event-card grid, AdminBookings (stats bar + cancel), AdminFanReactions (pin/delete cards), RankingsList dark-theme upgrade, CSS  added, StatsWizard red color fix |
| 10 | Admin UI Redesign | AdminShell rebuild, Dashboard/Users/Fighters/Events/ManageCard/Leaderboard/ResultsList/Bookings/FanReactions pages redesigned to dark design system; RankingsList upgraded; CSS content-pane added; StatsWizard red color fixed |
| 11 | Bug Fixes | Fixed 6 runtime FXML load failures: (1) AdminDashboard `text="$0.00"` → `"0.00"` (4 labels) — FXML treats `$` as variable binding prefix; (2) AdminBookings `text="$0"` → `"0"` same cause; (3) Added `<?import java.lang.String?>` to AdminUsers, FightersList, ResultsList, AdminFanReactions — `<String fx:value>` elements require explicit import; (4) Fixed `var pane = loader.load()` → `javafx.scene.Node pane` in DashboardController, ManageCardController, EventsListController — type erasure made `setAll(Object)` unresolvable |
| 12 | Adaptive UI | Shell starts maximized (`setMaximized(true)` on login→shell nav); min-size guards (860×580 login, 1100×700 shell); collapsible sidebar (48px icon-only ↔ 220px, `«»` toggle button in AdminShell + FanShell); `CONSTRAINED_RESIZE_POLICY` on 6 overflowing tables (AdminBookings, AdminUsers, RankingsList, Performance, ContractList, MatchProposals); unfroze `maxWidth` on Dashboard stats panel + Leaderboard right panel; Finance PieChart now uses `HBox.hgrow`; search field CSS min-width 300→180px |
