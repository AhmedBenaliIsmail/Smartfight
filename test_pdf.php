<?php
require 'vendor/autoload.php';

use Dompdf\Dompdf;
use Dompdf\Options;

$options = new Options();
$options->set('defaultFont', 'Helvetica');
$dompdf = new Dompdf($options);
$dompdf->loadHtml('<h1>Test</h1>');
$dompdf->render();
echo 'OK: ' . strlen($dompdf->output());
