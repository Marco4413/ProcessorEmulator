package io.github.hds.pemu.app;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class DumpMemoryPanel extends JPanel {

    public static class DumpMemorySettings {
        public final int WIDTH;
        public final boolean SHOW_HISTORY;
        public final boolean SHOW_POINTERS;

        protected DumpMemorySettings(int width, boolean showHistory, boolean showPointers) {
            WIDTH = width;
            SHOW_HISTORY = showHistory;
            SHOW_POINTERS = showPointers;
        }
    }

    private final JSpinner DUMP_WIDTH_SPINNER;
    private final JCheckBox SHOW_HISTORY;
    private final JCheckBox SHOW_POINTERS;

    protected DumpMemoryPanel() {
        super();

        GridLayout layout = new GridLayout(3, 2);
        layout.setVgap(5);
        setLayout(layout);

        add(new JLabel("Words on each line:"));
        SpinnerNumberModel dumpWidthModel = new SpinnerNumberModel(Byte.SIZE, Byte.SIZE, Byte.SIZE * Byte.SIZE, Byte.SIZE);
        DUMP_WIDTH_SPINNER = new JSpinner(dumpWidthModel);
        add(DUMP_WIDTH_SPINNER);

        add(new JLabel("Show names of executed instructions:"));
        SHOW_HISTORY = new JCheckBox();
        add(SHOW_HISTORY);

        add(new JLabel("Show {Instruction} and [Stack] Pointers:"));
        SHOW_POINTERS = new JCheckBox();
        add(SHOW_POINTERS);
    }

    public @NotNull DumpMemorySettings getSettings() {
        return new DumpMemorySettings((int) DUMP_WIDTH_SPINNER.getValue(), SHOW_HISTORY.isSelected(), SHOW_POINTERS.isSelected());
    }

}
