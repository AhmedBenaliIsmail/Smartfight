<?php
require __DIR__.'/vendor/autoload.php';

use Dompdf\Dompdf;
use Dompdf\Options;

$options = new Options();
$options->set('isHtml5ParserEnabled', true);
$options->set('isRemoteEnabled', false);

$dompdf = new Dompdf($options);
$html = '<html><body><h1>Hello World</h1></body></html>';
$dompdf->loadHtml($html);
$dompdf->setPaper('A4', 'portrait');

echo "Rendering...\n";
$dompdf->render();
echo "Done.\n";
