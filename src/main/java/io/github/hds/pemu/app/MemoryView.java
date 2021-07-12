package io.github.hds.pemu.app;

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

public final class MemoryView extends JFrame implements ITranslatable, IConfigurable {

    private static final String UNKNOWN_PROCESSOR = "?";

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
    private final JLabel RF_VALUES;

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

        RF_VALUES = new JLabel();
        addComponent(RF_VALUES, 3, 1, 1,  2);

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
        // We update the update timer
        UPDATE_TIMER.setDelay((int) ((double) UPDATE_INTERVAL_SPINNER.getValue() * 1000.0f));
        // If this frame isn't visible we don't bother updating it
        if (!isVisible()) return;

        // Getting the processor that is currently attached to the app
        IProcessor processor = app.currentProcessor;
        DefaultTableModel model = (DefaultTableModel) MEMORY_TABLE.getModel();
        // If no processor was found then we remove the table and set registers to unknown values
        if (processor == null) {
            model.setRowCount(0);
            model.setColumnCount(0);

            RF_VALUES.setText(UNKNOWN_PROCESSOR);
            return;
        }

        // Getting the history of executed processor instructions
        HashMap<Integer, String> history = processor.getInstructionHistory();

        // These will be populated when looking through registers
        Integer IPValue = null;
        Integer SPValue = null;

        /* Register/Flag Table */
        StringBuilder rfTable = new StringBuilder("<html><table><tr>");

        // Appending all registers to the table
        for (IRegister register : processor.getRegisters()) {
            String shortName = register.getShortName();
            int value = register.getValue();

            // If the name is either IP or SP,
            //  populate the above defined variables
            if (IPValue == null && shortName.equals("IP"))
                IPValue = value;
            else if (SPValue == null && shortName.equals("SP"))
                SPValue = value;

            rfTable.append("<td>")
                   .append(shortName)
                   .append("=")
                   .append(value)
                   .append("</td>");
        }

        rfTable.append("</tr><tr>");

        // Appending all flags to the table
        for (IFlag flag : processor.getFlags()) {
            rfTable.append("<td>")
                   .append(flag.getShortName())
                   .append("=")
                   .append(flag.getValue())
                   .append("</td>");
        }

        rfTable.append("</tr></table></html>");

        RF_VALUES.setText(rfTable.toString());

        // Making the table large enough to fit all the processor's memory
        int memSize = processor.getMemory().getSize();
        int cols = (int) COLS_SPINNER.getValue();
        int rows = (int) Math.ceil(memSize / (float) cols);

        if (model.getColumnCount() != cols)
            model.setColumnCount(cols);

        if (model.getRowCount() != rows)
            model.setRowCount(rows);

        // Getting the currently selected column and row
        int selectedRow = MEMORY_TABLE.getSelectedRow();
        int selectedCol = MEMORY_TABLE.getSelectedColumn();

        // If the cell pointed by the currently selected one needs to be
        //  highlighted we do that
        boolean enablePointedCellFeature = SHOW_SELECTED_CELL_POINTER.isSelected();
        MEMORY_TABLE.setPointedCellEnabled(enablePointedCellFeature);
        if (enablePointedCellFeature)
            MEMORY_TABLE.clearPointedCell();

        // For each memory address
        for (int i = 0; i < memSize; i++) {
            // We get its position on the table
            int x = i % cols;
            int y = i / cols;

            // We get the currently stored value at that address
            int valueAtCurrentIndex = processor.getMemory().getValueAt(i);
            // Highlighting pointed cell if necessary
            if (enablePointedCellFeature && y == selectedRow && x == selectedCol)
                MEMORY_TABLE.setPointedCell(valueAtCurrentIndex / cols, valueAtCurrentIndex % cols);

            // Getting the value to show to the user
            String value;
            if (SHOW_HISTORY.isSelected() && history != null && history.containsKey(i))
                // If the current value is an executed instruction use its name
                value = history.get(i);
            else if (SHOW_AS_CHAR.isSelected()) {
                // If the character can't be typed
                if (Character.isISOControl(valueAtCurrentIndex)) {
                    final String ESCAPE_CHARACTER = "\\";
                    // If it's a special character convert it
                    if (StringUtils.SpecialCharacters.isSpecialCharacter(valueAtCurrentIndex))
                        value = StringUtils.SpecialCharacters.toString((char) valueAtCurrentIndex, ESCAPE_CHARACTER);
                        // Else put it as a number with a backslash in front of it (That's done to differentiate between '0' and 0)
                    else value = ESCAPE_CHARACTER + valueAtCurrentIndex;
                    // Else if the character can be typed then show it
                } else value = String.valueOf((char) valueAtCurrentIndex);
            } else value = String.valueOf(valueAtCurrentIndex);

            // If the current cell is pointed by either IP or SP put the corresponding brackets
            if (SHOW_POINTERS.isSelected()) {
                if (IPValue != null && IPValue == i) value = "{ " + value + " }";
                else if (SPValue != null && SPValue == i) value = "[ " + value + " ]";
            }

            // Setting the value to be shown on the table
            model.setValueAt(value, y, x);
        }
    }

    @Override
    // Suppressing all, because the app should throw if the config isn't a good one
    @SuppressWarnings("all")
    public void loadConfig(@NotNull ConfigEvent e) {
        COLS_SPINNER.setValue(e.config.get(Integer.class, "memoryView.columns"));
        UPDATE_INTERVAL_SPINNER.setValue(e.config.get(Double.class, "memoryView.updateInterval"));
        SHOW_AS_CHAR.setSelected(e.config.get(Boolean.class, "memoryView.showAsChar"));
        SHOW_HISTORY.setSelected(e.config.get(Boolean.class, "memoryView.showHistory"));
        SHOW_POINTERS.setSelected(e.config.get(Boolean.class, "memoryView.showPointers"));
        SHOW_SELECTED_CELL_POINTER.setSelected(e.config.get(Boolean.class, "memoryView.showSelectedCellPointer"));
    }

    @Override
    public void saveConfig(@NotNull ConfigEvent e) {
        e.config.put("memoryView.columns", COLS_SPINNER.getValue());
        e.config.put("memoryView.updateInterval", UPDATE_INTERVAL_SPINNER.getValue());
        e.config.put("memoryView.showAsChar", SHOW_AS_CHAR.isSelected());
        e.config.put("memoryView.showHistory", SHOW_HISTORY.isSelected());
        e.config.put("memoryView.showPointers", SHOW_POINTERS.isSelected());
        e.config.put("memoryView.showSelectedCellPointer", SHOW_SELECTED_CELL_POINTER.isSelected());
    }

    @Override
    public void setDefaults(@NotNull ConfigEvent e) {
        e.config.put("memoryView.columns", 8);
        e.config.put("memoryView.updateInterval", 1.0f);
        e.config.put("memoryView.showAsChar", false);
        e.config.put("memoryView.showHistory", false);
        e.config.put("memoryView.showPointers", false);
        e.config.put("memoryView.showSelectedCellPointer", false);
    }
}
