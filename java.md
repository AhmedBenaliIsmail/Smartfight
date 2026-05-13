 SmartFight Java Desktop App — Rebuild Plan

  1. First, the "Doctrine" confusion (important)       
  Doctrine is a PHP-only ORM. Your Java app cannot    "use Doctrine." What your teacher almost certainly   means is:

  ▎ The Symfony app owns the database schema (via
  ▎ Doctrine entities + migrations). The Java app
  ▎ must read/write the same MySQL database,
  ▎ respecting the schema that Doctrine generated.

  So the rule is: Symfony is the schema authority.
  Java is a second client to the same DB. This is
  the architecture you should build for. If a
  teacher later insists Java must "go through
  Doctrine," that's only possible by calling Symfony   HTTP endpoints — but for a 3-PC live-sync demo,
  plain shared-DB access over the LAN is simpler and   what's almost certainly expected.

  2. Architecture (the big picture)

     PC #1 (MySQL Server)             PC #2
  (Symfony)              PC #3 (JavaFX)
     ┌────────────────────┐
  ┌──────────────┐            ┌──────────────┐
     │ MySQL :3306        │ ◄──TCP──► │ Symfony +
   │            │ JavaFX +     │
     │ smartfight schema  │           │ Doctrine ORM   │            │ JDBC client  │
     │ (owned by Doctrine │
  ◄────────────────TCP──────────────────►
     │
     │  migrations)       │
  └──────────────┘            └──────────────┘
     └────────────────────┘

  Both clients hit the same MySQL over the LAN. Add
  a fighter in Symfony → it lands in MySQL → JavaFX
  sees it on next refresh (or on a poll/notify).

  Live-sync strategy (pick one, in order of
  complexity):
  - A. Manual refresh button — simplest, always
  works, perfect for a demo. Recommended starting
  point.
  - B. Periodic polling (every 3–5s) on the visible
  list — feels live, dead simple.
  - C. MySQL trigger + notification table — Java
  polls a change_log table — overkill for uni demo.

  3. Schema strategy (the single most important
  decision)

  You have two choices. Choose A.

  - A. Symfony's Doctrine owns the schema. You
  generate it via php bin/console
  doctrine:migrations:migrate. Java reads it as-is,
  never alters it. Java entities are hand-written
  POJOs that mirror Doctrine entities.
  - B. Java owns it via the old
  smartfight_schema.sql. Then Symfony's Doctrine
  entities must be reverse-mapped to it
  (doctrine:mapping:import). Painful and fragile.

  Action item before writing any Java: export the
  current Symfony schema with php bin/console
  doctrine:schema:create --dump-sql (or read
  migrations/) and treat that as the contract the
  Java app codes against.

  4. Tech stack (locked in)

  Layer: Language
  Choice: Java 17
  Why: Matches old project, LTS
  ────────────────────────────────────────
  Layer: UI
  Choice: JavaFX 21 (FXML + CSS)
  Why: Required by spec
  ────────────────────────────────────────
  Layer: Build
  Choice: Maven + javafx-maven-plugin
  Why: Required
  ────────────────────────────────────────
  Layer: DB driver
  Choice: mysql-connector-j 8.x
  Why: Required
  ────────────────────────────────────────
  Layer: DB access
  Choice: JDBC + a thin DAO layer (no Hibernate)
  Why: Keeps it simple; matches old code style
  ────────────────────────────────────────
  Layer: Connection pool
  Choice: HikariCP
  Why: One line of setup, big reliability win over
    raw DriverManager
  ────────────────────────────────────────
  Layer: QR codes
  Choice: ZXing
  Why: Required
  ────────────────────────────────────────
  Layer: Password hashing
  Choice: bcrypt (jBCrypt or Spring Security crypto)  Why: Must match what Symfony uses — see §6
  ────────────────────────────────────────
  Layer: Logging
  Choice: SLF4J + Logback
  Why: Standard
  ────────────────────────────────────────
  Layer: Testing
  Choice: JUnit 5
  Why: Standard

  Skip: the JSP/web.xml part of the old project. The   Symfony app is the web app now. Don't waste time
  on index.jsp.

  5. Project structure

  smartfight-desktop/
  ├── pom.xml
  ├── src/main/java/tn/smartfight/
  │   ├── App.java                         # JavaFX
  entry point
  │   ├── config/
  │   │   ├── DBConnection.java            #
  HikariCP DataSource (singleton)
  │   │   └── AppConfig.java               # loads
  config.properties
  │   ├── model/                           # POJOs
  mirroring Doctrine entities
  │   │   ├── User.java
  │   │   ├── Fighter.java
  │   │   ├── Event.java
  │   │   └── ...
  │   ├── dao/                             # JDBC
  repositories
  │   │   ├── BaseDAO.java
  │   │   ├── FighterDAO.java
  │   │   └── ...
  │   ├── service/                         #
  business logic, validation
  │   │   ├── AuthService.java
  │   │   └── FighterService.java
  │   ├── controller/                      # FXML
  controllers
  │   │   ├── LoginController.java
  │   │   ├── FighterListController.java
  │   │   └── ...
  │   └── util/
  │       ├── PasswordHasher.java          # bcrypt
  — must match Symfony
  │       ├── QRCodeGenerator.java         # ZXing
  │       └── AlertHelper.java
  ├── src/main/resources/
  │   ├── config.properties                # db
  url/user/pass — externalized!
  │   ├── tn/smartfight/views/
  │   │   ├── Login.fxml
  │   │   ├── Dashboard.fxml
  │   │   └── ...
  │   └── tn/smartfight/styles/
  │       └── smartfight.css
  └── src/test/java/...

  Two non-obvious points:
  - config.properties externalized — DB host changes   per PC on demo day. Don't hardcode localhost.
  Load jdbc.url from a properties file the user can
  edit.
  - HikariCP, not raw DriverManager — handles
  network blips and reconnects. ~10 lines of setup.

  6. The bcrypt trap (read this carefully)

  If your Symfony app uses Symfony's default
  password hasher, it produces bcrypt hashes
  ($2y$...). The Java side must use the same
  algorithm and cost so a user registered in Symfony   can log in via JavaFX (and vice versa).

  - Symfony default: bcrypt, cost 13 (or auto which
  picks bcrypt).
  - Java side: jBCrypt or Spring Security's
  BCryptPasswordEncoder — verify the same $2y$13$...   hash works.
  - Verify this before building login. Take a hash
  from your Symfony DB, paste it into a Java unit
  test, confirm BCrypt.checkpw("mypassword", hash)
  returns true. If not, you have an algorithm
  mismatch to debug now, not on demo day.

  7. Phased build plan (sequenced, with checkpoints)
  I'd run this in roughly this order. Each phase
  ends with something you can demo.

  Phase 0 — Foundations (½ day)

  - Get latest Symfony DB schema dumped → reference
  SQL file in /docs/schema-from-symfony.sql
  - List every Doctrine entity + its fields → this
  is your model/ checklist
  - Create empty Maven project, JavaFX hello-world
  window runs
  - Checkpoint: mvn javafx:run opens a blank window

  Phase 1 — DB connectivity (½ day)

  - HikariCP DBConnection reading config.properties
  - Health-check screen on startup: "Connected to
  smartfight DB ✅" / red error
  - One smoke-test query (e.g., SELECT COUNT(*) FROM   user)
  - Checkpoint: App boots, shows green DB status
  from a remote MySQL host

  Phase 2 — Auth (1 day)

  - User.java model, UserDAO.java, AuthService.java
  - bcrypt verification (see §6)
  - Login.fxml + controller, basic CSS
  - Session held in a singleton or passed via
  controller factory
  - Checkpoint: Log in with a user created in
  Symfony

  Phase 3 — Core CRUD on one entity, end to end (1
  day)

  - Pick Fighter (it's the headline entity).
  - FighterDAO: list, get, create, update, delete
  - FighterListController with TableView, refresh
  button, edit/delete buttons
  - FighterFormController for create/edit
  - Checkpoint: Add a fighter in JavaFX → see it in
  Symfony's fighter list. The demo-critical moment.

  Phase 4 — Remaining entities (2–3 days)

  - Repeat the Fighter pattern for each entity
  Symfony exposes (Event, Match, Ticket, etc.)
  - Don't reinvent UI per screen — clone the
  FighterList/FighterForm pair.

  Phase 5 — JavaFX-only features (1 day)

  - QR code generation (ZXing) — e.g., ticket QR or
  fighter ID badge
  - Anything visual/desktop-y the teacher will
  reward (charts, media playback via javafx-media)
  - These are your differentiators vs. the Symfony
  web UI. Lean into desktop-native features.

  Phase 6 — Live-sync polish (½ day)

  - Add a refresh timer (3–5s) on visible list views   (Phase 4 already has manual refresh)
  - Add a "last synced" indicator
  - Checkpoint: Symfony adds a row, JavaFX shows it
  within 5s without user clicking anything

  Phase 7 — Demo-day hardening (½ day)

  - Build a runnable JAR / mvn javafx:jlink image
  - Test on a second PC pointing to a remote DB
  - Document the 3 commands for demo day (start
  MySQL, start Symfony, start Java)
  - Practice the demo flow

  Total: ~6–7 working days for one developer. Cut
  Phase 5 if time-constrained.

  8. Demo-day networking (don't skip)

  The "3 PCs talking to the same DB" is where teams
  lose marks. Set this up on day 1, not the night
  before.

  1. MySQL server PC: edit my.cnf → bind-address =
  0.0.0.0 (default is 127.0.0.1 — won't accept LAN
  connections). Restart MySQL.
  2. Create a remote-capable user: CREATE USER
  'smartfight'@'%' IDENTIFIED BY '...'; GRANT ALL ON   smartfight.* TO 'smartfight'@'%';
  3. Open firewall port 3306 on the DB PC.
  4. From the Symfony PC and Java PC, test: mysql -h   <db-pc-ip> -u smartfight -p. If this fails, your
  apps will fail too.
  5. Both apps point to <db-pc-ip>:3306, not
  localhost.

  9. Risks & how to handle them

  Risk: Symfony schema changes mid-development
  Mitigation: Lock the schema after Phase 0; treat
    further changes as a versioned event
  ────────────────────────────────────────
  Risk: bcrypt mismatch
  Mitigation: Verify in Phase 0 with a real hash
  from
    Symfony
  ────────────────────────────────────────
  Risk: Doctrine uses snake_case, Java DTOs use
    camelCase
  Mitigation: Map explicitly in DAO; don't assume
    column names
  ────────────────────────────────────────
  Risk: Foreign keys with cascading deletes behave
    unexpectedly
  Mitigation: Test delete operations early on a
    non-trivial entity
  ────────────────────────────────────────
  Risk: Demo-day network flakiness
  Mitigation: HikariCP retry + clear error UI (Phase
    1)
  ────────────────────────────────────────
  Risk: Teacher asks "but you said Doctrine?"
  Mitigation: Be ready to explain: "Doctrine is
    PHP-only; we share the database it manages,
  which
     is the same outcome."

  10. What I'd do right now

  5. Both apps point to <db-pc-ip>:3306, not localhost.

  9. Risks & how to handle them

  ┌─────────────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────────────────────────────────────────────┐
  │       ├── PasswordHasher.java          # bcrypt         │                                                Mitigation                                                 │
  — must match Symfony                              ────────┼───────────────────────────────────────────────────────────────────────────────────────────────────────────┤
  │       ├── QRCodeGenerator.java         # ZXing          │ Lock the schema after Phase 0; treat further changes as a versioned event                                 │
  │       └── AlertHelper.java                      ────────┼───────────────────────────────────────────────────────────────────────────────────────────────────────────┤
  ├── src/main/resources/                                   │ Verify in Phase 0 with a real hash from Symfony                                                           │
  │   ├── config.properties                # db     ────────┼───────────────────────────────────────────────────────────────────────────────────────────────────────────┤
  url/user/pass — externalized!                     e       │ Map explicitly in DAO; don't assume column names                                                          │
  │   ├── tn/smartfight/views/                      ────────┼───────────────────────────────────────────────────────────────────────────────────────────────────────────┤
  │   │   ├── Login.fxml                            ectedly │ Test delete operations early on a non-trivial entity                                                      │
  │   │   ├── Dashboard.fxml                        ────────┼───────────────────────────────────────────────────────────────────────────────────────────────────────────┤
  │   │   └── ...                                           │ HikariCP retry + clear error UI (Phase 1)                                                                 │
  │   └── tn/smartfight/styles/                     ────────┼───────────────────────────────────────────────────────────────────────────────────────────────────────────┤
  │       └── smartfight.css                                │ Be ready to explain: "Doctrine is PHP-only; we share the database it manages, which is the same outcome." │
  └── src/test/java/...                             ────────┴───────────────────────────────────────────────────────────────────────────────────────────────────────────┘

  Two non-obvious points:
  - config.properties externalized — DB host changes   per PC on demo day. Don't hardcode localhost.    and saved.
  Load jdbc.url from a properties file the user can ing.
  edit.                                             heck.
  - HikariCP, not raw DriverManager — handles
  network blips and reconnects. ~10 lines of setup. va app on PC-A can read a user row created by Symfony on PC-B," everything after is mechanical.

  6. The bcrypt trap (read this carefully)
                                                    ginning with the pom.xml + DBConnection + the bcrypt verification test — that's the riskiest unknown and we should de-risk it first. Or
  If your Symfony app uses Symfony's default        you need to mirror once you paste the Symfony entity/ folder or schema dump.
  password hasher, it produces bcrypt hashes
  ($2y$...). The Java side must use the same
  algorithm and cost so a user registered in Symfony   can log in via JavaFX (and vice versa).          r\Smartfight\java.md so i can review it even more                                                                                        
                                                                                                      
  - Symfony default: bcrypt, cost 13 (or auto which                                                                                                                                          
  picks bcrypt).
  - Java side: jBCrypt or Spring Security's         ─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
  BCryptPasswordEncoder — verify the same $2y$13$...                                                                                                                                            hash works.                                      ─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
  - Verify this before building login. Take a hash  nterrupt                                                                                                                                 
  from your Symfony DB, paste it into a Java unit                                                                                                                                            
  test, confirm BCrypt.checkpw("mypassword", hash)
  returns true. If not, you have an algorithm
  mismatch to debug now, not on demo day.

  7. Phased build plan (sequenced, with checkpoints)
  I'd run this in roughly this order. Each phase
  ends with something you can demo.

  Phase 0 — Foundations (½ day)

  - Get latest Symfony DB schema dumped → reference
  SQL file in /docs/schema-from-symfony.sql
  - List every Doctrine entity + its fields → this
  is your model/ checklist
  - Create empty Maven project, JavaFX hello-world
  window runs
  - Checkpoint: mvn javafx:run opens a blank window

  Phase 1 — DB connectivity (½ day)

  - HikariCP DBConnection reading config.properties
  - Health-check screen on startup: "Connected to
  smartfight DB ✅" / red error
  - One smoke-test query (e.g., SELECT COUNT(*) FROM   user)
  - Checkpoint: App boots, shows green DB status
  from a remote MySQL host

  Phase 2 — Auth (1 day)

  - User.java model, UserDAO.java, AuthService.java
  - bcrypt verification (see §6)
  - Login.fxml + controller, basic CSS
  - Session held in a singleton or passed via
  controller factory
  - Checkpoint: Log in with a user created in
  Symfony

  Phase 3 — Core CRUD on one entity, end to end (1
  day)

  - Pick Fighter (it's the headline entity).
  - FighterDAO: list, get, create, update, delete
  - FighterListController with TableView, refresh
  button, edit/delete buttons
  - FighterFormController for create/edit
  - Checkpoint: Add a fighter in JavaFX → see it in
  Symfony's fighter list. The demo-critical moment.

  Phase 4 — Remaining entities (2–3 days)

  - Repeat the Fighter pattern for each entity
  Symfony exposes (Event, Match, Ticket, etc.)
  - Don't reinvent UI per screen — clone the
  FighterList/FighterForm pair.

  Phase 5 — JavaFX-only features (1 day)

  - QR code generation (ZXing) — e.g., ticket QR or
  fighter ID badge
  - Anything visual/desktop-y the teacher will
  reward (charts, media playback via javafx-media)
  - These are your differentiators vs. the Symfony
  web UI. Lean into desktop-native features.

  Phase 6 — Live-sync polish (½ day)

  - Add a refresh timer (3–5s) on visible list views   (Phase 4 already has manual refresh)
  - Add a "last synced" indicator
  - Checkpoint: Symfony adds a row, JavaFX shows it
  within 5s without user clicking anything

  Phase 7 — Demo-day hardening (½ day)

  - Build a runnable JAR / mvn javafx:jlink image
  - Test on a second PC pointing to a remote DB
  - Document the 3 commands for demo day (start
  MySQL, start Symfony, start Java)
  - Practice the demo flow

  Total: ~6–7 working days for one developer. Cut
  Phase 5 if time-constrained.

  8. Demo-day networking (don't skip)

  The "3 PCs talking to the same DB" is where teams
  lose marks. Set this up on day 1, not the night
  before.

  1. MySQL server PC: edit my.cnf → bind-address =
  0.0.0.0 (default is 127.0.0.1 — won't accept LAN
  connections). Restart MySQL.
  2. Create a remote-capable user: CREATE USER
  'smartfight'@'%' IDENTIFIED BY '...'; GRANT ALL ON   smartfight.* TO 'smartfight'@'%';
  3. Open firewall port 3306 on the DB PC.
  4. From the Symfony PC and Java PC, test: mysql -h   <db-pc-ip> -u smartfight -p. If this fails, your
  apps will fail too.
  5. Both apps point to <db-pc-ip>:3306, not
  localhost.

  9. Risks & how to handle them

  Risk: Symfony schema changes mid-development
  Mitigation: Lock the schema after Phase 0; treat
    further changes as a versioned event
  ────────────────────────────────────────
  Risk: bcrypt mismatch
  Mitigation: Verify in Phase 0 with a real hash
  from
    Symfony
  ────────────────────────────────────────
  Risk: Doctrine uses snake_case, Java DTOs use
    camelCase
  Mitigation: Map explicitly in DAO; don't assume
    column names
  ────────────────────────────────────────
  Risk: Foreign keys with cascading deletes behave
    unexpectedly
  Mitigation: Test delete operations early on a
    non-trivial entity
  ────────────────────────────────────────
  Risk: Demo-day network flakiness
  Mitigation: HikariCP retry + clear error UI (Phase
    1)
  ────────────────────────────────────────
  Risk: Teacher asks "but you said Doctrine?"
  Mitigation: Be ready to explain: "Doctrine is
    PHP-only; we share the database it manages,
  which
     is the same outcome."

  10. What I'd do right now

  1. Get the current Symfony Doctrine schema dumped
  and saved.
  2. Verify the bcrypt hash format Symfony is
  │ Foreign keys with cascading deletes behave unexpectedly │ Test delete operations early on a non-trivial entity                                                      │
  ├─────────────────────────────────────────────────────────┼───────────────────────────────────────────────────────────────────────────────────────────────────────────┤
  │ Demo-day network flakiness                              │ HikariCP retry + clear error UI (Phase 1)                                                                 │
  ├─────────────────────────────────────────────────────────┼───────────────────────────────────────────────────────────────────────────────────────────────────────────┤
  │ Teacher asks "but you said Doctrine?"                   │ Be ready to explain: "Doctrine is PHP-only; we share the database it manages, which is the same outcome." │
  └─────────────────────────────────────────────────────────┴───────────────────────────────────────────────────────────────────────────────────────────────────────────┘

  10. What I'd do right now

  1. Get the current Symfony Doctrine schema dumped and saved.
  2. Verify the bcrypt hash format Symfony is producing.
  3. Set up the Maven skeleton + Phase 1 DB health-check.

  That's the first ~half day. Once you can prove "Java app on PC-A can read a user row created by Symfony on PC-B," everything after is mechanical.

  ---
  Want me to actually start building? I'd suggest beginning with the pom.xml + DBConnection + the bcrypt verification test — that's the riskiest unknown and we should de-risk it first. Or
  if you'd rather, I can list the Doctrine entities you need to mirror once you paste th