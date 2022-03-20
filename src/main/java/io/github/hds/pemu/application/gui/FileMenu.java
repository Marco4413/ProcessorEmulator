package io.github.hds.pemu.application.gui;

import io.github.hds.pemu.localization.ITranslatable;
import io.github.hds.pemu.localization.Translation;
import io.github.hds.pemu.localization.TranslationManager;
import io.github.hds.pemu.plugins.IPlugin;
import io.github.hds.pemu.plugins.PluginManager;
import io.github.hds.pemu.utils.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public final class FileMenu extends JMenu implements ITranslatable {

    private final ApplicationGUI appGui;

    private final JMenuItem OPEN_PROGRAM;
    private final JMenuItem CHANGE_LANGUAGE;
    private final JMenuItem LOAD_PLUGIN;
    private final JMenuItem QUIT;

    private final ImageIcon ICON_CHANGE_LANGUAGE;
    private final ImageIcon ICON_LOAD_PLUGIN;
    private final ImageIcon ICON_QUIT;

    private String localeSelectLanguageTitle = "";
    private String localeSelectLanguageMsg = "";
    private String localeSelectPluginTitle = "";
    private String localeSelectPluginMsg = "";

    protected FileMenu(@NotNull ApplicationGUI parentAppGui) {
        super();
        appGui = parentAppGui;

        TranslationManager.addTranslationListener(this);

        OPEN_PROGRAM = new JMenuItem();
        OPEN_PROGRAM.setIcon(GFileDialog.ICON_OPEN);
        OPEN_PROGRAM.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
        OPEN_PROGRAM.addActionListener(this::openProgram);
        add(OPEN_PROGRAM);

        ICON_CHANGE_LANGUAGE = IconUtils.importIcon("/assets/change_language.png", ApplicationGUI.MENU_ITEM_ICON_SIZE);

        CHANGE_LANGUAGE = new JMenuItem("Change Language");
        CHANGE_LANGUAGE.setIcon(ICON_CHANGE_LANGUAGE);
        CHANGE_LANGUAGE.addActionListener(this::changeLanguage);
        add(CHANGE_LANGUAGE);

        ICON_LOAD_PLUGIN = IconUtils.importIcon("/assets/load_plugin.png", ApplicationGUI.MENU_ITEM_ICON_SIZE);

        LOAD_PLUGIN = new JMenuItem("Load Plugin");
        LOAD_PLUGIN.setIcon(ICON_LOAD_PLUGIN);
        LOAD_PLUGIN.addActionListener(this::loadPlugin);
        add(LOAD_PLUGIN);

        ICON_QUIT = IconUtils.importIcon("/assets/quit.png", ApplicationGUI.MENU_ITEM_ICON_SIZE);

        QUIT = new JMenuItem();
        QUIT.setIcon(ICON_QUIT);
        QUIT.addActionListener(appGui::close);
        add(QUIT);
    }

    @Override
    public void updateTranslations(@NotNull Translation translation) {
        translation.translateComponent("fileMenu", this);
        translation.translateComponent("fileMenu.openProgram", OPEN_PROGRAM);
        translation.translateComponent("fileMenu.changeLanguage", CHANGE_LANGUAGE);
        translation.translateComponent("fileMenu.loadPlugin", LOAD_PLUGIN);
        translation.translateComponent("fileMenu.quit", QUIT);
        localeSelectLanguageTitle = translation.getOrDefault("fileMenu.selectLanguageTitle");
        localeSelectLanguageMsg = translation.getOrDefault("fileMenu.selectLanguageMsg");
        localeSelectPluginTitle = translation.getOrDefault("fileMenu.selectPluginTitle");
        localeSelectPluginMsg = translation.getOrDefault("fileMenu.selectPluginMsg");
    }

    private void openProgram(ActionEvent e) {
        GFileDialog gFileDialog = GFileDialog.getInstance();
        if (gFileDialog.showOpenDialog(this, appGui.APP.getCurrentProgram(), GFileDialog.getPEMUFileFilter(), GFileDialog.getPEMULibFileFilter()) == JFileChooser.APPROVE_OPTION)
            appGui.APP.setCurrentProgram(gFileDialog.getSelectedFile());
    }

    private void loadPlugin(ActionEvent e) {
        IPlugin[] availablePlugins = PluginManager.getRegisteredPlugins();

        IPlugin selectedPlugin = (IPlugin) JOptionPane.showInputDialog(
                this, localeSelectPluginMsg, localeSelectPluginTitle,
                JOptionPane.PLAIN_MESSAGE, ICON_LOAD_PLUGIN, availablePlugins, appGui.APP.getLoadedPlugin()
        );
        if (selectedPlugin == null) return;

        appGui.APP.loadPlugin(selectedPlugin);
    }
    
    private void changeLanguage(ActionEvent e) {
        Translation[] availableTranslations = TranslationManager.getAvailableTranslations();
        Translation currentTranslation = TranslationManager.getCurrentTranslation();

        Translation selectedTranslation = (Translation) JOptionPane.showInputDialog(
                this, localeSelectLanguageTitle, localeSelectLanguageMsg,
                JOptionPane.PLAIN_MESSAGE, ICON_CHANGE_LANGUAGE, availableTranslations, currentTranslation
        );
        if (selectedTranslation == null) return;

        TranslationManager.setCurrentTranslation(
                selectedTranslation.getShortName()
        );
    }
}
