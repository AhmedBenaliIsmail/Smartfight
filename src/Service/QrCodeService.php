<?php

namespace App\Service;

use App\Entity\EventBooking;
use Doctrine\DBAL\Connection;
use Endroid\QrCode\QrCode;
use Endroid\QrCode\Writer\SvgWriter;
use Endroid\QrCode\Encoding\Encoding;
use Endroid\QrCode\ErrorCorrectionLevel;

class QrCodeService
{
    public function __construct(private Connection $connection) {}

    public function buildFanQrData(EventBooking $booking): string
    {
        $event = $booking->getEvent();
        $location = ($event->getVenue() ?: 'TBA') . ', ' . ($event->getCity() ?: 'TBA');

        return sprintf(
            "SMARTFIGHT FAN TICKET\n-------------------\nEvent: %s\nDate: %s\nVenue: %s\n\nTicket Details\n-------------------\nFan: %s\nType: %s\nQty: %d\nRef: %s\nStatus: %s",
            $event->getName(),
            $event->getEventDate() ? $event->getEventDate()->format('d M Y') : 'N/A',
            $location,
            $booking->getUser()->getFullName(),
            str_replace('_', ' ', $booking->getTicketType()),
            $booking->getTicketQuantity(),
            $booking->getBookingReference(),
            $booking->getBookingStatus()
        );
    }

    public function generateQrBase64(string $data, int $size = 250): string
    {
        // Direct instantiation compatible with Endroid QR Code v5/v6
        $qrCode = new QrCode(
            data: $data,
            encoding: new Encoding('UTF-8'),
            errorCorrectionLevel: ErrorCorrectionLevel::Low,
            size: $size,
            margin: 10
        );

        $writer = new SvgWriter();
        $result = $writer->write($qrCode);

        return $result->getDataUri();
    }
}
