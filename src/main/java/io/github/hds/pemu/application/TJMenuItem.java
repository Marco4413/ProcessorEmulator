package io.github.hds.pemu.application;

import javax.swing.*;
import java.util.function.Function;

public class TJMenuItem extends JMenuItem {

    private final Function<TJMenuItem, Boolean> ENABLE_CONDITION;

    public TJMenuItem(Function<TJMenuItem, Boolean> enableCondition) {
        super();
        ENABLE_CONDITION = enableCondition;

        setModel(new DefaultButtonModel() {
            @Override
            public boolean isEnabled() {
                return TJMenuItem.this.isEnabled();
            }
        });
    }

    @Override
    public boolean isEnabled() {
        if (ENABLE_CONDITION == null)
            return super.isEnabled();
        else return super.isEnabled() && ENABLE_CONDITION.apply(this);
    }

}
