package io.github.marco4413.pemu.application;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public final class MemoryCellRenderer extends DefaultTableCellRenderer {

    protected MemoryCellRenderer() {
        super();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (table instanceof MemoryTable) {
            MemoryTable memoryTable = (MemoryTable) table;
            Integer[] pointedCell = memoryTable.getPointedCell();
            if (pointedCell != null) {
                if (row == pointedCell[0] && column == pointedCell[1]) {
                    this.setForeground(memoryTable.getPointedCellForeground());
                    this.setBackground(memoryTable.getPointedCellBackground());
                } else {
                    this.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
                    this.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                }
            }
        }

        return this;
    }

}
