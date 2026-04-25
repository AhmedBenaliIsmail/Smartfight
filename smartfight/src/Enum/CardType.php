<?php

namespace App\Enum;

enum CardType: string
{
    case MAIN_CARD = 'MAIN_CARD';
    case PRELIMS   = 'PRELIMS';

    public function label(): string
    {
        return match($this) {
            self::MAIN_CARD => 'Main Card',
            self::PRELIMS   => 'Prelims',
        };
    }

    public static function values(): array
    {
        return array_column(self::cases(), 'value');
    }
}
