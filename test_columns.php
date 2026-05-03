<?php
$pdo = new PDO('mysql:host=127.0.0.1;dbname=smartfight', 'root', '');
$stmt = $pdo->query('SHOW COLUMNS FROM user_roles');
print_r($stmt->fetchAll(PDO::FETCH_ASSOC));
