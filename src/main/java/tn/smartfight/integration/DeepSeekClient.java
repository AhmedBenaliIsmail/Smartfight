package tn.smartfight.integration;

import tn.smartfight.config.AppConfig;
import tn.smartfight.model.FighterDetails;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DeepSeekClient {
    private static final Logger LOG = Logger.getLogger(DeepSeekClient.class.getName());

    private final String apiUrl;
    private final String apiKey;
    private final HttpClient http;

    public DeepSeekClient(AppConfig cfg) {
        this.apiUrl = cfg.deepseekApiUrl;
        this.apiKey = cfg.deepseekApiKey;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    public String generateFighterProfile(FighterDetails f) {
        String prompt = buildProfilePrompt(f);
        return chatRaw(prompt, 600);
    }

    public String generateStyleTag(FighterDetails f) {
        String prompt = String.format(
                "In 3-5 words, describe the fighting style of %s %s " +
                "(record: %d-%d-%d, KO wins: %d, decision wins: %d). " +
                "Reply with only the tag, no punctuation.",
                f.getFirstName(), f.getLastName(),
                f.getWins(), f.getLosses(), f.getDraws(),
                f.getKoWins(), f.getDecisionWins()
        );
        return chatRaw(prompt, 20);
    }

    private String buildProfilePrompt(FighterDetails f) {
        return String.format(
                "Write a concise professional boxing/MMA fighter profile for %s \"%s\" %s. " +
                "Record: %d wins (%d KO, %d decision, %d technical), %d losses (%d KO), %d draws. " +
                "ELO: %.0f. Win streak: %d. Title defenses: %d. " +
                "Height: %d cm, Reach: %d cm, Weight: %d lbs, Age: %d. " +
                "Strikes thrown: %d, landed: %d. " +
                "Write 2-3 sentences in third person describing their style, strengths, and reputation.",
                f.getFirstName(), f.getNickname() != null ? f.getNickname() : "", f.getLastName(),
                f.getWins(), f.getKoWins(), f.getDecisionWins(), f.getTechnicalWins(),
                f.getLosses(), f.getKoLosses(), f.getDraws(),
                f.getEloRating(), f.getWinStreak(), f.getTitleDefenses(),
                f.getHeight(), f.getReach(), f.getWeight(), f.getAge(),
                f.getStrikesThrown(), f.getStrikesLanded()
        );
    }

    public String chatRaw(String userMessage, int maxTokens) {
        String body = String.format(
                "{\"model\":\"deepseek-chat\",\"max_tokens\":%d,\"messages\":[{\"role\":\"user\",\"content\":%s}]}",
                maxTokens, jsonString(userMessage)
        );
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl + "/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                LOG.warning("DeepSeek returned status " + response.statusCode());
                return "";
            }
            return extractContent(response.body());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "DeepSeek API call failed", e);
            return "";
        }
    }

    private String extractContent(String json) {
        // Parse "content":"..." from the first choice without pulling in a JSON library
        int idx = json.indexOf("\"content\":");
        if (idx < 0) return "";
        int start = json.indexOf('"', idx + 10) + 1;
        int end = start;
        while (end < json.length()) {
            if (json.charAt(end) == '"' && json.charAt(end - 1) != '\\') break;
            end++;
        }
        return json.substring(start, end)
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    private String jsonString(String value) {
        return "\"" + value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                + "\"";
    }
}
