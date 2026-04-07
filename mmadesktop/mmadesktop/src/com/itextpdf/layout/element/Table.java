package com.itextpdf.layout.element;

import com.itextpdf.layout.properties.UnitValue;

public class Table {
    public Table(UnitValue unitValue) {
    }

    public Table setWidth(UnitValue unitValue) {
        return this;
    }

    public Table addCell(Cell cell) {
        return this;
    }
}
