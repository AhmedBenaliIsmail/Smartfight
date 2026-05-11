package tn.smartfight.model;

import java.time.LocalDateTime;

public class Prediction {
    private int predictionId;
    private int userId;
    private int fightId;
    private Integer predictedWinnerId;
    private String predictedMethod;
    private Integer predictedRound;
    private LocalDateTime createdAt;

    // display fields
    private String username;
    private String predictedWinnerName;

    public Prediction() {}

    public int getPredictionId() { return predictionId; }
    public void setPredictionId(int predictionId) { this.predictionId = predictionId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getFightId() { return fightId; }
    public void setFightId(int fightId) { this.fightId = fightId; }

    public Integer getPredictedWinnerId() { return predictedWinnerId; }
    public void setPredictedWinnerId(Integer predictedWinnerId) { this.predictedWinnerId = predictedWinnerId; }

    public String getPredictedMethod() { return predictedMethod; }
    public void setPredictedMethod(String predictedMethod) { this.predictedMethod = predictedMethod; }

    public Integer getPredictedRound() { return predictedRound; }
    public void setPredictedRound(Integer predictedRound) { this.predictedRound = predictedRound; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPredictedWinnerName() { return predictedWinnerName; }
    public void setPredictedWinnerName(String predictedWinnerName) { this.predictedWinnerName = predictedWinnerName; }
}
