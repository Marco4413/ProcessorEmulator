package io.github.hds.pemu.app;

import io.github.hds.pemu.instructions.Instructions;
import io.github.hds.pemu.localization.ITranslatable;
import io.github.hds.pemu.localization.Translation;
import io.github.hds.pemu.localization.TranslationManager;
import io.github.hds.pemu.processor.ProcessorConfig;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public final class ProcessorConfigPanel extends JPanel implements ITranslatable {

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
        SpinnerNumberModel bitsModel = new SpinnerNumberModel(ProcessorConfig.DEFAULT_BITS, ProcessorConfig.MIN_BITS, ProcessorConfig.MAX_BITS, Byte.SIZE);
        BITS_SPINNER = new JSpinner(bitsModel);
        add(BITS_SPINNER);

        MEMORY_LABEL = new JLabel("Memory Size (Words): ");
        add(MEMORY_LABEL);
        SpinnerNumberModel memoryModel = new SpinnerNumberModel(ProcessorConfig.DEFAULT_MEMORY_SIZE, ProcessorConfig.MIN_MEMORY_SIZE, ProcessorConfig.MAX_MEMORY_SIZE, Byte.SIZE);
        MEMORY_SPINNER = new JSpinner(memoryModel);
        add(MEMORY_SPINNER);

        CLOCK_LABEL = new JLabel("Clock (Hz): ");
        add(CLOCK_LABEL);
        SpinnerNumberModel clockModel = new SpinnerNumberModel(ProcessorConfig.DEFAULT_CLOCK, ProcessorConfig.MIN_CLOCK, ProcessorConfig.MAX_CLOCK, 1);
        CLOCK_SPINNER = new JSpinner(clockModel);
        add(CLOCK_SPINNER);
    }

    public void setConfig(@NotNull ProcessorConfig config) {
        BITS_SPINNER.setValue(config.getBits());
        MEMORY_SPINNER.setValue(config.getMemorySize());
        CLOCK_SPINNER.setValue(config.getClock());
    }

    public @NotNull ProcessorConfig getConfig() {
        return new ProcessorConfig(
                Instructions.SET,
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
