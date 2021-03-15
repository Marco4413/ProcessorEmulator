package io.github.hds.pemu.app;

import io.github.hds.pemu.processor.ProcessorConfig;
import io.github.hds.pemu.processor.Word;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class ProcessorConfigPanel extends JPanel {

    private final @NotNull ProcessorConfig CONFIG;

    private final @NotNull JSpinner bitsSpinner;
    private final @NotNull JSpinner memorySpinner;
    private final @NotNull JSpinner clockSpinner;

    public ProcessorConfigPanel(@NotNull ProcessorConfig config) {
        super();
        GridLayout layout = new GridLayout(3, 2);
        layout.setVgap(5);
        setLayout(layout);

        CONFIG = config;
        add(new JLabel("Word Size (Bits): "));
        SpinnerNumberModel bitsModel = new SpinnerNumberModel(CONFIG.bits, Word.SizeBit8, Word.SizeBit24, Byte.SIZE);
        bitsSpinner = new JSpinner(bitsModel);
        add(bitsSpinner);

        add(new JLabel("Memory Size (Bytes): "));
        SpinnerNumberModel memoryModel = new SpinnerNumberModel(CONFIG.memSize, Byte.SIZE, Word.MaskBit24, Byte.SIZE);
        memorySpinner = new JSpinner(memoryModel);
        add(memorySpinner);

        add(new JLabel("Clock (Hz): "));
        SpinnerNumberModel clockModel = new SpinnerNumberModel(CONFIG.clock, 1, Integer.MAX_VALUE, 1);
        clockSpinner = new JSpinner(clockModel);
        add(clockSpinner);
    }

    public @NotNull ProcessorConfig apply() {
        CONFIG.bits = (int) bitsSpinner.getValue();
        CONFIG.memSize = (int) memorySpinner.getValue();
        CONFIG.clock = (int) clockSpinner.getValue();
        return CONFIG;
    }
}
