# SmartFight Feature Inventory (2026-04-24)

This document lists features and technical components across the static template and the Symfony app.

## Scope
- Static HTML template: FrontOffice and BackOffice folders.
- Symfony app: smartfight/ folder.

## Roles and Surfaces
- Fan (public site): browse content, book events, submit predictions, view leaderboard, manage notifications.
- Admin (back office): manage content, events, fighters, results, stats, predictions, rankings, notifications, bookings.

## Static Template - FrontOffice (Public)
Pages
- Home: hero, countdown to next event, upcoming events, stats, featured fighters, blog highlights.
- Events: filter by status, live event spotlight, event cards, search input.
- Event details: fight card, countdown, ticket CTA, main and undercard sections.
- Fighters: grid listing with categories and search; fighter profile detail page.
- Live stream: live result banner, reactions, reaction feed cards.
- Predictions: tabs, pick UI, leaderboard table, accuracy bars.
- Bookings: event selector, ticket tiers, quantity controls, order summary.
- Leaderboard: podium, rank table, season selector.
- Blog: featured article, category filter, article grid and detail page.
- Gallery: media grid and lightbox behavior.
- Notifications: list with unread states and mark all read.
- Contact: contact info and form layout.

FrontOffice behaviors (FrontOffice/js/main.js)
- Countdown timers for elements with data-event-date.
- Navbar scroll state and active link detection.
- Tab switching for predictions and other tabbed areas.
- Prediction card pick selection feedback.
- Live reaction buttons increment and feed prepend.
- Notification read state toggling and mark-all-read.
- Gallery lightbox modal.
- Booking event selector and quantity pricing total.
- Smooth scrolling for anchor links.
- IntersectionObserver reveal animations.
- Mobile nav auto-close on link click.

FrontOffice design tokens (FrontOffice/css/smartfight.css)
- Brand colors: primary red, gold, dark surfaces, borders, text colors.
- Typography: Oswald for headings, Inter for body.
- UI scale: spacing, radius, shadow, z-index tokens.

## Static Template - BackOffice (Admin)
Modules and pages
- Dashboard overview.
- Users: index, form, show.
- Fighters: index, form, show.
- Events: index, form, show.
- Bookings: index, show.
- Fight results: index, form, show.
- Blog: index, form, show.
- Predictions: index and scoring view.
- Reactions: index.
- Notifications: index and broadcast view.

Pattern
- Each module uses a 3-page flow: list, form, detail (except noted).

## Symfony App - Feature Inventory
Authentication and access
- Form login with role-based redirect (admin to blog list, fan to blog list).
- Access control gates:
  - /admin requires ROLE_ADMIN.
  - /booking, /predictions/submit, /notifications/mark require ROLE_FAN.

Admin modules (smartfight/src/Controller/Admin)
- Blog content CRUD with category filter, status filter, and pagination.
- Event CRUD with organizer validation and champions-only filter.
- Fighters list with search and weight class filter; recompute analytics.
- Fight results CRUD, validation of fighter selection, CSV export.
- Fight statistics CRUD and wizard for both fighters in a result.
- Rankings view by season with CSV export and recompute action.
- Performance leaderboard and per-fighter performance view, recompute action.
- Predictions list with filters, score form, scoring confirmation.
- Reactions moderation: filter, pin/unpin, soft delete.
- Notifications list and admin broadcast form.
- Bookings list with filters and cancel action; booking QR export.

Fan modules (smartfight/src/Controller/Front)
- Blog list with category filter, featured article, view count tracking.
- Blog detail page with related content.
- Booking flow: create booking, list my bookings, cancel booking, QR ticket.
- Predictions: event selection, load fights, submit multi-pick predictions.
- Leaderboard: season rankings and personal rank.
- Notifications: list, filters, mark single or all read, JSON snapshot.

Matchmaking and classement tools (smartfight/src/Controller)
- Classement listing, recalculation by discipline, top-10 chart data.
- Matchmaking generator and best opponent finder for combattants.
- Combattant CRUD and data sync from fighters.

Console
- app:analytics:backfill recomputes rankings and performance scores and backfills fight statistics.

## Feature Deep Dive (Current Behavior)
Authentication and routing
- Entry redirects to login; success redirect uses role to route admin vs fan. Files: [smartfight/src/Controller/SecurityController.php](smartfight/src/Controller/SecurityController.php), [smartfight/src/Security/LoginSuccessHandler.php](smartfight/src/Security/LoginSuccessHandler.php), [smartfight/config/packages/security.yaml](smartfight/config/packages/security.yaml)
- Route discovery is attribute-based across controller classes. Files: [smartfight/config/routes.yaml](smartfight/config/routes.yaml), [smartfight/src/Controller](smartfight/src/Controller)

FrontOffice static template behavior
- Page interactivity is driven by a single JS file (countdowns, tabs, reactions, booking totals, lightbox). File: [FrontOffice/js/main.js](FrontOffice/js/main.js)
- Branding and tokens are centralized in the CSS variables file. File: [FrontOffice/css/smartfight.css](FrontOffice/css/smartfight.css)

Fan blog
- List and detail are rendered from Twig with pagination and featured article logic. Files: [smartfight/src/Controller/Front/BlogController.php](smartfight/src/Controller/Front/BlogController.php), [smartfight/templates/front/blog/index.html.twig](smartfight/templates/front/blog/index.html.twig), [smartfight/templates/front/blog/show.html.twig](smartfight/templates/front/blog/show.html.twig)
- View count increments on detail view via repository. File: [smartfight/src/Repository/BlogArticleRepository.php](smartfight/src/Repository/BlogArticleRepository.php)

Fan booking
- Booking page loads bookable events, computes remaining capacity per event, and posts a booking request. Files: [smartfight/src/Controller/Front/BookingController.php](smartfight/src/Controller/Front/BookingController.php), [smartfight/src/Service/BookingService.php](smartfight/src/Service/BookingService.php)
- Booking creates or reuses a booking and issues a notification to the fan. File: [smartfight/src/Service/NotificationService.php](smartfight/src/Service/NotificationService.php)
- QR ticket is generated server-side for fan and admin views. File: [smartfight/src/Service/QrCodeService.php](smartfight/src/Service/QrCodeService.php)

Fan predictions and leaderboard
- Predictions page loads upcoming events and existing picks; fights for event are fetched via JSON endpoint. Files: [smartfight/src/Controller/Front/PredictionController.php](smartfight/src/Controller/Front/PredictionController.php), [smartfight/templates/front/prediction/index.html.twig](smartfight/templates/front/prediction/index.html.twig)
- Submission accepts multiple predictions per request and blocks duplicates per match and user. File: [smartfight/src/Controller/Front/PredictionController.php](smartfight/src/Controller/Front/PredictionController.php)
- Leaderboard aggregates scored predictions by season and computes accuracy. File: [smartfight/src/Service/LeaderboardService.php](smartfight/src/Service/LeaderboardService.php)

Fan notifications
- Inbox list supports filtering and unread-only. Files: [smartfight/src/Controller/Front/NotificationController.php](smartfight/src/Controller/Front/NotificationController.php), [smartfight/templates/front/notification/index.html.twig](smartfight/templates/front/notification/index.html.twig)
- Mark read and snapshot endpoints update read state and return counts. File: [smartfight/src/Controller/Front/NotificationController.php](smartfight/src/Controller/Front/NotificationController.php)

Admin blog
- Admin listing is filtered by search, category, status, and paginated. Files: [smartfight/src/Controller/Admin/BlogController.php](smartfight/src/Controller/Admin/BlogController.php), [smartfight/src/Repository/BlogArticleRepository.php](smartfight/src/Repository/BlogArticleRepository.php)
- Create/edit use form types with upload handling (Vich). File: [smartfight/src/Form/BlogArticleType.php](smartfight/src/Form/BlogArticleType.php)

Admin events and bookings
- Event CRUD supports champions-only filter and organizer validation. Files: [smartfight/src/Controller/Admin/EventController.php](smartfight/src/Controller/Admin/EventController.php), [smartfight/src/Form/EventType.php](smartfight/src/Form/EventType.php)
- Booking admin supports filters, stats, cancel action, and QR view. Files: [smartfight/src/Controller/Admin/BookingAdminController.php](smartfight/src/Controller/Admin/BookingAdminController.php), [smartfight/src/Repository/EventBookingRepository.php](smartfight/src/Repository/EventBookingRepository.php)

Admin fight results and statistics
- Results create/edit enforce fighter validation and trigger recompute on completion. Files: [smartfight/src/Controller/Admin/ResultController.php](smartfight/src/Controller/Admin/ResultController.php), [smartfight/src/Service/RankingService.php](smartfight/src/Service/RankingService.php), [smartfight/src/Service/AnalyticsEngine.php](smartfight/src/Service/AnalyticsEngine.php)
- Statistics wizard writes stats for both fighters in a single flow. Files: [smartfight/src/Controller/Admin/FightStatisticController.php](smartfight/src/Controller/Admin/FightStatisticController.php), [smartfight/src/Service/FightStatisticService.php](smartfight/src/Service/FightStatisticService.php)

Admin rankings and performance
- Rankings are recomputed from fighter ELO and grouped by weight class. Files: [smartfight/src/Service/RankingService.php](smartfight/src/Service/RankingService.php), [smartfight/src/Repository/RankingRepository.php](smartfight/src/Repository/RankingRepository.php)
- Performance scores are derived from fight outcomes and stored per season. Files: [smartfight/src/Service/AnalyticsEngine.php](smartfight/src/Service/AnalyticsEngine.php), [smartfight/src/Repository/PerformanceScoreRepository.php](smartfight/src/Repository/PerformanceScoreRepository.php)

Admin predictions, reactions, notifications
- Prediction scoring loads predictions by match and publishes scores to fans. Files: [smartfight/src/Controller/Admin/PredictionController.php](smartfight/src/Controller/Admin/PredictionController.php), [smartfight/src/Service/PredictionScoringService.php](smartfight/src/Service/PredictionScoringService.php)
- Reactions are moderated via pin and soft-delete. File: [smartfight/src/Controller/Admin/ReactionController.php](smartfight/src/Controller/Admin/ReactionController.php)
- Broadcast messages target audience segments. File: [smartfight/src/Service/NotificationService.php](smartfight/src/Service/NotificationService.php)

Matchmaking and classement
- Combattants are synced from fighters for matchmaking and classement flows. File: [smartfight/src/Service/MatchmakingDataSyncService.php](smartfight/src/Service/MatchmakingDataSyncService.php)
- Match quality uses weight class, win rate, and experience. File: [smartfight/src/Service/MatchmakingService.php](smartfight/src/Service/MatchmakingService.php)
- Classement recalculates score and rank per discipline. File: [smartfight/src/Service/ClassementService.php](smartfight/src/Service/ClassementService.php)

## Step-by-Step Request/Response Flows
Authentication
- GET /login -> render login form. File: [smartfight/src/Controller/SecurityController.php](smartfight/src/Controller/SecurityController.php)
- POST /login -> handled by security firewall -> success redirect by role. Files: [smartfight/config/packages/security.yaml](smartfight/config/packages/security.yaml), [smartfight/src/Security/LoginSuccessHandler.php](smartfight/src/Security/LoginSuccessHandler.php)

Fan blog
- GET /blog -> BlogController loads published query, applies category filter, paginates -> render list. Files: [smartfight/src/Controller/Front/BlogController.php](smartfight/src/Controller/Front/BlogController.php), [smartfight/src/Repository/BlogArticleRepository.php](smartfight/src/Repository/BlogArticleRepository.php), [smartfight/templates/front/blog/index.html.twig](smartfight/templates/front/blog/index.html.twig)
- GET /blog/{id} -> increment view count, load related -> render detail. Files: [smartfight/src/Controller/Front/BlogController.php](smartfight/src/Controller/Front/BlogController.php), [smartfight/src/Repository/BlogArticleRepository.php](smartfight/src/Repository/BlogArticleRepository.php), [smartfight/templates/front/blog/show.html.twig](smartfight/templates/front/blog/show.html.twig)

Fan booking
- GET /booking -> load bookable events, compute remaining seats -> render booking page. File: [smartfight/src/Controller/Front/BookingController.php](smartfight/src/Controller/Front/BookingController.php)
- POST /booking -> BookingService validates, creates booking, sends notification -> redirect to /booking/my. Files: [smartfight/src/Service/BookingService.php](smartfight/src/Service/BookingService.php), [smartfight/src/Service/NotificationService.php](smartfight/src/Service/NotificationService.php)
- GET /booking/my -> list user bookings. File: [smartfight/src/Controller/Front/BookingController.php](smartfight/src/Controller/Front/BookingController.php)
- POST /booking/{id}/cancel -> validate ownership and csrf -> cancel booking -> redirect. File: [smartfight/src/Controller/Front/BookingController.php](smartfight/src/Controller/Front/BookingController.php)
- GET /booking/{id}/qr -> generate QR response. File: [smartfight/src/Service/QrCodeService.php](smartfight/src/Service/QrCodeService.php)

Fan predictions and leaderboard
- GET /predictions -> load upcoming events, leaderboard, my predictions -> render. File: [smartfight/src/Controller/Front/PredictionController.php](smartfight/src/Controller/Front/PredictionController.php)
- GET /predictions/fights/{eventId} -> load accepted match proposals -> JSON. File: [smartfight/src/Controller/Front/PredictionController.php](smartfight/src/Controller/Front/PredictionController.php)
- POST /predictions/submit -> validate inputs, create FanPrediction rows, skip duplicates -> redirect. File: [smartfight/src/Controller/Front/PredictionController.php](smartfight/src/Controller/Front/PredictionController.php)
- GET /leaderboard -> aggregate rankings, compute accuracy -> render. File: [smartfight/src/Controller/Front/LeaderboardController.php](smartfight/src/Controller/Front/LeaderboardController.php)

Fan notifications
- GET /notifications -> filter by type or unread, paginate -> render. File: [smartfight/src/Controller/Front/NotificationController.php](smartfight/src/Controller/Front/NotificationController.php)
- POST /notifications/mark-all-read -> set all read for fan -> redirect. File: [smartfight/src/Controller/Front/NotificationController.php](smartfight/src/Controller/Front/NotificationController.php)
- POST /notifications/{id}/mark-read -> set single read -> redirect. File: [smartfight/src/Controller/Front/NotificationController.php](smartfight/src/Controller/Front/NotificationController.php)
- GET /notifications/snapshot -> return unread count and latest notification JSON. File: [smartfight/src/Controller/Front/NotificationController.php](smartfight/src/Controller/Front/NotificationController.php)

Admin blog
- GET /admin/blog -> filtered query and pagination -> render list. File: [smartfight/src/Controller/Admin/BlogController.php](smartfight/src/Controller/Admin/BlogController.php)
- GET /admin/blog/new -> render form. POST /admin/blog/new -> persist article -> redirect. File: [smartfight/src/Controller/Admin/BlogController.php](smartfight/src/Controller/Admin/BlogController.php)
- GET /admin/blog/{id} -> render detail. File: [smartfight/src/Controller/Admin/BlogController.php](smartfight/src/Controller/Admin/BlogController.php)
- GET/POST /admin/blog/{id}/edit -> update article -> redirect. File: [smartfight/src/Controller/Admin/BlogController.php](smartfight/src/Controller/Admin/BlogController.php)
- POST /admin/blog/{id}/delete -> delete -> redirect. File: [smartfight/src/Controller/Admin/BlogController.php](smartfight/src/Controller/Admin/BlogController.php)

Admin events
- GET /admin/events -> list events, optional champions filter. File: [smartfight/src/Controller/Admin/EventController.php](smartfight/src/Controller/Admin/EventController.php)
- GET/POST /admin/events/new -> validate organizer -> create event -> redirect. File: [smartfight/src/Controller/Admin/EventController.php](smartfight/src/Controller/Admin/EventController.php)
- GET /admin/events/{id} -> show event. File: [smartfight/src/Controller/Admin/EventController.php](smartfight/src/Controller/Admin/EventController.php)
- GET/POST /admin/events/{id}/edit -> validate organizer -> update event -> redirect. File: [smartfight/src/Controller/Admin/EventController.php](smartfight/src/Controller/Admin/EventController.php)
- POST /admin/events/{id}/delete -> delete -> redirect. File: [smartfight/src/Controller/Admin/EventController.php](smartfight/src/Controller/Admin/EventController.php)

Admin bookings
- GET /admin/bookings -> filter by event/status, paginate, compute stats -> render. File: [smartfight/src/Controller/Admin/BookingAdminController.php](smartfight/src/Controller/Admin/BookingAdminController.php)
- POST /admin/bookings/{id}/cancel -> cancel booking -> redirect. File: [smartfight/src/Controller/Admin/BookingAdminController.php](smartfight/src/Controller/Admin/BookingAdminController.php)
- GET /admin/bookings/{id}/qr -> generate admin QR response. File: [smartfight/src/Controller/Admin/BookingAdminController.php](smartfight/src/Controller/Admin/BookingAdminController.php)

Admin fight results
- GET /admin/results -> filter by status -> render list. File: [smartfight/src/Controller/Admin/ResultController.php](smartfight/src/Controller/Admin/ResultController.php)
- GET/POST /admin/results/new -> validate fighters and winner -> create result -> redirect to stats wizard. File: [smartfight/src/Controller/Admin/ResultController.php](smartfight/src/Controller/Admin/ResultController.php)
- GET /admin/results/{id} -> show result. File: [smartfight/src/Controller/Admin/ResultController.php](smartfight/src/Controller/Admin/ResultController.php)
- GET/POST /admin/results/{id}/edit -> validate fighters and winner -> update -> redirect. File: [smartfight/src/Controller/Admin/ResultController.php](smartfight/src/Controller/Admin/ResultController.php)
- POST /admin/results/{id}/delete -> delete -> redirect. File: [smartfight/src/Controller/Admin/ResultController.php](smartfight/src/Controller/Admin/ResultController.php)
- GET /admin/results/export.csv -> stream CSV. File: [smartfight/src/Controller/Admin/ResultController.php](smartfight/src/Controller/Admin/ResultController.php)

Admin fight statistics
- GET /admin/stats -> filter by fight result -> render list. File: [smartfight/src/Controller/Admin/FightStatisticController.php](smartfight/src/Controller/Admin/FightStatisticController.php)
- GET/POST /admin/stats/new -> validate unique stat per fight/fighter -> create -> redirect. File: [smartfight/src/Controller/Admin/FightStatisticController.php](smartfight/src/Controller/Admin/FightStatisticController.php)
- GET/POST /admin/stats/{id}/edit -> update -> redirect. File: [smartfight/src/Controller/Admin/FightStatisticController.php](smartfight/src/Controller/Admin/FightStatisticController.php)
- POST /admin/stats/{id}/delete -> delete -> redirect. File: [smartfight/src/Controller/Admin/FightStatisticController.php](smartfight/src/Controller/Admin/FightStatisticController.php)
- GET/POST /admin/stats/wizard/{fightResult} -> upsert stats for both fighters -> redirect to result. File: [smartfight/src/Controller/Admin/FightStatisticController.php](smartfight/src/Controller/Admin/FightStatisticController.php)

Admin rankings and performance
- GET /admin/rankings -> group by weight class and season -> render. File: [smartfight/src/Controller/Admin/RankingController.php](smartfight/src/Controller/Admin/RankingController.php)
- POST /admin/rankings/recompute -> recompute rankings -> redirect. File: [smartfight/src/Controller/Admin/RankingController.php](smartfight/src/Controller/Admin/RankingController.php)
- GET /admin/rankings/export.csv -> stream CSV. File: [smartfight/src/Controller/Admin/RankingController.php](smartfight/src/Controller/Admin/RankingController.php)
- GET /admin/performance -> load performance leaderboard -> render. File: [smartfight/src/Controller/Admin/PerformanceController.php](smartfight/src/Controller/Admin/PerformanceController.php)
- GET /admin/performance/show/{fighterId} -> load per fighter -> render. File: [smartfight/src/Controller/Admin/PerformanceController.php](smartfight/src/Controller/Admin/PerformanceController.php)
- POST /admin/performance/recompute -> recompute analytics -> redirect. File: [smartfight/src/Controller/Admin/PerformanceController.php](smartfight/src/Controller/Admin/PerformanceController.php)

Admin predictions
- GET /admin/predictions -> filter predictions, paginate, show stats -> render. File: [smartfight/src/Controller/Admin/PredictionController.php](smartfight/src/Controller/Admin/PredictionController.php)
- GET /admin/predictions/score -> load events and results -> render score form. File: [smartfight/src/Controller/Admin/PredictionController.php](smartfight/src/Controller/Admin/PredictionController.php)
- POST /admin/predictions/score/load -> return predictions JSON for a match. File: [smartfight/src/Controller/Admin/PredictionController.php](smartfight/src/Controller/Admin/PredictionController.php)
- POST /admin/predictions/score/confirm -> score predictions, send notifications -> redirect. File: [smartfight/src/Controller/Admin/PredictionController.php](smartfight/src/Controller/Admin/PredictionController.php)

Admin reactions and notifications
- GET /admin/reactions -> filter by fight result, type, status -> render list. File: [smartfight/src/Controller/Admin/ReactionController.php](smartfight/src/Controller/Admin/ReactionController.php)
- POST /admin/reactions/{id}/pin -> toggle pin -> redirect. File: [smartfight/src/Controller/Admin/ReactionController.php](smartfight/src/Controller/Admin/ReactionController.php)
- POST /admin/reactions/{id}/delete -> soft delete -> redirect. File: [smartfight/src/Controller/Admin/ReactionController.php](smartfight/src/Controller/Admin/ReactionController.php)
- GET /admin/notifications -> filter by type, paginate -> render. File: [smartfight/src/Controller/Admin/NotificationController.php](smartfight/src/Controller/Admin/NotificationController.php)
- GET/POST /admin/notifications/broadcast -> send broadcast -> redirect. File: [smartfight/src/Controller/Admin/NotificationController.php](smartfight/src/Controller/Admin/NotificationController.php)

Matchmaking and classement
- GET /combattant -> sync from fighters -> render list. File: [smartfight/src/Controller/CombattantController.php](smartfight/src/Controller/CombattantController.php)
- GET/POST /combat -> generate matches and best opponent -> render list. File: [smartfight/src/Controller/CombatController.php](smartfight/src/Controller/CombatController.php)
- POST /combat/find-opponent -> return best opponent JSON. File: [smartfight/src/Controller/CombatController.php](smartfight/src/Controller/CombatController.php)
- GET /classement -> optional recalculation -> render list. File: [smartfight/src/Controller/ClassementController.php](smartfight/src/Controller/ClassementController.php)

## Entity Breakdown by Feature (Fields and Relations)
Blog
- BlogArticle: id, category, author, title, content, summary, status, viewCount, imagePath, videoPath, createdAt, updatedAt. Relations: many-to-one category, many-to-one author. File: [smartfight/src/Entity/BlogArticle.php](smartfight/src/Entity/BlogArticle.php)
- BlogCategory: id, name, description, slug, imageUrl, createdAt, updatedAt. Relations: one-to-many articles. File: [smartfight/src/Entity/BlogCategory.php](smartfight/src/Entity/BlogCategory.php)
- User: id, firstName, lastName, email, password, phone, role, isActive, createdAt, updatedAt. Relations: many-to-one role. File: [smartfight/src/Entity/User.php](smartfight/src/Entity/User.php)

Events and booking
- Event: id, name, description, startDate, endDate, status, visibility, capacity, discipline, venueId, organizerId, isChampionsEvent, location, createdAt, updatedAt. Relations: many-to-one discipline. File: [smartfight/src/Entity/Event.php](smartfight/src/Entity/Event.php)
- EventBooking: id, event, user, bookingStatus, ticketQuantity, totalPrice, ticketType, bookingDate, bookingReference, createdAt, updatedAt. Relations: many-to-one event, many-to-one user. File: [smartfight/src/Entity/EventBooking.php](smartfight/src/Entity/EventBooking.php)

Predictions
- FanPrediction: id, matchProposal, fan, predictedWinner, predictedMethod, submittedAt, isLocked, pointsEarned, isScored, season. Relations: many-to-one matchProposal, many-to-one fan, many-to-one predictedWinner. File: [smartfight/src/Entity/FanPrediction.php](smartfight/src/Entity/FanPrediction.php)
- MatchProposal: id, event, fighter1, fighter2, compatibility, status, proposedAt, notes. Relations: many-to-one event, many-to-one fighter1, many-to-one fighter2. File: [smartfight/src/Entity/MatchProposal.php](smartfight/src/Entity/MatchProposal.php)
- Fighter: id, user, nickname, dateOfBirth, nationality, photoUrl, weightClassId, wins, losses, draws, eloRating, performanceScore, winStreak, strengthOfSchedule, koWins, submissionWins, decisionWins, championsEventWinStreak, titleDefenses, height, reach, weightClass, status, createdAt, updatedAt. Relations: many-to-one user. File: [smartfight/src/Entity/Fighter.php](smartfight/src/Entity/Fighter.php)

Leaderboard and performance
- Ranking: id, fighterId, weightClass, rankPosition, points, season, createdAt, updatedAt. Relations: none (fighterId is scalar). File: [smartfight/src/Entity/Ranking.php](smartfight/src/Entity/Ranking.php)
- PerformanceScore: id, fighterId, score, aggression, defense, technique, experience, season, computedAt, createdAt, updatedAt. Relations: none (fighterId is scalar). File: [smartfight/src/Entity/PerformanceScore.php](smartfight/src/Entity/PerformanceScore.php)

Fight results and statistics
- FightResult: id, event, match, fighterRed, fighterBlue, winner, method, fightNumber, status, roundEnded, timeEnded, notes, fightDate, createdAt. Relations: many-to-one event, many-to-one match, many-to-one fighterRed, many-to-one fighterBlue, many-to-one winner. File: [smartfight/src/Entity/FightResult.php](smartfight/src/Entity/FightResult.php)
- FightStatistic: id, fightResultId, fighterId, strikesLanded, strikesAttempted, takedownsLanded, takedownsAttempted, submissionAttempts, knockdowns, controlTimeSeconds, createdAt, updatedAt. Relations: none (fightResultId, fighterId are scalars). File: [smartfight/src/Entity/FightStatistic.php](smartfight/src/Entity/FightStatistic.php)

Notifications
- FanNotification: id, fan, type, title, message, relatedEvent, relatedFightId, isRead, createdAt. Relations: many-to-one fan, many-to-one relatedEvent. File: [smartfight/src/Entity/FanNotification.php](smartfight/src/Entity/FanNotification.php)

Reactions
- FanReaction: id, fightResult, fan, reactionType, comment, reactedAt, isPinned, isDeleted. Relations: many-to-one fightResult, many-to-one fan. File: [smartfight/src/Entity/FanReaction.php](smartfight/src/Entity/FanReaction.php)

Fan profile and preferences
- FanProfile: id, user, favoriteSport, country, bio. Relations: one-to-one user. File: [smartfight/src/Entity/FanProfile.php](smartfight/src/Entity/FanProfile.php)
- FanPreference: id, fan, favoriteDiscipline, favoriteFighter, createdAt, updatedAt. Relations: many-to-one fan, many-to-one discipline, many-to-one favoriteFighter. File: [smartfight/src/Entity/FanPreference.php](smartfight/src/Entity/FanPreference.php)
- Discipline: id, name, description, weightClass, roundDuration, maxRounds, createdAt, updatedAt. Relations: none. File: [smartfight/src/Entity/Discipline.php](smartfight/src/Entity/Discipline.php)

Matchmaking and classement
- Combattant: id, nickname, nationalite, weightClass, wins, losses, draws, discipline. Relations: one-to-many combats. File: [smartfight/src/Entity/Combattant.php](smartfight/src/Entity/Combattant.php)
- Combat: id, combattant1, combattant2, scoreIA, resultat, dateCombat. Relations: many-to-one combattant1, many-to-one combattant2. File: [smartfight/src/Entity/Combat.php](smartfight/src/Entity/Combat.php)
- Classement: id, combattant, score, rang, discipline. Relations: many-to-one combattant. File: [smartfight/src/Entity/Classement.php](smartfight/src/Entity/Classement.php)

Users and roles
- UserRole: id, name, description. Relations: none. File: [smartfight/src/Entity/UserRole.php](smartfight/src/Entity/UserRole.php)

## Change Considerations (Boxing Pivot)
- Language sweep across FrontOffice and Symfony templates to shift MMA terms to boxing, with updated labels and metadata. Files: [FrontOffice](FrontOffice), [smartfight/templates/front](smartfight/templates/front), [smartfight/templates/admin](smartfight/templates/admin)
- Fight results and statistics should be redefined to reflect boxing outcomes, rounds, and judge scores. Files: [smartfight/src/Controller/Admin/ResultController.php](smartfight/src/Controller/Admin/ResultController.php), [smartfight/src/Controller/Admin/FightStatisticController.php](smartfight/src/Controller/Admin/FightStatisticController.php), [smartfight/src/Entity/FightResult.php](smartfight/src/Entity/FightResult.php), [smartfight/src/Entity/FightStatistic.php](smartfight/src/Entity/FightStatistic.php)
- Prediction submission and scoring logic should expand to round picks and decision types. Files: [smartfight/src/Controller/Front/PredictionController.php](smartfight/src/Controller/Front/PredictionController.php), [smartfight/src/Service/PredictionScoringService.php](smartfight/src/Service/PredictionScoringService.php)
- Ranking logic should incorporate boxing belts, title defenses, and inactivity. Files: [smartfight/src/Service/RankingService.php](smartfight/src/Service/RankingService.php), [smartfight/src/Entity/Ranking.php](smartfight/src/Entity/Ranking.php)
- Booking tiers and venue data should include ringside seating and row mapping. Files: [smartfight/src/Service/BookingService.php](smartfight/src/Service/BookingService.php), [smartfight/src/Entity/EventBooking.php](smartfight/src/Entity/EventBooking.php), [smartfight (1).sql](smartfight%20(1).sql)
## Technical Stack and Bundles (Symfony)
Runtime
- PHP >= 8.1, Symfony 6.4.

Core bundles
- FrameworkBundle, SecurityBundle, TwigBundle.
- Doctrine ORM and Migrations.
- KnpPaginatorBundle for pagination.
- VichUploaderBundle for media uploads.

Key third-party libs
- endroid/qr-code for ticket QR generation.

Assets
- FrontOffice assets are served from smartfight/public/frontoffice.
- BackOffice assets are served from smartfight/public/backoffice.

## Business Rules and Services
BookingService
- Ticket types: VIP, REGULAR, STANDING with fixed pricing.
- Quantity must be 1-4.
- Only scheduled, public events are bookable.
- Enforces capacity and one confirmed booking per user per event.
- Sends booking confirmation notification.

PredictionScoringService
- Scores predictions per fight with base points plus method bonus.
- Locks after scoring and sends notification to each fan.

RankingService
- ELO-based ranking updates on completed fights.
- Grouped ranking per weight class and season.
- Updates win streak and champions event streak.

AnalyticsEngine
- Computes performance scores based on ELO and fight metrics.

NotificationService
- Sends single fan notifications.
- Broadcasts to audience segments.

MatchmakingService and ClassementService
- Match quality scoring and best-opponent selection.
- Classement recalculation with score formula and per-discipline ranking.

## Data Model (SQL)
Primary tables (smartfight (1).sql)
- admin, user, user_role, fan_profile, fan_preference.
- blog_article, blog_category.
- event, venue, event_schedule, event_booking, event_fighter.
- discipline, weight_class.
- fighter, coach, fighter_coach.
- match_proposal, fight_result, fight_statistic, fight_highlight, judge_score.
- fan_prediction, fan_notification, fan_reaction.
- ranking, performance_score.
- matchmaking_rule.

Notes
- smartfight (2).sql contains a smaller subset of the schema, aligned with the Symfony app tables.

## Template and App File Map
Static template
- FrontOffice pages: FrontOffice/*.html
- BackOffice pages: BackOffice/**/index.html, form.html, show.html

Symfony app
- Controllers: smartfight/src/Controller, smartfight/src/Controller/Admin, smartfight/src/Controller/Front
- Templates: smartfight/templates/admin and smartfight/templates/front
- Services: smartfight/src/Service
- Entities: smartfight/src/Entity
