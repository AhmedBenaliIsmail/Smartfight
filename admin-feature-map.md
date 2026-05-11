# SmartFight Admin — Complete Feature Map
> JavaFX Rebuild Reference · Based on screenshots + full source analysis

---

## GLOBAL DESIGN SYSTEM

### Color Palette
| Token | Value | Usage |
|---|---|---|
| Background Main | `#0a0a0a` / `#0d0d0d` | Page/window background |
| Sidebar BG | `#111111` | Left nav panel |
| Card BG | `#1a1a1a` | All data cards, panels |
| Card BG Hover | `#222222` | Hover state on cards |
| Primary Red | `#dc2626` | Buttons, badges, accents, logo |
| Gold / Amber | `#f59e0b` | Champion badges, warnings |
| Success Green | `#16a34a` | "Active", "Confirmed", positive metrics |
| Warning Orange | `#ea580c` | "Pending", medium risk |
| Muted Gray | `#374151` | Disabled, secondary elements |
| Text Primary | `#ffffff` | All heading / value text |
| Text Secondary | `#9ca3af` | Subtitles, metadata, placeholders |
| Border | `#2d2d2d` | Card borders, table dividers |

### Typography
- **Page titles**: UPPERCASE, bold (`700`), white, ~28–36px
- **Section subtitles**: normal weight, gray-400, ~13–14px
- **Table headers**: UPPERCASE, gray-500, small (~11px), letter-spaced
- **Data values**: white, bold or semi-bold
- **Badges/tags**: UPPERCASE, ~10–11px, pill-shaped, colored background

### Layout Structure
```
┌──────────────────────────────────────────────────────┐
│  TOP NAVBAR (full width, ~60px, dark)                │
│  [SMARTFIGHT logo] [Search bar........] [NAV] [USER] │
├─────────────┬────────────────────────────────────────┤
│ SIDEBAR     │  MAIN CONTENT AREA                     │
│ (~220px)    │  (padding ~24px, scrollable)           │
│ fixed left  │                                        │
│             │                                        │
│             │                                        │
├─────────────┴────────────────────────────────────────┤
│  FOOTER (dark, links: Blog, Predictions, Logout)     │
└──────────────────────────────────────────────────────┘
```

### Top Navbar — Every Page
- **Left**: "SMARTFIGHT" text logo in red (#dc2626), bold
- **Center**: Dark search input — placeholder "Search Fighters, Events, Analytics..." (global search → `SearchController`)
- **Right**: Text links — `NEWS` | `RANKINGS` | `ANALYTICS` | `RESULTS`
- **Far right**: Red pill badge "ADMIN", then circular avatar with first-letter initial + green online dot; clicking avatar shows profile link

### Left Sidebar — Admin Navigation
Every item has an icon + label. Active item has a left red border or red background highlight.

| # | Label | Icon | Route |
|---|---|---|---|
| 1 | Dashboard | Grid/home | `/` |
| 2 | Leaderboard | Trophy | `/leaderboard` (fan leaderboard) |
| 3 | Users Management | People | `/users` |
| 4 | Boxers | Fist/glove | `/fighters` |
| 5 | Events | Calendar | `/events` |
| 5a | └ Champions Event | Star (sub-item) | `/events/champions` |
| 6 | Bout Results | Clipboard | `/results` |
| 7 | Statistics | Bar chart | `/stats` |
| 8 | Performance | Lightning | `/performance` |
| 9 | World Ranking | Globe | `/rankings` |
| 10 | Blog / News | Newspaper | `/admin/blog` |
| 11 | Event Bookings | Ticket | `/admin/bookings` |
| 12 | Fan Reactions | Chat bubble | `/admin/reactions` |
| 13 | Voting Pool Admin | Vote/poll | `/admin/proposals` |
| 14 | Purses & Contracts | Document/money | `/admin/contracts` |
| 15 | Finance Department | Chart/$ | `/admin/finance` |

**Bottom of sidebar:** Avatar + "admin" label + logout link

### Component Design Patterns (apply everywhere)

**Buttons:**
- Primary: `background #dc2626`, white text, radius ~6px, UPPERCASE text, ~12–13px
- Secondary: `background #1f1f1f`, border `#333`, white text, same radius
- Danger/Delete: deep red or red with trash icon
- Icon buttons: 32–36px square, dark background, icon only

**Cards:**
- Background `#1a1a1a`, border `1px solid #2d2d2d`, border-radius ~10px, padding ~20px
- Hover: border-color lifts slightly or card brightens to `#222`

**Badges/Pills:**
- Border-radius `9999px` (fully rounded)
- Admin → red bg, white text
- User → gray bg, white text
- Completed → green bg
- Pending → amber/orange bg
- Live → orange animated (pulse)

**Tables:**
- Dark header row (slightly lighter than body)
- Alternating rows or single dark row with subtle border
- Status columns use colored badge pills

**Form Inputs:**
- Background `#1a1a1a`, border `#333`, text white, placeholder `#6b7280`
- Focus: border-color `#dc2626`
- Dropdowns: dark styled `<select>` or custom dropdown

**Charts (JavaFX equivalents):**
- Dark background pane, minimal white gridlines
- Line charts: red or cyan line
- Bar charts: blue/cyan bars
- Pie/donut: colored segments with legend

---

## PAGE 1 — DASHBOARD (Command Center)
**Route:** `GET /`  
**Controller:** `DashboardController::index()`

### Header
- Title: **"COMMAND CENTER"** (uppercase, bold, ~32px)
- Subtitle: "System Overview & Management Tools" (gray)
- Top-right badge: **"ADMIN SESSIONS ACTIVE"** (red pill, blinking dot)

### Section A — Quick Action Cards (2×2 grid)
Four dark cards, each with an icon, title, subtitle, and red action button:

| Card | Icon | Title | Subtitle | Button Label | Button Action |
|---|---|---|---|---|---|
| 1 | Calendar | Schedule Fight | "Create a new bout" | SCHEDULE FIGHT | → `GET /results/schedule` |
| 2 | Trophy | Manage Events | "View/edit your events" | MANAGE EVENTS | → `GET /events` |
| 3 | Fist | Boxer Roster | "Control fighter data" | BOXER ROSTER | → `GET /fighters` |
| 4 | Newspaper | Publish News | "Write blog article" | PUBLISH NEWS | → `GET /admin/blog/new` |

### Section B — Financial Summary (4 metric boxes in a row)
Thin horizontal bar with 4 cells, each cell has value + label + micro trend line:
- **GROSS REVENUE**: `$1,171.68` — green upward spark line below
- **CUSTOMER DEPOSITS**: `$0.00` — red label "Customer Obligation"
- **NET OPERATING P/L**: `$1,171.68` — green label "Operating Profit"
- **PENDING PAYOUTS**: `$0.00` — green label "Awaiting Distribution"

Data comes from: `BookingRepository::getAdminStats()` + `ContractRepository::getFinancialStats()`

### Section C — Fan Favorite Center
- Title: **"FAN FAVORITE CENTER"**
- Subtitle: "The Leaderboard fan system. These are the most anticipated matchups."
- Button top-right: **"CREATE FAN EVENT"** (red) → `POST /events/fan-favorite/create` (takes top 3 voted proposals, auto-builds event)

**3 Proposal Cards** (side by side, labeled #1/#2/#3 MOST WANTED):
- Badge: "#1 MOST WANTED" (red), "#2 MOST WANTED" (amber), "#3 MOST WANTED" (gray)
- Fighter 1 name + division
- "vs"
- Fighter 2 name + division
- Vote count badge (number)
- Source: `MatchProposalRepository::findTopVoted(3)`

### Section D — Charts (bottom row)
**Left chart — Platform Revenue Growth:**
- Title: "PLATFORM REVENUE GROWTH", "Last 7 Days" button
- Line chart, dark bg, red line, 7 data points (days of week as labels)
- Data: `BookingRepository::getDailyRevenueLast7Days()`

**Right chart — User Growth:**
- Title: "USER GROWTH"
- Bar chart, dark bg, blue/cyan bars
- Shows last 7 days new registrations
- Stats below: TOTAL FANS count + INCREASE % badge
- Data: `UserRepository::getDailyRegistrationsLast7Days()`

### Section E — Recent Activity (also part of dashboard, separate card)
- Title: "RECENT ACTIVITY"
- Table: EVENT / NAME | BOXERS | STATUS
  - 5 most recent fight results
  - STATUS badge: green "COMPLETED", orange "PENDING"
- Right stats: Event count, Fight count, System Uptime %, Engine Status (Active)

---

## PAGE 2 — LEADERBOARD
**Route:** `GET /leaderboard`  
**Note:** This is the fan prediction leaderboard showing who guessed fight results most accurately.

### Header
- Title: **"ELITE LEADERBOARD"** (massive, UPPERCASE, centered)
- Subtitle: "2026 SEASON PERFORMANCE ANALYTICS"
- Badge top-right: **"YEARLY PRIZE ACTIVE"** (red pill)

### Section A — Top 3 Podium
Three cards side by side (center = #1, left = #2, right = #3). Styled like podium blocks:
- **#1 card** (center, largest): Gold-bordered, circular avatar with letter, rank crown icon
  - Fighter/User name in UPPERCASE
  - Points (e.g., "20 PTS")
  - Division label: "ELITE TIER"
- **#2 card** (left): Silver border, smaller
- **#3 card** (right): Bronze border, smaller
- Each card has edit icon (pencil) and delete icon in corner

### Section B — Right Panel: 2026 Rewards
Sidebar panel listing season prizes:
- Title Fight Travel
- Ringside + Analyst
- Fighter Analyst
Styled as a vertical list with checkmark/bullet icons.

### Section C — Scoring Logic Panel (right side)
Visual breakdown of how points are awarded:
| Criteria | Points | Color |
|---|---|---|
| Correct Winner | +5 | Green up arrow |
| Method of Victory | +2 | Green up arrow |
| Exact Round Bid | +2 | Green up arrow |
| Perfect Score Bonus | +bonus | Green up arrow |

### Section D — Full Season Standings Table
Below the podium, a full paginated table:

**Columns:** RANK | CONTENDER | PREDICTIVE AMOUNT | TOTAL POINTS | STATUS

- **RANK**: Number
- **CONTENDER**: Full name + username (gray subtitle)
- **PREDICTIVE AMOUNT**: Horizontal mini-bar chart showing accuracy
- **TOTAL POINTS**: Numeric
- **STATUS**: Badge — "IN CONTENTION" (green), "CONTENDER" (orange), "CONTINUE" (gray)

**Search bar** above table: "Search contenders..." dark input

Data source: Users ranked by prediction accuracy/points from `PredictionRepository`

---

## PAGE 3 — USERS MANAGEMENT
**Route:** `GET /users`  
**Controller:** `UserController::index()`

### Header
- Title: **"USERS"**
- Subtitle: "Manage admin and fan accounts"
- Button right: **"REGISTER ADMIN"** (red) → `GET/POST /users/new`

### Filter Bar
- Dark search input: "Search accounts..."
- Filters on right side

### Users Table
**Columns:** ID | USERNAME | EMAIL | PREDICTION PTS | ROLE | CREATED AT | ACTIONS

| Column | Detail |
|---|---|
| ID | auto-increment integer |
| USERNAME | Text + role badge (red "adm" for admin, gray for user) |
| EMAIL | Email address (gray text) |
| PREDICTION PTS | Integer score |
| ROLE | Pill badge: ADMIN (red bg), USER (green/gray bg) |
| CREATED AT | Date string |
| ACTIONS | Pencil (edit) + Red trash (delete) buttons |

**Edit action:** opens inline form or separate page  
**Delete action:** `POST /users/{id}/delete` — removes user from DB

### Create User Form (`/users/new`)
Fields: Username, Password, Email  
Role is set to USER by default; admin registration sets ROLE_ADMIN.

---

## PAGE 4 — BOXERS / ORGANIZATION ROSTER
**Route:** `GET /fighters`  
**Controller:** `FighterController::index()`

### Header
- Title: **"ORGANIZATION ROSTER"**
- Subtitle: "Managing professional combatant portfolios"
- Button: **"PREDICT INJURIES AI"** (dark gray) → `GET /performance/injury-predictions`
- Button: **"ADD BOXER"** (red) → `GET /fighters/new`

### Filter / Search Bar (horizontal, below header)
- Text input: "Search for a fighter..."
- **COUNTRY** dropdown (All Countries)
- **CATEGORY** dropdown (All Categories)  
- **STYLE** dropdown (All Styles — options: SLUGGER, TACTICIAN, BOXER, BRAWLER, COUNTER-PUNCHER)
- **ELO MIN** number input
- **WIN RATE MIN %** number input
- Filter/Apply button

### Fighter Cards Grid (3 columns)
Each card is a dark panel with:
- **Photo** (top ~60% of card, large photo or placeholder with fighter initials)
- **AI Style Tag badge** (pill, color-coded by style — e.g., TACTICIAN=purple, SLUGGER=orange)
- **Fighter name** (large, UPPERCASE, white)
- **Division** (gray subtitle)
- **Three stats row:**
  - WIN RATE: `89%`
  - ELO SCORE: `784`
  - RECORD: `25-3-0`
- **DIVISION STANDING**: progress bar with `790/5000` label
- **Action icons** (bottom right): pencil edit, red trash delete

**On click of a card / edit button** → `GET /fighters/{id}/edit`

### Add/Edit Fighter Form (`/fighters/new` or `/fighters/{id}/edit`)
Fields:
- First Name, Last Name, Nickname
- Weight Division (dropdown)
- Nationality
- Wins, Losses, Draws, KO Wins, Technical Wins, Decision Wins
- Height (cm), Reach (cm)
- Manager (dropdown — selects a User)
- Photo upload (file input)
- Button: **"GENERATE AI PROFILE"** (per-fighter) → `POST /fighters/{id}/generate-ai-profile`
  - Calls DeepSeek AI → sets AI Style Tag + AI Description on fighter

### Recalculate Rankings Button
On the list page: **"RECALC RANKINGS"** button → `POST /fighters/recalc-rankings`  
Triggers `RankingService::recomputeAllRankings()`

### AI Injury Modal (triggered from any page with "PREDICT INJURIES AI")
Overlay modal showing:
- Title: **"INJURY PREDICTION ANALYST"** with 5-star rating
- Doctor name: "Dr. James Wong | PhD Biomechanics" (AI persona)
- **Risk Level** meter bar (0–100%, colored red when high)
- **Injury Zone**: body part badge (e.g., "RIGHT SHOULDER") + days-to-alert number in amber
- **Recommendation**: green checkmark + text (e.g., "Shoulder rest + PT 2x/day")
- Stats row: Accuracy % | Days Alert | ROI %
- Button: **"VIEW DETAILED ANALYSIS"** (red) → `GET /performance/injury-predictions`

---

## PAGE 5 — EVENT MANAGEMENT
**Route:** `GET /events`  
**Controller:** `EventController::index()`

### Header
- Title: **"EVENT MANAGEMENT"**
- Subtitle: "Directing professional boxing spectacles and world title events"
- View toggle: Grid icon / List icon (top right corner)
- Button: **"CREATE EVENT"** (red) → `GET /events/new`

### Search Bar
- Dark input: "Search event, venue..." 
- Searches by event name, venue, city

### Stats Bar (above grid, small)
Four inline counters: Total Events | Upcoming | Past | Champions | Total Fights

### Event Cards Grid (2–3 per row)
Each card:
- **Event name** (white, bold, large — uppercase)
- **Date** (with calendar icon, gray)
- **Venue + City** (with location pin icon, gray)
- **Organization badge** (colored pill — WBC/WBA/IBF/WBO/INDEPENDENT/UNDISPUTED/FAN CHOICE)
- **Sanctioning Progress**: "SANCTIONING PROGRESS: X/3 BOUTS" + red fill progress bar
- Button: **"MANAGE CARD"** (red) → `GET /results/{id}/manage-card`
- Status indicator if event is LIVE (orange pulse badge)

### Card actions (hover or icon):
- Edit icon → `GET/POST /events/{id}/edit`
- Delete icon → `POST /events/{id}/delete`

### Create Event Form (`/events/new`)
Fields: Event Name, Date (datetime picker), Venue, City, Organization (dropdown)  
Submit → redirects to `/events`

### Edit Event Form (`/events/{id}/edit`)
Same fields as create, pre-filled.

### Champions Event sub-page (`/events/champions`)
Filters event list to only championship-sanctioned events. Same card layout.

---

## PAGE 6 — CREATE WORLD TITLE CARD
**Route:** `GET/POST /events/champions-event/new`  
**Controller:** `EventController::newChampionsEvent()`

### Header
- Title: **"CREATE WORLD TITLE CARD"**
- Subtitle: "Automatically schedule #1 vs #2 Contenders for Sanctioned Titles"
- Back button top-right

### Form: EVENT & SANCTIONING DETAILS
| Field | Type |
|---|---|
| Event Branding (name) | Text input |
| Date | Date/time picker |
| Primary Organization | Text input (e.g., UNDISPUTED) |
| Venue | Select dropdown |
| City | Select dropdown |

### Form: SELECT CHAMPIONSHIP DIVISIONS
Description: "Select 3 different divisions. The system will auto-schedule the top 2 ranked boxers in each."
- Title Fight 1 Division: dropdown (all weight divisions)
- Title Fight 2 Division: dropdown
- Title Fight 3 Division: dropdown

**Validation**: All 3 must be distinct. Each division must have ≥2 ranked fighters.

### Submit Button
**"Generate Sanctioned Card"** (red, full width)  
Logic: Looks up `Ranking` table for top 2 fighters per selected division → auto-creates 3 scheduled fights → redirects to manage-card page.

---

## PAGE 7 — MANAGE EVENT FIGHT CARD
**Route:** `GET /results/{id}/manage-card`  
**Controller:** `ResultController::manageEventResults()`

### Header
- Title: **"MANAGE CARD: [EVENT NAME]"** (dynamic)
- Subtitle: "Run the professional boxing card with precision"
- Button: **"Back Schedule"** → `GET /events`
- Info bubble (top right, if card full): "This event card is full (max 3 bouts)"

### Scheduled Bouts List
Each fight is a horizontal row/card:
- **BOUT #** badge (red circle with number)
- **Fighter 1** block: photo + name (large) + weight class + ELO badge (gray pill)
- **VS** divider (bold, red, centered)
- **Fighter 2** block: photo + name + weight class + ELO badge
- **Status badge**: "VERIFIED" (green), "FINAL" (gold), "PENDING" (amber)
- **Edit icons** on both fighters (pencil icon → opens result entry)
- **Delete icon** → `POST /events/fights/{fightId}/delete`
- **Note/comment icon** (sticky note visual) if notes exist

### Add Fight (below bout list, if slots available)
Dropdown: Fighter 1 select, Fighter 2 select, Fight Number select  
Button: **"ADD FIGHT"** → `POST /events/{id}/fights/add`

### AI Matchmake Button
**"AI Suggest Match"** button → `GET /events/{id}/fights/ai-matchmake`  
Returns JSON with best fighter pairing (heuristic 8-vector scoring).  
Shows modal:
- Fighter 1 name, style, record, stats
- Fighter 2 name, style, record, stats
- Match Quality badge (EXCELLENT / GOOD / FAIR)
- Button: **"Apply Match"** → `POST /events/{id}/fights/ai-apply` (saves to DB)

---

## PAGE 8 — FIGHT RESULTS
**Route:** `GET /results`  
**Controller:** `ResultController::index()`

### Header
- Title: **"FIGHT RESULTS"**
- Buttons (top right):
  - **"EXPORT CSV"** → `GET /results/export` (downloads CSV)
  - **"BOUT STAT"** → `GET /stats`
  - **"SCHEDULE"** → `GET /results/schedule`
  - **"PDF REPORT"** → `GET /results/export-pdf`
- Filter: Organization dropdown (WBC/WBA/All) + Archive Status dropdown
- View toggle: grid/list

### Search Bar
- Dark input: "Search events, boxers, or venues..."

### Event Result Cards (grid)
One card per event. Each card:
- **Organization badge** (top corner pill — e.g., "STANDARD EVENT", "3F CHAMPIONSHIP")
- **Event name** (white bold)
- **Date + Venue** (gray)
- **Fighter names** (if fights logged)
- **Status badges**: "PENDING" (amber), fight count
- **"NO DATA"** text if no completed fights yet
- Edit / delete icons

### Schedule Fight Form (`/results/schedule`)
Fields: Event dropdown, Fight Number, Fighter 1, Fighter 2  
Creates a `FightResult` row with status=SCHEDULED.

---

## PAGE 9 — ENTER FIGHT RESULT
**Route:** `GET/POST /results/{id}/enter`  
**Controller:** `ResultController::enter()`

### Header
- Shows both fighter names prominently
- Fight context (event, fight number)

### Result Form
| Field | Type |
|---|---|
| Winner | Radio: Fighter 1 / Fighter 2 / Draw |
| Method of Victory | Dropdown: KO, TKO, DECISION, DISQUALIFICATION |
| Decision Type (if DECISION) | Dropdown: UD, SD, MD |
| Round | Number input |
| Fight Date | Date picker |
| Highlight Video URL | Text input |
| Video File | File upload |

**Submit** → calls `FightResultService::enterResult()` which:
- Sets winner, method, round, status=COMPLETED
- Updates fighter win/loss records
- Triggers ranking recalculation
- Creates notifications for all fans
- Redirects to manage-card page

---

## PAGE 10 — FIGHT STATISTICS INDEX
**Route:** `GET /stats`  
**Controller:** `FightStatisticController::index()`

### Header
- Title: **"AGGREGATED FIGHT PERFORMANCE & ARCHIVE METRICS"**
- Search bar: "SEARCH BY BOUT, FIGHTER OR WEIGHT CLASS..."
- Sort dropdown: "CHRONOLOGICAL" / "ALPHABETICAL"

### Bout Rows (one per completed fight with stats)
Each row is a dark card:
- **Event name** (left, bold)
- **Date + location** (gray)
- **Fighter 1 name** | Total: `XX` thrown / `XX` landed
- **Fighter 2 name** | Total: `XX` thrown / `XX` landed
- Button: **"OPEN SCORER"** (red) → `GET /stats/show/{fightId}`
- Delete button → removes all stats for that fight

---

## PAGE 11 — FIGHT STATISTICS DETAIL (Bout Scorer)
**Route:** `GET /stats/show/{fightId}`  
**Controller:** `FightStatisticController::show()`

### Header
- **"[FIGHTER 1 NAME] vs [FIGHTER 2 NAME]"** — dual-color giant text (Fighter 1 in blue, Fighter 2 in red)
- Event name + date (gray below)

### Fighter Stats Cards (side by side)
Two panels, one per fighter:
- **Punch Accuracy**: big percentage (e.g., "40.9%")
- **Punches Landed**: `52`
- **Punches Thrown**: `127`
- **JAB EFFICIENCY %** (horizontal mini bar)
- **PUNCH EFFICIENCY %** (horizontal mini bar)
- **POWER PUNCHES %** (horizontal mini bar)

### Round-by-Round Log Table
Columns: **ROUND** | **FIGHTER** | **LANDED/THROWN** | **ACC %** | **JABS** | **POWER** | **KDS**
- Color-coded alternating by fighter (blue row = Fighter 1, red row = Fighter 2)
- Rounds numbered R1 through RN
- DATA SOURCE: SMARTFIGHT STAT A+ label in top right

### AI Bout Intelligence Section
- Title: **"AI BOUT INTELLIGENCE"** with AI badge
- 4 metrics: Accuracy Points | Total Punches | Knockdowns | Efficiency %
- **"KNOCKOUT SUMMARY"**: AI-generated paragraph (from `BoutAnalysisService`)
- **"AI BOUT THEMES"**: Second AI paragraph

### Round Commentary Log (below)
Per-round AI commentary, each with:
- Round number badge (e.g., "R1 ANALYSIS")
- Paragraph of broadcast-style text generated by `RoundCommentaryService`

---

## PAGE 12 — ENTER FIGHT STATISTICS (Per Round)
**Route:** `GET/POST /results/{id}/stats`  
**Controller:** `ResultController::stats()`

### Purpose
Data entry for CompuBox-style per-round punch stats after a fight is completed.

### Layout
Two-column form (Fighter 1 left, Fighter 2 right) with round tabs/navigation.

### Fields per fighter per round:
- Punches Thrown / Punches Landed
- Power Punches Thrown / Landed
- Jabs Thrown / Jabs Landed
- Uppercuts Thrown / Landed
- Right Hand Thrown / Landed
- Left Hand Thrown / Landed
- Body Shots Landed
- Knockdowns

### Validation (enforced server-side):
- Landed ≤ Thrown for every punch type
- Right + Left thrown = Total thrown
- Right + Left landed = Total landed
- No negative values

### AI Stats Generation
Button: **"GENERATE AI STATS"** → `POST /results/{id}/generate-ai-stats`  
Auto-fills all round data with realistic random values + generates AI "Inside the Numbers" paragraph.

### Round Commentary
Button per round: **"Generate Round Commentary"** → `POST /results/{id}/generate-round-commentary`  
Returns broadcast-style text for that round.

### Inside The Numbers (text area)
Optional rich-text analysis panel. Admin can type manually or use AI generation.

---

## PAGE 13 — PERFORMANCE ANALYTICS ENGINE
**Route:** `GET /performance`  
**Controller:** `PerformanceController::index()`

### Header
- Title: **"ANALYTICS ENGINE — PERFORMANCE SCORES"**
- Subtitle: "Track Comprehensive Accuracy, Skill Metric, PO Efficacy, and Title Efficiency"
- Button: **"Recalculate AI Performance"** (red) → `POST /performance/recalculate`

### Filter Bar
- Search input: "Search Boxers..."
- Sort dropdown: options are:
  - Sort by Highest Performance (default)
  - Sort by Lowest Performance
  - Sort by ELO (desc)
  - Sort by Win Streak (desc)
  - Sort by SOS (desc)
- **"Apply"** button (red)

### Performance Table
Sortable, paginated (6 pages visible in screenshot):

**Columns:** BOXER | PERFORMANCE SCORE | ELO RATING | WIN STREAK | ACTIONS

| Column | Detail |
|---|---|
| BOXER | Fighter name (clickable) |
| PERFORMANCE SCORE | Colored value (red = high, gray = low) |
| ELO RATING | Colored badge (number) |
| WIN STREAK | Integer |
| ACTIONS | "AI Analyse" button (dark/outline) |

**"AI Analyse" button per row** → opens modal or navigates to `/performance/{id}` showing scouting report, fight history, etc.

### Pagination
"1 2 3 4 5 6... PAGE 1 OF 6" style navigation

---

## PAGE 14 — WORLD RANKINGS
**Route:** `GET /rankings`  
**Controller:** `RankingController::index()`

### Header
- Search input: "Search fighters by name..."
- **QR Download** button → `GET /rankings/qr-download` (SVG QR code linking to `/rankings/mobile`)
- **Export CSV** button → `GET /rankings/export`
- **Recalculate** button → `POST /rankings/recalculate`

### Division Sections (one per weight class)
For each weight division, a collapsible/expanded section:

**Champion Card (full width, gold border):**
- Fighter photo (large, left side)
- Fighter name (very large, white)
- Nickname (in quotes, gray)
- Record badges: `W-L-D` colored pills
- Stats row: ELO | Total Fights | Performance Score
- "WORLD CHAMPION" gold badge (top right)

**Rankings Table (fighters #2 onward):**
Columns: RANK | FIGHTER | RECORD | TOTAL RATING | ELO | PERF SCORE | STREAK

- RANK: `#2`, `#3`, etc. with colored ranking badge
- FIGHTER: Name + photo thumbnail (small)
- RECORD: `W-D(loss)-D` green/red pills
- TOTAL RATING: number (Glicko-ELO composite)
- ELO: raw ELO number
- PERF SCORE: decimal
- STREAK: win streak number

### Divisions covered (from RankingService):
Heavyweight, Light Heavyweight, Super Middleweight, Middleweight, Super Welterweight, Welterweight, Lightweight, Super Featherweight, Featherweight, Super Bantamweight, Bantamweight, Flyweight

Organizations: WBC, WBA, IBF, WBO, MEDIA (5 sets of rankings)

---

## PAGE 15 — BLOG / ARTICLE INVENTORY
**Route:** `GET /admin/blog`  
**Controller:** `Admin\BlogController::index()`

### Header
- Title: **"ARTICLE INVENTORY"**
- Subtitle: "Manage and curate your platform's editorial content"
- Button: **"+ CREATE NEW ARTICLE"** (red) → `GET /admin/blog/new`

### Filter Bar
- Search input: "Search article title or author..."
- **All Categories** dropdown (pulls from `BlogCategory`)
- **All Statuses** dropdown (PUBLISHED / DRAFT / ARCHIVED)
- **"APPLY FILTERS"** button (dark)

### Article Table
**Columns:** HEADLINE & CONTENT | CATEGORY | ENGAGEMENT | STATUS | RELEASE DATE | ACTIONS

| Column | Detail |
|---|---|
| HEADLINE & CONTENT | Article title (bold) + author name (gray below) |
| CATEGORY | Colored badge pill (e.g., "Story" blue, "Analysis" red) |
| ENGAGEMENT | Number with book icon (view/like count) |
| STATUS | "PUBLISHED" (green), "DRAFT" (amber yellow) |
| RELEASE DATE | Date (e.g., Apr 17, 2026) |
| ACTIONS | Pencil edit + red trash delete |

### Create/Edit Article Form (`/admin/blog/new` or `/admin/blog/{id}/edit`)
Built with Symfony form (`BlogArticleType`):
- Title field
- Content (rich text / textarea)
- Category dropdown (linked to `BlogCategory`)
- Status dropdown (DRAFT / PUBLISHED)
- Cover image upload (optional)
- Release date

---

## PAGE 16 — EVENT BOOKINGS (Admin)
**Route:** `GET /admin/bookings`  
**Controller:** `Admin\BookingAdminController::index()`

### Header
- Title: **"EVENT BOOKINGS"** (inferred)
- Stats bar: Total bookings | Revenue | Confirmed count | Cancelled count

### Filter Bar
- **All Events** dropdown (all events from DB)
- **All Status** dropdown (CONFIRMED / CANCELLED / PENDING)
- Filter button

### Bookings Table (paginated, 15 per page)
**Columns:** USER | EVENT | TYPE | EMAIL | AMOUNT | QTY | STATUS | ACTIONS

| Column | Detail |
|---|---|
| USER | Fan name |
| EVENT | Event name |
| TYPE | Ticket type badge (VIP RINGSIDE, PREMIUM LOWER, REGULAR SEATING, BALCONY, STANDING ROOM) |
| EMAIL | User's email |
| AMOUNT | `$XX.XX` |
| QTY | Integer |
| STATUS | "CONFIRMED" (green), "CANCELLED" (red) |
| ACTIONS | QR icon, Cancel button |

**QR icon** → `GET /admin/bookings/{id}/qr` — renders a QR code PNG (250×250) encoding booking details  
**Cancel button** → `POST /admin/bookings/{id}/cancel` — calls `BookingService::cancelBooking()`

### Create Booking (Admin) (`/admin/bookings/create`)
Manual booking creation:
- Event dropdown
- User dropdown
- Ticket Type dropdown (5 types with prices)
- Quantity
Submit → `BookingService::createBooking()`

### Ticket Types and Prices:
- VIP_RINGSIDE
- PREMIUM_LOWER
- REGULAR_SEATING
- BALCONY
- STANDING_ROOM

---

## PAGE 17 — FAN REACTIONS
**Route:** `GET /admin/reactions`  
**Controller:** `Admin\ReactionController::index()`

### Header
- Title: **"FAN REACTIONS"**
- Subtitle: "X reactions" (live count)

### Filter Bar
- Search input
- **All Fights** dropdown (all fight results)
- **All Types** dropdown (reaction types: FIRE, LOVE, SHOCK, etc.)
- **All** (moderation status) dropdown
- Filter button

### Reaction Cards (grid, 3–4 per row)
Each card (dark):
- **User name** (bold) + timestamp (gray)
- **Fight context**: "fight name" label
- **Reaction type badge**: e.g., "FIRE" (orange), "LOVE" (pink), etc.
- **Reaction text** body (user's written comment)
- **Pin toggle button**: bookmark/pin icon → `POST /admin/reactions/{id}/pin`
  - If pinned, card gets a gold border or "PINNED" badge
- **Delete button** (red X) → `POST /admin/reactions/{id}/delete` (soft delete: `setIsDeleted(true)`)

---

## PAGE 18 — VOTING POOL ADMIN (Match Proposals)
**Route:** `GET /admin/proposals`  
**Controller:** `Admin\MatchProposalAdminController::index()`

### Header
- Title: **"VOTING POOL MANAGEMENT"**
- Subtitle: "Select matchups for fans to vote on. High-voted bouts appear on the Community Card."
- Button: **"AI GENERATE PROPOSALS"** (dark/blue outline) → `POST /api/ai/generate-proposals` (generates 5 AI proposals and saves them)
- Button: **"ADD TO POOL"** (red) → `GET /admin/proposals/create`

### Proposal List (sorted by vote count DESC)
Each proposal row (card-style):
- **Fight # badge** (sequential number)
- **Fighter 1 block**: photo thumbnail, name, weight class, ELO rating badge
- **"VS"** separator (red, bold)
- **Fighter 2 block**: photo thumbnail, name, weight class, ELO rating badge
- **Added on date** (gray, small)
- **Status badge**: "PENDING" (amber), "APPROVED" (green), "REJECTED" (gray)
- **Vote count** (when > 0, shown as number with upvote icon)
- **Compatibility score** (percentage shown if set)
- **Action buttons**:
  - Approve (green checkmark) → `POST /admin/proposals/{id}/approve`
  - Reject (red X) → `POST /admin/proposals/{id}/reject`
  - Delete (trash) → soft delete or hard delete

### Create Proposal Form (`/admin/proposals/create`)
Step 1: Select weight class (dropdown)  
Step 2: Select Fighter 1 and Fighter 2 (filtered by weight class, sorted by ELO)  
Validation: Fighters must be different.  
Auto-calculates compatibility = `100 - (ELO diff / 10)`

### AI Generate Proposals Flow
Button triggers `POST /api/ai/generate-proposals` with `{"count": 5}`  
Server calls `MatchmakingService::generateProposalsLocal(5)` — 8-vector scoring:
1. Weight integrity (25 pts)
2. ELO parity (20 pts)
3. Record parity (15 pts)
4. Height/reach balance (15 pts)
5. Lethality (10 pts)
6. Precision (10 pts)
7. Style diversity bonus (5 pts)

Result: Creates `MatchProposal` entities with status=PENDING, compatibility score, and notes.

---

## PAGE 19 — FIGHTER PURSES (Contracts)
**Route:** `GET /admin/contracts`  
**Controller:** `Admin\AdminContractController::index()`

### Header
- Title: **"FIGHTER PURSES"** (red/orange, large)
- Subtitle: "Managing BO contract agreements for the 2026 season"
- Live indicator: **"LIVE PAYROLL ENABLED"** (animated red dot + label)
- Button: **"NEW ASSESSMENT"** (red) → opens create contract inline form
- Button: **"Audit Logs"** (dark outline)

### Stats Bar (4 counters)
- Total Payouts: sum of all paid contracts
- Pending Approval: count of unpaid contracts
- Fully Committed: `X Active` (count of active contracts)
- (Optional: Total Committed amount)

### Filter Bar
- Search input: "Search boxer or event..."
- **Status All** dropdown (Active / Inactive / Paid / Unpaid)
- **All Pay** / **All Due** toggles

### Contracts Table
**Columns:** FIGHTER IDENTITY | SANCTIONED EVENT | BASE PAY | WIN BONUS | STATUS | TOTAL PURSE | ACTIONS

| Column | Detail |
|---|---|
| FIGHTER IDENTITY | Fighter photo (small) + name + AI style tag badge |
| SANCTIONED EVENT | Event name + date (gray) |
| BASE PAY | `$100` etc. |
| WIN BONUS | `+$100` (green, shows bonus earned if won) |
| STATUS | "Active" (green badge) |
| TOTAL PURSE | Calculated: base + (won → +win bonus) |
| ACTIONS | PDF icon, missed-weight toggle, delete (red) |

**PDF icon** → `GET /admin/contracts/{id}/pdf` — generates PDF contract via DomPDF  
**Missed-weight toggle** → `POST /admin/contracts/{id}/toggle-weight` — marks fighter as missing weight (affects payout calculation)  
**Delete** → `POST /admin/contracts/{id}/delete`  
**Pay button** (when fight completed) → `POST /admin/contracts/{id}/pay` — marks as PAID

### Contract Payout Logic
`ContractService::calculateFinalPurse(contract, fightResult)`:
- Winner gets base_pay + win_bonus
- Loser gets base_pay only
- Missed weight: partial deduction

### Create Contract (inline form or modal)
Fields: Event (dropdown), Fighter (dropdown — filtered by fighters in that event), Base Pay ($), Win Bonus ($)  
Validation: No duplicate contract for same fighter+event.

### PDF Export List
**"Audit Logs" / PDF List** → `GET /admin/contracts/pdf-list` — generates a full PDF list of all contracts.

---

## PAGE 20 — FINANCE DEPARTMENT
**Route:** `GET /admin/finance`  
**Controller:** `Admin\FinanceAdminController::index()`

### Header
- Title: **"FINANCE DEPARTMENT"** (white, large, bold)
- Subtitle: "Total Revenue & Financial Analytics Platform"
- Button: **"EXPORT BILAN"** (gray outline, top right) — exports financial data

### KPI Stat Cards (top row, 4 cards)
| Card | Value | Color |
|---|---|---|
| TOTAL REVENUE | `$1,171.68` | Green (upward arrow) |
| TICKETS SOLD | `8` | Blue |
| AVG TICKET PRICE | `$146.46` | Orange |
| REFUND RATE | `0.0%` | Green |

Data from: `BookingRepository::getAdminStats()`

### Revenue Trend Chart (Last 30 Days)
- Line chart, dark bg, red/pink smooth line
- X-axis: dates (30 days)
- Y-axis: revenue amount ($)
- Data: raw SQL query on `event_booking` table, last 30 days daily totals

### Daily Distribution Chart
- Bar or area chart showing booking volume by day
- Uses same 30-day data

### Revenue by Fight Type (Pie/Donut Chart)
- Segments: each ticket type (VIP RINGSIDE, PREMIUM LOWER, REGULAR, BALCONY, STANDING)
- Center shows: TOTAL REVENUE $X,XXX
- Data from: SQL GROUP BY ticket_type

### Financial Bilan Table (Bilan Par Événement)
Scrollable table at bottom:

**Columns:** EVENT | BOOKINGS | REVENUE | PERFORMANCE | STATUS

| Column | Detail |
|---|---|
| EVENT | Event name + organizer |
| BOOKINGS | Count of confirmed bookings |
| REVENUE | Total revenue `$XXX.XX` |
| PERFORMANCE | Red horizontal progress bar (relative to max revenue event) |
| STATUS | "REVENUE POSITIVE" (green), "REVENUE NEGATIVE" (red) |

Paginated (5–10 per page).

---

## PAGE 21 — AI INJURY PREDICTIONS
**Route:** `GET /performance/injury-predictions`  
**Controller:** `PerformanceController::injuryPredictions()`

### Header
- Section title: **"INJURY RISK ALGORITHM"**
- Three algorithm factor cards (explanation of the algorithm):
  - **AGITATION**: "Eliminates over 1.4x scenarios with exponentially higher enthalpies" (formula display)
  - **COMBINATOR**: "This indicator clearly shows when the neural networks coincide with cumulative stress"
  - **TRANSMITTER**: "This function: risk = AFGT/TKO × 0.627tho is weighted heavily on neurological risk"

### Top 3 At-Risk Fighter Cards (horizontal, 3 cards)
Each card (dark, red border if high risk):
- Fighter photo
- Fighter name (bold, white)
- Weight class
- **RISK %**: large percentage number
- **Risk Level bar**: gradient bar (green → amber → red)
- **WIN STAGE**: percentage mini-bar
- **TITLE STAGE**: percentage mini-bar
- Stats row: Accuracy % | Days Alert | ROI %
- Button: **"DOWNLOAD PDF REPORT"** (red) → `GET /performance/injury-pdf/{id}`

### Full Roster Risk Analysis Table
Below the top 3 cards, a paginated table:

**Columns:** FIGHTER | RISK % | LEVEL | ZONE | ALERT | ACTION

| Column | Detail |
|---|---|
| FIGHTER | Name + style tag (small) |
| RISK % | Percentage |
| LEVEL | Colored badge: HIGH (red), MEDIUM (orange/amber), LOW (green) |
| ZONE | Body part (e.g., "Rib Cage", "Right Shoulder", "9th Page") |
| ALERT | "No" (green badge) or "Yes" (red badge, days countdown) |
| ACTION | Download PDF icon → per-fighter PDF |

---

## PAGE 22 — PREDICTIONS CENTER (Admin Read-Only View)
**Route:** `GET /predictions`  
**Controller:** `PredictionController` (or similar)

### Warning Banner
Blue information box at top:
> "You are viewing the Prediction Center as an Administrator. You can see upcoming fights and community predictions, but cannot submit predictions yourself"

### Your Recent History (right sidebar)
- "No completed predictions yet." (for admin accounts that haven't predicted)

### Open For Predictions Section
Each upcoming fight shown as a card:

**Fight Card:**
- Event name + date (top, gray)
- Status badge: "PENDING" (amber pill)
- Fighter 1 photo + name + record (W-L)
- "VS" separator (bold)
- Fighter 2 photo + name + record
- **AI Win Probability bar**: dual-color horizontal bar (red=F1 %, blue=F2 %)
  - Shows percentages on each side (e.g., "31.2%" vs "42.8%")
  - Calls `AIService::analyzeFightDynamics()` or heuristic
- **Method of Victory** dropdown: `Decision (UD/SD/MD)` / `KO` / `TKO` etc.
- **Predicted Round** dropdown: `N/A (Decision)` or round numbers
- Button: **"Submit — Read-Only Mode"** (grayed out, disabled for admin)

Multiple fight cards stacked vertically.

---

## PAGE 23 — USER PROFILE
**Route:** `GET/POST /users/profile`  
**Controller:** `UserController::profile()`

### Layout
Centered card, dark background:

### Avatar
- Large circular avatar (~80–100px), dark bg with first letter of username
- Username displayed large below
- Subtitle: "ID #X — ROLE: [ROLE]"

### Section A — Personal Details
- **USERNAME** label + current value (gray) + editable input
- **EMAIL ADDRESS** label + current value + editable input
- Button: **"UPDATE IDENTITY"** (red) → `POST /users/profile` with `action=update_info`

### Section B — Security Protocol
- **NEW PASSWORD** input (type=password)
- Helper text: "Leave blank to keep current — Minimum 8 characters with specialized symbols recommended"
- Button: **"UPDATE PASSCODE"** (red) → `POST /users/profile` with `action=change_password`

### Section C — Biometric Authentication
- Card: **"FACE ID / BIOMETRIC LOGIN"**
- Description: "Secure your account with hardware-backed biometric authentication"
- Button: **"ACTIVATE FACE ID"** (red) → triggers WebAuthn registration flow
- Button: **"CAMERA SCAN"** (dark outline) → live camera face verification (demo mode)

---

## ADDITIONAL AI ENDPOINTS (accessible from multiple pages)

All require ROLE_ADMIN. All are AJAX/JSON, not page loads.

| Route | Purpose | Triggered From |
|---|---|---|
| `POST /api/ai/stat-suggestions` | Suggest per-round stats for a fight | Stats entry form |
| `POST /api/ai/matchmaking-suggestions` | AI matchmaking via DeepSeek | Event fight card |
| `POST /api/ai/generate-proposals` | Local 8-vector match proposals | Voting pool page |
| `POST /api/ai/simulate` | Pre-fight win probability | Fight entry / proposal page |
| `POST /api/ai/scouting-report` | Tactical scouting breakdown | Performance page |
| `POST /api/ai/post-fight-recap` | Journalistic fight recap | Stats detail page |
| `POST /api/ai/compare-result` | Predicted vs actual comparison | Results page |
| `POST /api/ai/predict-injury` | Biomechanical injury risk | Boxer roster / injury page |

---

## GLOBAL SEARCH
**Route:** `GET /search?q=...`  
**Controller:** `SearchController`

Triggered from top navbar search bar.  
Searches across: Fighters, Events, Fight Results.  
Returns a results page grouped by type.

---

## NOTIFICATIONS
**Route:** `GET /notifications` (fan-facing but visible in admin nav)

- Notifications created automatically when: fight results are entered, rankings update
- `NotificationService::notifyAllFans()` creates `Notification` entities for every user
- Admin sees all notifications in the sidebar bell icon

---

## DATABASE ENTITIES — JavaFX Model Mirror

| Entity | Key Fields | Java Class |
|---|---|---|
| `User` | userId, username, password(bcrypt), email, webauthnCredentialId | `User.java` |
| `Fighter` | fighterId, firstName, lastName, nickname, wins, losses, draws, koWins, eloRating, performanceScore, height, reach, nationality, photoFilename, aiStyleTag, aiDescription, weightDivision→, manager→ | `Fighter.java` |
| `WeightDivision` | id, name | `WeightDivision.java` |
| `Event` | eventId, eventName, eventDate, venue, city, organization, status | `Event.java` |
| `FightResult` | resultId, event→, fighter1→, fighter2→, winner→, fightNumber, methodOfVictory, decisionType, roundNumber, scheduledRounds, status, fightDate, insideTheNumbers, isBeltFight | `FightResult.java` |
| `FightStatistic` | id, fightResult→, fighter→, round, punchesThrown, punchesLanded, powerPunchesThrown, powerPunchesLanded, jabsThrown, jabsLanded, rightHandThrown, rightHandLanded, leftHandThrown, leftHandLanded, bodyShotsLanded, knockdowns, commentary | `FightStatistic.java` |
| `Ranking` | id, fighter→, weightDivision→, organization, rankPosition, eloRating, performanceScore, isChampion | `Ranking.java` |
| `MatchProposal` | id, fighter1→, fighter2→, status(PENDING/APPROVED/REJECTED), voteCount, compatibility, notes, proposedAt, event→, weightDivision→ | `MatchProposal.java` |
| `EventBooking` | id, user→, event→, ticketType, quantity, totalPrice, bookingStatus, bookingDate | `EventBooking.java` |
| `FighterContract` | id, fighter→, event→, basePay, winBonus, isPaid, isMissedWeight, calculatedPayout | `FighterContract.java` |
| `BlogArticle` | id, title, content, author, category→, status, releaseDate | `BlogArticle.java` |
| `BlogCategory` | id, name | `BlogCategory.java` |
| `FanReaction` | id, user→, fightResult→, reactionType, content, isPinned, isDeleted, createdAt | `FanReaction.java` |
| `Notification` | id, user→, message, isRead, createdAt | `Notification.java` |
| `Prediction` | id, user→, fightResult→, predictedWinner→, predictedMethod, predictedRound, pointsEarned | `Prediction.java` |
| `PerformanceScore` | id, fighter→, scoreValue, calculatedAt | `PerformanceScore.java` |

---

## JAVAFX IMPLEMENTATION NOTES

### Screen Navigation Pattern
Use a single `Stage` with a `BorderPane`:
- **Left**: Sidebar `VBox` with nav buttons — always visible after login
- **Top**: Top navbar `HBox` — always visible after login
- **Center**: Swappable `Pane` — load different FXML per page

### Sidebar Active State
Keep a reference to the currently active button. On navigation, remove "active" CSS class from previous, add to new.

### Dark Theme CSS Approach
Create `smartfight.css` using JavaFX CSS variables:
```css
.root { -fx-background-color: #0d0d0d; }
.card { -fx-background-color: #1a1a1a; -fx-border-color: #2d2d2d; -fx-border-radius: 10; }
.btn-primary { -fx-background-color: #dc2626; -fx-text-fill: white; }
.badge-admin { -fx-background-color: #dc2626; -fx-text-fill: white; }
.badge-success { -fx-background-color: #16a34a; -fx-text-fill: white; }
.badge-pending { -fx-background-color: #f59e0b; -fx-text-fill: black; }
```

### Charts (JavaFX)
- Line charts: `LineChart<String, Number>` with dark styling
- Bar charts: `BarChart<String, Number>`
- Pie charts: `PieChart`
- Style via CSS to match dark theme

### Tables (JavaFX)
Use `TableView<T>` with `TableColumn<T, ?>` for all list pages.  
Add custom cell factories for colored badge columns.

### Pagination
Use JavaFX `Pagination` control or manual page buttons with DAO `LIMIT/OFFSET`.

### File Uploads (Fighter Photos)
Use `FileChooser` → copy to a local `uploads/boxers/` directory → store filename in DB.

### QR Code Generation
Use ZXing: `QRCodeWriter` → encode booking/fighter URL → display as `ImageView` in JavaFX.
