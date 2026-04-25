<?php

namespace App\Service;

use App\Entity\EventBooking;
use Endroid\QrCode\Builder\Builder;
use Endroid\QrCode\Encoding\Encoding;
use Endroid\QrCode\ErrorCorrectionLevel;
use Endroid\QrCode\Writer\PngWriter;
use Endroid\QrCode\Writer\SvgWriter;
use Symfony\Component\HttpFoundation\Response;

class QrCodeService
{
    public function __construct() {}

    public function buildFanQrData(EventBooking $booking): string
    {
        return sprintf(
            'SMARTFIGHT|REF:%s|EVENT:%d|USER:%d|TYPE:%s|QTY:%d',
            $booking->getBookingReference(),
            $booking->getEvent()->getId(),
            $booking->getUser()->getId(),
            $booking->getTicketType(),
            $booking->getTicketQuantity()
        );
    }

    public function buildAdminQrData(EventBooking $booking): string
    {
        $venueCity = $booking->getEvent()->getCity() ?? 'Unknown';

        return sprintf(
            "SMARTFIGHT TICKET\nRef: %s\nEvent: %s\nDate: %s\nVenue: %s\nFan: %s\nType: %s\nQty: %d\nTotal: %s TND",
            $booking->getBookingReference(),
            $booking->getEvent()->getName(),
            $booking->getEvent()->getStartsAt()?->format('Y-m-d') ?? '—',
            $venueCity,
            $booking->getUser()->getFullName(),
            $booking->getTicketType(),
            $booking->getTicketQuantity(),
            $booking->getTotalPrice()
        );
    }

    public function generateQrPng(string $data, int $size = 250): string
    {
        $result = Builder::create()
            ->writer(extension_loaded('gd') ? new PngWriter() : new SvgWriter())
            ->writerOptions([])
            ->data($data)
            ->encoding(new Encoding('UTF-8'))
            ->errorCorrectionLevel(ErrorCorrectionLevel::Low)
            ->size($size)
            ->margin(10)
            ->build();

        return $result->getString();
    }

    public function generateQrResponse(string $data, int $size = 250): Response
    {
        $result = Builder::create()
            ->writer(extension_loaded('gd') ? new PngWriter() : new SvgWriter())
            ->writerOptions([])
            ->data($data)
            ->encoding(new Encoding('UTF-8'))
            ->errorCorrectionLevel(ErrorCorrectionLevel::Low)
            ->size($size)
            ->margin(10)
            ->build();

        return new Response(
            $result->getString(),
            Response::HTTP_OK,
            ['Content-Type' => $result->getMimeType()]
        );
    }
}
