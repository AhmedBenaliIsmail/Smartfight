<?php

namespace App\Enum;

enum MatchStatus: string
{
    case SCHEDULED  = 'SCHEDULED';
    case COMPLETED  = 'COMPLETED';
    case CANCELLED  = 'CANCELLED';
    case NO_CONTEST = 'NO_CONTEST';

    public function label(): string
    {
        return match($this) {
            self::SCHEDULED  => 'Scheduled',
            self::COMPLETED  => 'Completed',
            self::CANCELLED  => 'Cancelled',
            self::NO_CONTEST => 'No Contest',
        };
    }

    public function badgeClass(): string
    {
        return match($this) {
            self::SCHEDULED  => 'bg-primary',
            self::COMPLETED  => 'bg-success',
            self::CANCELLED  => 'bg-secondary',
            self::NO_CONTEST => 'bg-warning',
        };
    }

    public static function values(): array
    {
        return array_column(self::cases(), 'value');
    }
}
