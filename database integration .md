# Database Comparison: `mma_system.sql` vs `smartfight.sql`

## Overview

| | `mma_system.sql` | `smartfight.sql` |
|---|---|---|
| **Purpose** | Active backend (root Symfony app) | Legacy backend (`smartfight/` subdirectory) |
| **Focus** | Real-world boxing (Usyk, Fury, Canelo…) | Tunisian MMA/multi-discipline league |
| **Charset** | `utf8mb4_general_ci` | `utf8mb4_unicode_ci` |
| **Migrations** | 2 (Apr 24) | 10 (Apr 11–26) |
| **Total tables** | 24 | 25 |

---

## Table-by-Table Breakdown

### Identical Structure (both databases)

| Table | Notes |
|---|---|
| `blog_article` | Same 12 columns, different column ordering |
| `blog_category` | Identical |
| `discipline` | Identical |
| `doctrine_migration_versions` | Identical structure, different migration records |

---

### Same Name, Different Schema

#### `event` vs `events`

| Column | `mma_system` (`events`) | `smartfight` (`event`) |
|---|---|---|
| PK | `eventId` (camelCase) | `id` |
| Date | `eventDate` (date only) | `starts_at` / `ends_at` (datetime range) |
| Org | `organization` (WBC/WBO/IBF…) | — |
| Discipline | — | `discipline_id` FK |
| Capacity | `seat_capacity` | `capacity` |
| Image | `poster_filename` | `poster_url` |
| Extra | `is_champions_event`, `belt_organization` | `description`, `venue_name` |

#### `fighters` vs `fighter`

| Aspect | `mma_system` (`fighters`) | `smartfight` (`fighter`) |
|---|---|---|
| PK | `fighterId` (camelCase) | `id` |
| Name | `firstName`, `lastName` columns | `first_name`, `last_name` (nullable, added later) |
| User link | — | `user_id` FK |
| AI fields | `ai_style_tag`, `ai_description`, `strength`, `weakness` | — |
| Sport type | Boxing-only (`koWins`, `decisionWins`, `technical_wins`) | MMA (`ko_wins`, `submission_wins`, `decision_wins`, `champions_event_win_streak`) |
| Stats | `strikes_thrown/landed` on the fighter row | Separate `fight_stat` / `fight_statistic` tables |
| Discipline | Single discipline implied | `discipline_id` FK — multi-discipline |

#### `fight_results` vs `fight_result`

| Aspect | `mma_system` (`fight_results`) | `smartfight` (`fight_result`) |
|---|---|---|
| PK | `resultId` (camelCase) | `id` |
| Fighters | `fighter1Id`, `fighter2Id`, `winnerId` | `fighter_red_id`, `fighter_blue_id`, `winner_id` |
| Finish detail | `methodOfVictory`, `decision_type`, `knockdown_round` | `method`, `round_ended`, `time_ended`, `notes` |
| Boxing extras | `belt_organization`, `fighter1_odds`, `fighter2_odds`, `inside_the_numbers`, `is_belt_fight` | `knockdowns_fighter_red`, `knockdowns_fighter_blue` |
| Media | `highlight_video_url`, `video_path` | — |
| Event link | `eventId` (camelCase) | `event_id` |

#### `fight_statistic` (same name, completely different columns)

| `mma_system` | `smartfight` |
|---|---|
| Boxing-focused: jabs, power punches, right/left hand, body shots, uppercuts (20 columns) | MMA-focused: takedowns, submission_attempts, control_time_seconds (15 columns) |
| References `fighters` table | References `fighter` table |

#### `match_proposal`

| Column | `mma_system` | `smartfight` |
|---|---|---|
| FK to weight | `weight_division_id` | `weight_class_id` |
| Voting | `vote_count` (denormalized counter) | — |
| Extra scheduling | — | `scheduled_rounds`, `is_title_fight`, `odds_fighter1/2`, `card_position`, `card_type` |

#### `notifications` vs `fan_notification`

| Aspect | `mma_system` (`notifications`) | `smartfight` (`fan_notification`) |
|---|---|---|
| PK | `notificationId` (camelCase) | `id` |
| Content | `message` only | `title` + `message` |
| Targeting | `userId` FK to users | `fan_id` FK |
| Context | `type` only | `related_event_id`, `related_fight_id` FKs |
| Types | NEW_ARTICLE, BOOKING, RANKING | NEW_EVENT, ADMIN_BROADCAST, PREDICTION_SCORED, LEADERBOARD_CHANGE |

#### `predictions` vs `fan_prediction`

| Aspect | `mma_system` (`predictions`) | `smartfight` (`fan_prediction`) |
|---|---|---|
| PK | `predictionId` | `id` |
| Links to | `fightId` (fight_results) | `match_proposal_id` |
| Season | — | `season` field |
| State tracking | `is_processed` | `is_locked`, `is_scored` |
| Points | `points_awarded`, `predicted_round` | `points_earned` (no round prediction) |

#### `ranking`

| Aspect | `mma_system` | `smartfight` |
|---|---|---|
| Org-based | Yes: WBC, WBA, IBF, WBO, MEDIA (5 rows per fighter per division) | No |
| Season-based | No | Yes: `season` field |
| Champion | `is_champion` flag | — |
| Weight ref | `weight_division_id` | `weight_class` string + `weight_class_id` |

#### `performance_score`

| Aspect | `mma_system` | `smartfight` |
|---|---|---|
| Timestamps | `calculated_at` only | `created_at`, `updated_at`, `computed_at` |
| Sub-scores | All nullable | All have `NOT NULL DEFAULT 0` |
| Season | — | `season` field |

#### `users` vs `user`

| Aspect | `mma_system` (`users`) | `smartfight` (`user`) |
|---|---|---|
| PK | `userId` (camelCase) | `id` |
| Identity | `username` (login key) | `first_name`, `last_name`, `email` |
| Auth extras | `webauthn_credential_id`, `webauthn_public_key`, `face_photo`, `verification_token`, `reset_token` | `phone`, `is_active`, `updated_at` |
| Username | Present as login identifier | Optional (`username` nullable) |

#### `roles` vs `user_role`

| Aspect | `mma_system` (`roles`) | `smartfight` (`user_role`) |
|---|---|---|
| PK | `roleId` | `id` |
| Types | ADMIN, USER (2) | ADMIN, ORGANIZER, FIGHTER, COACH, FAN (5) |
| Description | — | `description` column |
| Assignment | Many-to-many via `user_roles` join table | Direct `role_id` FK on `user` |

#### `weight_division` vs `weight_class`

| Aspect | `mma_system` (`weight_division`) | `smartfight` (`weight_class`) |
|---|---|---|
| Weight columns | `max_weight_lbs` only | `min_weight` + `max_weight` (kg) |
| Discipline | — | `discipline_id` FK |
| Champion | — | `champion_id` FK |
| Display | `slug` | `slug`, `displayOrder` |

---

### Only in `mma_system.sql`

| Table | Purpose |
|---|---|
| `fan_vote` | Community voting on match proposals (many-to-many `user` ↔ `match_proposal`) |
| `fighter_contract` | Fighter pay tracking (`base_pay`, `win_bonus`, `calculated_payout`, `is_paid`) |
| `public_key_credential_source` | WebAuthn/FIDO2 credential store (empty placeholder table) |

### Only in `smartfight.sql`

| Table | Purpose |
|---|---|
| `classement` | French-named ranking (`combattant_id`, `score`, `rang`, `discipline`) |
| `combat` | French-named fight record with `score_ia` AI field |
| `combattant` | Simplified fighter (nickname-only, no name columns, Tunisian local data) |
| `fight_stat` | Secondary MMA stats (`head_shots_landed`, `clinch_strikes`, `created_at`) |
| `system_meta` | Key-value config store (`last_status_refresh` timestamp) |

---

## Key Takeaways

1. **mma_system is boxing-focused, smartfight is multi-discipline MMA.** The fighter tables, stat columns, and fight result schemas reflect fundamentally different sports contexts.

2. **mma_system has richer authentication** — WebAuthn fields, email verification tokens, Face ID photo storage. smartfight uses a simpler first_name/last_name user with a direct role FK and phone number.

3. **Role granularity differs significantly** — smartfight has 5 domain roles (ORGANIZER, FIGHTER, COACH, etc.); mma_system only distinguishes ADMIN vs USER.

4. **mma_system tracks finances** (`fighter_contract`) and community voting (`fan_vote`); smartfight does not.

5. **smartfight has richer notifications** (title, related entity FKs, leaderboard events); mma_system notifications are simpler but more numerous in practice.

6. **The three French tables** (`classement`, `combat`, `combattant`) in smartfight are a remnant of an earlier version of the project and are not connected to the main Symfony entity layer.

7. **Weight classification**: mma_system's `weight_division` is boxing-only (lbs, no discipline link); smartfight's `weight_class` supports multiple disciplines with a champion FK.
