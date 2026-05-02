<?php
// 1. Delete the files containing secrets
@unlink("smartfight console credentaials.txt");
@unlink("smartfight/.env");

// 2. Sanitize .env.example
$envExampleFile = ".env.example";
if (file_exists($envExampleFile)) {
    $content = file_get_contents($envExampleFile);
    $content = preg_replace("/GOOGLE_CLIENT_ID=.*/", "GOOGLE_CLIENT_ID=FAKE_ID", $content);
    $content = preg_replace("/GOOGLE_CLIENT_SECRET=.*/", "GOOGLE_CLIENT_SECRET=FAKE_SECRET", $content);
    file_put_contents($envExampleFile, $content);
}

// 3. Git operations: stage files, soft reset to squash history and destroy the commits with secrets
echo shell_exec("git add .");
echo shell_exec("git commit -m \"Mask secrets\"");
// Find the merge base between current branch and origin/main to safely squash
$base = trim(shell_exec("git merge-base HEAD origin/main"));
if (!$base) {
    $base = "origin/main";
}
echo shell_exec("git reset --soft " . escapeshellarg($base));

// 4. Commit the single clean squashed state
echo shell_exec("git commit -m \"Complete Version AHMEDAISSA\"");

// 5. Force push to the remote branch
echo shell_exec("git push origin AHMEDAISSA --force");
