<?php
require __DIR__.'/vendor/autoload.php';

use Dompdf\Dompdf;
use Dompdf\Options;
use App\Kernel;
use Symfony\Component\Dotenv\Dotenv;

(new Dotenv())->bootEnv(__DIR__.'/.env');

$kernel = new Kernel('dev', true);
$kernel->boot();
$container = $kernel->getContainer();
$twig = $container->get('twig');
$em = $container->get('doctrine')->getManager();
$event = $em->getRepository(\App\Entity\Event::class)->find(1);

$html = $twig->render('event/flyer.html.twig', ['event' => $event]);

$options = new Options();
$options->set('isHtml5ParserEnabled', true);
$options->set('isRemoteEnabled', false);

$dompdf = new Dompdf($options);
$dompdf->loadHtml($html);
$dompdf->setPaper('A4', 'portrait');

echo "Rendering...\n";
$dompdf->render();
echo "Done.\n";
