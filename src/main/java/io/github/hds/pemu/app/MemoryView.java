package io.github.hds.pemu.app;

import io.github.hds.pemu.processor.Processor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;

public class MemoryView extends JFrame {

    private static final String R_VALUES_FORMAT = "<html><table><tr><td>IP=%s</td><td>SP=%s</td></tr><tr><td>ZF=%s</td><td>CF=%s</td></tr></table>";
    private static final String R_VALUES_UNKNOWN = "?";

    private final Application app;

    private final Timer UPDATE_TIMER;
    private final JTable MEMORY_TABLE;

    private final JSpinner COLS_SPINNER;
    private final JSpinner UPDATE_DELAY;
    private final JCheckBox SHOW_AS_CHAR;
    private final JCheckBox SHOW_HISTORY;
    private final JCheckBox SHOW_POINTERS;
    private final JLabel REG_VALUES;

    protected MemoryView(@NotNull Application parentApp) {
        super("Memory View");
        app = parentApp;

        setIconImage(new ImageIcon(System.class.getResource("/assets/memory_view.png")).getImage());

        setSize(800, 600);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        setLayout(new GridBagLayout());

        // Adding Options
        addComponent(new JLabel("Words on each row:", JLabel.RIGHT), 0, 0);
        SpinnerNumberModel colsModel = new SpinnerNumberModel(Byte.SIZE, Byte.SIZE, Byte.SIZE * Byte.SIZE, Byte.SIZE);
        COLS_SPINNER = new JSpinner(colsModel);
        addComponent(COLS_SPINNER, 1, 0);

        addComponent(new JLabel("Update interval:", JLabel.RIGHT), 2, 0);
        SpinnerNumberModel updateDelayModel = new SpinnerNumberModel(1.0f, 0.01f, 5.0f, 0.01f);
        UPDATE_DELAY = new JSpinner(updateDelayModel);
        addComponent(UPDATE_DELAY, 3, 0);

        SHOW_AS_CHAR = new JCheckBox("Show values as chars");
        addComponent(SHOW_AS_CHAR, 0, 1);

        SHOW_HISTORY = new JCheckBox("Show names of executed instructions");
        addComponent(SHOW_HISTORY, 1, 1);

        SHOW_POINTERS = new JCheckBox("Show { Instruction } and [ Stack ] Pointers");
        addComponent(SHOW_POINTERS, 2, 1);

        REG_VALUES = new JLabel();
        addComponent(REG_VALUES, 3, 1);

        // Adding non-editable table
        MEMORY_TABLE = new JTable() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // We want to clear the selection if another component is focused
        //  Or if this Frame loses focus which makes the table lose focus
        MEMORY_TABLE.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                MEMORY_TABLE.clearSelection();
            }
        });

        // And we also want to clear the selection if we click outside the table
        //  In an empty spot of the Frame, I'm not sure if this is the best way of doing it
        final boolean[] isOutsideTable = { false };
        MEMORY_TABLE.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isOutsideTable[0] = false;
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isOutsideTable[0] = true;
            }
        });

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isOutsideTable[0]) MEMORY_TABLE.clearSelection();
            }
        });

        // Removing table header and adding auto-resize
        MEMORY_TABLE.setTableHeader(null);
        MEMORY_TABLE.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        // Setting renderer for strings
        DefaultTableCellRenderer tableCellRenderer = (DefaultTableCellRenderer) MEMORY_TABLE.getDefaultRenderer(String.class);
        tableCellRenderer.setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
        // Setting selection mode
        MEMORY_TABLE.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        MEMORY_TABLE.setCellSelectionEnabled(true);
        // Adding table to Frame
        addComponent(new JScrollPane(MEMORY_TABLE), 0, 2, 4, 1, 1.0f, 1.0f);

        UPDATE_TIMER = new Timer(0, this::updateFrame);
        updateFrame(null);
        UPDATE_TIMER.start();
    }

    public void addComponent(@NotNull Component component, int x, int y) {
        addComponent(component, x, y, 1, 1);
    }

    public void addComponent(@NotNull Component component, int x, int y, int w, int h) {
        addComponent(component, x, y, w, h, 1.0f, 0.0f);
    }

    public void addComponent(@NotNull Component component, int x, int y, int w, int h, float wX, float wY) {
        addComponent(component, x, y, w, h, wX, wY, 5);
    }

    public void addComponent(@NotNull Component component, int x, int y, int w, int h, float wX, float wY, int margin) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(margin, margin, margin, margin);
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.gridwidth = w;
        constraints.gridheight = h;
        constraints.weightx = wX;
        constraints.weighty = wY;
        add(component, constraints);
    }

    public void updateFrame(ActionEvent e) {
        UPDATE_TIMER.setDelay((int) ((double) UPDATE_DELAY.getValue() * 1000.0f));

        if (!isVisible()) return;

        Processor processor = app.currentProcessor;
        DefaultTableModel model = (DefaultTableModel) MEMORY_TABLE.getModel();
        if (processor == null) {
            model.setRowCount(0);
            model.setColumnCount(0);

            REG_VALUES.setText(String.format(R_VALUES_FORMAT, R_VALUES_UNKNOWN, R_VALUES_UNKNOWN, R_VALUES_UNKNOWN, R_VALUES_UNKNOWN));
            return;
        }

        // Getting copy of current history and pointers (Don't know if this is needed
        //  but it's here to prevent from getting newer instruction translations)
        HashMap<Integer, String> history = new HashMap<>(processor.HISTORY);
        int IP = processor.IP.value;
        int SP = processor.SP.value;
        boolean ZF = processor.ZERO.value;
        boolean CF = processor.CARRY.value;

        REG_VALUES.setText(String.format(R_VALUES_FORMAT, IP, SP, ZF, CF));

        int memSize = processor.MEMORY.getSize();
        int cols = (int) COLS_SPINNER.getValue();
        int rows = (int) Math.ceil(memSize / (float) cols);

        if (model.getColumnCount() != cols)
            model.setColumnCount(cols);

        if (model.getRowCount() != rows)
            model.setRowCount(rows);

        for (int i = 0; i < memSize; i++) {
            int x = i % cols;
            int y = i / cols;

            String value = SHOW_AS_CHAR.isSelected() ?
                    String.valueOf((char) processor.MEMORY.getValueAt(i)) : String.valueOf(processor.MEMORY.getValueAt(i));

            if (SHOW_HISTORY.isSelected() && history.containsKey(i))
                value = history.get(i);
            if (SHOW_POINTERS.isSelected())
                if (IP == i) value = "{ " + value + " }";
                else if (SP == i) value = "[ " + value + " ]";

            model.setValueAt(value, y, x);
        }
    }

}