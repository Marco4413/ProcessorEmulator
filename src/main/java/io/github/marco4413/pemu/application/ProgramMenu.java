package io.github.marco4413.pemu.application;

import io.github.marco4413.pemu.localization.ITranslatable;
import io.github.marco4413.pemu.utils.IconUtils;
import io.github.marco4413.pemu.localization.Translation;
import io.github.marco4413.pemu.localization.TranslationManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.KeyEvent;

public final class ProgramMenu extends JMenu implements ITranslatable {

    private final ApplicationGUI appGui;

    private final TJMenuItem VERIFY;
    private final TJMenuItem OBFUSCATE;

    private final ImageIcon ICON_VERIFY;
    private final ImageIcon ICON_OBFUSCATE;

    protected ProgramMenu(@NotNull ApplicationGUI parentAppGui) {
        super();
        appGui = parentAppGui;

        TranslationManager.addTranslationListener(this);

        ICON_VERIFY = IconUtils.importIcon("/assets/verify.png", ApplicationGUI.MENU_ITEM_ICON_SIZE);

        VERIFY = new TJMenuItem(i -> appGui.APP.getCurrentProgram() != null);
        VERIFY.setIcon(ICON_VERIFY);
        VERIFY.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
        VERIFY.addActionListener(e -> appGui.APP.verifyProgram());
        add(VERIFY);

        ICON_OBFUSCATE = IconUtils.importIcon("/assets/obfuscate.png", ApplicationGUI.MENU_ITEM_ICON_SIZE);

        OBFUSCATE = new TJMenuItem(i -> appGui.APP.getCurrentProgram() != null);
        OBFUSCATE.setIcon(ICON_OBFUSCATE);
        OBFUSCATE.addActionListener(e -> appGui.APP.obfuscateProgram());
        add(OBFUSCATE);
    }

    @Override
    public void updateTranslations(@NotNull Translation translation) {
        translation.translateComponent("programMenu", this);
        translation.translateComponent("programMenu.verify", VERIFY);
        translation.translateComponent("programMenu.obfuscate", OBFUSCATE);
    }
}
