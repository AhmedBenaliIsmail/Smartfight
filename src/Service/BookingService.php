<?php

namespace App\Service;

use App\Entity\Event;
use App\Entity\EventBooking;
use App\Entity\User;
use App\Repository\EventBookingRepository;
use App\Repository\FightResultRepository;
use App\Repository\MatchProposalRepository;
use App\Repository\UserRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\Mailer\MailerInterface;
use Symfony\Bridge\Twig\Mime\TemplatedEmail;
use Symfony\Component\Mime\Email;

use App\Service\QrCodeService;

class BookingService
{
    public const PRICE_VIP_RINGSIDE = 200.00;
    public const PRICE_PREMIUM_LOWER = 120.00;
    public const PRICE_REGULAR_SEATING = 75.00;
    public const PRICE_BALCONY = 50.00;
    public const PRICE_STANDING_ROOM = 30.00;

    public function __construct(
        private EntityManagerInterface $em,
        private EventBookingRepository $bookingRepository,
        private NotificationService $notificationService,
        private FightResultRepository $fightResultRepo,
        private MatchProposalRepository $matchProposalRepo,
        private MailerInterface $mailer,
        private QrCodeService $qrCodeService,
        private UserRepository $userRepo,
    ) {}

    public function createBooking(User $user, Event $event, string $ticketType, int $quantity): EventBooking
    {
        $ticketType = strtoupper($ticketType);
        $validTypes = [
            EventBooking::TYPE_VIP_RINGSIDE,
            EventBooking::TYPE_PREMIUM_LOWER,
            EventBooking::TYPE_REGULAR_SEATING,
            EventBooking::TYPE_BALCONY,
            EventBooking::TYPE_STANDING_ROOM,
        ];

        if (!in_array($ticketType, $validTypes, true)) {
            throw new \InvalidArgumentException('Invalid ticket type.');
        }

        if ($quantity < 1 || $quantity > 4) {
            throw new \InvalidArgumentException('Quantity must be between 1 and 4.');
        }

        // Allow admins to book any event even if it's draft/private. Regular booking checks handled in controller.
        
        $existing = $this->bookingRepository->findOneByEventAndUser((int) $event->getId(), (int) $user->getUserId());
        if ($existing && $existing->isConfirmed()) {
            throw new \RuntimeException('You already have a confirmed booking for this event.');
        }

        $confirmedQty = $this->bookingRepository->getTotalConfirmedQty((int) $event->getId());
        $remaining = $event->getCapacity() - $confirmedQty;

        if ($quantity > $remaining) {
            throw new \RuntimeException('Not enough seats remaining for this event.');
        }

        $priceInfo = $this->getDynamicPricingForEvent($event, $user);
        $finalUnitPrice = $priceInfo['prices'][$ticketType];

        $booking = $existing ?? new EventBooking();
        $booking->setEvent($event);
        $booking->setUser($user);
        $booking->setTicketType($ticketType);
        $booking->setTicketQuantity($quantity);
        $booking->setBookingStatus(EventBooking::STATUS_CONFIRMED);
        $booking->setBookingDate(new \DateTime());
        $booking->setTotalPrice(number_format($finalUnitPrice * $quantity, 2, '.', ''));
        $booking->setBookingReference('SF-' . strtoupper(bin2hex(random_bytes(4))));

        $this->em->persist($booking);
        $this->em->flush();

        $this->notificationService->notifyUser(
            $user,
            sprintf('Your booking for %s (Ref: %s) is confirmed!', $event->getName(), $booking->getBookingReference()),
            'BOOKING'
        );

        try {
            try {
                $qrSrc = $this->qrCodeService->generateQrPngBase64(
                    $this->qrCodeService->buildFanQrData($booking), 250
                );
            } catch (\Throwable $gdError) {
                // GD extension not available — fall back to external QR API
                $qrSrc = "https://api.qrserver.com/v1/create-qr-code/?size=250x250&data="
                    . urlencode($this->qrCodeService->buildFanQrData($booking));
            }

            $email = (new Email())
                ->from('mahdidaly24@gmail.com')
                ->to($user->getEmail())
                ->subject('Your SmartFight Ticket: ' . $event->getName())
                ->html(sprintf(
                    '<h1>&#127942; Booking Confirmed!</h1>
                     <p>Hello %s, your ticket is ready for <strong>%s</strong>.</p>
                     <div style="background:#f4f4f4;padding:20px;border-radius:10px;text-align:center;">
                        <h2>TICKET QR CODE</h2>
                        <img src="%s" alt="Ticket QR" style="width:250px;height:250px;border:10px solid white;background:white;display:block;margin:0 auto;">
                        <p><strong>Reference:</strong> %s</p>
                        <p><strong>Type:</strong> %s | <strong>Qty:</strong> %d</p>
                     </div>
                     <p>Show this QR code at the venue entrance.</p>',
                    htmlspecialchars($user->getUsername()),
                    htmlspecialchars($event->getName()),
                    $qrSrc,
                    htmlspecialchars($booking->getBookingReference()),
                    htmlspecialchars(str_replace('_', ' ', $booking->getTicketType())),
                    $booking->getTicketQuantity()
                ));

            $this->mailer->send($email);
        } catch (\Exception $e) {
            // Write to a dedicated log file we can definitely find
            file_put_contents('email_debug.log', '[' . date('Y-m-d H:i:s') . '] BOOKING EMAIL ERROR: ' . $e->getMessage() . PHP_EOL, FILE_APPEND);
        }

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

    public function getDynamicPricingForEvent(Event $event, ?User $user = null): array
    {
        $basePrices = [
            EventBooking::TYPE_VIP_RINGSIDE => self::PRICE_VIP_RINGSIDE,
            EventBooking::TYPE_PREMIUM_LOWER => self::PRICE_PREMIUM_LOWER,
            EventBooking::TYPE_REGULAR_SEATING => self::PRICE_REGULAR_SEATING,
            EventBooking::TYPE_BALCONY => self::PRICE_BALCONY,
            EventBooking::TYPE_STANDING_ROOM => self::PRICE_STANDING_ROOM,
        ];

        // 1. ELO Scaling
        $fights = $this->fightResultRepo->findByEvent((int) $event->getId());
        $totalElo = 0;
        $fighterCount = 0;
        
        foreach ($fights as $fight) {
            if ($fight->getFighter1()) {
                $totalElo += $fight->getFighter1()->getEloRating();
                $fighterCount++;
            }
            if ($fight->getFighter2()) {
                $totalElo += $fight->getFighter2()->getEloRating();
                $fighterCount++;
            }
        }
        
        $avgElo = $fighterCount > 0 ? ($totalElo / $fighterCount) : 1500;
        $eloMultiplier = $avgElo / 1500;
        
        // 2. Champions Event Factor
        $championsMultiplier = ($event->getOrganization() !== 'INDEPENDENT') ? 1.5 : 1.0;
        
        // 3. Fan Voted Discount
        $fanDiscountMultiplier = 1.0;
        $hasFanVote = false;
        
        foreach ($fights as $fight) {
            if ($fight->getFighter1() && $fight->getFighter2()) {
                $proposals = $this->matchProposalRepo->findBy([
                    'fighter1' => [$fight->getFighter1(), $fight->getFighter2()],
                    'fighter2' => [$fight->getFighter1(), $fight->getFighter2()],
                ]);
                
                foreach ($proposals as $p) {
                    if ($p->getVoteCount() > 0) {
                        $hasFanVote = true;
                        $fanDiscountMultiplier = 0.9; // 10% discount
                        break 2;
                    }
                }
            }
        }

        // 4. Loyalty Discount (Every 4th booking is discounted)
        $loyaltyMultiplier = 1.0;
        if ($user) {
            $pastCount = $this->bookingRepository->countConfirmedByUser((int) $user->getUserId());
            // If they have 3, 7, 11... confirmed bookings, the NEXT one is discounted
            if ($pastCount > 0 && ($pastCount + 1) % 4 === 0) {
                $loyaltyMultiplier = 0.9; // 10% discount
            }
        }

        $finalPrices = [];
        foreach ($basePrices as $type => $basePrice) {
            $finalPrices[$type] = round($basePrice * $eloMultiplier * $championsMultiplier * $fanDiscountMultiplier * $loyaltyMultiplier, 2);
        }
        
        // Construct explanation message
        $messages = [];
        if ($loyaltyMultiplier < 1.0) {
            $messages[] = "💎 Loyalty Reward: 10% discount applied! This is your 4th (or 8th, 12th...) booking. Thank you for your continued support!";
        }
        if ($championsMultiplier > 1.0) {
            $messages[] = "👑 Championship Premium: This is a recognized World Title Event.";
        }
        if ($eloMultiplier >= 1.2) {
            $messages[] = sprintf("🔥 Premium Pricing: This event features Elite-level fighters (Average ELO: %d).", round($avgElo));
        } elseif ($eloMultiplier <= 0.9) {
            $messages[] = sprintf("📉 Discount Pricing: Developing prospects showcase (Average ELO: %d).", round($avgElo));
        }
        if ($hasFanVote) {
            $messages[] = "💎 Fan Reward: 10% discount applied because the community voted for a matchup on this card!";
        }
        if (empty($messages)) {
            $messages[] = "🎟️ Standard base pricing applied.";
        }

        return [
            'prices' => $finalPrices,
            'messages' => $messages,
            'avgElo' => round($avgElo),
        ];
    }

    public function announceFanFavoriteEvent(Event $event, array $fights): void
    {
        $fans = $this->userRepo->findAll();
        $fightListHtml = "<ul>";
        foreach ($fights as $f) {
            $fightListHtml .= sprintf("<li><strong>%s vs %s</strong></li>", 
                $f->getFighter1()->getFullName(), 
                $f->getFighter2()->getFullName()
            );
        }
        $fightListHtml .= "</ul>";

        foreach ($fans as $fan) {
            if (!in_array('ROLE_USER', $fan->getRoles())) continue;

            try {
                $email = (new Email())
                    ->from('mahdidaly24@gmail.com')
                    ->to($fan->getEmail())
                    ->subject('🔥 FAN FAVORITE EVENT: ' . $event->getEventName())
                    ->html(sprintf(
                        '<h1>🏆 THE FANS HAVE SPOKEN!</h1>
                         <p>Hello %s, we have created an exclusive event based on your votes!</p>
                         <h3>THE FIGHT CARD:</h3>
                         %s
                         <div style="background:#dc2626;color:white;padding:20px;border-radius:10px;text-align:center;margin:20px 0;">
                            <h2>EXCLUSIVE 10%% DISCOUNT</h2>
                            <p>Use this event to claim your special fan-voter discount!</p>
                            <p><strong>BOOK NOW AND SAVE 10%% AUTOMATICALLY</strong></p>
                         </div>
                         <p>Don\'t miss out on the matches you wanted to see!</p>',
                        $fan->getUsername(),
                        $fightListHtml
                    ));
                $this->mailer->send($email);
            } catch (\Exception $e) {
                file_put_contents('email_debug.log', '[' . date('Y-m-d H:i:s') . '] ANNOUNCE EMAIL ERROR: ' . $e->getMessage() . PHP_EOL, FILE_APPEND);
            }
        }
        
        $this->notificationService->notifyFanFavoriteEvent($event->getEventName());
    }
}
