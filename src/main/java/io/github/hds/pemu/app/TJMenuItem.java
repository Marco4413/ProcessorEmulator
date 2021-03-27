package io.github.hds.pemu.app;

import javax.swing.*;
import java.awt.*;
import java.util.function.Function;

public class TJMenuItem extends JMenuItem {

    private final Function<TJMenuItem, Boolean> ENABLE_CONDITION;

    public TJMenuItem(Function<TJMenuItem, Boolean> enableCondition) {
        super();
        ENABLE_CONDITION = enableCondition;
    }

    public TJMenuItem(Icon icon, Function<TJMenuItem, Boolean> enableCondition) {
        super(icon);
        ENABLE_CONDITION = enableCondition;
    }

    public TJMenuItem(String text, Function<TJMenuItem, Boolean> enableCondition) {
        super(text);
        ENABLE_CONDITION = enableCondition;
    }

    public TJMenuItem(Action a, Function<TJMenuItem, Boolean> enableCondition) {
        super(a);
        ENABLE_CONDITION = enableCondition;
    }

    public TJMenuItem(String text, Icon icon, Function<TJMenuItem, Boolean> enableCondition) {
        super(text, icon);
        ENABLE_CONDITION = enableCondition;
    }

    public TJMenuItem(String text, int mnemonic, Function<TJMenuItem, Boolean> enableCondition) {
        super(text, mnemonic);
        ENABLE_CONDITION = enableCondition;
    }

    @Override
    public boolean isEnabled() {
        if (ENABLE_CONDITION == null)
            return super.isEnabled();
        else return super.isEnabled() && ENABLE_CONDITION.apply(this);
    }

    @Override
    public Color getForeground() {
        // For some reason this doesn't happen by default
        return isEnabled() ? super.getForeground() : (new Color(UIManager.getColor("MenuItem.disabledForeground").getRGB()));
    }
}
