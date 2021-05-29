package io.github.hds.pemu.app;

import io.github.hds.pemu.config.ConfigManager;
import io.github.hds.pemu.config.IConfigurable;
import io.github.hds.pemu.localization.ITranslatable;
import io.github.hds.pemu.localization.Translation;
import io.github.hds.pemu.localization.TranslationManager;
import io.github.hds.pemu.memory.flags.IFlag;
import io.github.hds.pemu.memory.registers.IRegister;
import io.github.hds.pemu.processor.IProcessor;
import io.github.hds.pemu.tokenizer.keyvalue.KeyValueData;
import io.github.hds.pemu.utils.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;

public class MemoryView extends JFrame implements ITranslatable, IConfigurable {

    private static final String R_VALUES_FORMAT = "<html><table><tr><td>IP=%s</td><td>SP=%s</td></tr><tr><td>ZF=%s</td><td>CF=%s</td></tr></table></html>";
    private static final String R_VALUES_UNKNOWN = "?";

    private final Application app;

    private final Timer UPDATE_TIMER;
    private final JTable MEMORY_TABLE;

    private final JLabel COLS_LABEL;
    private final JLabel UPDATE_INTERVAL_LABEL;

    private final JSpinner COLS_SPINNER;
    private final JSpinner UPDATE_INTERVAL_SPINNER;
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

    @Override
    public void updateTranslations(@NotNull Translation translation) {
        translation.translateFrame("memoryView", this);
        translation.translateComponent("memoryView.colsLabel", COLS_LABEL);
        translation.translateComponent("memoryView.updateIntervalLabel", UPDATE_INTERVAL_LABEL);
        translation.translateComponent("memoryView.showAsChar", SHOW_AS_CHAR);
        translation.translateComponent("memoryView.showHistory", SHOW_HISTORY);
        translation.translateComponent("memoryView.showPointers", SHOW_POINTERS);
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

        for (int i = 0; i < memSize; i++) {
            int x = i % cols;
            int y = i / cols;

            String value = SHOW_AS_CHAR.isSelected() ?
                    String.valueOf((char) processor.getMemory().getValueAt(i)) : String.valueOf(processor.getMemory().getValueAt(i));

            if (SHOW_HISTORY.isSelected() && history != null && history.containsKey(i))
                value = history.get(i);
            if (SHOW_POINTERS.isSelected())
                if (IP != null && IP.getValue() == i) value = "{ " + value + " }";
                else if (SP != null && SP.getValue() == i) value = "[ " + value + " ]";

            model.setValueAt(value, y, x);
        }
    }

    @Override
    public void loadConfig(@NotNull KeyValueData config) {
        COLS_SPINNER.setValue(config.get(Integer.class, "memoryView.columns"));
        UPDATE_INTERVAL_SPINNER.setValue(config.get(Double.class, "memoryView.updateInterval"));
        SHOW_AS_CHAR.setSelected(config.get(Boolean.class, "memoryView.showAsChar"));
        SHOW_HISTORY.setSelected(config.get(Boolean.class, "memoryView.showHistory"));
        SHOW_POINTERS.setSelected(config.get(Boolean.class, "memoryView.showPointers"));
    }

    @Override
    public void saveConfig(@NotNull KeyValueData config) {
        config.put("memoryView.columns", COLS_SPINNER.getValue());
        config.put("memoryView.updateInterval", UPDATE_INTERVAL_SPINNER.getValue());
        config.put("memoryView.showAsChar", SHOW_AS_CHAR.isSelected());
        config.put("memoryView.showHistory", SHOW_HISTORY.isSelected());
        config.put("memoryView.showPointers", SHOW_POINTERS.isSelected());
    }

    @Override
    public void setDefaults(@NotNull KeyValueData defaultConfig) {
        defaultConfig.put("memoryView.columns", 8);
        defaultConfig.put("memoryView.updateInterval", 1.0f);
        defaultConfig.put("memoryView.showAsChar", false);
        defaultConfig.put("memoryView.showHistory", false);
        defaultConfig.put("memoryView.showPointers", false);
    }
}
