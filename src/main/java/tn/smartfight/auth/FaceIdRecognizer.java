package tn.smartfight.auth;

import com.github.sarxos.webcam.Webcam;
import tn.smartfight.dao.UserDao;
import tn.smartfight.model.User;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FaceIdRecognizer {
    private static final Logger LOG = Logger.getLogger(FaceIdRecognizer.class.getName());

    /**
     * Opens the default webcam, captures one frame, and returns it as a base64-encoded JPEG.
     * Caller is responsible for handling exceptions (no webcam, driver unavailable, etc.).
     */
    public static String captureBase64() throws Exception {
        Webcam webcam = Webcam.getDefault(3000, TimeUnit.MILLISECONDS);
        if (webcam == null) {
            throw new IllegalStateException("No webcam detected. Please connect a camera and try again.");
        }
        try {
            webcam.open();
            Thread.sleep(400); // allow auto-exposure to settle
            BufferedImage img = webcam.getImage();
            if (img == null) {
                throw new IllegalStateException("Webcam returned an empty frame — try again.");
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "JPEG", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } finally {
            if (webcam.isOpen()) webcam.close();
        }
    }

    /**
     * Demo recognition: captures a frame (verifying the camera works), then returns the
     * first user in the DB with a registered face photo.  No real biometric comparison
     * is performed — this is intentional for the university demo.
     */
    public static User recognize(UserDao userDao) throws Exception {
        captureBase64(); // confirms camera is accessible
        User user = userDao.findFirstWithFacePhoto();
        if (user == null) {
            throw new IllegalStateException(
                    "No Face ID registered. Ask an admin to register your face ID first.");
        }
        return user;
    }

    /** Captures a frame and stores it as the face photo for the given user. */
    public static void registerFaceId(int userId, UserDao userDao) throws Exception {
        String base64 = captureBase64();
        userDao.saveFacePhoto(userId, base64);
        LOG.info("Face ID registered for userId=" + userId);
    }
}
