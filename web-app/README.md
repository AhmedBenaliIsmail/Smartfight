# SmartFight — Web App (Symfony 6.4)

## Status
This folder will contain the Symfony web application for SmartFight.
It will be scaffolded manually using Composer (see commands below).

## Structure (to be created)
```
web-app/
├── src/
│   ├── Entity/          ← Doctrine entities (Event, Venue, Discipline)
│   ├── Repository/      ← Doctrine repositories
│   ├── Controller/
│   │   ├── Back/        ← BackOffice CRUD controllers
│   │   └── Front/       ← FrontOffice controllers
│   └── Form/            ← Symfony Form types
├── templates/
│   ├── back/event/      ← Twig templates for admin
│   └── front/event/     ← Twig templates for public
├── config/
│   └── packages/doctrine.yaml
├── .env                 ← DATABASE_URL=mysql://root:@127.0.0.1:3306/smartfight
└── composer.json
```

## Manual Setup Steps (Phase 5 — to be executed by you)

### THIS STEP MUST BE DONE BY YOU MANUALLY

1. Open a terminal in this `web-app/` folder
2. Run:
```bash
composer create-project symfony/skeleton:"6.4.*" .
composer require orm twig form validator
composer require symfony/orm-pack
composer require symfony/form symfony/validator
```
3. Configure `.env`:
```
DATABASE_URL="mysql://root:@127.0.0.1:3306/smartfight?serverVersion=8.0"
```
4. All entity and controller files will be generated in Phase 5.
