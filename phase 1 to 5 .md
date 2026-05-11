Gap Analysis — SmartFight JavaFX Desktop vs. Implementation Plan

Plan Build Sequence vs. Actual State

┌─────────┬──────────────────────────────────────────────────────┬──────────────────────────────┐
│  Phase  │                What the Plan Requires                │            Status            │
├─────────┼──────────────────────────────────────────────────────┼──────────────────────────────┤
│ Phase 0 │ Maven skeleton, config, DB connection                │ ✅ Done                      │
├─────────┼──────────────────────────────────────────────────────┼──────────────────────────────┤
│ Phase   │ CSS, shared widgets, Pebble+PDF smoke test           │ ⚠️ Partial — CSS is a 2-line │
│ 0.5     │                                                      │  stub                        │
├─────────┼──────────────────────────────────────────────────────┼──────────────────────────────┤
│ Phase 1 │ Auth core: login, registration, DB health            │ ✅ Done (with caveats)       │
├─────────┼──────────────────────────────────────────────────────┼──────────────────────────────┤
│ Phase 2 │ Registration + email verify                          │ ✅ Done (with caveats)       │
├─────────┼──────────────────────────────────────────────────────┼──────────────────────────────┤
│ Phase 3 │ Fighter full CRUD + photo upload + AI profile        │ ⚠️ Partial — CRUD works,     │
│         │                                                      │ photo & AI absent            │
├─────────┼──────────────────────────────────────────────────────┼──────────────────────────────┤
│ Phase 4 │ Event CRUD + fight result entry                      │ ⚠️ Partial — Event CRUD      │
│         │                                                      │ works; fight results absent  │
├─────────┼──────────────────────────────────────────────────────┼──────────────────────────────┤
│ Phase   │ Rankings, bookings pricing, stats, predictions, AI,  │ ❌ Not started               │
│ 5–12    │ notifications, finance, contracts, face ID           │                              │
└─────────┴──────────────────────────────────────────────────────┴──────────────────────────────┘

  ---
Phase 0–2: Foundations & Auth

What is done correctly:
- AppConfig, DBConnection (HikariCP pool=8, leak=5000ms), Session, PasswordVerifier all match spec.
- $2y$ → $2a$ BCrypt conversion: confirmed correct.
- Token is exactly 64 hex chars (32 bytes SecureRandom).
- Login routes ROLE_ADMIN → AdminShell, else FanShell. No FX-thread DB calls.
- RegisterController: BCrypt cost 13, 64-hex token, Pebble email, all on background Task.
- GmailMailer (Jakarta Mail, STARTTLS 587), PebbleRenderer, PdfRenderer all implemented.

Critical gaps:

1. upload.dir missing from both AppConfig.java and config.properties. Every Phase 3+ feature
   (fighter photos, blog images, fight highlights, PDF output dirs) depends on this. Nothing can write
   a file without it.
2. deepseek.api.key and deepseek.api.url missing from AppConfig and config.properties. Phase 9 AI
   features cannot be configured.
3. mail.smtp.host is a require() call — it throws IllegalStateException if blank. The spec says all
   SMTP fields are optional; the implementation treats the host as required.
4. SMTP failure is post-commit: user row and role are inserted into the DB before the SMTP check
   fires. If SMTP is unconfigured, the DB has the user but the task reports failure. A retry will throw
   a duplicate-key error. No transaction wraps insert + email.
5. PasswordVerifierTest tests cost-12 BCrypt, not cost-10 or cost-13. The spec explicitly requires
   unit tests against both cost-10 and cost-13 real DB hashes.
6. Session.currentUser and DBConnection.getDataSource() have unsynchronized lazy initialization —
   technically a JMM race, though unlikely to bite in demo.

  ---
Phase 3: Fighter CRUD

What is done correctly:
- FighterDao has all 5 operations: findAll, getById, create, update, deleteById.
- FightersListController wires New/Edit/Delete with confirmation dialogs.
- FighterFormController supports both CREATE and EDIT modes correctly.
- All FXML fx:ids match controller @FXML fields exactly (no injection failures).

Gaps:

┌───────────────────────────────────────────┬───────────────────────────────────────────────────┐
│                   Item                    │                      Status                       │
├───────────────────────────────────────────┼───────────────────────────────────────────────────┤
│ Fighter fields performanceScore,          │                                                   │
│ winStreak, strengthOfSchedule (default    │ ❌ Absent from model, DAO SQL, and form           │
│ 1500), titleDefenses                      │                                                   │
├───────────────────────────────────────────┼───────────────────────────────────────────────────┤
│                                           │ ⚠️ Mixes camelCase (koWins) with snake_case       │
│ FighterDao SQL column naming              │ (technical_wins, ko_losses, weight_division_id) — │
│                                           │  spec says all camelCase                          │
├───────────────────────────────────────────┼───────────────────────────────────────────────────┤
│ Photo upload (FileChooser → copy to       │ ❌ Completely absent                              │
│ upload.dir/boxers/)                       │                                                   │
├───────────────────────────────────────────┼───────────────────────────────────────────────────┤
│ "Generate AI Profile" button              │ ❌ Completely absent                              │
├───────────────────────────────────────────┼───────────────────────────────────────────────────┤
│ All DAO methods silently swallow          │ ⚠️ Critical cross-cutting issue — SQL errors      │
│ exceptions (catch (Exception ignored))    │ disappear with no log, no rethrow                 │
└───────────────────────────────────────────┴───────────────────────────────────────────────────┘

  ---
Phase 4: Event CRUD

What is done correctly:
- EventDao: all 5 operations present.
- EventsListController: New/Edit/Delete wired with confirmation dialogs.

Gaps:

┌────────────────────────────────────────────────────────────────┬──────────────────────────────┐
│                              Item                              │            Status            │
├────────────────────────────────────────────────────────────────┼──────────────────────────────┤
│ organizationField — should be a ComboBox with values WBC, WBA, │ ❌ Is a plain TextField; no  │
│  IBF, WBO, INDEPENDENT, FAN CHOICE                             │ enum enforcement             │
├────────────────────────────────────────────────────────────────┼──────────────────────────────┤
│ Default values: organization=INDEPENDENT, status=SCHEDULED,    │ ❌ Form opens blank; no      │
│ visibility=PUBLIC                                              │ defaults set                 │
├────────────────────────────────────────────────────────────────┼──────────────────────────────┤
│ championsField — should be a CheckBox                          │ ❌ Is a plain TextField      │
├────────────────────────────────────────────────────────────────┼──────────────────────────────┤
│ Fight result entry screen                                      │ ❌ Not started               │
└────────────────────────────────────────────────────────────────┴──────────────────────────────┘

  ---
Bookings (Phase 6 prep)

┌─────────────────────────────────────────────────────┬─────────────────────────────────────────┐
│                        Item                         │                 Status                  │
├─────────────────────────────────────────────────────┼─────────────────────────────────────────┤
│ ReferenceGenerator: SF- + 8 uppercase hex chars     │ ✅ Correct                              │
├─────────────────────────────────────────────────────┼─────────────────────────────────────────┤
│ BookingDao: findByUserId, create                    │ ✅ Done                                 │
├─────────────────────────────────────────────────────┼─────────────────────────────────────────┤
│ Quantity validated > 0 only — spec requires cap at  │ ❌ Upper bound missing                  │
│ 4                                                   │                                         │
├─────────────────────────────────────────────────────┼─────────────────────────────────────────┤
│ Ticket type validated against enum VIP_RINGSIDE,    │ ❌ Any non-blank string accepted        │
│ PREMIUM_LOWER...                                    │                                         │
├─────────────────────────────────────────────────────┼─────────────────────────────────────────┤
│ Dynamic pricing formula (5 multipliers, BigDecimal) │ ❌ Not started; price is a free-entry   │
│                                                     │ text field                              │
├─────────────────────────────────────────────────────┼─────────────────────────────────────────┤
│ Event selected by typed integer ID                  │ ❌ Should be a ComboBox/dropdown of     │
│                                                     │ real events                             │
├─────────────────────────────────────────────────────┼─────────────────────────────────────────┤
│ Booking cancel/update                               │ ❌ Not implemented                      │
└─────────────────────────────────────────────────────┴─────────────────────────────────────────┘

  ---
Blog

BlogDao and BlogController are read-only list displays. No create, update, or delete is implemented
at any layer (model, DAO, controller, FXML).

  ---
Admin/Fan Shell Navigation

┌──────────────────────┬──────────────────────┬─────────────────────────────────────────────────┐
│        Shell         │    Buttons wired     │               Missing navigation                │
├──────────────────────┼──────────────────────┼─────────────────────────────────────────────────┤
│ AdminShellController │ Dashboard, Fighters, │ Blog admin, Bookings admin, Results, Rankings,  │
│                      │  Events              │ Finance, Contracts, Users                       │
├──────────────────────┼──────────────────────┼─────────────────────────────────────────────────┤
│ FanShellController   │ Dashboard, Bookings, │ Fighter browse, Predictions, Reactions          │
│                      │  Blog                │                                                 │
└──────────────────────┴──────────────────────┴─────────────────────────────────────────────────┘

  ---
CSS (smartfight.css)

The file is a 2-line stub:
.root { -fx-font-family: "Segoe UI"; -fx-background-color: #111111; }
.label { -fx-text-fill: #d4af37; -fx-font-size: 20px; }

Everything in spec §6.2 is absent: status pill classes (.status-pill-scheduled, .status-pill-live,
etc.), color token system (-accent-gold, -corner-red, etc.), typography scale (.h1/.h2/.h3/.body),
card styles, button styles.

  ---
Phases 5–12: Not Started

None of the following have any source files:
- RankingService (ELO, SOS, processCompletedFight, updateGlobalRankings)
- BookingService (dynamic pricing formula, QR embedding)
- FightResultService, FightStatisticController (3-phase wizard)
- PredictionService, AnalyticsEngine
- DeepSeekClient, AIService (with heuristic fallbacks)
- NotificationService (polling panel)
- QrCodeGenerator (ZXing)
- Blog admin CRUD, Finance, Contracts, Match Proposals, Reactions, Users, UserProfile
- Face ID (FaceIdRecognizer)

  ---
Implementation Progress (updated 2026-05-09)
═══════════════════════════════════════════

LAYER 0 — Critical Fixes                                              ✅ COMPLETE
───────────────────────────────────────────────────────────────────────────────
✅ 0.1  upload.dir, deepseek.api.key, deepseek.api.url added to AppConfig + config.properties
✅ 0.1  mail.smtp.host changed from require() → optional (blank-tolerant)
✅ 0.2  FighterDao, EventDao, BookingDao, BlogDao: catch(Exception ignored) → Logger.SEVERE
✅ 0.3  RegisterController: best-effort SMTP (DB row kept; user warned if mail not configured)
✅ 0.4  PasswordVerifierTest: added cost-13, $2y$ prefix rewrite, unknown-prefix=false tests

LAYER 1 — Phase 3: Fighter CRUD Completion                            ✅ COMPLETE
───────────────────────────────────────────────────────────────────────────────
✅ 1.1  FighterDetails model — rewritten as mutable POJO with all 12 new DB columns
✅ 1.2  FighterDao SQL — create/getById/update now cover all 30 fighter columns
✅ 1.3  FighterForm.fxml — 29 rows: all 12 new fields + DatePicker + photo chooser + AI button
✅ 1.4  FighterFormController — photo upload (FileChooser→upload.dir/boxers/UUID.ext), Task wrapping on save + AI
✅ 1.5  DeepSeekClient — HttpClient-based, generateFighterProfile() + generateStyleTag(), Bearer auth

LAYER 2 — Phase 4: Event CRUD + Fight Results                         ✅ COMPLETE
───────────────────────────────────────────────────────────────────────────────
✅ 2.1  EventForm.fxml — org/status/visibility → ComboBox; champions → CheckBox
✅ 2.2  EventFormController — ComboBox defaults (INDEPENDENT/SCHEDULED/PUBLIC), Task wrapping
✅ 2.3  FightResult model — 21 DB columns + 4 display-only joined fields
✅ 2.4  FightResultDao — findAll (with LEFT JOINs for names), getById, create, update, deleteById
✅ 2.5  ResultsList.fxml + ResultsListController — 7-column table, New/Edit/Delete wired
✅ 2.6  ResultForm.fxml + ResultFormController — full 21-field form, ComboBox for event/fighters/method/status
✅ 2.7  AdminShell.fxml + AdminShellController — Results nav button wired

LAYER 3 — Phase 0.5: CSS                                              ✅ COMPLETE
───────────────────────────────────────────────────────────────────────────────
✅ 3.1  smartfight.css — full design system: color tokens, status pills (9 variants), typography (h1/h2/h3/caption/body),
         cards, buttons (primary/danger/outline/default), nav-btn/nav-btn-active, TableView dark theme,
         form fields (text/combo/check/date), toolbar, scrollbar, shell chrome

Priority Next Actions (following §14 build sequence)

Fix immediately before moving forward:
1. ✅ Add upload.dir to AppConfig and config.properties
2. ✅ Add deepseek.api.key / deepseek.api.url to AppConfig and config.properties
3. ✅ Fix mail.smtp.host from require() to optional (blank-tolerant)
4. ✅ Fix PasswordVerifierTest to use real cost-13 BCrypt hashes + $2y$ prefix test
5. ✅ Replace all catch (Exception ignored) in DAOs with Logger.log(Level.SEVERE, ...)
6. Add missing fighter fields (all 12 DB columns) to FighterDetails, FighterDao SQL, FighterFormController

Phase 3 completion (before Phase 4):
7. Photo upload in FighterFormController (FileChooser → copy to upload.dir/boxers/)
8. "Generate AI Profile" button — real DeepSeekClient call

Phase 4 completion:
9. Convert EventForm organization/status/visibility to ComboBox with correct enums and defaults
10. Fight result entry screen — full 21-column FightResultDao + ResultFormController

Phase 0.5 completion:
11. Build out smartfight.css with full color token system and component styles

Then Phase 5 (ranking algorithm) — the highest-risk, highest-value feature per the plan.