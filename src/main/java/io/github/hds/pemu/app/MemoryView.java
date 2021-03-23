package io.github.hds.pemu.app;

import io.github.hds.pemu.processor.Processor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;

public class MemoryView extends JFrame {

    private final Application app;

    private final Timer UPDATE_TIMER;
    private final JTable MEMORY_TABLE;

    private final JSpinner DUMP_WIDTH_SPINNER;
    private final JCheckBox SHOW_HISTORY;
    private final JCheckBox SHOW_POINTERS;
    private final JSpinner UPDATE_DELAY;

    private static GridBagConstraints createConstraint(int fill, int margin, int x, int y, int width, int height, float weightX, float weightY) {
        GridBagConstraints constraint = new GridBagConstraints();
        constraint.fill = fill;
        constraint.insets = new Insets(margin, margin, margin, margin);
        constraint.gridx = x;
        constraint.gridy = y;
        constraint.gridwidth = width;
        constraint.gridheight = height;
        constraint.weightx = weightX;
        constraint.weighty = weightY;
        return constraint;
    }

    protected MemoryView(@NotNull Application parentApp) {
        super("Memory View");
        app = parentApp;

        setIconImage(new ImageIcon(System.class.getResource("/assets/memory_view.png")).getImage());

        setSize(800, 600);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        setLayout(new GridBagLayout());

        // Adding Options
        GridBagConstraints firstRow = createConstraint(GridBagConstraints.BOTH, 5, 0, 0, 1, 1, 1.0f, 0.0f);

        add(new JLabel("Words on each row:"), firstRow, true);
        SpinnerNumberModel dumpWidthModel = new SpinnerNumberModel(Byte.SIZE, Byte.SIZE, Byte.SIZE * Byte.SIZE, Byte.SIZE);
        DUMP_WIDTH_SPINNER = new JSpinner(dumpWidthModel);
        add(DUMP_WIDTH_SPINNER, firstRow, true);

        SHOW_HISTORY = new JCheckBox("Show names of executed instructions.");
        add(SHOW_HISTORY, firstRow, true);

        SHOW_POINTERS = new JCheckBox("Show { Instruction } and [ Stack ] Pointers.");
        add(SHOW_POINTERS, firstRow, true);

        add(new JLabel("Update interval:"), firstRow, true);
        SpinnerNumberModel updateDelayModel = new SpinnerNumberModel(1.0f, 0.1f, 5.0f, 0.1f);
        UPDATE_DELAY = new JSpinner(updateDelayModel);
        add(UPDATE_DELAY, firstRow, true);

        // Adding non-editable table
        MEMORY_TABLE = new JTable() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        // Removing table header and adding auto-resize
        MEMORY_TABLE.setTableHeader(null);
        MEMORY_TABLE.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        // Setting renderer for strings
        DefaultTableCellRenderer tableCellRenderer = (DefaultTableCellRenderer) MEMORY_TABLE.getDefaultRenderer(String.class);
        tableCellRenderer.setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
        // Adding table to Frame
        add(new JScrollPane(MEMORY_TABLE), createConstraint(GridBagConstraints.BOTH, 0, 0, 1, firstRow.gridx, 1, 1.0f, 1.0f));

        UPDATE_TIMER = new Timer(0, this::updateTable);
        UPDATE_TIMER.start();
    }

    public void add(@NotNull Component component, @NotNull GridBagConstraints constraints, boolean autoIncrementX) {
        add(component, constraints);
        if (autoIncrementX) constraints.gridx++;
    }

    public void updateTable(ActionEvent e) {
        UPDATE_TIMER.setDelay((int) ((double) UPDATE_DELAY.getValue() * 1000.0f));

        if (!isVisible()) return;

        Processor processor = app.currentProcessor;
        DefaultTableModel model = (DefaultTableModel) MEMORY_TABLE.getModel();
        if (processor == null) {
            model.setRowCount(0);
            model.setColumnCount(0);
            return;
        }

        // Getting copy of current history and pointers (Don't know if this is needed
        //  but it's here to prevent from getting newer instruction translations)
        HashMap<Integer, String> history = new HashMap<>(processor.HISTORY);
        int IP = processor.IP.value;
        int SP = processor.SP.value;

        int width = (int) DUMP_WIDTH_SPINNER.getValue();
        int memSize = processor.MEMORY.getSize();
        model.setRowCount((int) Math.ceil(memSize / (float) width));
        model.setColumnCount(width);

        for (int i = 0; i < memSize; i++) {
            int x = i % width;
            int y = i / width;

            String value = String.valueOf(processor.MEMORY.getValueAt(i));
            if (SHOW_HISTORY.isSelected() && history.containsKey(i))
                value = history.get(i);
            if (SHOW_POINTERS.isSelected())
                if (IP == i) value = "{ " + value + " }";
                else if (SP == i) value = "[ " + value + " ]";

            model.setValueAt(value, y, x);
        }
    }

}
