package io.github.hds.pemu.app;

import io.github.hds.pemu.compiler.Compiler;
import io.github.hds.pemu.processor.Processor;
import io.github.hds.pemu.processor.ProcessorConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.net.URI;

public class Application extends JFrame implements KeyListener {

    private static Application INSTANCE;
    private static final String APP_TITLE = "PEMU";
    private static final String WEBPAGE = "https://github.com/hds536jhmk/ProcessorEmulator";

    private final ProcessorConfig PROCESSOR_CONFIG;
    private @Nullable File currentProgram = null;
    private @Nullable Processor currentProcessor = null;

    private final JFileChooser FILE_DIALOG;
    private final Integer[] dumpMemoryPossibleValues = { 8, 16, 24, 32, 48, 64 };
    private int dumpMemoryLastSelected = 0;

    private Application(@NotNull ProcessorConfig initialConfig) throws HeadlessException {
        super();
        PROCESSOR_CONFIG = initialConfig;
        updateTitle();

        final int MENU_ITEM_ICON_SIZE = 20;
        setIconImage(new ImageIcon(System.class.getResource("/assets/icon.png")).getImage());

        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        // FILE MENU
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');
        menuBar.add(fileMenu);

        ImageIcon openIcon = new ImageIcon(System.class.getResource("/assets/open_file.png"));
        ImageIcon scaledOpenIcon = new ImageIcon(openIcon.getImage().getScaledInstance(MENU_ITEM_ICON_SIZE, MENU_ITEM_ICON_SIZE, Image.SCALE_SMOOTH));

        JMenuItem fOpenProgram = new JMenuItem("Open Program", 'O');
        fOpenProgram.setIcon(scaledOpenIcon);
        fOpenProgram.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
        fOpenProgram.addActionListener(this::openProgram);
        fileMenu.add(fOpenProgram);

        ImageIcon quitIcon = new ImageIcon(System.class.getResource("/assets/quit.png"));
        ImageIcon scaledQuitIcon = new ImageIcon(quitIcon.getImage().getScaledInstance(MENU_ITEM_ICON_SIZE, MENU_ITEM_ICON_SIZE, Image.SCALE_SMOOTH));

        JMenuItem fQuit = new JMenuItem("Quit", 'Q');
        fQuit.setIcon(scaledQuitIcon);
        fQuit.addActionListener(e -> { INSTANCE.stopProcessor(null); INSTANCE.dispose(); });
        fileMenu.add(fQuit);

        FILE_DIALOG = new JFileChooser();
        FILE_DIALOG.setCurrentDirectory(new File("./"));
        FILE_DIALOG.setMultiSelectionEnabled(false);
        FILE_DIALOG.setFileFilter(new FileNameExtensionFilter("PEMU program", "pemu"));

        // PROCESSOR MENU
        JMenu processorMenu = new JMenu("Processor");
        processorMenu.setMnemonic('P');
        menuBar.add(processorMenu);

        ImageIcon runIcon = new ImageIcon(System.class.getResource("/assets/run.png"));
        ImageIcon scaledRunIcon = new ImageIcon(runIcon.getImage().getScaledInstance(MENU_ITEM_ICON_SIZE, MENU_ITEM_ICON_SIZE, Image.SCALE_SMOOTH));

        JMenuItem pRunProcessor = new JMenuItem("Run", 'R');
        pRunProcessor.setIcon(scaledRunIcon);
        pRunProcessor.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        pRunProcessor.addActionListener(this::runProcessor);
        processorMenu.add(pRunProcessor);

        ImageIcon stopIcon = new ImageIcon(System.class.getResource("/assets/stop.png"));
        ImageIcon scaledStopIcon = new ImageIcon(stopIcon.getImage().getScaledInstance(MENU_ITEM_ICON_SIZE, MENU_ITEM_ICON_SIZE, Image.SCALE_SMOOTH));

        JMenuItem pStopProcessor = new JMenuItem("Stop", 'S');
        pStopProcessor.setIcon(scaledStopIcon);
        pStopProcessor.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
        pStopProcessor.addActionListener(this::stopProcessor);
        processorMenu.add(pStopProcessor);

        ImageIcon configIcon = new ImageIcon(System.class.getResource("/assets/configure.png"));
        ImageIcon scaledConfigIcon = new ImageIcon(configIcon.getImage().getScaledInstance(MENU_ITEM_ICON_SIZE, MENU_ITEM_ICON_SIZE, Image.SCALE_SMOOTH));

        JMenuItem pConfigProcessor = new JMenuItem("Configure", 'C');
        pConfigProcessor.setIcon(scaledConfigIcon);
        pConfigProcessor.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        pConfigProcessor.addActionListener(this::configureProcessor);
        processorMenu.add(pConfigProcessor);

        ImageIcon dumpIcon = new ImageIcon(System.class.getResource("/assets/dump_memory.png"));
        ImageIcon scaledDumpIcon = new ImageIcon(dumpIcon.getImage().getScaledInstance(MENU_ITEM_ICON_SIZE, MENU_ITEM_ICON_SIZE, Image.SCALE_SMOOTH));

        JMenuItem pDumpProcessor = new JMenuItem("Dump Memory", 'D');
        pDumpProcessor.setIcon(scaledDumpIcon);
        pDumpProcessor.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK));
        pDumpProcessor.addActionListener(this::dumpMemory);
        processorMenu.add(pDumpProcessor);

        // ABOUT MENU
        JMenu aboutMenu = new JMenu("About");
        aboutMenu.setMnemonic('A');
        menuBar.add(aboutMenu);

        ImageIcon webIcon = new ImageIcon(System.class.getResource("/assets/webpage.png"));
        ImageIcon scaledWebIcon = new ImageIcon(webIcon.getImage().getScaledInstance(MENU_ITEM_ICON_SIZE, MENU_ITEM_ICON_SIZE, Image.SCALE_SMOOTH));

        JMenuItem aOpenWebpage = new JMenuItem("Open Webpage", 'O');
        aOpenWebpage.setIcon(scaledWebIcon);
        aOpenWebpage.addActionListener(
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
        aboutMenu.add(aOpenWebpage);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(Console.POutput.ELEMENT), new JScrollPane(Console.Debug.ELEMENT));
        splitPane.setResizeWeight(0.5d);
        splitPane.resetToPreferredSizes();
        add(splitPane);

        Console.POutput.ELEMENT.addKeyListener(this);
    }

    public void updateTitle() {
        if (currentProgram == null)
            setTitle(APP_TITLE + " (No Program Selected)");
        else
            setTitle(APP_TITLE + " (" + currentProgram.getAbsolutePath() + ")");
    }

    public void setCurrentProgram(@NotNull File program) {
        if (program.exists() && program.canRead())
            currentProgram = program;
        else currentProgram = null;
        updateTitle();
    }

    public static @NotNull Application getInstance(ProcessorConfig initialConfig) {
        if (INSTANCE == null)
            INSTANCE = new Application(initialConfig);
        return INSTANCE;
    }

    public static @NotNull Application getInstance() {
        if (INSTANCE == null)
            INSTANCE = new Application(new ProcessorConfig());
        return INSTANCE;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (currentProcessor == null) return;

        char typed = e.getKeyChar();
        if (typed == KeyEvent.CHAR_UNDEFINED)
            currentProcessor.pressedChar = '\0';
        else
            currentProcessor.pressedChar = typed;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (currentProcessor == null) return;
        currentProcessor.pressedKey = e.getKeyCode();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (currentProcessor == null) return;

        char character = e.getKeyChar();
        if (character != KeyEvent.CHAR_UNDEFINED && Character.toLowerCase(character) == Character.toLowerCase(currentProcessor.pressedChar))
            currentProcessor.pressedChar = '\0';

        if (e.getKeyCode() == currentProcessor.pressedKey)
            currentProcessor.pressedKey = KeyEvent.VK_UNDEFINED;
    }

    public void dumpMemory(ActionEvent e) {
        if (currentProcessor == null) {
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

        Console.Debug.println("Memory Dump:\n" + currentProcessor.MEMORY.toString(true, selected));
    }

    public void openProgram(ActionEvent e) {
        if (FILE_DIALOG.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            setCurrentProgram(FILE_DIALOG.getSelectedFile());
    }

    public void configureProcessor(ActionEvent e) {
        ProcessorConfigPanel panel = new ProcessorConfigPanel(PROCESSOR_CONFIG);
        int result = JOptionPane.showConfirmDialog(this, panel, "Configure Processor", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION)
            panel.apply();
    }

    public void runProcessor(ActionEvent e) {
        // Make sure that the last thread is dead
        if (currentProcessor != null && currentProcessor.isRunning()) {
            Console.Debug.println("Processor is already running!");
            return;
        }

        // Clear Debug console and check if a program is specified
        Console.Debug.clear();
        if (currentProgram == null) {
            Console.Debug.println("No program specified!");
            return;
        }

        // Create a new Processor with the specified values
        try {
            currentProcessor = new Processor(PROCESSOR_CONFIG);
        } catch (Exception err) {
            Console.Debug.println("Couldn't create processor.");
            Console.Debug.printStackTrace(err);
            return;
        }

        // Compile the selected program
        int[] compiledProgram;
        try {
            compiledProgram = Compiler.compileFile(currentProgram, currentProcessor);
        } catch (Exception err) {
            Console.Debug.println("Compilation error. (for file @'" + currentProgram.getAbsolutePath() + "')");
            Console.Debug.printStackTrace(err);
            return;
        }

        Console.Debug.println(
                "Compiled file (" + currentProgram.getName() + ") occupies "
              + compiledProgram.length * currentProcessor.MEMORY.WORD.BYTES + " / "
              + currentProcessor.MEMORY.getSize() * currentProcessor.MEMORY.WORD.BYTES + " Bytes"
        );

        // Load compiled program into memory
        try {
            currentProcessor.MEMORY.setValuesAt(0, compiledProgram);
        } catch (Exception err) {
            Console.Debug.println("Error while loading program into memory!");
            Console.Debug.printStackTrace(err);
            return;
        }

        // Run the processor
        try {
            Console.Debug.println("Running Processor:\n" + currentProcessor.getInfo());
            Console.POutput.clear();

            // We want to make sure that if the Processor fails, details about the error show on the Console
            Thread processorThread = new Thread(currentProcessor) {
                @Override
                public void run() {
                    try {
                        super.run();
                    } catch (Exception err) {
                        currentProcessor.stop(); // Make sure to stop the processor if it fails
                        Console.Debug.println("Error while running program!");
                        Console.Debug.printStackTrace(err);
                    }
                    Console.Debug.println("Processor stopped!\n");
                }
            };
            processorThread.start();
        } catch (Exception err) {
            Console.Debug.println("Error while starting processor's thread!");
            Console.Debug.printStackTrace(err);
        }
    }

    public void stopProcessor(ActionEvent e) {
        if (currentProcessor == null || !currentProcessor.isRunning()) {
            Console.Debug.println("Couldn't stop processor because it isn't currently running!");
            return;
        }
        currentProcessor.stop();
    }
}
