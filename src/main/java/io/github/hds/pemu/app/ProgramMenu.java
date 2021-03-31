package io.github.hds.pemu.app;

import io.github.hds.pemu.utils.ITranslatable;
import io.github.hds.pemu.utils.IconUtils;
import io.github.hds.pemu.utils.Translation;
import io.github.hds.pemu.utils.TranslationManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.KeyEvent;

public class ProgramMenu extends JMenu implements ITranslatable {

    private final Application app;

    private final TJMenuItem VERIFY;
    private final TJMenuItem OBFUSCATE;

    private final ImageIcon ICON_VERIFY;
    private final ImageIcon ICON_OBFUSCATE;

    protected ProgramMenu(@NotNull Application parentApp) {
        super();
        app = parentApp;

        TranslationManager.addTranslationListener(this);

        ICON_VERIFY = IconUtils.importIcon("/assets/verify.png", Application.MENU_ITEM_ICON_SIZE);;

        VERIFY = new TJMenuItem(i -> app.currentProgram != null);
        VERIFY.setIcon(ICON_VERIFY);
        VERIFY.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
        VERIFY.addActionListener(app::verifyProgram);
        add(VERIFY);

        ICON_OBFUSCATE = IconUtils.importIcon("/assets/obfuscate.png", Application.MENU_ITEM_ICON_SIZE);;

        OBFUSCATE = new TJMenuItem(i -> app.currentProgram != null);
        OBFUSCATE.setIcon(ICON_OBFUSCATE);
        OBFUSCATE.addActionListener(app::obfuscateProgram);
        add(OBFUSCATE);
    }

    @Override
    public void updateTranslations(@NotNull Translation translation) {
        translation.translateComponent("programMenu", this);
        translation.translateComponent("programMenu.verify", VERIFY);
        translation.translateComponent("programMenu.obfuscate", OBFUSCATE);
    }
}
