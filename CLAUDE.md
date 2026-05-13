# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SmartFight is a Symfony 7.1 web application for a professional combat sports management platform. It supports two distinct user-facing interfaces — an **Admin back-office** for league management and a **Fan front-office** for public engagement — backed by an AI layer powered by the DeepSeek LLM API (with deterministic heuristic fallbacks when the API is unavailable).

## Tech Stack

- **Framework**: Symfony 7.1 (PHP ≥ 8.2)
- **Database**: MySQL (primary, via `DATABASE_URL` env) or PostgreSQL (Docker compose default)
- **ORM**: Doctrine with PHP attribute mappings (`src/Entity/`)
- **Templates**: Twig (`templates/`)
- **Auth**: Form login + Google OAuth (`KnpUOAuth2ClientBundle`) + WebAuthn/Face-ID
- **AI**: DeepSeek Chat API (`AIService`), with full heuristic fallbacks
- **Other**: VichUploader (file uploads), KnpPaginator, DomPDF, endroid/qr-code, Symfony Mailer

## Environment Setup

Copy `.env.example` to `.env.dev` and fill in:
- `DATABASE_URL` — MySQL DSN
- `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` — Google OAuth
- `MAILER_DSN` — Gmail SMTP
- `DEEPSEEK_API_KEY` — AI features (optional; heuristic fallbacks activate when absent)

## Common Commands

```bash
# Install dependencies
composer install

# Run the dev server
symfony server:start
# or
php -S localhost:8001 -t public/

# Database
php bin/console doctrine:database:create
php bin/console doctrine:migrations:migrate

# Seed test data
php bin/console app:seed-data

# Create an admin user
php bin/console app:create-admin

# Update event statuses (SCHEDULED → LIVE → COMPLETED)
php bin/console app:update-event-status

# Clear cache
php bin/console cache:clear

# Run all migrations fresh
php bin/console doctrine:schema:drop --force && php bin/console doctrine:migrations:migrate
```

## Architecture

### Dual-Interface Split

The app has two separate template hierarchies and navigation systems:

- **Admin** (`/` and `/admin/*`): extends `templates/admin/base_admin.html.twig`. `DashboardController` redirects non-admins to the fan dashboard.
- **Fan front-office** (`/fan/*`, `/bookings`, `/predictions`, `/front/*`): extends `templates/front/base_front.html.twig`.

Routes are discovered via PHP attributes on controllers (`config/routes.yaml` uses attribute scanning).

### Security & Roles

`config/packages/security.yaml` defines:
- `ROLE_ADMIN` → `ROLE_USER` (hierarchy)
- Login by `username` (not email). Email is used only for notifications.
- `app_login` is both the login path and check path.
- Google OAuth handled by `src/Security/GoogleAuthenticator.php`.
- Face-ID/WebAuthn: `FaceIdController` stores `webauthnCredentialId` on `User`. The visual face-matching endpoint (`/face-id/verify-visual`) is a demo — it finds the first user with a stored photo and logs them in unconditionally.

### Entity Model (key relationships)

```
User ──< Role (ManyToMany via user_roles)
Fighter ──> WeightDivision
Fighter ──> User (manager)
Event ── EventBooking ──> User
FightResult ──> Event, Fighter (x2), WeightDivision
FightStatistic ──> FightResult (per-round CompuBox stats)
Ranking ──> Fighter, WeightDivision (per-org snapshots)
MatchProposal ──> Fighter (x2), WeightDivision
Prediction ──> User, FightResult
BlogArticle ──> BlogCategory
Notification ──> User
```

### AI Layer (`src/Service/AIService.php`)

All AI methods follow the same pattern:
1. Compute a heuristic baseline (deterministic, no API call).
2. Build a JSON-only prompt and call `callDeepSeek()`.
3. On failure (API key missing, timeout, JSON parse error), return the heuristic result with `is_fallback: true`.

AI endpoints are all under `#[Route('/api/ai')]` in `AIController` and require `ROLE_ADMIN`.

Key AI operations:
- `analyzeFightDynamics` — pre-fight win probability (ELO/heuristic matrix)
- `generatePostFightRecap` — journalistic recap after a fight
- `generateScoutingReport` — tactical coaching breakdown
- `suggestFightStats` — CompuBox-style per-round stats
- `predictInjuryRisk` — biomechanical injury risk from fighter age/load/KO history
- `generateFighterProfile` — AI style tag and bio for fighter profiles

### Ranking Engine (`src/Service/RankingService.php`)

Implements a Glicko-ELO hybrid with:
- Dynamic K-factor (60 for <12 fights, 32 otherwise)
- KO/decision bonuses
- Strength of Schedule (SoS) via recursive average opponent ELO
- Inactivity decay (0 points after 2 years inactive)
- Formula: `P = (RatingVector × 0.45) + (SoS × 0.35) + (LegacyBonus × 0.20)`
- Rankings are wiped and rebuilt per division/org on every `processCompletedFight()` call, generating rows for WBC, WBA, IBF, WBO, and MEDIA organizations.

### Matchmaking Engine (`src/Service/MatchmakingService.php`)

Two paths:
- **AI-assisted** (`/api/ai/matchmaking-suggestions`): sends fighter data to DeepSeek, falls back to local algorithm.
- **Local 8-vector Score IA** (`/api/ai/generate-proposals`): purely deterministic, no API dependency. Scores pairs 0–100 across weight integrity (25 pts), ELO parity (20), record parity (15), height/reach balance (15), lethality (10), precision (10), diversity bonus (5). Persists results as `MatchProposal` entities with status `PENDING`.

### Console Commands

| Command | Purpose |
|---|---|
| `app:update-event-status` | Transitions SCHEDULED→LIVE (on event date) and LIVE→COMPLETED (when all fights done) |
| `app:seed-data` | Populates test fighters, events, and results |
| `app:create-admin` | Interactive admin user creation |

### File Uploads

VichUploader handles fighter photos (`photo_filename`) and event posters (`poster_filename`). Uploaded files land in `public/uploads/`.

### PDF / QR Generation

- `PdfService` wraps DomPDF, rendering Twig templates to PDF (contracts, rankings, fight hub).
- `QrCodeService` wraps endroid/qr-code for booking QR codes.

### Notifications

`NotificationService` persists `Notification` entities. `notifyAllFans()` iterates all users with `ROLE_USER` — no pub/sub, no queue. Called automatically after fight results are processed and rankings update.

## Key Conventions

- Entity column names use camelCase (`name: 'fighterId'`) while PHP properties are also camelCase. The ORM naming strategy is `underscore_number_aware` but explicit `name:` overrides are common — check the `#[ORM\Column]` annotation before assuming column names.
- `Fighter::getCalculatedFightingStyle()` returns AI style tag if set, otherwise derives it from KO rate / accuracy / volume heuristics.
- `FaceIdController::verifyVisual` is a demo shortcut — it matches any stored face photo, not the submitted one.
- The database is MySQL in production (`.env.example`) but the Docker compose file uses PostgreSQL. The migrations and schema may have diverged between environments.
