<?php
// test_mail.php
require 'vendor/autoload.php';

use Symfony\Component\Mailer\Transport;
use Symfony\Component\Mailer\Mailer;
use Symfony\Component\Mime\Email;
use Symfony\Component\Dotenv\Dotenv;

$dotenv = new Dotenv();
$dotenv->load(__DIR__.'/.env');

$dsn = $_ENV['MAILER_DSN'];
echo "Testing DSN: $dsn\n";

try {
    $transport = Transport::fromDsn($dsn);
    $mailer = new Mailer($transport);

    $email = (new Email())
        ->from('mahdidaly24@gmail.com')
        ->to('mahdidaly24@gmail.com')
        ->subject('SmartFight SMTP Test')
        ->text('If you see this, your Gmail SMTP is working perfectly!');

    $mailer->send($email);
    echo "SUCCESS: Email sent to mahdidaly24@gmail.com!\n";
} catch (\Exception $e) {
    echo "ERROR: " . $e->getMessage() . "\n";
}
