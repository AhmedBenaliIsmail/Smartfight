package com.itextpdf.layout.element;

import com.itextpdf.layout.properties.TextAlignment;

public class Paragraph {
    public Paragraph() {
    }

    public Paragraph(String text) {
    }

    public Paragraph setFontSize(float size) {
        return this;
    }

    public Paragraph setBold() {
        return this;
    }

    public Paragraph setTextAlignment(TextAlignment alignment) {
        return this;
    }
}
