package io.github.hds.pemu.app;

import io.github.hds.pemu.localization.ITranslatable;
import io.github.hds.pemu.utils.IconUtils;
import io.github.hds.pemu.localization.Translation;
import io.github.hds.pemu.localization.TranslationManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class ProcessorMenu extends JMenu implements ITranslatable {

    private final Application app;

    private final TJMenuItem RUN;
    private final TJMenuItem STOP;
    private final TJMenuItem CONFIGURE;
    private final TJMenuItem OPEN_MEMORY_VIEW;
    private final TJMenuItem PAUSE_RESUME;
    private final TJMenuItem STEP;

    private final ImageIcon ICON_RUN;
    private final ImageIcon ICON_STOP;
    private final ImageIcon ICON_CONFIGURE;
    private final ImageIcon ICON_OPEN_MEMORY_VIEW;
    private final ImageIcon ICON_PAUSE_RESUME;
    private final ImageIcon ICON_STEP;

    private final ProcessorConfigPanel CONFIG_PANEL;

    private @NotNull String localeConfigPanelTitle = "";

    protected ProcessorMenu(@NotNull Application parentApp) {
        super();
        app = parentApp;

        TranslationManager.addTranslationListener(this);

        CONFIG_PANEL = new ProcessorConfigPanel();

        ICON_RUN = IconUtils.importIcon("/assets/run.png", Application.MENU_ITEM_ICON_SIZE);

        RUN = new TJMenuItem(i -> app.currentProgram != null && (app.currentProcessor == null || !app.currentProcessor.isRunning()));
        RUN.setIcon(ICON_RUN);
        RUN.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        RUN.addActionListener(app::runProcessor);
        add(RUN);

        ICON_STOP = IconUtils.importIcon("/assets/stop.png", Application.MENU_ITEM_ICON_SIZE);

        STOP = new TJMenuItem(i -> app.currentProcessor != null && app.currentProcessor.isRunning());
        STOP.setIcon(ICON_STOP);
        STOP.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
        STOP.addActionListener(app::stopProcessor);
        add(STOP);

        ICON_CONFIGURE = IconUtils.importIcon("/assets/configure.png", Application.MENU_ITEM_ICON_SIZE);

        CONFIGURE = new TJMenuItem(null);
        CONFIGURE.setIcon(ICON_CONFIGURE);
        CONFIGURE.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        CONFIGURE.addActionListener(this::configureProcessor);
        add(CONFIGURE);

        ICON_OPEN_MEMORY_VIEW = IconUtils.importIcon("/assets/memory_view.png", Application.MENU_ITEM_ICON_SIZE);

        OPEN_MEMORY_VIEW = new TJMenuItem(i -> app.currentProcessor != null);
        OPEN_MEMORY_VIEW.setIcon(ICON_OPEN_MEMORY_VIEW);
        OPEN_MEMORY_VIEW.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK));
        OPEN_MEMORY_VIEW.addActionListener(this::openMemoryView);
        add(OPEN_MEMORY_VIEW);

        ICON_PAUSE_RESUME = IconUtils.importIcon("/assets/pause_resume.png", Application.MENU_ITEM_ICON_SIZE);;

        PAUSE_RESUME = new TJMenuItem(i -> app.currentProcessor != null && app.currentProcessor.isRunning());
        PAUSE_RESUME.setIcon(ICON_PAUSE_RESUME);
        PAUSE_RESUME.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.SHIFT_DOWN_MASK));
        PAUSE_RESUME.addActionListener(app::toggleProcessorExecution);
        add(PAUSE_RESUME);

        ICON_STEP = IconUtils.importIcon("/assets/step.png", Application.MENU_ITEM_ICON_SIZE);;

        STEP = new TJMenuItem(i -> app.currentProcessor != null && app.currentProcessor.isRunning() &&  app.currentProcessor.isPaused());
        STEP.setIcon(ICON_STEP);
        STEP.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.SHIFT_DOWN_MASK));
        STEP.addActionListener(app::stepProcessor);
        add(STEP);
    }

    @Override
    public void updateTranslations(@NotNull Translation translation) {
        translation.translateComponent("processorMenu", this);
        translation.translateComponent("processorMenu.run", RUN);
        translation.translateComponent("processorMenu.stop", STOP);
        translation.translateComponent("processorMenu.configure", CONFIGURE);
        translation.translateComponent("processorMenu.openMemoryView", OPEN_MEMORY_VIEW);
        translation.translateComponent("processorMenu.pauseResume", PAUSE_RESUME);
        translation.translateComponent("processorMenu.step", STEP);
        localeConfigPanelTitle = translation.getOrDefault("processorMenu.configPanelTitle");
    }

    public void openMemoryView(ActionEvent e) {
        app.MEMORY_VIEW.setVisible(true);
    }

    public void configureProcessor(ActionEvent e) {
        CONFIG_PANEL.setConfig(app.processorConfig);
        int result = JOptionPane.showConfirmDialog(this, CONFIG_PANEL, localeConfigPanelTitle, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION)
            app.setProcessorConfig(CONFIG_PANEL.getConfig());
    }

}
