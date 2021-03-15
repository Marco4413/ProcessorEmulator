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

    public ProcessorConfigPanel(@NotNull ProcessorConfig config) {
        super();
        GridLayout layout = new GridLayout(2, 2);
        layout.setVgap(5);
        setLayout(layout);

        CONFIG = config;
        add(new JLabel("Word Bits: "));
        SpinnerNumberModel bitsModel = new SpinnerNumberModel(CONFIG.bits, Word.SizeBit8, Word.SizeBit24, Byte.SIZE);
        bitsSpinner = new JSpinner(bitsModel);
        add(bitsSpinner);

        add(new JLabel("Memory Size: "));
        SpinnerNumberModel memoryModel = new SpinnerNumberModel(CONFIG.memSize, Byte.SIZE, Word.MaskBit24, Byte.SIZE);
        memorySpinner = new JSpinner(memoryModel);
        add(memorySpinner);
    }

    public @NotNull ProcessorConfig apply() {
        CONFIG.bits = (int) bitsSpinner.getValue();
        CONFIG.memSize = (int) memorySpinner.getValue();
        return CONFIG;
    }
}
