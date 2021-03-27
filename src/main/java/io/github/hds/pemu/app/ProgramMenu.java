package io.github.hds.pemu.app;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
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

        ICON_VERIFY = new ImageIcon(
                new ImageIcon(System.class.getResource("/assets/verify.png"))
                        .getImage().getScaledInstance(Application.MENU_ITEM_ICON_SIZE, Application.MENU_ITEM_ICON_SIZE, Image.SCALE_SMOOTH)
        );

        VERIFY = new TJMenuItem("Verify", 'V', i -> app.currentProgram != null);
        VERIFY.setIcon(ICON_VERIFY);
        VERIFY.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
        VERIFY.addActionListener(app::verifyProgram);
        add(VERIFY);

        ICON_OBFUSCATE = new ImageIcon(
                new ImageIcon(System.class.getResource("/assets/obfuscate.png"))
                        .getImage().getScaledInstance(Application.MENU_ITEM_ICON_SIZE, Application.MENU_ITEM_ICON_SIZE, Image.SCALE_SMOOTH)
        );

        OBFUSCATE = new TJMenuItem("Obfuscate", 'O', i -> app.currentProgram != null);
        OBFUSCATE.setIcon(ICON_OBFUSCATE);
        OBFUSCATE.addActionListener(app::obfuscateProgram);
        add(OBFUSCATE);
    }
}
