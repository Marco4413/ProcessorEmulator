package io.github.hds.pemu.console;

import io.github.hds.pemu.application.gui.ApplicationGUI;
import io.github.hds.pemu.application.gui.GFileDialog;
import io.github.hds.pemu.application.gui.TJMenuItem;
import io.github.hds.pemu.localization.ITranslatable;
import io.github.hds.pemu.localization.Translation;
import io.github.hds.pemu.localization.TranslationManager;
import io.github.hds.pemu.utils.IconUtils;
import io.github.hds.pemu.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.PrintWriter;
import java.util.function.Function;

public final class ConsoleContextualMenu extends JPopupMenu implements ITranslatable {
    private final TJMenuItem SAVE;
    private final TJMenuItem CLEAR;
    private final TJMenuItem RESET_FONT_SIZE;

    private final ImageIcon ICON_CLEAR;
    private final ImageIcon ICON_RESET_ZOOM;

    private static ConsoleContextualMenu INSTANCE;

    private @NotNull String localeSaveErrorPanelTitle = "";
    private @NotNull String localeSaveErrorPanelMsg = "";

    private ConsoleContextualMenu() {
        super();

        TranslationManager.addTranslationListener(this);

        Function<TJMenuItem, Boolean> enableCondition = i -> getInvoker() instanceof ConsoleComponent && ((ConsoleComponent) getInvoker()).getText().length() > 0;
        SAVE = new TJMenuItem(enableCondition);
        SAVE.setIcon(GFileDialog.ICON_SAVE);
        SAVE.addActionListener(this::saveConsole);
        add(SAVE);

        ICON_CLEAR = IconUtils.importIcon("/assets/clear.png", ApplicationGUI.MENU_ITEM_ICON_SIZE);

        CLEAR = new TJMenuItem(enableCondition);
        CLEAR.setIcon(ICON_CLEAR);
        CLEAR.addActionListener(this::clearConsole);
        add(CLEAR);

        ICON_RESET_ZOOM = IconUtils.importIcon("/assets/reset_zoom.png", ApplicationGUI.MENU_ITEM_ICON_SIZE);

        RESET_FONT_SIZE = new TJMenuItem(i -> getInvoker() instanceof ConsoleComponent && ((ConsoleComponent) getInvoker()).getFontSize() != ConsoleComponent.DEFAULT_FONT_SIZE);
        RESET_FONT_SIZE.setIcon(ICON_RESET_ZOOM);
        RESET_FONT_SIZE.addActionListener(this::resetZoom);
        add(RESET_FONT_SIZE);
    }

    protected static @NotNull ConsoleContextualMenu getInstance() {
        if (INSTANCE == null) INSTANCE = new ConsoleContextualMenu();
        return INSTANCE;
    }

    @Override
    public void updateTranslations(@NotNull Translation translation) {
        translation.translateComponent("consoleContextualMenu.save", SAVE);
        translation.translateComponent("consoleContextualMenu.clear", CLEAR);
        translation.translateComponent("consoleContextualMenu.resetZoom", RESET_FONT_SIZE);

        localeSaveErrorPanelTitle = translation.getOrDefault("consoleContextualMenu.saveErrorPanelTitle");
        localeSaveErrorPanelMsg = translation.getOrDefault("consoleContextualMenu.saveErrorPanelMsg");
    }

    public void saveConsole(ActionEvent e) {
        if (!(getInvoker() instanceof ConsoleComponent)) return;
        GFileDialog gFileDialog = GFileDialog.getInstance();
        if (gFileDialog.showSaveDialog(this, null, GFileDialog.getTextFileFilter()) == JFileChooser.APPROVE_OPTION) {
            File file = gFileDialog.getSelectedFile();
            try {
                PrintWriter writer = new PrintWriter(file, "UTF-8");
                writer.print(((ConsoleComponent) getInvoker()).getText());
                writer.close();
            } catch (Exception err) {
                JOptionPane.showMessageDialog(
                        this, StringUtils.format(localeSaveErrorPanelMsg, file.getName(), err.getMessage()),
                        StringUtils.format(localeSaveErrorPanelTitle, gFileDialog.getDialogTitle()), JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    public void clearConsole(ActionEvent e) {
        if (!(getInvoker() instanceof ConsoleComponent)) return;
        ((ConsoleComponent) getInvoker()).clear();
    }

    public void resetZoom(ActionEvent e) {
        if (!(getInvoker() instanceof ConsoleComponent)) return;
        ((ConsoleComponent) getInvoker()).resetFontSize();
    }
}
