package io.github.hds.pemu.app;

import io.github.hds.pemu.app.memorytable.MemoryTable;
import io.github.hds.pemu.config.ConfigEvent;
import io.github.hds.pemu.config.ConfigManager;
import io.github.hds.pemu.config.IConfigurable;
import io.github.hds.pemu.localization.ITranslatable;
import io.github.hds.pemu.localization.Translation;
import io.github.hds.pemu.localization.TranslationManager;
import io.github.hds.pemu.memory.flags.IFlag;
import io.github.hds.pemu.memory.registers.IRegister;
import io.github.hds.pemu.processor.IProcessor;
import io.github.hds.pemu.utils.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;

public class MemoryView extends JFrame implements ITranslatable, IConfigurable {

    private static final String R_VALUES_FORMAT = "<html><table><tr><td>IP=%s</td><td>SP=%s</td></tr><tr><td>ZF=%s</td><td>CF=%s</td></tr></table></html>";
    private static final String R_VALUES_UNKNOWN = "?";

    private final Application app;

    private final Timer UPDATE_TIMER;
    private final MemoryTable MEMORY_TABLE;

    private final JLabel COLS_LABEL;
    private final JLabel UPDATE_INTERVAL_LABEL;

    private final JSpinner COLS_SPINNER;
    private final JSpinner UPDATE_INTERVAL_SPINNER;
    private final JCheckBox SHOW_SELECTED_CELL_POINTER;
    private final JCheckBox SHOW_AS_CHAR;
    private final JCheckBox SHOW_HISTORY;
    private final JCheckBox SHOW_POINTERS;
    private final JLabel REG_VALUES;

    protected MemoryView(@NotNull Application parentApp) {
        super();
        app = parentApp;

        setIconImage(IconUtils.importIcon("/assets/memory_view.png", Application.FRAME_ICON_SIZE).getImage());

        setSize(Application.FRAME_WIDTH, Application.FRAME_HEIGHT);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        ConfigManager.addConfigListener(this);
        TranslationManager.addTranslationListener(this);

        setLayout(new GridBagLayout());

        // Adding Options
        COLS_LABEL = new JLabel("", JLabel.RIGHT);
        addComponent(COLS_LABEL, 0, 0);
        SpinnerNumberModel colsModel = new SpinnerNumberModel(Byte.SIZE, Byte.SIZE, Byte.SIZE * Byte.SIZE, Byte.SIZE);
        COLS_SPINNER = new JSpinner(colsModel);
        addComponent(COLS_SPINNER, 1, 0);

        UPDATE_INTERVAL_LABEL = new JLabel("", JLabel.RIGHT);
        addComponent(UPDATE_INTERVAL_LABEL, 2, 0);
        SpinnerNumberModel updateDelayModel = new SpinnerNumberModel(1.0f, 0.01f, 5.0f, 0.01f);
        UPDATE_INTERVAL_SPINNER = new JSpinner(updateDelayModel);
        addComponent(UPDATE_INTERVAL_SPINNER, 3, 0);

        SHOW_AS_CHAR = new JCheckBox();
        addComponent(SHOW_AS_CHAR, 0, 1);

        SHOW_HISTORY = new JCheckBox();
        addComponent(SHOW_HISTORY, 1, 1);

        SHOW_POINTERS = new JCheckBox();
        addComponent(SHOW_POINTERS, 2, 1);

        SHOW_SELECTED_CELL_POINTER = new JCheckBox();
        addComponent(SHOW_SELECTED_CELL_POINTER, 1, 2);

        REG_VALUES = new JLabel();
        addComponent(REG_VALUES, 3, 1, 1,  2);

        MEMORY_TABLE = new MemoryTable();

        // Adding table to Frame
        addComponent(new JScrollPane(MEMORY_TABLE), 0, 3, 4, 1, 1.0f, 1.0f);

        UPDATE_TIMER = new Timer(0, this::updateFrame);
        updateFrame(null);
        UPDATE_TIMER.start();
    }

    @Override
    public void updateTranslations(@NotNull Translation translation) {
        translation.translateFrame("memoryView", this);
        translation.translateComponent("memoryView.colsLabel", COLS_LABEL);
        translation.translateComponent("memoryView.updateIntervalLabel", UPDATE_INTERVAL_LABEL);
        translation.translateComponent("memoryView.showAsChar", SHOW_AS_CHAR);
        translation.translateComponent("memoryView.showHistory", SHOW_HISTORY);
        translation.translateComponent("memoryView.showSelectedCellPointer", SHOW_SELECTED_CELL_POINTER);
        SHOW_POINTERS.setText(StringUtils.format(translation.getOrDefault("memoryView.showPointers"), "{", "}", "[", "]"));
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
        // TODO: When you've got some time to look closely at this code, do it
        //        I think there are some things you can improve
        UPDATE_TIMER.setDelay((int) ((double) UPDATE_INTERVAL_SPINNER.getValue() * 1000.0f));

        if (!isVisible()) return;

        IProcessor processor = app.currentProcessor;
        DefaultTableModel model = (DefaultTableModel) MEMORY_TABLE.getModel();
        if (processor == null) {
            model.setRowCount(0);
            model.setColumnCount(0);

            REG_VALUES.setText(String.format(R_VALUES_FORMAT, R_VALUES_UNKNOWN, R_VALUES_UNKNOWN, R_VALUES_UNKNOWN, R_VALUES_UNKNOWN));
            return;
        }

        HashMap<Integer, String> history = processor.getInstructionHistory();
        IRegister IP = processor.getRegister("IP");
        IRegister SP = processor.getRegister("SP");
        IFlag ZF = processor.getFlag("ZF");
        IFlag CF = processor.getFlag("CF");

        REG_VALUES.setText(
                String.format(
                        R_VALUES_FORMAT,
                        IP == null ? -1 : IP.getValue(), SP == null ? -1 : SP.getValue(),
                        ZF == null ? -1 : ZF.getValue(), CF == null ? -1 : CF.getValue()
                )
        );

        int memSize = processor.getMemory().getSize();
        int cols = (int) COLS_SPINNER.getValue();
        int rows = (int) Math.ceil(memSize / (float) cols);

        if (model.getColumnCount() != cols)
            model.setColumnCount(cols);

        if (model.getRowCount() != rows)
            model.setRowCount(rows);

        int selectedRow = MEMORY_TABLE.getSelectedRow();
        int selectedCol = MEMORY_TABLE.getSelectedColumn();

        boolean enablePointedCellFeature = SHOW_SELECTED_CELL_POINTER.isSelected();
        MEMORY_TABLE.setPointedCellEnabled(enablePointedCellFeature);
        if (enablePointedCellFeature)
            MEMORY_TABLE.setPointedCell();

        for (int i = 0; i < memSize; i++) {
            int x = i % cols;
            int y = i / cols;

            int valueAtCurrentIndex = processor.getMemory().getValueAt(i);
            if (enablePointedCellFeature && y == selectedRow && x == selectedCol)
                MEMORY_TABLE.setPointedCell(valueAtCurrentIndex / cols, valueAtCurrentIndex % cols);

            String value = SHOW_AS_CHAR.isSelected() ?
                    String.valueOf((char) valueAtCurrentIndex) : String.valueOf(valueAtCurrentIndex);

            if (SHOW_HISTORY.isSelected() && history != null && history.containsKey(i))
                value = history.get(i);
            if (SHOW_POINTERS.isSelected())
                if (IP != null && IP.getValue() == i) value = "{ " + value + " }";
                else if (SP != null && SP.getValue() == i) value = "[ " + value + " ]";

            model.setValueAt(value, y, x);
        }
    }

    @Override
    public void loadConfig(@NotNull ConfigEvent e) {
        COLS_SPINNER.setValue(e.CONFIG.get(Integer.class, "memoryView.columns"));
        UPDATE_INTERVAL_SPINNER.setValue(e.CONFIG.get(Double.class, "memoryView.updateInterval"));
        SHOW_AS_CHAR.setSelected(e.CONFIG.get(Boolean.class, "memoryView.showAsChar"));
        SHOW_HISTORY.setSelected(e.CONFIG.get(Boolean.class, "memoryView.showHistory"));
        SHOW_POINTERS.setSelected(e.CONFIG.get(Boolean.class, "memoryView.showPointers"));
        SHOW_SELECTED_CELL_POINTER.setSelected(e.CONFIG.get(Boolean.class, "memoryView.showSelectedCellPointer"));
    }

    @Override
    public void saveConfig(@NotNull ConfigEvent e) {
        e.CONFIG.put("memoryView.columns", COLS_SPINNER.getValue());
        e.CONFIG.put("memoryView.updateInterval", UPDATE_INTERVAL_SPINNER.getValue());
        e.CONFIG.put("memoryView.showAsChar", SHOW_AS_CHAR.isSelected());
        e.CONFIG.put("memoryView.showHistory", SHOW_HISTORY.isSelected());
        e.CONFIG.put("memoryView.showPointers", SHOW_POINTERS.isSelected());
        e.CONFIG.put("memoryView.showSelectedCellPointer", SHOW_SELECTED_CELL_POINTER.isSelected());
    }

    @Override
    public void setDefaults(@NotNull ConfigEvent e) {
        e.CONFIG.put("memoryView.columns", 8);
        e.CONFIG.put("memoryView.updateInterval", 1.0f);
        e.CONFIG.put("memoryView.showAsChar", false);
        e.CONFIG.put("memoryView.showHistory", false);
        e.CONFIG.put("memoryView.showPointers", false);
        e.CONFIG.put("memoryView.showSelectedCellPointer", false);
    }
}
