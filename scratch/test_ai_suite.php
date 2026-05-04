<?php
require __DIR__ . '/../vendor/autoload.php';

use App\Kernel;
use Symfony\Component\Dotenv\Dotenv;

(new Dotenv())->bootEnv(__DIR__ . '/../.env');

$kernel = new Kernel('dev', true);
$kernel->boot();

$container = $kernel->getContainer();
$aiService = $container->get('App\Service\AIService');

echo "AIService loaded successfully. Checking methods...\n";

if (method_exists($aiService, 'simulateFight')) echo "[OK] simulateFight exists\n";
if (method_exists($aiService, 'generatePostFightRecap')) echo "[OK] generatePostFightRecap exists\n";
if (method_exists($aiService, 'generateScoutingReport')) echo "[OK] generateScoutingReport exists\n";

echo "All good!\n";
