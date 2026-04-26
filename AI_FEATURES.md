# AI Features Implementation - SmartFight

## Overview

This document describes the new AI-powered features added to SmartFight for intelligent fight statistics entry and matchmaking suggestions.

## Features Implemented

### 1. **AI Auto-Fill Statistics** (Stats Entry Page)
- **Location**: `/stats/new` - Fight Statistics Entry Form
- **Button**: "AI Auto-Fill" (Purple button with sparkles icon)
- **How it works**:
  - Admin enters fighters and round number
  - Clicks "AI Auto-Fill" button
  - DeepSeek API analyzes fighter profiles (style, win rate, recent performance, strengths/weaknesses)
  - Generates realistic round statistics for both fighters
  - Form auto-populates with suggestions
  - Admin can review and adjust before saving

### 2. **AI Matchmaking Suggestions** (Fight Scheduling Page)
- **Location**: `/results/schedule` - Schedule Fight Page
- **Button**: "AI Suggestions" (Purple button with sparkles icon)
- **How it works**:
  - Admin clicks "AI Suggestions" button
  - DeepSeek API analyzes all available fighters
  - Generates balanced matchups considering:
    - Weight class compatibility
    - ELO rating balance
    - Fighting style diversity
    - Recent form
    - Win rate comparisons
  - Suggests first match with explanation (excitement level: high/medium/low)
  - Form auto-populates with suggested fighters
  - Admin can accept or generate new suggestions

## Technical Architecture

### Services

#### `AIService` (`src/Service/AIService.php`)
- Communicates with DeepSeek API
- Methods:
  - `suggestFightStats()` - Generate round statistics suggestions
  - `suggestMatches()` - Generate fight matchups
  - `analyzeFightDynamics()` - Analyze tactical fight dynamics
- Returns JSON responses with success status and data
- Handles API errors gracefully

#### `MatchmakingService` (`src/Service/MatchmakingService.php`)
- Business logic for intelligent matchmaking
- Methods:
  - `suggestMatches()` - Get AI suggestions with fallback
  - `findBalancedOpponent()` - Algorithmic matchmaking using ELO and record
  - `calculateMatchScore()` - Score match quality
- Falls back to algorithmic matching if AI unavailable

### API Endpoints

#### `POST /api/ai/stat-suggestions`
**Purpose**: Generate fight statistics suggestions for a round

**Request**:
```json
{
  "fighter1_id": 1,
  "fighter2_id": 2,
  "round": 3
}
```

**Response**:
```json
{
  "success": true,
  "suggestions": {
    "fighter1": {
      "punches_thrown": 140,
      "punches_landed": 52,
      "power_punches_thrown": 45,
      "power_punches_landed": 22,
      "jabs_thrown": 95,
      "jabs_landed": 30,
      "body_shots_landed": 8,
      "knockdowns": 0,
      "explanation": "..."
    },
    "fighter2": { ... }
  }
}
```

#### `POST /api/ai/matchmaking-suggestions`
**Purpose**: Generate fight matchup suggestions

**Request**:
```json
{
  "count": 5
}
```

**Response**:
```json
{
  "success": true,
  "matches": [
    {
      "fighter1_id": 1,
      "fighter2_id": 3,
      "reason": "Balanced ELO ratings and contrasting styles make for exciting match",
      "excitement_level": "high"
    }
  ],
  "count_returned": 1
}
```

### Controllers

#### `AIController` (`src/Controller/AIController.php`)
- Route prefix: `/api/ai`
- Handles:
  - Statistics suggestions endpoint
  - Matchmaking suggestions endpoint
- All methods require `ROLE_ADMIN`
- Prepares fighter data from repository
- Returns JSON responses

### Frontend Integration

#### Statistics Form Enhancement
**File**: `templates/statistic/form.html.twig`
- New purple "AI Auto-Fill" button
- JavaScript fetch to `/api/ai/stat-suggestions`
- Populates form fields with AI suggestions
- Triggers validation
- Shows user-friendly alerts for success/error

#### Matchmaking Form Enhancement
**File**: `templates/result/schedule.html.twig`
- New purple "AI Suggestions" button
- JavaScript fetch to `/api/ai/matchmaking-suggestions`
- Auto-populates fighter selections
- Shows match reasoning and excitement level
- Allows trying different suggestions

## Configuration

### Environment Variables

**Required**:
```env
DEEPSEEK_API_KEY=sk-xxxxxxxxxxxxxxxxxxxx
```

**Location**: `.env` file (already configured)

**Current Value**: From your .env setup

### Service Registration

**File**: `config/services.yaml`
```yaml
App\Service\AIService:
    arguments:
        $deepseekApiKey: '%env(DEEPSEEK_API_KEY)%'
```

## Usage Guide

### For Stats Entry

1. Go to **Admin Dashboard** → **Statistics** → **New**
2. Select Event → Select Fight
3. Enter Round number
4. Click **"AI Auto-Fill"** button
5. Review the suggested statistics
6. Adjust any values as needed (e.g., if you watched the actual fight)
7. Click **"Save Round X Stats"**

### For Fight Scheduling

1. Go to **Admin Dashboard** → **Results** → **Schedule Fight**
2. Select Event and Fight Number
3. Click **"AI Suggestions"** button
4. Review the suggested matchup:
   - Fighter names
   - Match reason
   - Excitement level
5. Accept or click again for more suggestions
6. Click **"Schedule Match"** to confirm

## AI Decision Logic

### Fight Statistics Scoring
- Punches: Based on fighter style and recent performance
- Power Punches: Considers fighter strength profile
- Jabs: Related to overall punch volume
- Body Work: Based on fighting style (aggressive vs tactical)
- Knockdowns: Rare event, suggested occasionally for powerful strikers
- Accuracy: Derived from win rate and skill level

### Matchmaking Scoring
- **Weight Class**: +25 points if same division
- **ELO Compatibility**: -1 point per ELO difference (closer = better)
- **Record Balance**: +20 if win rates within 15%, +10 if within 30%
- **Style Diversity**: +15 points if different fighting styles
- **Recent Form**: Factored into overall match appeal

## Fallback Behavior

If DeepSeek API is unavailable:
- Statistics suggestion: Returns error message, admin manually enters
- Matchmaking suggestion: Uses algorithmic fallback
  - Finds balanced opponents using:
    - ELO ratings (±150 tolerance)
    - Same weight class preference
    - Recent match history check
    - Competitive record balance

## Error Handling

### API Errors
- Connection timeout: Shows user-friendly error
- Invalid response: Gracefully handled with fallback
- Rate limiting: Notifies user to retry
- Invalid API key: Shows configuration error

### User Interface
- Loading indicators during AI processing
- Clear success/error alerts
- Validation errors for invalid data
- Form remains editable if suggestion fails

## Performance Considerations

- AI requests are async (non-blocking)
- Typical response time: 5-10 seconds
- Client-side form validation prevents invalid submissions
- Suggestions can be modified before saving
- Fallback algorithms run instantly if API unavailable

## Security

- All endpoints require `ROLE_ADMIN` authorization
- CSRF protection via Symfony (automatic)
- API key stored securely in `.env`
- No sensitive fighter data exposed externally
- Requests validated before sending to AI

## Future Enhancements

- Cache AI suggestions for performance
- Allow users to train custom AI models
- Real-time fight analysis during events
- Predictive fight outcome analysis
- Round-by-round tactical suggestions
- Fighter performance trending

## Troubleshooting

### "AI Auto-Fill button doesn't work"
- Check `.env` has valid `DEEPSEEK_API_KEY`
- Check browser console for network errors
- Verify you have `ROLE_ADMIN` permission
- Try cache clear: `php bin/console cache:clear`

### "API Key not configured"
- Ensure `.env` file exists with `DEEPSEEK_API_KEY`
- Run: `php bin/console cache:clear`
- Check key is not empty

### "Suggestions seem wrong"
- AI uses recent fight history to learn patterns
- First suggestions may be generic
- More fight data = better suggestions over time
- You can always manually adjust suggested values

## Testing the AI Features

### Quick Test
1. Start the server: `symfony server:start`
2. Login as admin
3. Go to Statistics → New
4. Select any Event and Fight
5. Click "AI Auto-Fill"
6. Watch it populate with suggested stats!

### Testing Matchmaking
1. Login as admin
2. Go to Results → Schedule Fight
3. Click "AI Suggestions"
4. Should see a fighter pair suggestion
5. Review the match reason

## Support

For issues or questions about the AI features:
- Check the error message in the UI
- Review browser console (F12 → Console)
- Verify API key is valid
- Check internet connection to DeepSeek API
