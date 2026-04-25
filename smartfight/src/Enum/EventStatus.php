<?php

namespace App\Enum;

enum EventStatus: string
{
    case SCHEDULED = 'SCHEDULED';
    case LIVE      = 'LIVE';
    case COMPLETED = 'COMPLETED';
    case CANCELLED = 'CANCELLED';

    public function label(): string
    {
        return match($this) {
            self::SCHEDULED => 'Scheduled',
            self::LIVE      => 'Live',
            self::COMPLETED => 'Completed',
            self::CANCELLED => 'Cancelled',
        };
    }

    public function badgeClass(): string
    {
        return match($this) {
            self::SCHEDULED => 'bg-primary',
            self::LIVE      => 'bg-danger',
            self::COMPLETED => 'bg-success',
            self::CANCELLED => 'bg-secondary',
        };
    }

    public static function values(): array
    {
        return array_column(self::cases(), 'value');
    }
}
