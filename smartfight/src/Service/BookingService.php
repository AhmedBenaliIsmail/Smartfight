<?php

namespace App\Service;

use App\Entity\Event;
use App\Entity\EventBooking;
use App\Entity\User;
use App\Repository\EventBookingRepository;
use Doctrine\ORM\EntityManagerInterface;

class BookingService
{
    public const PRICE_VIP = 50.00;
    public const PRICE_REGULAR = 25.00;
    public const PRICE_STANDING = 10.00;

    public function __construct(
        private EntityManagerInterface $em,
        private EventBookingRepository $bookingRepository,
        private NotificationService $notificationService,
    ) {}

    public function createBooking(User $user, Event $event, string $ticketType, int $quantity): EventBooking
    {
        $ticketType = strtoupper($ticketType);

        if (!in_array($ticketType, [EventBooking::TYPE_VIP, EventBooking::TYPE_REGULAR, EventBooking::TYPE_STANDING], true)) {
            throw new \InvalidArgumentException('Invalid ticket type.');
        }

        if ($quantity < 1 || $quantity > 4) {
            throw new \InvalidArgumentException('Quantity must be between 1 and 4.');
        }

        if ($event->getStatus() !== 'SCHEDULED' || $event->getVisibility() !== 'PUBLIC') {
            throw new \RuntimeException('This event is not bookable.');
        }

        $existing = $this->bookingRepository->findOneByEventAndUser((int) $event->getId(), (int) $user->getId());
        if ($existing && $existing->isConfirmed()) {
            throw new \RuntimeException('You already have a confirmed booking for this event.');
        }

        $confirmedQty = $this->bookingRepository->getTotalConfirmedQty((int) $event->getId());
        $remaining = $event->getCapacity() - $confirmedQty;

        if ($quantity > $remaining) {
            throw new \RuntimeException('Not enough seats remaining for this event.');
        }

        $booking = $existing ?? new EventBooking();
        $booking->setEvent($event);
        $booking->setUser($user);
        $booking->setTicketType($ticketType);
        $booking->setTicketQuantity($quantity);
        $booking->setBookingStatus(EventBooking::STATUS_CONFIRMED);
        $booking->setBookingDate(new \DateTime());
        $booking->setTotalPrice(number_format($this->getPricePerTicket($ticketType) * $quantity, 2, '.', ''));
        $booking->setBookingReference('SF-' . strtoupper(bin2hex(random_bytes(4))));

        $this->em->persist($booking);
        $this->em->flush();

        $this->notificationService->sendToFan(
            $user,
            'ADMIN_BROADCAST',
            'Booking Confirmed',
            sprintf('Your booking for %s (Ref: %s) is confirmed!', $event->getName(), $booking->getBookingReference()),
            $event->getId()
        );

        return $booking;
    }

    public function cancelBooking(EventBooking $booking): void
    {
        if ($booking->isCancelled()) {
            return;
        }

        $booking->setBookingStatus(EventBooking::STATUS_CANCELLED);
        $this->em->flush();
    }

    public function getPricePerTicket(string $type): float
    {
        return match (strtoupper($type)) {
            EventBooking::TYPE_VIP => self::PRICE_VIP,
            EventBooking::TYPE_REGULAR => self::PRICE_REGULAR,
            EventBooking::TYPE_STANDING => self::PRICE_STANDING,
            default => throw new \InvalidArgumentException('Unsupported ticket type.'),
        };
    }
}
