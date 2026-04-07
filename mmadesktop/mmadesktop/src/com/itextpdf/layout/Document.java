package com.itextpdf.layout;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

public class Document {
    public Document(PdfDocument pdfDocument, PageSize pageSize) {
    }

    public Document setMargins(float top, float right, float bottom, float left) {
        return this;
    }

    public Document add(Paragraph paragraph) {
        return this;
    }

    public Document add(Table table) {
        return this;
    }

    public void close() {
    }
}
