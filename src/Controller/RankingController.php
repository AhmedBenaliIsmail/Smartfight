<?php
namespace App\Controller;

use App\Service\RankingService;
use Dompdf\Dompdf;
use Dompdf\Options;
use Endroid\QrCode\Color\Color;
use Endroid\QrCode\QrCode;
use Endroid\QrCode\Writer\SvgWriter;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpFoundation\StreamedResponse;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\Routing\Generator\UrlGeneratorInterface;

#[Route('/rankings')]
class RankingController extends AbstractController
{
    // ─── Your LAN IP ────────────────────────────────────────────────────────
    private const LAN_IP   = '192.168.1.14';
    private const LAN_PORT = '8001';

    #[Route('', name: 'app_rankings')]
    public function index(Request $request, RankingService $rankingService): Response
    {
        $this->denyAccessUnlessGranted('ROLE_USER');

        $fighters = $rankingService->getRankedFighters();
        $q = strtolower(trim($request->query->get('q', '')));
        if ($q) {
            $fighters = array_filter($fighters, fn($f) => str_contains(strtolower($f->getFullName()), $q));
            $fighters = array_values($fighters);
        }

        $groupedRankings = [];
        foreach ($fighters as $f) {
            $wd = $f->getWeightDivision() ? $f->getWeightDivision()->getName() : 'Unclassified';
            $groupedRankings[$wd][] = $f;
        }

        $lastUpdated = new \DateTime();

        // QR → mobile-friendly public page (no login needed on phone)
        $rankingUrl = sprintf(
            'http://%s:%s%s',
            self::LAN_IP,
            self::LAN_PORT,
            $this->generateUrl('app_ranking_mobile')
        );
        $qrSvg = $this->buildQrSvg($rankingUrl);

        return $this->render('ranking/index.html.twig', [
            'groupedRankings' => $groupedRankings,
            'q'               => $request->query->get('q', ''),
            'lastUpdated'     => $lastUpdated,
            'qrSvg'           => $qrSvg,
            'rankingUrl'      => $rankingUrl,
        ]);
    }

    // ─── PUBLIC mobile view (no login — scanned from QR) ────────────────────
    #[Route('/mobile', name: 'app_ranking_mobile')]
    public function mobile(RankingService $rankingService): Response
    {
        // No denyAccessUnlessGranted here — intentionally public
        $fighters = $rankingService->getRankedFighters();

        $groupedRankings = [];
        foreach ($fighters as $f) {
            $wd = $f->getWeightDivision() ? $f->getWeightDivision()->getName() : 'Unclassified';
            $groupedRankings[$wd][] = $f;
        }

        $pdfUrl = sprintf(
            'http://%s:%s%s',
            self::LAN_IP,
            self::LAN_PORT,
            $this->generateUrl('app_ranking_pdf')
        );

        return $this->render('ranking/mobile.html.twig', [
            'groupedRankings' => $groupedRankings,
            'lastUpdated'     => new \DateTime(),
            'pdfUrl'          => $pdfUrl,
        ]);
    }

    // ─── PUBLIC PDF (no login — opened from mobile page or QR directly) ─────
    #[Route('/pdf', name: 'app_ranking_pdf')]
    public function qrPdf(RankingService $rankingService): Response
    {
        // No denyAccessUnlessGranted — intentionally public for mobile access
        $fighters = $rankingService->getRankedFighters();

        $groupedRankings = [];
        foreach ($fighters as $f) {
            $wd = $f->getWeightDivision() ? $f->getWeightDivision()->getName() : 'Unclassified';
            $groupedRankings[$wd][] = $f;
        }

        $html = $this->renderView('ranking/pdf.html.twig', [
            'groupedRankings' => $groupedRankings,
            'lastUpdated'     => new \DateTime(),
        ]);

        $options = new Options();
        $options->set('defaultFont', 'Helvetica');
        $options->setIsRemoteEnabled(true);

        $dompdf = new Dompdf($options);
        $dompdf->loadHtml($html);
        $dompdf->setPaper('A4', 'portrait');
        $dompdf->render();

        return new Response(
            $dompdf->output(),
            200,
            [
                'Content-Type'        => 'application/pdf',
                'Content-Disposition' => 'inline; filename="smartfight-rankings.pdf"',
            ]
        );
    }

    #[Route('/recalculate', name: 'app_ranking_recalc', methods: ['POST'])]
    public function recalculate(RankingService $rankingService): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        $rankingService->recomputeAllRankings();
        $this->addFlash('success', 'Global boxing rankings recalculated from match results!');
        return $this->redirectToRoute('app_rankings');
    }

    #[Route('/export', name: 'app_ranking_export')]
    public function export(RankingService $rankingService): StreamedResponse
    {
        $this->denyAccessUnlessGranted('ROLE_USER');
        $fighters = $rankingService->getRankedFighters();

        $response = new StreamedResponse(function () use ($fighters) {
            $out = fopen('php://output', 'w');
            fputcsv($out, ['Rank', 'Boxer', 'Division', 'ELO', 'Performance', 'Streak', 'SOS', 'Wins', 'Losses']);
            foreach ($fighters as $i => $f) {
                fputcsv($out, [
                    $i + 1, $f->getFullName(),
                    $f->getWeightDivision() ? $f->getWeightDivision()->getName() : 'N/A',
                    (int) $f->getEloRating(), (int) $f->getPerformanceScore(),
                    $f->getWinStreak(), (int) $f->getStrengthOfSchedule(),
                    $f->getWins(), $f->getLosses(),
                ]);
            }
            fclose($out);
        });

        $response->headers->set('Content-Type', 'text/csv');
        $response->headers->set('Content-Disposition', 'attachment; filename="boxing_rankings.csv"');
        return $response;
    }

    #[Route('/qr-download', name: 'app_ranking_qr_download')]
    public function qrDownload(): Response
    {
        $this->denyAccessUnlessGranted('ROLE_USER');
        $url = sprintf(
            'http://%s:%s%s',
            self::LAN_IP,
            self::LAN_PORT,
            $this->generateUrl('app_ranking_mobile')
        );
        $svg = $this->buildQrSvg($url);

        return new Response($svg, 200, [
            'Content-Type'        => 'image/svg+xml',
            'Content-Disposition' => 'attachment; filename="smartfight-rankings-qr.svg"',
        ]);
    }

    private function buildQrSvg(string $url): string
    {
        try {
            $writer = new SvgWriter();
            $qr     = new QrCode(
                data: $url,
                size: 200,
                margin: 10,
                foregroundColor: new Color(220, 38, 38),
                backgroundColor: new Color(9, 9, 11),
            );
            return $writer->write($qr)->getString();
        } catch (\Throwable) {
            return '';
        }
    }
}