# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**SmartFight** is a UFC/MMA web application template with two distinct interfaces — a public-facing FrontOffice and an admin BackOffice. This is a static HTML/CSS/JS template (no build system, no package manager, no server).

The project is part of a PIDEV 3A 2025-2026 student project. A companion SQL database schema (`smartfight (1).sql`) defines the backend data model.

## Structure

```
template/
├── FrontOffice/        # Public-facing user interface
│   ├── index.html      # Homepage / hero
│   ├── matches.html    # Events listing
│   ├── team.html       # Fighters listing
│   ├── team-details.html
│   ├── match-details.html
│   ├── live-stream.html
│   ├── predictions.html
│   ├── booking.html
│   ├── leaderboard.html
│   ├── blog.html / blog-details.html
│   ├── gallery.html
│   ├── notifications.html
│   ├── contact.html
│   ├── css/
│   │   ├── smartfight.css   # Design tokens / CSS variables (source of truth for brand colors)
│   │   └── style.css        # Main stylesheet (imports smartfight.css)
│   └── js/main.js           # Countdown timers, tab switching, filter logic
│
└── BackOffice/         # Admin dashboard
    ├── dashboard.html  # Main admin entry point
    ├── css/
    │   └── smartfight-admin.css  # Tabler overrides for dark theme
    ├── users/          # index.html, form.html, show.html
    ├── fighters/       # index.html, form.html, show.html
    ├── events/         # index.html, form.html, show.html
    ├── bookings/       # index.html, show.html
    ├── fight-results/  # index.html, form.html, show.html
    ├── blog/           # index.html, form.html, show.html
    ├── predictions/    # index.html, score.html
    ├── reactions/      # index.html
    └── notifications/  # index.html, broadcast.html
```

## Tech Stack

**FrontOffice:**
- Bootstrap 5.3.3 (CDN)
- Font Awesome 6.5.1 (CDN)
- Google Fonts: Oswald (headings) + Inter (body)
- Vanilla JS (no framework)

**BackOffice:**
- Tabler UI v1.0.0-beta17 (CDN) — Bootstrap-based admin framework
- Font Awesome 6.5.0 (CDN)
- Dark theme forced via `data-bs-theme="dark"` on `<html>`

**No build step** — open HTML files directly in a browser or serve with any static file server (e.g., `npx serve .` or VS Code Live Server).

## Design System

All brand tokens live in `FrontOffice/css/smartfight.css`. The admin overrides them in `BackOffice/css/smartfight-admin.css`. Key values:

| Token | Value | Use |
|---|---|---|
| `--primary` | `#dc2626` | Red — primary actions, CTAs |
| `--gold` | `#c9a227` | Gold — rankings, achievements |
| `--bg-dark` | `#09090b` | Page background |
| `--bg-surface` | `#18181b` | Card / panel background |
| `--border-color` | `#27272a` | Dividers, card borders |

Headings use `font-family: Oswald` (uppercase). Body uses `Inter`.

## Sidebar Pattern (BackOffice)

The sidebar is **duplicated inline** in every BackOffice page — there is no include/template system. When adding a new admin module or changing navigation, update the sidebar in every affected HTML file. The active link is indicated by adding `class="nav-link active"` to the current page's `<a>` tag.

## Page Patterns

Each BackOffice module follows a 3-page pattern:
- `index.html` — data table with search/filter and action buttons
- `form.html` — create/edit form (shared for both operations)
- `show.html` — read-only detail view

Exceptions: `bookings` has no form (read-only + status actions), `predictions` has `score.html` instead of `form.html`, `notifications` has `broadcast.html`, `reactions` is index-only.

## JavaScript

`FrontOffice/js/main.js` is the only JS file. It handles:
- **Countdown timers** — reads `data-event-date="YYYY-MM-DD"` attributes on `.sf-countdown` elements; fires at 20:00 event time
- **Tab/filter UI** — fighter category tabs, fight card tabs, prediction filter buttons

The BackOffice has no custom JS — relies entirely on Tabler's built-in Bootstrap JS (loaded from CDN).
