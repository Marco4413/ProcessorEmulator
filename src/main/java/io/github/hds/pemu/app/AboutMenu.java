package io.github.hds.pemu.app;

import io.github.hds.pemu.localization.ITranslatable;
import io.github.hds.pemu.localization.Translation;
import io.github.hds.pemu.localization.TranslationManager;
import io.github.hds.pemu.utils.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.net.URI;

public class AboutMenu extends JMenu implements ITranslatable {

    private final Application app;
    private static final String WEBPAGE = "https://github.com/hds536jhmk/ProcessorEmulator";

    private final JMenuItem OPEN_WEBPAGE;

    private final ImageIcon ICON_OPEN_WEBPAGE;

    private @NotNull String localeOpenLinkErrorTitle = "";
    private @NotNull String localeOpenLinkErrorMsg = "";

    protected AboutMenu(@NotNull Application parentApp) {
        super();
        app = parentApp;

        TranslationManager.addTranslationListener(this);

        ICON_OPEN_WEBPAGE = IconUtils.importIcon("/assets/webpage.png", Application.MENU_ITEM_ICON_SIZE);

        OPEN_WEBPAGE = new JMenuItem();
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
                        JOptionPane.showMessageDialog(this, StringUtils.format(localeOpenLinkErrorMsg, WEBPAGE), localeOpenLinkErrorTitle, JOptionPane.WARNING_MESSAGE);
                }
        );
        add(OPEN_WEBPAGE);
    }

    @Override
    public void updateTranslations(@NotNull Translation translation) {
        translation.translateComponent("aboutMenu", this);
        translation.translateComponent("aboutMenu.openWebpage", OPEN_WEBPAGE);
        localeOpenLinkErrorTitle = translation.getOrDefault("aboutMenu.openLinkErrorTitle");
        localeOpenLinkErrorMsg = translation.getOrDefault("aboutMenu.openLinkErrorMsg");
    }
}
