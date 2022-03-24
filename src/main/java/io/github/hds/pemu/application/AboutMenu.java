package io.github.hds.pemu.application;

import io.github.hds.pemu.localization.ITranslatable;
import io.github.hds.pemu.localization.Translation;
import io.github.hds.pemu.localization.TranslationManager;
import io.github.hds.pemu.utils.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.net.URI;

public final class AboutMenu extends JMenu implements ITranslatable {

    private final ApplicationGUI appGui;
    private static final String WEBPAGE = "https://github.com/hds536jhmk/ProcessorEmulator";
    private static final String DOCUMENTATION = "https://github.com/hds536jhmk/ProcessorEmulator/blob/master/DOCUMENTATION.md";

    private final JMenuItem OPEN_WEBPAGE;
    private final JMenuItem OPEN_DOCUMENTATION;

    private final ImageIcon ICON_OPEN_WEBPAGE;
    private final ImageIcon ICON_OPEN_DOCUMENTATION;

    private @NotNull String localeOpenLinkErrorTitle = "";
    private @NotNull String localeOpenLinkErrorMsg = "";

    protected AboutMenu(@NotNull ApplicationGUI parentAppGui) {
        super();
        appGui = parentAppGui;

        TranslationManager.addTranslationListener(this);

        ICON_OPEN_WEBPAGE = IconUtils.importIcon("/assets/webpage.png", ApplicationGUI.MENU_ITEM_ICON_SIZE);
        ICON_OPEN_DOCUMENTATION = IconUtils.importIcon("/assets/documentation.png", ApplicationGUI.MENU_ITEM_ICON_SIZE);

        OPEN_WEBPAGE = new JMenuItem();
        OPEN_WEBPAGE.setIcon(ICON_OPEN_WEBPAGE);
        OPEN_WEBPAGE.addActionListener(e -> openURL(WEBPAGE));
        add(OPEN_WEBPAGE);

        OPEN_DOCUMENTATION = new JMenuItem();
        OPEN_DOCUMENTATION.setIcon(ICON_OPEN_DOCUMENTATION);
        OPEN_DOCUMENTATION.addActionListener(e -> openURL(DOCUMENTATION));
        add(OPEN_DOCUMENTATION);
    }

    private void openURL(@NotNull String url) {
        boolean failed = true;
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE))
                try {
                    desktop.browse(new URI(url));
                    failed = false;
                } catch (Exception ignored) { }
        }

        if (failed)
            JOptionPane.showMessageDialog(this, StringUtils.format(localeOpenLinkErrorMsg, url), localeOpenLinkErrorTitle, JOptionPane.WARNING_MESSAGE);
    }

    @Override
    public void updateTranslations(@NotNull Translation translation) {
        translation.translateComponent("aboutMenu", this);
        translation.translateComponent("aboutMenu.openWebpage", OPEN_WEBPAGE);
        translation.translateComponent("aboutMenu.openDocumentation", OPEN_DOCUMENTATION);
        localeOpenLinkErrorTitle = translation.getOrDefault("aboutMenu.openLinkErrorTitle");
        localeOpenLinkErrorMsg = translation.getOrDefault("aboutMenu.openLinkErrorMsg");
    }
}
