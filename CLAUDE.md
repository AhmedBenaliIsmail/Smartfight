# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

JavaFX 21 desktop port of an existing Symfony web app (SmartFight). It reuses the original Symfony MariaDB schema (`smartfight.sql`) and the original Twig email/PDF templates rendered via Pebble. Single-machine localhost deployment: Symfony, MariaDB, and the JavaFX app run on the same PC.

The high-level porting plan lives in `java implemntation.md`. Per-task progress is appended to `PROGRESS.md` (most recent state at the bottom of each section).

## Build / run / test

Java 17 + Maven. JavaFX dependencies are pulled by Maven; run via the `javafx-maven-plugin`:

```
mvn javafx:run        # launch desktop app (mainClass tn.smartfight.App)
mvn test              # JUnit 5 via surefire
mvn -Dtest=ClassName#method test   # single test
mvn package           # builds smartfight-desktop.jar (no fat-jar)
```

Runtime requirements:
- MariaDB/MySQL on `localhost:3306` with the `smartfight` schema loaded from `smartfight.sql`.
- Edit `src/main/resources/config.properties` for `db.*`, `mail.smtp.*`, `mail.from`, and `symfony.base.url` (used to build email verification links — they currently point at the Symfony app, not the desktop app).

Email-sending flows (registration, resend) require non-blank `mail.smtp.user`, `mail.smtp.password`, and `mail.from`; tests / non-email flows tolerate blank SMTP values.

## Architecture

Layering is plain JavaFX MVC + JDBC, no DI container:

- `App.java` — JavaFX entry point. Calls `AppConfig.loadFromClasspath("config.properties")` (required before anything touches `DBConnection`), shows `Login.fxml`, and closes the Hikari pool in `stop()`.
- `config/`
  - `AppConfig` — singleton holding the parsed `config.properties`. `AppConfig.get()` throws if `loadFromClasspath` hasn't run, so tests must call `AppConfig.fromProperties(...)` first.
  - `DBConnection` — lazy HikariCP `DataSource`. Always inject a `DataSource` into DAOs in tests (the `UserDao(DataSource)` constructor pattern); the no-arg constructor uses the global pool.
  - `Session` — singleton holding the logged-in `User`. `AccessGuard.isAdmin()` checks `ROLE_ADMIN` on the session user.
- `controller/` — FXML controllers, one per view. Background work (DB calls, SMTP, PDF) runs on `javafx.concurrent.Task` worker threads; UI updates happen in `setOnSucceeded`/`setOnFailed` (which run on the FX thread). Don't move JDBC onto the FX thread.
- `dao/` — hand-rolled JDBC, prepared statements, try-with-resources. No ORM. Column names follow the legacy Symfony schema and are a **mix of camelCase and snake_case** (`userId`, `createdDate`, `is_verified`, `verification_token`); when adding queries, check `smartfight.sql` rather than guessing.
- `auth/PasswordVerifier` — supports both jBCrypt (`$2y$`/`$2a$`/`$2b$`, any cost) and Argon2id (`$argon2…`). The legacy DB has a mix; never assume one format. New users created from the desktop app are bcrypt cost 13.
- `integration/` — `PebbleRenderer` for HTML, `PdfRenderer` (Flying Saucer + OpenPDF) for PDFs, `GmailMailer` for SMTP. Templates live under `src/main/resources/templates/` (the original Twig directory was copied verbatim — Pebble is largely Twig-compatible).
- `model/` — plain POJOs mirroring DB rows.

### View layout / role routing

FXML lives under `src/main/resources/tn/smartfight/views/`:
- `Login.fxml`, `Register.fxml`, `Dashboard.fxml` (legacy placeholder).
- `AdminShell.fxml` + `views/admin/*` — admin features (fighters, events, …).
- `FanShell.fxml` + `views/fan/*` — fan features (bookings, blog, …).

`LoginController` chooses the shell by checking `ROLE_ADMIN` on the loaded user. The two shells share `BaseShellController`, which owns a `StackPane contentHost` swap-in pattern (`loadContent("/tn/smartfight/views/admin/Foo.fxml")`) and the logout flow that clears `Session` and reloads `Login.fxml` into the existing stage. New screens should plug into that pane rather than opening new windows, except for modal forms (which use a `Stage` + `showAndWait`).

`AdminShell` enforces access via `AccessGuard.isAdmin()` in `initialize()`; mirror that pattern in any new admin-only entrypoint.

## Conventions for changes here

- The Symfony app and its DB are the source of truth for column names and password formats. When adding a DAO query, verify column casing against `smartfight.sql` before writing SQL.
- Long-running work in controllers must use `javafx.concurrent.Task`; never block the FX thread on JDBC, SMTP, or PDF rendering.
- Append a short entry to `PROGRESS.md` (Completed / In Progress / Next / File Notes) when finishing meaningful work — that file is how successive sessions resume context.
- Twig templates copied from Symfony stay under `templates/` and are rendered with Pebble; tweak template variables in both the renderer call site and the template (see `RegisterController` + `templates/emails/verification.html.twig` for the established pattern: `user`, `verifyUrl`, `now`).
