package tn.smartfight.service;

import tn.smartfight.dao.NotificationDao;
import tn.smartfight.model.Notification;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NotificationService {
    private static final Logger LOG = Logger.getLogger(NotificationService.class.getName());
    private final NotificationDao dao;

    public NotificationService() { this(new NotificationDao()); }
    public NotificationService(NotificationDao dao) { this.dao = dao; }

    public void notifyUser(int userId, String message, String type) {
        try {
            dao.create(new Notification(userId, message, type));
        } catch (Exception e) {
            LOG.log(Level.WARNING, "notifyUser failed uid=" + userId, e);
        }
    }

    public void notifyAllFans(String message, String type) {
        List<Integer> ids = dao.findAllUserIds();
        for (int uid : ids) notifyUser(uid, message, type);
    }

    public void notifyRankingUpdate(String fightDescription) {
        notifyAllFans("🥊 World Rankings have been updated following " + fightDescription + "!", "RANKING");
    }

    public void notifyNewArticle(String articleTitle) {
        notifyAllFans("New article published: " + articleTitle + ". Check it out!", "NEW_ARTICLE");
    }

    public void notifyPredictionResult(int userId, int points) {
        notifyUser(userId,
                "Prediction result recorded! You earned " + points + " prediction points.", "PREDICTION");
    }

    public void notifyFanFavoriteEvent(String eventName) {
        notifyAllFans("Fan Favorite Night event scheduled: " + eventName + "!", "FAN_FAVORITE");
    }

    public void notifyBookingConfirmed(int userId, String bookingRef, String eventName) {
        notifyUser(userId,
                "Your booking for " + eventName + " (Ref: " + bookingRef + ") is confirmed!", "BOOKING");
    }
}
