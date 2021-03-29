package io.github.hds.pemu.app;

import io.github.hds.pemu.utils.IconUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.KeyEvent;

public class ProgramMenu extends JMenu {

    private final Application app;

    private final TJMenuItem VERIFY;
    private final TJMenuItem OBFUSCATE;

    private final ImageIcon ICON_VERIFY;
    private final ImageIcon ICON_OBFUSCATE;

    protected ProgramMenu(@NotNull Application parentApp) {
        super("Program");
        app = parentApp;

        ICON_VERIFY = IconUtils.importIcon("/assets/verify.png", Application.MENU_ITEM_ICON_SIZE);;

        VERIFY = new TJMenuItem("Verify", 'V', i -> app.currentProgram != null);
        VERIFY.setIcon(ICON_VERIFY);
        VERIFY.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
        VERIFY.addActionListener(app::verifyProgram);
        add(VERIFY);

        ICON_OBFUSCATE = IconUtils.importIcon("/assets/obfuscate.png", Application.MENU_ITEM_ICON_SIZE);;

        OBFUSCATE = new TJMenuItem("Obfuscate", 'O', i -> app.currentProgram != null);
        OBFUSCATE.setIcon(ICON_OBFUSCATE);
        OBFUSCATE.addActionListener(app::obfuscateProgram);
        add(OBFUSCATE);
    }
}
