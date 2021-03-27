package io.github.hds.pemu.app;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class ProcessorMenu extends JMenu {

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

    protected ProcessorMenu(@NotNull Application parentApp) {
        super("Processor");
        app = parentApp;
        CONFIG_PANEL = new ProcessorConfigPanel();

        setMnemonic('P');

        ICON_RUN = new ImageIcon(
                new ImageIcon(System.class.getResource("/assets/run.png"))
                        .getImage().getScaledInstance(Application.MENU_ITEM_ICON_SIZE, Application.MENU_ITEM_ICON_SIZE, Image.SCALE_SMOOTH)
        );

        RUN = new TJMenuItem("Run", 'R', i -> app.currentProgram != null && (app.currentProcessor == null || !app.currentProcessor.isRunning()));
        RUN.setIcon(ICON_RUN);
        RUN.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        RUN.addActionListener(app::runProcessor);
        add(RUN);

        ICON_STOP = new ImageIcon(
                new ImageIcon(System.class.getResource("/assets/stop.png"))
                        .getImage().getScaledInstance(Application.MENU_ITEM_ICON_SIZE, Application.MENU_ITEM_ICON_SIZE, Image.SCALE_SMOOTH)
        );

        STOP = new TJMenuItem("Stop", 'S', i -> app.currentProcessor != null && app.currentProcessor.isRunning());
        STOP.setIcon(ICON_STOP);
        STOP.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
        STOP.addActionListener(app::stopProcessor);
        add(STOP);

        ICON_CONFIGURE = new ImageIcon(
                new ImageIcon(System.class.getResource("/assets/configure.png"))
                        .getImage().getScaledInstance(Application.MENU_ITEM_ICON_SIZE, Application.MENU_ITEM_ICON_SIZE, Image.SCALE_SMOOTH)
        );

        CONFIGURE = new TJMenuItem("Configure", 'C', null);
        CONFIGURE.setIcon(ICON_CONFIGURE);
        CONFIGURE.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        CONFIGURE.addActionListener(this::configureProcessor);
        add(CONFIGURE);

        ICON_OPEN_MEMORY_VIEW = new ImageIcon(
                new ImageIcon(System.class.getResource("/assets/memory_view.png"))
                        .getImage().getScaledInstance(Application.MENU_ITEM_ICON_SIZE, Application.MENU_ITEM_ICON_SIZE, Image.SCALE_SMOOTH)
        );

        OPEN_MEMORY_VIEW = new TJMenuItem("Open Memory View", 'M', i -> app.currentProcessor != null);
        OPEN_MEMORY_VIEW.setIcon(ICON_OPEN_MEMORY_VIEW);
        OPEN_MEMORY_VIEW.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK));
        OPEN_MEMORY_VIEW.addActionListener(this::openMemoryView);
        add(OPEN_MEMORY_VIEW);

        ICON_PAUSE_RESUME = new ImageIcon(
                new ImageIcon(System.class.getResource("/assets/pause_resume.png"))
                        .getImage().getScaledInstance(Application.MENU_ITEM_ICON_SIZE, Application.MENU_ITEM_ICON_SIZE, Image.SCALE_SMOOTH)
        );

        PAUSE_RESUME = new TJMenuItem("Pause/Resume", 'P', i -> app.currentProcessor != null && app.currentProcessor.isRunning());
        PAUSE_RESUME.setIcon(ICON_PAUSE_RESUME);
        PAUSE_RESUME.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.SHIFT_DOWN_MASK));
        PAUSE_RESUME.addActionListener(app::toggleProcessorExecution);
        add(PAUSE_RESUME);

        ICON_STEP = new ImageIcon(
                new ImageIcon(System.class.getResource("/assets/step.png"))
                        .getImage().getScaledInstance(Application.MENU_ITEM_ICON_SIZE, Application.MENU_ITEM_ICON_SIZE, Image.SCALE_SMOOTH)
        );

        STEP = new TJMenuItem("Step", 'S', i -> app.currentProcessor != null && app.currentProcessor.isRunning() &&  app.currentProcessor.isPaused());
        STEP.setIcon(ICON_STEP);
        STEP.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.SHIFT_DOWN_MASK));
        STEP.addActionListener(app::stepProcessor);
        add(STEP);
    }

    public void openMemoryView(ActionEvent e) {
        app.MEMORY_VIEW.setVisible(true);
    }

    public void configureProcessor(ActionEvent e) {
        CONFIG_PANEL.setConfig(app.processorConfig);
        int result = JOptionPane.showConfirmDialog(this, CONFIG_PANEL, "Configure Processor", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            app.processorConfig = CONFIG_PANEL.getConfig();
            if (app.currentProcessor != null) app.currentProcessor.CLOCK.setClock(app.processorConfig.clock);
        }
    }

}
