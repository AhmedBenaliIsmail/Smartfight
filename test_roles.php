<?php
$pdo = new PDO('mysql:host=127.0.0.1;dbname=smartfight', 'root', '');
$stmt = $pdo->query('SELECT u.username, r.roleName FROM users u LEFT JOIN user_roles ur ON u.userId = ur.userId LEFT JOIN roles r ON ur.roleId = r.roleId');
print_r($stmt->fetchAll(PDO::FETCH_ASSOC));
