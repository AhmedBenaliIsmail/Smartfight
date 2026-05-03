<?php
// 1. Commit the final PDF fix
echo shell_exec("git add src/Controller/RankingController.php 2>&1");
echo shell_exec("git commit -m \"Fix QR code pointing to PDF\" 2>&1");

// 2. Switch to AHMEDDAISSA (double D)
echo shell_exec("git checkout -B AHMEDDAISSA 2>&1");

// 3. Force push the sanitized, final code to origin AHMEDDAISSA
echo shell_exec("git push -u origin AHMEDDAISSA --force 2>&1");

// 4. Delete the wrong remote branch AHMEDAISSA (single D)
echo shell_exec("git push origin --delete AHMEDAISSA 2>&1");

// 5. Delete the wrong local branch AHMEDAISSA
echo shell_exec("git branch -D AHMEDAISSA 2>&1");
