package io.github.hds.pemu.app;

import io.github.hds.pemu.Main;
import io.github.hds.pemu.config.ConfigEvent;
import io.github.hds.pemu.config.ConfigManager;
import io.github.hds.pemu.config.IConfigurable;
import io.github.hds.pemu.files.FileUtils;
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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

public final class FileMenu extends JMenu implements ITranslatable, IConfigurable {

    private final Application app;

    private final JMenuItem OPEN_PROGRAM;
    private final JMenuItem CHANGE_LANGUAGE;
    protected final JMenuItem LOAD_PLUGIN;
    private final JMenuItem QUIT;

    private final ImageIcon ICON_CHANGE_LANGUAGE;
    private final ImageIcon ICON_LOAD_PLUGIN;
    private final ImageIcon ICON_QUIT;

    private String localeSelectLanguageTitle = "";
    private String localeSelectLanguageMsg = "";
    private String localeSelectPluginTitle = "";
    private String localeSelectPluginMsg = "";

    protected FileMenu(@NotNull Application parentApp) {
        super();
        app = parentApp;

        ConfigManager.addConfigListener(this);
        TranslationManager.addTranslationListener(this);

        OPEN_PROGRAM = new JMenuItem();
        OPEN_PROGRAM.setIcon(GFileDialog.ICON_OPEN);
        OPEN_PROGRAM.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
        OPEN_PROGRAM.addActionListener(this::openProgram);
        add(OPEN_PROGRAM);

        ICON_CHANGE_LANGUAGE = IconUtils.importIcon("/assets/change_language.png", Application.MENU_ITEM_ICON_SIZE);

        CHANGE_LANGUAGE = new JMenuItem("Change Language");
        CHANGE_LANGUAGE.setIcon(ICON_CHANGE_LANGUAGE);
        CHANGE_LANGUAGE.addActionListener(this::changeLanguage);
        add(CHANGE_LANGUAGE);

        ICON_LOAD_PLUGIN = IconUtils.importIcon("/assets/load_plugin.png", Application.MENU_ITEM_ICON_SIZE);

        LOAD_PLUGIN = new JMenuItem("Load Plugin");
        LOAD_PLUGIN.setIcon(ICON_LOAD_PLUGIN);
        LOAD_PLUGIN.addActionListener(this::loadPlugin);
        add(LOAD_PLUGIN);

        ICON_QUIT = IconUtils.importIcon("/assets/quit.png", Application.MENU_ITEM_ICON_SIZE);

        QUIT = new JMenuItem();
        QUIT.setIcon(ICON_QUIT);
        QUIT.addActionListener(app::close);
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
        if (gFileDialog.showOpenDialog(this, app.currentProgram, GFileDialog.getPEMUFileFilter(), GFileDialog.getPEMULibFileFilter()) == JFileChooser.APPROVE_OPTION)
            app.setCurrentProgram(gFileDialog.getSelectedFile());
    }

    private void loadPlugin(ActionEvent e) {
        IPlugin[] availablePlugins = PluginManager.getRegisteredPlugins();

        IPlugin selectedPlugin = (IPlugin) JOptionPane.showInputDialog(
                this, localeSelectPluginMsg, localeSelectPluginTitle,
                JOptionPane.PLAIN_MESSAGE, ICON_LOAD_PLUGIN, availablePlugins, app.getLoadedPlugin()
        );
        if (selectedPlugin == null) return;

        app.loadPlugin(selectedPlugin);
    }

    private void changeLanguage(ActionEvent e) {
        ArrayList<Translation> availableTranslations = new ArrayList<>();
        InputStream stream = Main.class.getResourceAsStream("/localization/languages.txt");
        Scanner scanner = new Scanner(stream);
        while (scanner.hasNextLine()) {
            String languageName = scanner.nextLine().trim();
            if (languageName.length() == 0) continue;
            try {
                availableTranslations.add(
                        TranslationManager.loadTranslation(FileUtils.getPathWithExtension("/localization/" + languageName, "lang"))
                );
            } catch (Exception ignored) { }
        }

        String[] languagesNames = new String[availableTranslations.size()];
        String currentLanguage = null;
        for (int i = 0; i < languagesNames.length; i++) {
            Translation translation = availableTranslations.get(i);
            String translationName = translation.getName();
            if (TranslationManager.getCurrentTranslation().getName().equals(translationName)) currentLanguage = translationName;
            languagesNames[i] = translationName;
        }
        if (languagesNames.length == 0) return;
        if (currentLanguage == null) currentLanguage = languagesNames[0];

        String selectedLanguage = (String) JOptionPane.showInputDialog(
                this, localeSelectLanguageTitle, localeSelectLanguageMsg,
                JOptionPane.PLAIN_MESSAGE, ICON_CHANGE_LANGUAGE, languagesNames, currentLanguage
        );
        if (selectedLanguage == null) return;

        for (int i = 0; i < languagesNames.length; i++) {
            if (languagesNames[i].equals(selectedLanguage)) {
                Translation selectedTranslation = availableTranslations.get(i);
                TranslationManager.setCurrentTranslation(selectedTranslation);
                break;
            }
        }
    }

    @Override
    public void loadConfig(@NotNull ConfigEvent e) {
        String languageName = e.config.get(String.class, "selectedLanguage");
        TranslationManager.setCurrentTranslation(
                TranslationManager.loadTranslation(
                        FileUtils.getPathWithExtension("/localization/" + languageName, "lang")
                )
        );
    }

    @Override
    public void saveConfig(@NotNull ConfigEvent e) {
        String selectedLanguage = TranslationManager.getCurrentTranslation().getShortName();
        e.config.put("selectedLanguage", selectedLanguage);
    }

    @Override
    public void setDefaults(@NotNull ConfigEvent e) {
        e.config.put("selectedLanguage", "en-us");
    }
}
