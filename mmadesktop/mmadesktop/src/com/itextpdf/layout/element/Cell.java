package com.itextpdf.layout.element;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.layout.properties.TextAlignment;

public class Cell {
    public Cell add(Paragraph paragraph) {
        return this;
    }

    public Cell setBackgroundColor(Color color) {
        return this;
    }

    public Cell setTextAlignment(TextAlignment alignment) {
        return this;
    }
}
