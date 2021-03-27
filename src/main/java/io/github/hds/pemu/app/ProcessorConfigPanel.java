package io.github.hds.pemu.app;

import io.github.hds.pemu.processor.Clock;
import io.github.hds.pemu.processor.ProcessorConfig;
import io.github.hds.pemu.processor.Word;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class ProcessorConfigPanel extends JPanel {

    private final @NotNull JSpinner BITS_SPINNER;
    private final @NotNull JSpinner MEMORY_SPINNER;
    private final @NotNull JSpinner CLOCK_SPINNER;

    protected ProcessorConfigPanel() {
        super();

        GridLayout layout = new GridLayout(0, 2);
        layout.setVgap(5);
        setLayout(layout);

        add(new JLabel("Word Size (Bits): "));
        SpinnerNumberModel bitsModel = new SpinnerNumberModel(Word.SizeBit8, Word.SizeBit8, Word.SizeBit24, Byte.SIZE);
        BITS_SPINNER = new JSpinner(bitsModel);
        add(BITS_SPINNER);

        add(new JLabel("Memory Size (Words): "));
        SpinnerNumberModel memoryModel = new SpinnerNumberModel(Byte.SIZE, Byte.SIZE, Word.MaskBit24, Byte.SIZE);
        MEMORY_SPINNER = new JSpinner(memoryModel);
        add(MEMORY_SPINNER);

        add(new JLabel("Clock (Hz): "));
        SpinnerNumberModel clockModel = new SpinnerNumberModel(Clock.MIN_CLOCK, Clock.MIN_CLOCK, Clock.MAX_CLOCK, 1);
        CLOCK_SPINNER = new JSpinner(clockModel);
        add(CLOCK_SPINNER);
    }

    public void setConfig(@NotNull ProcessorConfig config) {
        BITS_SPINNER.setValue(config.bits);
        MEMORY_SPINNER.setValue(config.memSize);
        CLOCK_SPINNER.setValue(config.clock);
    }

    public @NotNull ProcessorConfig getConfig() {
        return new ProcessorConfig(
            (int) BITS_SPINNER.getValue(),
            (int) MEMORY_SPINNER.getValue(),
            (int) CLOCK_SPINNER.getValue()
        );
    }
}
