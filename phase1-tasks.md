# SmartFight Phase 1 — Boxing Migration Task Plan

## Context

SmartFight is a Symfony 6.4 / MariaDB app being migrated from MMA to boxing as the primary discipline. Phase 1 is a **data + auth foundation** sprint — schema migration, boxing enums/weight classes, and fan registration (basic email/password + Google OAuth).

**Stack:** Symfony 6.4, Doctrine ORM 3.x, PHP 8 attributes (no `@ORM\`), VichUploader 2.9, KnpPaginator 6.x, string-based constant classes (not PHP 8.1 native enums).

---

## Dev A — Identity & Events Track

> Owns: Who fights, where, when. Fighter CRUD, Event CRUD, WeightClass, Rankings, Registration.

---

### A-1 · WeightClass Entity

**New file:** `src/Entity/WeightClass.php`

Fields (all using `#[ORM\...]` attributes):
- `id` (int, auto PK)
- `name` (string 50)
- `minWeight` (decimal 5,2, nullable) — kg
- `maxWeight` (decimal 5,2, nullable) — kg
- `discipline` (ManyToOne → Discipline, NOT NULL)
- `champion` (ManyToOne → Fighter, nullable)
- `displayOrder` (int)
- `slug` (string 100, unique)

**New file:** `src/Repository/WeightClassRepository.php`

---

### A-2 · Fighter Entity Update

**File:** `src/Entity/Fighter.php`

Changes:
- Add `firstName` (string 100, nullable)
- Add `lastName` (string 100, nullable)
- Add `countryCode` (string 2, nullable) — ISO 3166-1 alpha-2
- Add `weightClassEntity` (ManyToOne → WeightClass, nullable) — use property name `weightClassEntity` to avoid collision with legacy `weightClass` string
- Add `discipline` (ManyToOne → Discipline, NOT NULL)
- Make `user` nullable (change `#[ORM\JoinColumn(nullable: false)]` → `nullable: true`)
- Keep all legacy metrics (`wins`, `losses`, `koWins`, `submissionWins`, etc.) — still used by services

**File:** `src/Form/FighterType.php`
- Replace `weightClassId` integer field with `weightClassEntity` EntityType → WeightClass
- Add `discipline` EntityType → Discipline
- Add `firstName`, `lastName`, `countryCode` text fields

---

### A-3 · Event Entity Update

**File:** `src/Entity/Event.php`

Add fields:
- `startsAt` (datetime)
- `endsAt` (datetime)
- `venueName` (string 150, nullable)
- `city` (string 100, nullable)
- `country` (string 2, nullable)
- `posterUrl` (string 255, nullable)
- `posterFile` (VichUploader, not persisted)

Keep `startDate`/`endDate` mapped until migration confirms data transfer.

**File:** `src/Form/EventType.php`
- Replace `startDate`/`endDate` DateType fields with `startsAt`/`endsAt` DateTimeType
- Add `venueName`, `city`, `country`, `posterFile` (VichFileType)

**File:** `config/packages/vich_uploader.yaml`
Add mapping:
```yaml
event_poster:
    uri_prefix: /uploads/events
    upload_destination: '%kernel.project_dir%/public/uploads/events'
    namer: Vich\UploaderBundle\Naming\SmartUniqueNamer
```

---

### A-4 · Rankings Page Update

**File:** `src/Entity/Ranking.php`
- Add `weightClass` (ManyToOne → WeightClass, nullable initially)

**File:** `src/Controller/Admin/RankingController.php`
- Update list to filter/group by WeightClass entity (not string)

**File:** `src/Service/RankingService.php`
- Update `recomputeRankings()` to join on `weight_class_id` FK instead of `weight_class` varchar

---

### A-5 · Fan Registration — Basic (Email/Password)

**New file:** `src/Controller/RegistrationController.php`

Route: `GET/POST /register`

Logic:
1. Show registration form (firstName, lastName, email, password, phone optional)
2. Hash password with `UserPasswordHasherInterface`
3. Assign `ROLE_FAN` via `UserRole` entity (fetch by name = 'ROLE_FAN')
4. Create `FanProfile` record linked to the new User
5. Redirect to `/login` with flash message

**New file:** `src/Form/RegistrationFormType.php`
Fields: firstName, lastName, email, plainPassword (RepeatedType), phone (optional), agreeTerms (CheckboxType)

**New file:** `templates/registration/register.html.twig`
Extends front office base layout.

**File:** `config/packages/security.yaml`
- Add `/register` to `access_control` as `PUBLIC_ACCESS`

---

### A-6 · Fan Registration — Google OAuth

**New packages to install:**
```bash
composer require knpuniversity/oauth2-client-bundle
composer require league/oauth2-google
```

**New files:**
- `src/Security/GoogleAuthenticator.php` — extends `SocialAuthenticator`; fetches/creates User on OAuth callback; assigns ROLE_FAN
- `config/packages/knpu_oauth2_client.yaml` — register Google client

**Routes added (via bundle):**
- `/connect/google` — redirect to Google
- `/connect/google/check` — OAuth callback

**File:** `templates/registration/register.html.twig` — add "Sign in with Google" button.

**`.env` additions needed:**
```
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
```

> **Note:** Social login users are created with a random password hash. `user_id` on Fighter is nullable for these users — they won't have a fighter profile by default.

---

## Dev B — Fights & Stats Track

> Owns: The actual fighting. Enums, MatchProposal, FightResult, FightStat, CSV, Admin commands, Status automation.

---

### B-1 · Enum Classes

**New files in `src/Enum/`:**

**`EventStatus.php`** — `SCHEDULED`, `LIVE`, `COMPLETED`, `CANCELLED`

**`MatchStatus.php`** — `SCHEDULED`, `CANCELLED`, `COMPLETED`, `NO_CONTEST`

**`CardType.php`** — `MAIN_CARD`, `PRELIMS`

**`FightMethod.php`** — `KO`, `TKO`, `UD` (Unanimous Decision), `SD` (Split Decision), `MD` (Majority Decision), `PTS` (Points), `DQ` (Disqualification), `TD` (Technical Decision), `NC` (No Contest), `DRAW`

Each class must expose:
- `public static function getChoices(): array` — for Symfony ChoiceType
- `public static function getLabel(string $value): string` — for Twig display

Replace all hardcoded string choices in:
- `src/Form/FightResultType.php`
- `src/Form/PredictionSubmitType.php`
- `src/Form/EventType.php` (coordinate with Dev A)

---

### B-2 · MatchProposal Entity Update

**File:** `src/Entity/MatchProposal.php`

Add fields:
- `scheduledRounds` (int) — valid values: 4, 6, 8, 10, 12
- `isTitleFight` (bool, default false)
- `weightClass` (ManyToOne → WeightClass, NOT NULL) — Dev B adds FK; Dev A owns WeightClass entity
- `oddsFighter1` (decimal 5,2, nullable) — decimal/European format
- `oddsFighter2` (decimal 5,2, nullable)
- `cardPosition` (int, nullable) — 1 = main event
- `cardType` (string, CardType constants, nullable)

Modify:
- `status` — validate against MatchStatus constants

**File:** `src/Form/MatchProposalType.php`
- Add ChoiceType for `scheduledRounds` (choices: 4, 6, 8, 10, 12)
- Add `isTitleFight` CheckboxType
- Add `weightClass` EntityType → WeightClass
- Add `oddsFighter1`, `oddsFighter2` NumberType
- Add `cardType` ChoiceType using `CardType::getChoices()`

---

### B-3 · FightResult Entity Update

**File:** `src/Entity/FightResult.php`

Add fields:
- `knockdownsFighterRed` (int, default 0)
- `knockdownsFighterBlue` (int, default 0)

Modify:
- `method` — use `FightMethod` constants for form choices and validation

**File:** `src/Form/FightResultType.php`
- Replace method choices with `FightMethod::getChoices()`
- Add `knockdownsFighterRed`, `knockdownsFighterBlue` IntegerType fields

---

### B-4 · FightStatistic (FightStat) Entity Update

**File:** `src/Entity/FightStatistic.php`

Add fields:
- `punchesThrown` (int, default 0)
- `punchesLanded` (int, default 0)
- `roundNumber` (int, nullable) — NULL = fight totals; 1–12 = per-round breakdown

Keep in DB temporarily (drop in Phase 2 after confirming no active usage):
- `strikesLanded`, `strikesAttempted`, `takedownsLanded`, `takedownsAttempted`, `submissionAttempts`, `controlTimeSeconds`

**File:** `src/Form/FightStatisticType.php`
- Remove MMA-only fields from form: takedowns, submissionAttempts, controlTimeSeconds
- Add `punchesThrown`, `punchesLanded`, `roundNumber`
- Keep `knockdowns` (already boxing-compatible)

---

### B-5 · SystemMeta Entity

**New file:** `src/Entity/SystemMeta.php`

Fields: `id`, `key` (string 100, unique), `value` (string 255), `updatedAt` (datetime)

**New file:** `src/Repository/SystemMetaRepository.php`

Used by `RefreshEventStatusCommand` to track `last_status_refresh`.

---

### B-6 · Admin Commands

**New file:** `src/Command/CreateAdminCommand.php`
- Interactive prompts: email, firstName, lastName, password
- `--force` flag to skip confirmation
- Assigns ROLE_ADMIN via UserRole; checks for duplicate email

**New file:** `src/Command/RefreshEventStatusCommand.php`
- Reads `last_status_refresh` from SystemMeta
- Updates Event status: `startsAt <= now < endsAt` → LIVE; `endsAt < now` → COMPLETED
- Writes updated timestamp back to SystemMeta; logs count

---

### B-7 · CSV Export for Fight Statistics

**File:** `src/Controller/Admin/FightStatisticController.php`

Add `exportCsv()` action:
- Route: `GET /admin/statistics/export`
- Streams CSV (headers: event, fighter, punchesThrown, punchesLanded, knockdowns, roundNumber)
- Uses Symfony `StreamedResponse`

---

### B-8 · Status Automation

**New file:** `src/Service/EventStatusService.php`
- `refreshStatuses(): int` — updates LIVE/COMPLETED based on startsAt/endsAt; returns count updated
- Called by both the listener and the command

**New file:** `src/EventListener/StatusRefreshListener.php`
- Hooks on `kernel.request`
- Checks SystemMeta `last_status_refresh`; if older than 5 min → calls `EventStatusService::refreshStatuses()`
- Single indexed SELECT — negligible overhead

---

## Shared: Database Migration

One migration file. **Dev A writes, Dev B reviews line-by-line.**

```sql
-- 1. Create weight_class
CREATE TABLE weight_class (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  min_weight DECIMAL(5,2),
  max_weight DECIMAL(5,2),
  discipline_id INT NOT NULL,
  champion_id INT NULL,
  displayOrder INT NOT NULL DEFAULT 0,
  slug VARCHAR(100) NOT NULL UNIQUE,
  CONSTRAINT fk_wc_discipline FOREIGN KEY (discipline_id) REFERENCES discipline(id),
  CONSTRAINT fk_wc_champion FOREIGN KEY (champion_id) REFERENCES fighter(id) ON DELETE SET NULL
);

-- 2. Seed 17 boxing weight classes (all linked to Boxing discipline)
INSERT INTO weight_class (name, min_weight, max_weight, displayOrder, slug, discipline_id) VALUES
  ('Heavyweight',         91.00, 120.00,  1, 'heavyweight',         (SELECT id FROM discipline WHERE name='Boxing')),
  ('Cruiserweight',       86.00,  90.72,  2, 'cruiserweight',       (SELECT id FROM discipline WHERE name='Boxing')),
  ('Light Heavyweight',   76.00,  79.38,  3, 'light-heavyweight',   (SELECT id FROM discipline WHERE name='Boxing')),
  ('Super Middleweight',  73.00,  76.20,  4, 'super-middleweight',  (SELECT id FROM discipline WHERE name='Boxing')),
  ('Middleweight',        69.00,  72.57,  5, 'middleweight',        (SELECT id FROM discipline WHERE name='Boxing')),
  ('Super Welterweight',  66.00,  69.85,  6, 'super-welterweight',  (SELECT id FROM discipline WHERE name='Boxing')),
  ('Welterweight',        63.00,  66.68,  7, 'welterweight',        (SELECT id FROM discipline WHERE name='Boxing')),
  ('Super Lightweight',   61.00,  63.50,  8, 'super-lightweight',   (SELECT id FROM discipline WHERE name='Boxing')),
  ('Lightweight',         58.00,  61.23,  9, 'lightweight',         (SELECT id FROM discipline WHERE name='Boxing')),
  ('Super Featherweight', 56.00,  58.97, 10, 'super-featherweight', (SELECT id FROM discipline WHERE name='Boxing')),
  ('Featherweight',       54.00,  57.15, 11, 'featherweight',       (SELECT id FROM discipline WHERE name='Boxing')),
  ('Super Bantamweight',  52.00,  55.34, 12, 'super-bantamweight',  (SELECT id FROM discipline WHERE name='Boxing')),
  ('Bantamweight',        50.00,  53.52, 13, 'bantamweight',        (SELECT id FROM discipline WHERE name='Boxing')),
  ('Super Flyweight',     49.00,  52.16, 14, 'super-flyweight',     (SELECT id FROM discipline WHERE name='Boxing')),
  ('Flyweight',           48.00,  50.80, 15, 'flyweight',           (SELECT id FROM discipline WHERE name='Boxing')),
  ('Light Flyweight',     46.00,  48.99, 16, 'light-flyweight',     (SELECT id FROM discipline WHERE name='Boxing')),
  ('Minimumweight',        0.00,  47.63, 17, 'minimumweight',       (SELECT id FROM discipline WHERE name='Boxing'));

-- 3. Create system_meta + seed
CREATE TABLE system_meta (
  id INT AUTO_INCREMENT PRIMARY KEY,
  `key` VARCHAR(100) NOT NULL UNIQUE,
  value VARCHAR(255) NOT NULL,
  updatedAt DATETIME NOT NULL
);
INSERT INTO system_meta (`key`, value, updatedAt) VALUES ('last_status_refresh', '1970-01-01 00:00:00', NOW());

-- 4. Fighter changes
ALTER TABLE fighter
  ADD COLUMN firstName VARCHAR(100) NULL,
  ADD COLUMN lastName VARCHAR(100) NULL,
  ADD COLUMN countryCode CHAR(2) NULL,
  ADD COLUMN weight_class_id INT NULL,
  ADD COLUMN discipline_id INT NULL,
  ADD CONSTRAINT fk_fighter_wc FOREIGN KEY (weight_class_id) REFERENCES weight_class(id) ON DELETE SET NULL,
  ADD CONSTRAINT fk_fighter_discipline FOREIGN KEY (discipline_id) REFERENCES discipline(id),
  MODIFY COLUMN user_id INT NULL;
UPDATE fighter SET discipline_id = (SELECT id FROM discipline WHERE name='Boxing' LIMIT 1) WHERE discipline_id IS NULL;
ALTER TABLE fighter MODIFY COLUMN discipline_id INT NOT NULL;

-- 5. Event changes
ALTER TABLE event
  ADD COLUMN startsAt DATETIME NULL,
  ADD COLUMN endsAt DATETIME NULL,
  ADD COLUMN venueName VARCHAR(150) NULL,
  ADD COLUMN city VARCHAR(100) NULL,
  ADD COLUMN country CHAR(2) NULL,
  ADD COLUMN posterUrl VARCHAR(255) NULL;
UPDATE event SET startsAt = CONCAT(start_date, ' 00:00:00'), endsAt = CONCAT(end_date, ' 23:59:59') WHERE start_date IS NOT NULL;

-- 6. MatchProposal changes
ALTER TABLE match_proposal
  ADD COLUMN scheduledRounds INT NULL,
  ADD COLUMN isTitleFight TINYINT(1) NOT NULL DEFAULT 0,
  ADD COLUMN weight_class_id INT NULL,
  ADD COLUMN oddsFighter1 DECIMAL(5,2) NULL,
  ADD COLUMN oddsFighter2 DECIMAL(5,2) NULL,
  ADD COLUMN cardPosition INT NULL,
  ADD COLUMN cardType VARCHAR(20) NULL,
  ADD CONSTRAINT fk_mp_wc FOREIGN KEY (weight_class_id) REFERENCES weight_class(id);
UPDATE match_proposal SET status = 'SCHEDULED' WHERE status = 'PENDING';
UPDATE match_proposal SET status = 'COMPLETED' WHERE status = 'ACCEPTED';
UPDATE match_proposal SET status = 'CANCELLED' WHERE status = 'REJECTED';

-- 7. FightResult changes
ALTER TABLE fight_result
  ADD COLUMN knockdownsFighterRed  INT NOT NULL DEFAULT 0,
  ADD COLUMN knockdownsFighterBlue INT NOT NULL DEFAULT 0;
UPDATE fight_result SET method = 'TKO' WHERE method = 'SUBMISSION';
UPDATE fight_result SET method = 'UD'  WHERE method = 'DECISION';

-- 8. FightStatistic boxing fields
ALTER TABLE fight_statistic
  ADD COLUMN punchesThrown INT NOT NULL DEFAULT 0,
  ADD COLUMN punchesLanded INT NOT NULL DEFAULT 0,
  ADD COLUMN roundNumber   INT NULL;

-- 9. Ranking — add weight_class FK
ALTER TABLE ranking
  ADD COLUMN weight_class_id INT NULL,
  ADD CONSTRAINT fk_ranking_wc FOREIGN KEY (weight_class_id) REFERENCES weight_class(id);
```

---

## Verification Checklist

| # | Check | Command / Action |
|---|-------|-----------------|
| 1 | Schema valid | `php bin/console doctrine:schema:validate` → passes |
| 2 | Migration clean | `php bin/console doctrine:migrations:migrate` → no errors |
| 3 | 17 boxing weight classes | `SELECT COUNT(*) FROM weight_class` → 17 |
| 4 | system_meta seeded | `SELECT * FROM system_meta` → 1 row |
| 5 | Method values migrated | `SELECT DISTINCT method FROM fight_result` → only boxing methods |
| 6 | Fan registration | Visit `/register`, fill form, login → ROLE_FAN assigned |
| 7 | Google OAuth | Click "Sign in with Google" → OAuth flow → auto-creates User with ROLE_FAN |
| 8 | Create event | Admin fills startsAt, venueName, city, poster → saves OK |
| 9 | Enter fight result | Admin picks `UD` or `SD` method → saves OK |
| 10 | Fight stats form | Punches fields visible; takedowns/submission fields absent |
| 11 | Status refresh | `php bin/console app:refresh-event-status` → updates LIVE/COMPLETED events |
| 12 | Create admin | `php bin/console app:create-admin` → prompts, creates ROLE_ADMIN user |
| 13 | Legacy data intact | All 6 fight results, 8 fighters, events still visible in admin |
