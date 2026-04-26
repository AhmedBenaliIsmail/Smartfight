<?php
namespace App\Service;

use Symfony\Contracts\HttpClient\HttpClientInterface;

class AIService
{
    private string $apiKey;
    private string $apiUrl = 'https://api.deepseek.com/chat/completions';

    public function __construct(
        private HttpClientInterface $httpClient,
        string $deepseekApiKey = '',
    ) {
        $this->apiKey = $deepseekApiKey ?: ($_ENV['DEEPSEEK_API_KEY'] ?? '');
    }

    /**
     * Generate AI suggestions for fight statistics based on fighter profiles
     */
    public function suggestFightStats(array $fighter1Data, array $fighter2Data, int $round = 1): array
    {
        if (!$this->apiKey) {
            return [
                'success' => false,
                'error' => 'API Key not configured',
                'suggestions' => null
            ];
        }

        $prompt = $this->buildStatSuggestionPrompt($fighter1Data, $fighter2Data, $round);
        
        try {
            $response = $this->httpClient->request('POST', $this->apiUrl, [
                'headers' => [
                    'Authorization' => 'Bearer ' . $this->apiKey,
                    'Content-Type' => 'application/json',
                ],
                'json' => [
                    'model' => 'deepseek-chat',
                    'messages' => [
                        [
                            'role' => 'system',
                            'content' => 'You are an expert boxing statistician. Analyze fight patterns and suggest realistic statistics based on fighter profiles. Always respond with valid JSON.'
                        ],
                        [
                            'role' => 'user',
                            'content' => $prompt
                        ]
                    ],
                    'temperature' => 0.7,
                    'max_tokens' => 1000,
                ],
                'timeout' => 30,
            ]);

            $content = $response->toArray();
            
            if (isset($content['choices'][0]['message']['content'])) {
                $aiResponse = $content['choices'][0]['message']['content'];
                
                // Extract JSON from response
                $jsonMatch = preg_match('/\{[\s\S]*\}/', $aiResponse, $matches);
                if ($jsonMatch) {
                    $suggestions = json_decode($matches[0], true);
                    return [
                        'success' => true,
                        'suggestions' => $suggestions
                    ];
                }
            }

            return [
                'success' => false,
                'error' => 'Invalid response from API',
                'suggestions' => null
            ];
        } catch (\Exception $e) {
            // FALLBACK: If API fails (e.g. 402 Payment Required), generate realistic mock stats
            if (str_contains($e->getMessage(), '402') || str_contains($e->getMessage(), '401')) {
                return [
                    'success' => true,
                    'is_mock' => true,
                    'suggestions' => $this->generateMockStats($fighter1Data, $fighter2Data, $round)
                ];
            }

            return [
                'success' => false,
                'error' => $e->getMessage(),
                'suggestions' => null
            ];
        }
    }

    /**
     * Local fallback generator for when API is unavailable/unpaid
     */
    private function generateMockStats(array $f1, array $f2, int $round): array
    {
        $gen = function($style) {
            $thrown = rand(45, 85);
            if (str_contains(strtoupper($style), 'PRESSURE')) $thrown += 15;
            if (str_contains(strtoupper($style), 'OUT-BOXER')) $thrown -= 10;
            
            $accuracy = rand(25, 45) / 100;
            $landed = (int)($thrown * $accuracy);
            
            $rt = (int)($thrown * (rand(50, 60) / 100));
            $lt = $thrown - $rt;
            $rl = (int)($landed * (rand(50, 60) / 100));
            $ll = $landed - $rl;

            $pwt = (int)($thrown * 0.6);
            $pwl = (int)($landed * 0.7);
            
            return [
                'punches_thrown' => $thrown, 'punches_landed' => $landed,
                'power_punches_thrown' => $pwt, 'power_punches_landed' => $pwl,
                'jabs_thrown' => $thrown - $pwt, 'jabs_landed' => $landed - $pwl,
                'right_hand_thrown' => $rt, 'right_hand_landed' => $rl,
                'left_hand_thrown' => $lt, 'left_hand_landed' => $ll,
                'uppercuts_thrown' => (int)($thrown * 0.1), 'uppercuts_landed' => (int)($landed * 0.1),
                'body_shots_landed' => (int)($landed * 0.2), 'knockdowns' => (rand(0, 100) > 95 ? 1 : 0)
            ];
        };

        return [
            'fighter1' => $gen($f1['style']),
            'fighter2' => $gen($f2['style']),
            'inside_the_numbers' => "LOCAL ENGINE: Round {$round} showed high tactical engagement. {$f1['name']} worked behind the jab while {$f2['name']} looked for power openings."
        ];
    }

    /**
     * Get AI matchmaking suggestions
     */
    public function suggestMatches(array $availableFighters): array
    {
        if (!$this->apiKey) {
            return [
                'success' => false,
                'error' => 'API Key not configured',
                'matches' => []
            ];
        }

        $prompt = $this->buildMatchmakingPrompt($availableFighters);
        
        try {
            $response = $this->httpClient->request('POST', $this->apiUrl, [
                'headers' => [
                    'Authorization' => 'Bearer ' . $this->apiKey,
                    'Content-Type' => 'application/json',
                ],
                'json' => [
                    'model' => 'deepseek-chat',
                    'messages' => [
                        [
                            'role' => 'system',
                            'content' => 'You are an expert boxing matchmaker. Suggest competitive, balanced fights that would be exciting for fans. Always respond with valid JSON containing match pairs.'
                        ],
                        [
                            'role' => 'user',
                            'content' => $prompt
                        ]
                    ],
                    'temperature' => 0.8,
                    'max_tokens' => 2000,
                ],
                'timeout' => 30,
            ]);

            $content = $response->toArray();
            
            if (isset($content['choices'][0]['message']['content'])) {
                $aiResponse = $content['choices'][0]['message']['content'];
                
                // Extract JSON from response
                $jsonMatch = preg_match('/\{[\s\S]*\}/', $aiResponse, $matches);
                if ($jsonMatch) {
                    $suggestions = json_decode($matches[0], true);
                    return [
                        'success' => true,
                        'matches' => $suggestions['matches'] ?? []
                    ];
                }
            }

            return [
                'success' => false,
                'error' => 'Invalid response from API',
                'matches' => []
            ];
        } catch (\Exception $e) {
            return [
                'success' => false,
                'error' => $e->getMessage(),
                'matches' => []
            ];
        }
    }

    /**
     * Analyze fight dynamics and suggest tactical insights
     */
    public function analyzeFightDynamics(array $fighter1Stats, array $fighter2Stats): array
    {
        if (!$this->apiKey) {
            return [
                'success' => false,
                'error' => 'API Key not configured',
                'analysis' => null
            ];
        }

        $prompt = "Analyze the boxing match dynamics between these two fighters:\n\n";
        $prompt .= "Fighter 1:\n" . json_encode($fighter1Stats, JSON_PRETTY_PRINT) . "\n\n";
        $prompt .= "Fighter 2:\n" . json_encode($fighter2Stats, JSON_PRETTY_PRINT) . "\n\n";
        $prompt .= "Provide a JSON response with: tactical_analysis (string), advantage (fighter1|fighter2|balanced), key_factors (array)";

        try {
            $response = $this->httpClient->request('POST', $this->apiUrl, [
                'headers' => [
                    'Authorization' => 'Bearer ' . $this->apiKey,
                    'Content-Type' => 'application/json',
                ],
                'json' => [
                    'model' => 'deepseek-chat',
                    'messages' => [
                        [
                            'role' => 'system',
                            'content' => 'You are a boxing analyst. Provide tactical insights in JSON format.'
                        ],
                        [
                            'role' => 'user',
                            'content' => $prompt
                        ]
                    ],
                    'temperature' => 0.7,
                    'max_tokens' => 1500,
                ],
                'timeout' => 30,
            ]);

            $content = $response->toArray();
            
            if (isset($content['choices'][0]['message']['content'])) {
                $aiResponse = $content['choices'][0]['message']['content'];
                
                $jsonMatch = preg_match('/\{[\s\S]*\}/', $aiResponse, $matches);
                if ($jsonMatch) {
                    $analysis = json_decode($matches[0], true);
                    return [
                        'success' => true,
                        'analysis' => $analysis
                    ];
                }
            }

            return [
                'success' => false,
                'error' => 'Invalid response from API',
                'analysis' => null
            ];
        } catch (\Exception $e) {
            return [
                'success' => false,
                'error' => $e->getMessage(),
                'analysis' => null
            ];
        }
    }

    private function buildStatSuggestionPrompt(array $f1, array $f2, int $round): string
    {
        $prompt = "You are a CompuBox expert. Generate realistic round {$round} boxing stats for these fighters. Follow the STRICT MATH RULES below.\n\n";

        $prompt .= "Fighter 1: {$f1['name']} | Style: {$f1['style']} | Win Rate: {$f1['win_rate']}% | Form: {$f1['recent_fights_summary']} | Strength: {$f1['strength']} | Weakness: {$f1['weakness']}\n\n";
        $prompt .= "Fighter 2: {$f2['name']} | Style: {$f2['style']} | Win Rate: {$f2['win_rate']}% | Form: {$f2['recent_fights_summary']} | Strength: {$f2['strength']} | Weakness: {$f2['weakness']}\n\n";

        $prompt .= "STRICT MATH RULES — ALL must be satisfied or the response is wrong:\n";
        $prompt .= "1. right_hand_thrown + left_hand_thrown = punches_thrown (exactly)\n";
        $prompt .= "2. right_hand_landed + left_hand_landed = punches_landed (exactly)\n";
        $prompt .= "3. Every landed value <= its thrown counterpart\n";
        $prompt .= "4. All numbers must be non-negative integers\n\n";

        $prompt .= "Return ONLY raw JSON, no markdown:\n";
        $prompt .= "{\n";
        $prompt .= "  \"fighter1\": {\n";
        $prompt .= "    \"punches_thrown\": 0, \"punches_landed\": 0,\n";
        $prompt .= "    \"power_punches_thrown\": 0, \"power_punches_landed\": 0,\n";
        $prompt .= "    \"jabs_thrown\": 0, \"jabs_landed\": 0,\n";
        $prompt .= "    \"right_hand_thrown\": 0, \"right_hand_landed\": 0,\n";
        $prompt .= "    \"left_hand_thrown\": 0, \"left_hand_landed\": 0,\n";
        $prompt .= "    \"uppercuts_thrown\": 0, \"uppercuts_landed\": 0,\n";
        $prompt .= "    \"body_shots_landed\": 0, \"knockdowns\": 0\n";
        $prompt .= "  },\n";
        $prompt .= "  \"fighter2\": { same fields },\n";
        $prompt .= "  \"inside_the_numbers\": \"2-3 sentence CompuBox broadcast summary citing actual numbers, e.g. '{$f1['name']} landed X of Y total punches (Z%). {$f2['name']} held the connect advantage W to V in power punches.'\"\n";
        $prompt .= "}\n";

        return $prompt;
    }

    private function buildMatchmakingPrompt(array $fighters): string
    {
        $prompt = "I have these available fighters and need matchmaking suggestions:\n\n";
        
        foreach ($fighters as $f) {
            $prompt .= "Fighter ID {$f['id']}: {$f['name']}\n";
            $prompt .= "- Weight: {$f['weight']} lbs, Division: {$f['division']}\n";
            $prompt .= "- Record: {$f['wins']}-{$f['losses']}-{$f['draws']}\n";
            $prompt .= "- Win Rate: {$f['win_rate']}%\n";
            $prompt .= "- ELO: {$f['elo']}\n";
            $prompt .= "- Style: {$f['style']}\n";
            $prompt .= "- Recent Form: {$f['form']}\n\n";
        }

        $prompt .= "Return a JSON object with suggested matches:\n";
        $prompt .= "{\n";
        $prompt .= "  \"matches\": [\n";
        $prompt .= "    { \"fighter1_id\": <id>, \"fighter2_id\": <id>, \"reason\": \"Why this is a great matchup\", \"excitement_level\": \"high/medium/low\" },\n";
        $prompt .= "    ...\n";
        $prompt .= "  ]\n";
        $prompt .= "}";

        return $prompt;
    }
    public function generateFighterProfile(array $fighterData): array
    {
        if (!$this->apiKey) {
            return [
                'success' => false,
                'error' => 'API Key not configured',
            ];
        }

        $prompt = "You are a professional boxing analyst. Analyze the following fighter's record and physical stats to generate an accurate fighting style description.\n\n";
        $prompt .= "Fighter: {$fighterData['name']}\n";
        $prompt .= "Record: {$fighterData['wins']} Wins, {$fighterData['losses']} Losses, {$fighterData['draws']} Draws\n";
        $prompt .= "KOs: {$fighterData['ko_wins']}\n";
        $prompt .= "Height: {$fighterData['height']} cm\n";
        $prompt .= "Reach: {$fighterData['reach']} cm\n\n";

        $prompt .= "Return ONLY raw JSON, no markdown:\n";
        $prompt .= "{\n";
        $prompt .= "  \"aiStyleTag\": \"A short 1-3 word style tag (e.g. 'Relentless Slugger', 'Tactical Out-Boxer', 'Devastating Power Puncher')\",\n";
        $prompt .= "  \"aiDescription\": \"A 2-sentence professional breakdown of what this record and physical attributes suggest about their fighting style inside the ring.\"\n";
        $prompt .= "}\n";

        try {
            $response = $this->httpClient->request('POST', $this->apiUrl, [
                'headers' => [
                    'Authorization' => 'Bearer ' . $this->apiKey,
                    'Content-Type' => 'application/json',
                ],
                'json' => [
                    'model' => 'deepseek-chat',
                    'messages' => [
                        [
                            'role' => 'system',
                            'content' => 'You are an expert boxing analyst. Always respond with valid JSON.'
                        ],
                        [
                            'role' => 'user',
                            'content' => $prompt
                        ]
                    ],
                    'temperature' => 0.6,
                    'max_tokens' => 500,
                ],
                'timeout' => 30,
            ]);

            $content = $response->toArray();
            
            if (isset($content['choices'][0]['message']['content'])) {
                $aiResponse = $content['choices'][0]['message']['content'];
                
                $jsonMatch = preg_match('/\{[\s\S]*\}/', $aiResponse, $matches);
                if ($jsonMatch) {
                    $result = json_decode($matches[0], true);
                    return [
                        'success' => true,
                        'profile' => $result
                    ];
                }
            }

            return [
                'success' => false,
                'error' => 'Invalid response from API'
            ];
        } catch (\Exception $e) {
            return [
                'success' => false,
                'error' => $e->getMessage()
            ];
        }
    }
}
