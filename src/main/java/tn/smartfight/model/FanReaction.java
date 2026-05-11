package tn.smartfight.model;

import java.time.LocalDateTime;

public class FanReaction {
    private int id;
    private String reactionType;
    private String comment;
    private LocalDateTime reactedAt;
    private boolean isPinned;
    private boolean isDeleted;
    private int fightResultId;
    private int fanId;

    // Denormalised display fields (joined from users / events / fighters)
    private String fanUsername;
    private String eventName;
    private String fighter1Name;
    private String fighter2Name;

    public FanReaction() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getReactionType() { return reactionType; }
    public void setReactionType(String reactionType) { this.reactionType = reactionType; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getReactedAt() { return reactedAt; }
    public void setReactedAt(LocalDateTime reactedAt) { this.reactedAt = reactedAt; }

    public boolean isPinned() { return isPinned; }
    public void setPinned(boolean pinned) { isPinned = pinned; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public int getFightResultId() { return fightResultId; }
    public void setFightResultId(int fightResultId) { this.fightResultId = fightResultId; }

    public int getFanId() { return fanId; }
    public void setFanId(int fanId) { this.fanId = fanId; }

    public String getFanUsername() { return fanUsername; }
    public void setFanUsername(String fanUsername) { this.fanUsername = fanUsername; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getFighter1Name() { return fighter1Name; }
    public void setFighter1Name(String fighter1Name) { this.fighter1Name = fighter1Name; }

    public String getFighter2Name() { return fighter2Name; }
    public void setFighter2Name(String fighter2Name) { this.fighter2Name = fighter2Name; }

    public String getEmoji() {
        if (reactionType == null) return "💬";
        return switch (reactionType.toUpperCase()) {
            case "FIRE"          -> "🔥";
            case "SHOCK"         -> "😱";
            case "RESPECT"       -> "👏";
            case "DOMINANT"      -> "🏆";
            case "CONTROVERSIAL" -> "💔";
            default              -> "💬";
        };
    }

    public String getFightLabel() {
        if (eventName == null && fighter1Name == null) return "Unknown Fight";
        String event = eventName != null ? eventName : "Event";
        String f1    = fighter1Name != null ? fighter1Name : "?";
        String f2    = fighter2Name != null ? fighter2Name : "?";
        return event + ": " + f1 + " vs " + f2;
    }
}
