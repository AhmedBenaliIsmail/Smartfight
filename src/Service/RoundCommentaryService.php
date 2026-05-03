<?php
namespace App\Service;

class RoundCommentaryService
{
    public function generateForRound(
        string $f1Name, array $f1, string $f2Name, array $f2, int $round
    ): string {
        $f1l    = (int)($f1['landed']       ?? 0);
        $f2l    = (int)($f2['landed']       ?? 0);
        $f1t    = (int)($f1['thrown']       ?? 0);
        $f2t    = (int)($f2['thrown']       ?? 0);
        $f1kd   = (int)($f1['kds']          ?? 0);
        $f2kd   = (int)($f2['kds']          ?? 0);
        $f1pl   = (int)($f1['power_landed'] ?? 0);
        $f1pt   = (int)($f1['power_thrown'] ?? 0);
        $f2pl   = (int)($f2['power_landed'] ?? 0);
        $f2pt   = (int)($f2['power_thrown'] ?? 0);
        $f1body = (int)($f1['body_shots']   ?? 0);
        $f2body = (int)($f2['body_shots']   ?? 0);
        $f1jabs = (int)($f1['jabs_landed']  ?? 0);
        $f2jabs = (int)($f2['jabs_landed']  ?? 0);

        $winner = $f1l >= $f2l ? $f1Name : $f2Name;
        $loser  = $f1l >= $f2l ? $f2Name : $f1Name;

        $sentences = [];

        // ── Sentence 1: Who won the round ────────────────────────────────
        if ($f1l === $f2l) {
            $variants = [
                "Round {$round} was dead even — both fighters landed {$f1l} punches apiece.",
                "CompuBox shows an even split in Round {$round}: {$f1l} connects for {$f1Name}, {$f2l} for {$f2Name}.",
                "A perfectly balanced round on the CompuBox numbers — {$f1l} landed for each man.",
                "Too close to call on the CompuBox numbers. Both men connected on {$f1l} — this one goes to the judges.",
            ];
            $sentences[] = $variants[array_rand($variants)];
        } elseif ($f1l > $f2l) {
            $openers = ['edged', 'controlled', 'outworked', 'dominated', 'swept', 'outlanded', 'took command of the round against'];
            $op      = $openers[array_rand($openers)];
            $variants = [
                "{$f1Name} {$op} Round {$round}, landing {$f1l} of {$f1t} total punches compared to {$f2Name}'s {$f2l}.",
                "CompuBox gives Round {$round} to {$f1Name}, who connected on {$f1l} to {$f2Name}'s {$f2l} out of {$f2t} thrown.",
                "{$f1Name} held the connect advantage in Round {$round}, landing {$f1l} versus {$f2l} for {$f2Name}.",
                "According to CompuBox, {$f1Name} had a {$f1l}-to-{$f2l} edge in Round {$round} — a clear margin.",
                "{$f1Name} with a {$f1l}-to-{$f2l} advantage in total connects. That is the story of Round {$round}.",
                "The CompuBox numbers tell the story of Round {$round}: {$f1Name} outlanded {$f2Name} {$f1l} to {$f2l}.",
                "Round {$round} to {$f1Name} — {$f1l} of {$f1t} punches landed, {$f2Name} connecting on just {$f2l}.",
            ];
            $sentences[] = $variants[array_rand($variants)];
        } else {
            $openers = ['edged', 'controlled', 'outworked', 'dominated', 'swept', 'outlanded', 'took command of the round against'];
            $op      = $openers[array_rand($openers)];
            $variants = [
                "{$f2Name} {$op} Round {$round}, connecting on {$f2l} of {$f2t} punches while {$f1Name} landed {$f1l}.",
                "CompuBox credits Round {$round} to {$f2Name}, who outlanded {$f1Name} {$f2l} to {$f1l}.",
                "{$f2Name} held the volume edge in Round {$round}, landing {$f2l} punches compared to {$f1l} for {$f1Name}.",
                "According to CompuBox, {$f2Name} had a {$f2l}-to-{$f1l} edge in Round {$round} — a clear margin.",
                "{$f2Name} with a {$f2l}-to-{$f1l} advantage in total connects. That is the story of Round {$round}.",
                "The CompuBox numbers tell the story of Round {$round}: {$f2Name} outlanded {$f1Name} {$f2l} to {$f1l}.",
                "Round {$round} to {$f2Name} — {$f2l} of {$f2t} punches landed, {$f1Name} connecting on just {$f1l}.",
            ];
            $sentences[] = $variants[array_rand($variants)];
        }

        // ── Sentence 2: Round-phase context ──────────────────────────────
        if ($round <= 3) {
            $variants = [
                "It's still early in the conversation, but the numbers don't lie.",
                "The feeling-out process is over — the stats show who's dictating terms.",
                "Early exchanges, and already a pattern is beginning to form.",
                "In the opening stanzas, small advantages compound quickly.",
            ];
        } elseif ($round <= 7) {
            $variants = [
                "As the fight settles into a rhythm, {$winner} is beginning to impose their will.",
                "The middle rounds are where fights are won, and {$winner} is building their case.",
                "We're into the second chapter of this fight, and {$winner} holds the edge.",
                "At the halfway mark, the CompuBox numbers are telling a story {$loser} cannot ignore.",
                "The championship rounds are coming — this is no time to be giving away frames.",
            ];
        } else {
            $variants = [
                "In the championship rounds, every landed punch counts double.",
                "This is championship territory, and {$winner} is rising to the occasion.",
                "The urgency is real and the numbers are piling up for {$loser}.",
                "You cannot gift away rounds at this stage. {$winner} knows it and is cashing in.",
                "The scorecards are being written right now. {$winner} is writing a strong chapter.",
            ];
        }
        $sentences[] = $variants[array_rand($variants)];

        // ── Sentence 3: Knockdown (conditional) ──────────────────────────
        if ($f1kd > 0) {
            $kdLabel  = $f1kd === 1 ? 'a knockdown' : "{$f1kd} knockdowns";
            $variants = [
                "{$f1Name} scored {$kdLabel}, sending {$f2Name} to the canvas in a pivotal moment.",
                "The round was defined by {$f1Name}'s {$kdLabel} — dropping {$f2Name} and shifting the momentum completely.",
                "{$f1Name} dug those feet into the canvas and let it go — a knockdown that changed everything.",
                "A massive moment. {$f1Name} puts {$f2Name} down. {$f2Name} gets up, but that's a 10-8 round on every card.",
                "The dream scenario for {$f1Name} — {$kdLabel} in Round {$round} that the judges will remember.",
                "{$f1Name} scores {$kdLabel} and the crowd erupts. {$f2Name} survived, but the damage is done.",
            ];
            $sentences[] = $variants[array_rand($variants)];
        } elseif ($f2kd > 0) {
            $kdLabel  = $f2kd === 1 ? 'a knockdown' : "{$f2kd} knockdowns";
            $variants = [
                "{$f2Name} scored {$kdLabel}, putting {$f1Name} down and threatening a stoppage.",
                "The story of Round {$round} was {$f2Name}'s {$kdLabel}, sending {$f1Name} to the canvas.",
                "{$f2Name} dug those feet into the canvas and let it go — a knockdown that changed everything.",
                "A massive moment. {$f2Name} puts {$f1Name} down. {$f1Name} gets up, but that's a 10-8 round on every card.",
                "The dream scenario for {$f2Name} — {$kdLabel} in Round {$round} that the judges will remember.",
                "{$f2Name} scores {$kdLabel} and the crowd erupts. {$f1Name} survived, but the damage is done.",
            ];
            $sentences[] = $variants[array_rand($variants)];
        }

        // ── Sentence 4: Signature stat ────────────────────────────────────
        $f1PowerAcc = $f1pt > 0 ? ($f1pl / $f1pt) * 100 : 0;
        $f2PowerAcc = $f2pt > 0 ? ($f2pl / $f2pt) * 100 : 0;
        $jabDiff    = abs($f1jabs - $f2jabs);

        // Flow connector to prepend
        $connectors = ['Meanwhile, ', 'On the other side, ', 'That said, ', 'And yet, ', ''];
        $conn       = $connectors[array_rand($connectors)];

        if ($f1PowerAcc >= 50 || $f2PowerAcc >= 50) {
            if ($f1PowerAcc >= $f2PowerAcc) {
                $name = $f1Name; $opp = $f2Name;
                $pl = $f1pl; $pt = $f1pt; $acc = round($f1PowerAcc);
            } else {
                $name = $f2Name; $opp = $f1Name;
                $pl = $f2pl; $pt = $f2pt; $acc = round($f2PowerAcc);
            }
            $variants = [
                "{$conn}{$name} was devastating with the power shots, connecting on {$pl} of {$pt} ({$acc}% accuracy).",
                "{$conn}In the power game, {$name} stood out — landing {$pl} of {$pt} big shots at a {$acc}% clip.",
                "{$conn}{$name} was surgical with the power punches, finding a home for {$pl} of {$pt} attempts — {$acc}% accuracy.",
                "{$conn}The CompuBox data on power punches is alarming for {$opp}: {$name} connected at a {$acc}% clip.",
                "{$conn}When {$name} threw the big shots, they landed — {$pl} of {$pt} for {$acc}%. That's the kind of accuracy that wins rounds.",
                "{$conn}{$name} lit up the power punch column: {$pl} of {$pt} at {$acc}% accuracy. A dominant display.",
                "{$conn}You cannot sustain that power accuracy. {$pl} of {$pt} landed for {$name} — an elite number.",
            ];
            $sentences[] = $variants[array_rand($variants)];

        } elseif ($f1body >= 4 || $f2body >= 4) {
            if ($f1body >= $f2body && $f1body >= 4) {
                $name = $f1Name; $opp = $f2Name; $bs = $f1body;
            } else {
                $name = $f2Name; $opp = $f1Name; $bs = $f2body;
            }
            $variants = [
                "{$conn}{$name} made the body a priority, racking up {$bs} body shots to wear down {$opp}.",
                "{$conn}Note the body work from {$name} — {$bs} shots downstairs that will pay dividends in the later rounds.",
                "{$conn}{$name} went south of the border {$bs} times. That is an investment that collects interest late.",
                "{$conn}The body attack from {$name} was relentless — {$bs} shots to the midsection keeping {$opp} honest.",
                "{$conn}{$name} digging downstairs in Round {$round} — {$bs} body shots in the bank.",
                "{$conn}{$bs} body shots landed for {$name}. Every one of those has a cumulative cost on {$opp}'s legs.",
            ];
            $sentences[] = $variants[array_rand($variants)];

        } elseif ($jabDiff >= 5) {
            $jabLeader  = $f1jabs > $f2jabs ? $f1Name : $f2Name;
            $jabTrailer = $f1jabs > $f2jabs ? $f2Name : $f1Name;
            $jabCount   = max($f1jabs, $f2jabs);
            $oppCount   = min($f1jabs, $f2jabs);
            $variants   = [
                "{$conn}{$jabLeader} controlled the range with {$jabCount} jabs landed, dictating distance throughout the round.",
                "{$conn}The jab was key — {$jabLeader} used it to great effect, connecting on {$jabCount} to control the pocket.",
                "{$conn}{$jabLeader}'s jab was the difference maker: {$jabCount} landed, using the stick as both a rangefinder and a scoring weapon.",
                "{$conn}Behind {$jabCount} jabs landed, {$jabLeader} was picking {$jabTrailer} apart from the outside — clinical work.",
                "{$conn}That jab is so quick, so sharp, and so precise. {$jabCount} landed in Round {$round} alone.",
                "{$conn}{$jabLeader} with a {$jabCount}-to-{$oppCount} edge in jabs. When your jab is working like that, everything else opens up.",
            ];
            $sentences[] = $variants[array_rand($variants)];

        } else {
            // Volume fallback
            $winnerThrown = $f1l >= $f2l ? $f1t : $f2t;
            $winnerLanded = $f1l >= $f2l ? $f1l : $f2l;
            $corner       = $f1l >= $f2l ? 'red' : 'blue';
            if ($winnerThrown > 0) {
                $variants = [
                    "{$conn}{$winner} brought the activity this round — {$winnerThrown} punches thrown, keeping pressure on {$loser} throughout.",
                    "{$conn}Volume was the story for {$winner}: {$winnerThrown} thrown, {$winnerLanded} connected. That's relentless output from the {$corner} corner.",
                    "{$conn}{$winner} put up {$winnerThrown} punches in Round {$round}. That's championship-level work rate.",
                ];
                $sentences[] = $variants[array_rand($variants)];
            }
        }

        return implode(' ', $sentences);
    }
}
