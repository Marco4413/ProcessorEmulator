package io.github.hds.pemu.app;

import io.github.hds.pemu.processor.Clock;
import io.github.hds.pemu.processor.ProcessorConfig;
import io.github.hds.pemu.processor.Word;
import io.github.hds.pemu.utils.ITranslatable;
import io.github.hds.pemu.utils.Translation;
import io.github.hds.pemu.utils.TranslationManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class ProcessorConfigPanel extends JPanel implements ITranslatable {

    private final @NotNull JLabel BITS_LABEL;
    private final @NotNull JLabel MEMORY_LABEL;
    private final @NotNull JLabel CLOCK_LABEL;

    private final @NotNull JSpinner BITS_SPINNER;
    private final @NotNull JSpinner MEMORY_SPINNER;
    private final @NotNull JSpinner CLOCK_SPINNER;

    protected ProcessorConfigPanel() {
        super();

        TranslationManager.addTranslationListener(this);

        GridLayout layout = new GridLayout(0, 2);
        layout.setVgap(5);
        setLayout(layout);

        BITS_LABEL = new JLabel("Word Size (Bits): ");
        add(BITS_LABEL);
        SpinnerNumberModel bitsModel = new SpinnerNumberModel(Word.SizeBit8, Word.SizeBit8, Word.SizeBit24, Byte.SIZE);
        BITS_SPINNER = new JSpinner(bitsModel);
        add(BITS_SPINNER);

        MEMORY_LABEL = new JLabel("Memory Size (Words): ");
        add(MEMORY_LABEL);
        SpinnerNumberModel memoryModel = new SpinnerNumberModel(Byte.SIZE, Byte.SIZE, Word.MaskBit24, Byte.SIZE);
        MEMORY_SPINNER = new JSpinner(memoryModel);
        add(MEMORY_SPINNER);

        CLOCK_LABEL = new JLabel("Clock (Hz): ");
        add(CLOCK_LABEL);
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

    @Override
    public void updateTranslations(@NotNull Translation translation) {
        translation.translateComponent("processorConfigPanel.bitsLabel", BITS_LABEL);
        translation.translateComponent("processorConfigPanel.memoryLabel", MEMORY_LABEL);
        translation.translateComponent("processorConfigPanel.clockLabel", CLOCK_LABEL);
    }
}
