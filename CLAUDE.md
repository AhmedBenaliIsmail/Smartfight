# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**SmartFight** is a Symfony 7.1 UFC/MMA management application with two interfaces: an admin dashboard and a public fan-facing site. It manages fighters, events, bookings, fight results, predictions, rankings, blog content, and notifications.

## Development Commands

All commands run from the repo root (where `composer.json` lives). The root Symfony app is the **primary** one — ignore the `smartfight/` subdirectory (it is a legacy/alternate version).

```bash
# Start dev server
symfony server:start

# Clear cache (required after config changes or entity changes in prod)
php bin/console cache:clear

# Database migrations
php bin/console doctrine:migrations:diff    # generate migration from entity changes
php bin/console doctrine:migrations:migrate # apply pending migrations

# Code generation
php bin/console make:entity
php bin/console make:controller
php bin/console make:form

# Create admin user
php bin/console app:create-admin

# Docker (PostgreSQL for local dev alternative)
docker compose up -d
```

The `.env` file (not committed) must be created from `.env.example`. The app defaults to MySQL; Docker spins up PostgreSQL.

## Architecture

### Two Parallel Codebases (Important)

1. **Root Symfony app** (`src/`, `templates/`, `config/`) — the live, working application.
2. **Static HTML prototypes** (`FrontOffice/`, `BackOffice/`) — design reference only, not served by Symfony.
3. **`smartfight/` subdirectory** — a separate, older Symfony project. Not the active codebase.

### Request Flow

Routes are defined via PHP attributes on controllers in `src/Controller/`. All routes load from `config/routes.yaml` via attribute scanning of `src/Controller/`.

The entry point `/` (`app_dashboard`) checks `ROLE_ADMIN` and redirects non-admins to `app_fan_dashboard` (front-facing dashboard).

### Controller Layout

```
src/Controller/
├── Admin/          # Admin-only: BlogController, BookingAdminController, MatchProposalAdminController, ReactionController, AdminContractController
├── Front/          # Fan-facing: BlogController, BookingController, FanDashboardController, ReactionController
├── AIController    # POST /api/ai/* — stat suggestions + matchmaking (ROLE_ADMIN only)
├── DashboardController   # / — redirects based on role
├── SecurityController    # /login, /register, /forgot-password, /reset-password, /verify-email
├── FaceIdController      # /face-id/* — WebAuthn/Face ID registration + authentication
├── GoogleController      # /connect/google/* — OAuth2 Google login
└── ... (EventController, FighterController, ResultController, PredictionController, etc.)
```

### Template Hierarchy

```
templates/base.html.twig
├── templates/admin/base_admin.html.twig    → all admin views
└── templates/front/base_front.html.twig   → all fan-facing views
```

Admin sidebar and navbar are **Twig partials** (`templates/admin/partials/sidebar.html.twig`, `navbar.html.twig`) — unlike the static prototypes, they are included once, not duplicated per page.

### Role System

Roles use a custom `Role` entity (table `role`, field `roleName`) mapped many-to-many to `User` via `user_roles`. Symfony's `getRoles()` on `User` maps these to `ROLE_<UPPERCASE_ROLENAME>` strings (e.g., `ROLE_ADMIN`, `ROLE_USER`). The security provider loads users by `username` (not email).

Password recovery and registration both accept username **or** email as identifier.

### AI Features

`AIService` (`src/Service/AIService.php`) calls the DeepSeek API (`deepseek-chat` model) for:
- `POST /api/ai/stat-suggestions` — auto-fill round fight stats
- `POST /api/ai/matchmaking-suggestions` — suggest balanced fighter pairings

`MatchmakingService` wraps `AIService` with an algorithmic fallback (ELO ±150 tolerance, same weight class). Requires `DEEPSEEK_API_KEY` in `.env`.

### Key Services

| Service | Responsibility |
|---|---|
| `AIService` | DeepSeek API calls for stats + matchmaking |
| `MatchmakingService` | Matchmaking with AI + algorithmic fallback |
| `BookingService` | Event ticket booking logic |
| `PredictionService` | Fan fight prediction logic |
| `RankingService` | Fighter ranking calculation |
| `NotificationService` | Fan notification dispatch |
| `QrCodeService` | QR code generation for bookings |

### Authentication Methods

1. **Form login** — username + password, CSRF-protected
2. **Google OAuth** — via `GoogleAuthenticator` (`src/Security/GoogleAuthenticator.php`) + KnpU OAuth2 bundle
3. **Face ID** — browser WebAuthn API + `face-api.js` loaded via CDN; credential stored on `User.webauthnCredentialId` / `webauthnPublicKey`

Email verification is required after registration (token in `User.verificationToken`).

### Database

Doctrine ORM with attribute-based mapping on entities in `src/Entity/`. Column names use camelCase (`fighterId`, `userId`, `firstName`) while Doctrine's underscore naming strategy applies to auto-generated tables. Custom `@ORM\Column(name: ...)` overrides are common — check entity definitions before querying raw SQL.

Primary key pattern: `userId`, `fighterId`, etc. (not `id`).

## Environment Variables

| Variable | Purpose |
|---|---|
| `DATABASE_URL` | MySQL or PostgreSQL connection string |
| `APP_SECRET` | Symfony app secret (32 chars) |
| `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` | Google OAuth |
| `MAILER_DSN` | SMTP mailer (Gmail TLS port 587 recommended) |
| `DEEPSEEK_API_KEY` | AI features (optional; fallback works without it) |
| `DEFAULT_URI` | Base URL for absolute link generation in emails |
