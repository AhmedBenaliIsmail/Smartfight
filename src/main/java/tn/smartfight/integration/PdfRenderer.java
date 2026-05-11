package tn.smartfight.integration;

import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PdfRenderer {
    private static final Logger LOG = Logger.getLogger(PdfRenderer.class.getName());

    public static byte[] render(String html) {
        try {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            renderer.createPDF(out);
            return out.toByteArray();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "PdfRenderer failed", e);
            return new byte[0];
        }
    }
}
