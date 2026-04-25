<?php

namespace App\EventListener;

use App\Service\EventStatusService;
use Symfony\Component\EventDispatcher\EventSubscriberInterface;
use Symfony\Component\HttpKernel\Event\RequestEvent;
use Symfony\Component\HttpKernel\KernelEvents;

class StatusRefreshListener implements EventSubscriberInterface
{
    public function __construct(private readonly EventStatusService $eventStatusService) {}

    public static function getSubscribedEvents(): array
    {
        return [KernelEvents::REQUEST => ['onKernelRequest', 8]];
    }

    public function onKernelRequest(RequestEvent $event): void
    {
        if (!$event->isMainRequest()) {
            return;
        }

        if ($_ENV['APP_DISABLE_STATUS_REFRESH_LISTENER'] ?? false) {
            return;
        }

        $path = $event->getRequest()->getPathInfo();

        if (str_starts_with($path, '/_') || str_starts_with($path, '/api/')) {
            return;
        }

        try {
            $this->eventStatusService->refreshAllThrottled();
        } catch (\Throwable) {
        }
    }
}
