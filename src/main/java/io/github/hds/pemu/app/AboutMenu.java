package io.github.hds.pemu.app;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.net.URI;

public class AboutMenu extends JMenu {

    private final Application app;
    private static final String WEBPAGE = "https://github.com/hds536jhmk/ProcessorEmulator";

    private final JMenuItem OPEN_WEBPAGE;

    private final ImageIcon ICON_OPEN_WEBPAGE;

    protected AboutMenu(@NotNull Application parentApp) {
        super("About");
        app = parentApp;

        setMnemonic('A');

        ICON_OPEN_WEBPAGE = new ImageIcon(
                new ImageIcon(System.class.getResource("/assets/webpage.png"))
                        .getImage().getScaledInstance(Application.MENU_ITEM_ICON_SIZE, Application.MENU_ITEM_ICON_SIZE, Image.SCALE_SMOOTH)
        );

        OPEN_WEBPAGE = new JMenuItem("Open Webpage", 'O');
        OPEN_WEBPAGE.setIcon(ICON_OPEN_WEBPAGE);
        OPEN_WEBPAGE.addActionListener(
                e -> {
                    boolean failed = true;
                    if (Desktop.isDesktopSupported()) {
                        Desktop desktop = Desktop.getDesktop();
                        if (desktop.isSupported(Desktop.Action.BROWSE))
                            try {
                                desktop.browse(new URI(WEBPAGE));
                                failed = false;
                            } catch (Exception ignored) { }
                    }

                    if (failed)
                        JOptionPane.showMessageDialog(this, "Couldn't open link: " + WEBPAGE, "Failed to open Webpage.", JOptionPane.WARNING_MESSAGE);
                }
        );
        add(OPEN_WEBPAGE);
    }

}
