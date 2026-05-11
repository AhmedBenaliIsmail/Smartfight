package tn.smartfight.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Map;

public class QrCodeGenerator {

    public static BufferedImage generate(String text) throws Exception {
        QRCodeWriter writer = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = Map.of(
                EncodeHintType.CHARACTER_SET, "UTF-8",
                EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L,
                EncodeHintType.MARGIN, 10);
        BitMatrix m = writer.encode(text, BarcodeFormat.QR_CODE, 250, 250, hints);
        return MatrixToImageWriter.toBufferedImage(m);
    }

    public static WritableImage toWritableImage(String text) throws Exception {
        return SwingFXUtils.toFXImage(generate(text), null);
    }

    public static byte[] toPngBytes(String text) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(generate(text), "PNG", out);
        return out.toByteArray();
    }

    public static String toPngBase64(String text) throws Exception {
        return Base64.getEncoder().encodeToString(toPngBytes(text));
    }
}
