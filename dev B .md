 Session Summary — SmartFight Boxing Migration (Phase 1   
  Spec Compliance)                                         
                                                           
  What was done                                            
                                                             The goal was to audit the existing Dev B Phase 1         
  implementation against the new                             SmartFight_Phase1_Specification.md and fix every gap   
  where the code didn't match the boxing migration
  requirements.

  ---
  Enums — Boxing terminology

  File: FightMethod.php
  Problem: MMA values: KO_TKO, SUBMISSION,
    UNANIMOUS_DECISION, etc.
  Fix: Boxing values: KO, TKO, UD, SD, MD, PTS, DQ, TD, NC,
    DRAW
  ────────────────────────────────────────
  File: MatchStatus.php
  Problem: MMA matchmaking states: ACCEPTED, REJECTED
  Fix: Correct boxing states: SCHEDULED, COMPLETED,
    CANCELLED, NO_CONTEST
  ────────────────────────────────────────
  File: CardType.php
  Problem: Had extra EARLY_PRELIMS case
  Fix: Removed — spec only allows MAIN_CARD and PRELIMS

  ---
  Status Automation — Logic and naming

  File: EventStatusService.php
  Problem: Throttle was 60s instead of 5 min; logic used
    endsAt with no FightResult check
  Fix: Throttle → 300s; computeStatus() now checks whether
    all child MatchProposal records have a linked
    FightResult to decide LIVE vs COMPLETED
  ────────────────────────────────────────
  File: RefreshEventStatusCommand.php
  Problem: Wrong command name app:event-status:refresh;
    didn't update system_meta
  Fix: Renamed to app:events:refresh-status; now calls
    refreshAndUpdateMeta()
  ────────────────────────────────────────
  File: StatusRefreshListener.php
  Problem: No way to disable in production when cron is
    running
  Fix: Added APP_DISABLE_STATUS_REFRESH_LISTENER env var
    check

  ---
  Entity cleanup — Dropped legacy fields

  Event entity had 6 fields the spec explicitly drops. All
  removed from the entity, form, and every template that
  referenced them:

  - startDate / endDate → replaced by startsAt / endsAt
  - venueId / organizerId → dead integer columns, removed
  - isChampionsEvent → moved to match level (isTitleFight
  on MatchProposal)
  - location → replaced by structured venueName, city,
  country

  Files updated: Event.php, EventType.php,
  EventController.php, EventRepository.php,
  admin/event/form.html.twig, admin/event/index.html.twig,
  admin/event/show.html.twig, front/booking/book.html.twig,   front/prediction/index.html.twig, QrCodeService.php,
  RankingService.php, PredictionController.php

  ---
  Registration & Admin Command

  File: User.php
  Problem: No username field
  Fix: Added username (VARCHAR 50, unique, nullable)
  ────────────────────────────────────────
  File: RegistrationFormType.php
  Problem: Missing username; had extra phone; no validation
    constraints
  Fix: Added username, removed phone, added NotBlank/Length
    on all fields
  ────────────────────────────────────────
  File: CreateAdminCommand.php
  Problem: No username prompt; no password confirmation
  Fix: Added username prompt with uniqueness check; added
    confirm-password step
  ────────────────────────────────────────
  File: registration/register.html.twig
  Problem: Rendered phone, missing username
  Fix: Swapped phone → username field

  ---
  Migration

  Version20260425000000.php — adds username to the user
  table and drops the 6 legacy event columns from the
  database. Ran successfully.

  ---
  Final cleanup

  - Removed discipline field from the event creation form
  (last request).
  - All 500 errors resolved; Symfony container and cache
  clear with no errors.

───────────────────────────────────────────────────────────
❯                                                     
───────────────────────────────────────────────────────────
  ⏵⏵ accept edits on (shift+tab to cycle)
                                                           
                                                           
                                                           
                                                           

  ---
  Registration & Admin Command

  ┌─────────────────────────────────┬──────────────────────────────────────────────────────────────┬──────────────────────────────────────────────────────────────────────────┐
  │              File               │                           Problem                            │                                   Fix                                    │
  ├─────────────────────────────────┼──────────────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────────────────┤
  │ User.php                        │ No username field                                            │ Added username (VARCHAR 50, unique, nullable)                            │
  ├─────────────────────────────────┼──────────────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────────────────┤
  │ RegistrationFormType.php        │ Missing username; had extra phone; no validation constraints │ Added username, removed phone, added NotBlank/Length on all fields       │
  ├─────────────────────────────────┼──────────────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────────────────┤
  │ CreateAdminCommand.php          │ No username prompt; no password confirmation                 │ Added username prompt with uniqueness check; added confirm-password step │
  ├─────────────────────────────────┼──────────────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────────────────┤
  │ registration/register.html.twig │ Rendered phone, missing username                             │ Swapped phone → username field                                           │
  └─────────────────────────────────┴──────────────────────────────────────────────────────────────┴──────────────────────────────────────────────────────────────────────────┘

  ---
  Migration

  Version20260425000000.php — adds username to the user table and drops the 6 legacy event columns from the database. Ran successfully.

  ---
  Final cleanup

  - Removed discipline field from the event creation form (last request).
  - All 500 errors resolved; Symfony container and cache clear with no errors.