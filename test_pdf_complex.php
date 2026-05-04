<?php
require __DIR__.'/vendor/autoload.php';

use Dompdf\Dompdf;
use Dompdf\Options;

$html = '<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Event Flyer</title>
    <style>
        body { font-family: "Helvetica", "Arial", sans-serif; margin: 0; padding: 0; background: #000; color: #fff; }
        .flyer-container { padding: 40px; text-align: center; border: 10px solid #dc2626; border-radius: 10px; margin: 20px; background: #18181b; }
        .flyer-header { font-size: 48px; color: #dc2626; text-transform: uppercase; margin-bottom: 10px; font-weight: bold; }
        .flyer-org { font-size: 24px; color: #fbbf24; margin-bottom: 30px; letter-spacing: 2px; }
        .flyer-details { font-size: 20px; margin-bottom: 20px; }
        .flyer-date { font-size: 28px; font-weight: bold; margin: 30px 0; color: #fff; }
        .flyer-venue { font-size: 22px; color: #a1a1aa; }
        .footer { margin-top: 50px; font-size: 14px; color: #52525b; border-top: 1px solid #3f3f46; padding-top: 20px; }
    </style>
</head>
<body>
    <div class="flyer-container">
        <div class="flyer-header">SmartFight 100</div>
        <div class="flyer-org">WBC SANCTIONED EVENT</div>
        <div class="flyer-date">Saturday, May 10th, 2026</div>
        <div class="flyer-venue">
            <div>Madison Square Garden</div>
            <div>New York</div>
        </div>
        <div class="footer">
            SmartFight Management &copy; 2026<br>
            Book your tickets at smartfight.tn
        </div>
    </div>
</body>
</html>';

$options = new Options();
$options->set('isHtml5ParserEnabled', true);
$options->set('isRemoteEnabled', false);

$dompdf = new Dompdf($options);
$dompdf->loadHtml($html);
$dompdf->setPaper('A4', 'portrait');

echo "Rendering complex HTML...\n";
$dompdf->render();
echo "Done.\n";
