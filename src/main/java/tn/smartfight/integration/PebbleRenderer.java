package tn.smartfight.integration;

import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.template.PebbleTemplate;

import java.io.StringWriter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PebbleRenderer {
    private static final Logger LOG = Logger.getLogger(PebbleRenderer.class.getName());

    private static final PebbleEngine ENGINE = new PebbleEngine.Builder()
            .loader(new io.pebbletemplates.pebble.loader.ClasspathLoader())
            .build();

    public static String render(String templatePath, Map<String, Object> context) {
        try {
            PebbleTemplate template = ENGINE.getTemplate(templatePath);
            StringWriter writer = new StringWriter();
            template.evaluate(writer, context);
            return writer.toString();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "PebbleRenderer failed for: " + templatePath, e);
            return "";
        }
    }
}
