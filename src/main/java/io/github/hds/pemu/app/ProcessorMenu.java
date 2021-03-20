package io.github.hds.pemu.app;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class ProcessorMenu extends JMenu {

    private final Application app;

    private final Integer[] dumpMemoryPossibleValues = { 8, 16, 24, 32, 48, 64 };
    private int dumpMemoryLastSelected = 0;

    private final JMenuItem RUN;
    private final JMenuItem STOP;
    private final JMenuItem CONFIGURE;
    private final JMenuItem DUMP_MEMORY;

    private final ImageIcon ICON_RUN;
    private final ImageIcon ICON_STOP;
    private final ImageIcon ICON_CONFIGURE;
    private final ImageIcon ICON_DUMP_MEMORY;

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

        RUN = new JMenuItem("Run", 'R');
        RUN.setIcon(ICON_RUN);
        RUN.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        RUN.addActionListener(app::runProcessor);
        add(RUN);

        ICON_STOP = new ImageIcon(
                new ImageIcon(System.class.getResource("/assets/stop.png"))
                        .getImage().getScaledInstance(Application.MENU_ITEM_ICON_SIZE, Application.MENU_ITEM_ICON_SIZE, Image.SCALE_SMOOTH)
        );

        STOP = new JMenuItem("Stop", 'S');
        STOP.setIcon(ICON_STOP);
        STOP.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
        STOP.addActionListener(app::stopProcessor);
        add(STOP);

        ICON_CONFIGURE = new ImageIcon(
                new ImageIcon(System.class.getResource("/assets/configure.png"))
                        .getImage().getScaledInstance(Application.MENU_ITEM_ICON_SIZE, Application.MENU_ITEM_ICON_SIZE, Image.SCALE_SMOOTH)
        );

        CONFIGURE = new JMenuItem("Configure", 'C');
        CONFIGURE.setIcon(ICON_CONFIGURE);
        CONFIGURE.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        CONFIGURE.addActionListener(this::configureProcessor);
        add(CONFIGURE);

        ICON_DUMP_MEMORY = new ImageIcon(
                new ImageIcon(System.class.getResource("/assets/dump_memory.png"))
                        .getImage().getScaledInstance(Application.MENU_ITEM_ICON_SIZE, Application.MENU_ITEM_ICON_SIZE, Image.SCALE_SMOOTH)
        );

        DUMP_MEMORY = new JMenuItem("Dump Memory", 'D');
        DUMP_MEMORY.setIcon(ICON_DUMP_MEMORY);
        DUMP_MEMORY.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK));
        DUMP_MEMORY.addActionListener(this::dumpMemory);
        add(DUMP_MEMORY);
    }

    public void dumpMemory(ActionEvent e) {
        if (app.currentProcessor == null) {
            Console.Debug.println("No processor was ever started yet.");
            return;
        }

        Integer selected = (Integer) JOptionPane.showInputDialog(
                this, "Words on each line:", "Memory Dump",
                JOptionPane.QUESTION_MESSAGE, null, dumpMemoryPossibleValues,
                dumpMemoryPossibleValues[dumpMemoryLastSelected]
        );

        if (selected == null) return;
        for (int i = 0; i < dumpMemoryPossibleValues.length; i++)
            if (dumpMemoryPossibleValues[i].equals(selected)) dumpMemoryLastSelected = i;

        Console.Debug.println("Memory Dump:\n" + app.currentProcessor.MEMORY.toString(true, selected));
    }

    public void configureProcessor(ActionEvent e) {
        CONFIG_PANEL.setConfig(app.processorConfig);
        int result = JOptionPane.showConfirmDialog(this, CONFIG_PANEL, "Configure Processor", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION)
            app.processorConfig = CONFIG_PANEL.getConfig();
    }

}
