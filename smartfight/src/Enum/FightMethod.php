<?php

namespace App\Enum;

enum FightMethod: string
{
    case KO   = 'KO';
    case TKO  = 'TKO';
    case UD   = 'UD';
    case SD   = 'SD';
    case MD   = 'MD';
    case PTS  = 'PTS';
    case DQ   = 'DQ';
    case TD   = 'TD';
    case NC   = 'NC';
    case DRAW = 'DRAW';

    public function label(): string
    {
        return match($this) {
            self::KO   => 'Knockout',
            self::TKO  => 'Technical Knockout',
            self::UD   => 'Unanimous Decision',
            self::SD   => 'Split Decision',
            self::MD   => 'Majority Decision',
            self::PTS  => 'Points',
            self::DQ   => 'Disqualification',
            self::TD   => 'Technical Draw',
            self::NC   => 'No Contest',
            self::DRAW => 'Draw',
        };
    }

    public function isStoppage(): bool
    {
        return in_array($this, [self::KO, self::TKO], true);
    }

    public static function values(): array
    {
        return array_column(self::cases(), 'value');
    }

    public static function labels(): array
    {
        $result = [];
        foreach (self::cases() as $case) {
            $result[$case->value] = $case->label();
        }
        return $result;
    }
}
