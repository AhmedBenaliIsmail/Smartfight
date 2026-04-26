# 🥊 SmartFight Project Reference Guide

This document provides a clear map of the project architecture and a post-mortem feedback report on recent technical resolutions.

## 📝 Technical Post-Mortem (What was wrong?)

### 1. FaceID Biometric Failure
*   **The Issue**: A variable naming conflict (`modelsReady` vs `modelsLoaded`) occurred during a performance optimization merge. This caused the UI to wait indefinitely for a "Ready" signal that never arrived.
*   **The Fix**: Synchronized the naming convention and implemented a **3.5-second Fallback Timer**. Now, even if the AI initialization is slow, the login button force-enables itself to prevent the user from being stuck.

### 2. Email Recovery Failure
*   **The Issue**: Port 465 (SSL) was being intermittently rejected by Gmail's security filters.
*   **The Fix**: Migrated the `MAILER_DSN` to **Port 587 (TLS)**. This is the modern industry standard for SMTP and ensures high deliverability to Gmail accounts.

### 3. Missing Account Feedback
*   **The Issue**: The system lacked specific feedback for non-existent emails, leading users to think the system was broken when they simply had no account.
*   **The Fix**: Added explicit "You don't have an account" and "Enter valid email" validations in the `SecurityController`.

---

## 🗺️ Project Architecture Map

To make finding files easy, follow this organizational scheme:

### 🎨 Frontend (Views & Assets)
*   `templates/security/`: Login, Register, Forgot Password, and Biometric logic.
*   `templates/front/`: The Fan Dashboard and Public-facing pages.
*   `templates/fighter/`: Boxer profiles and roster management.
*   `public/css/`: Custom styles (including the premium glassmorphism effects).
*   `public/js/`: Client-side scripts for AI and real-time updates.

### 🧠 Backend (Logic & Data)
*   `src/Controller/`: Request handlers (The "Traffic Control").
    *   `FaceIdController.php`: Handles all biometric handshakes.
    *   `SecurityController.php`: Handles Login, Register, and Recovery logic.
*   `src/Service/`: Advanced Business Logic (The "Brain").
    *   `AiService.php`: DeepSeek/AI analysis engine.
    *   `RankingService.php`: ELO calculation logic.
*   `src/Entity/`: Database structure (Fighters, Users, Fights).

### ⚙️ Configuration
*   `.env`: Global settings (Database, Mailer, AI Keys).
*   `config/`: Symfony system configuration (Routes, Security, Services).

---

## 🚀 Pro-Tips for Navigation
*   **Search (Ctrl+P)**: Type `sec cont` to jump to `SecurityController`.
*   **Styles**: All premium UI updates are unified in `public/css/smartfight-admin.css`.
*   **Logs**: Check `var/log/dev.log` if an email doesn't arrive or a page crashes.
