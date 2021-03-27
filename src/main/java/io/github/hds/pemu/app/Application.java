package io.github.hds.pemu.app;

import io.github.hds.pemu.compiler.Compiler;
import io.github.hds.pemu.processor.Processor;
import io.github.hds.pemu.processor.ProcessorConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

public class Application extends JFrame implements KeyListener {

    public static final int MENU_ITEM_ICON_SIZE = 20;
    private static Application INSTANCE;
    private static final String APP_TITLE = "PEMU";

    protected final FileMenu FILE_MENU;
    protected final ProgramMenu PROGRAM_MENU;
    protected final ProcessorMenu PROCESSOR_MENU;
    protected final AboutMenu ABOUT_MENU;

    protected @Nullable File currentProgram = null;
    protected @Nullable Processor currentProcessor = null;
    protected @NotNull ProcessorConfig processorConfig;

    protected final MemoryView MEMORY_VIEW;

    private Application(@NotNull ProcessorConfig initialConfig) throws HeadlessException {
        super();
        processorConfig = initialConfig;
        updateTitle();

        setIconImage(new ImageIcon(System.class.getResource("/assets/icon.png")).getImage());

        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        MEMORY_VIEW = new MemoryView(this);

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        // FILE MENU
        FILE_MENU = new FileMenu(this);
        menuBar.add(FILE_MENU);

        // PROGRAM MENU
        PROGRAM_MENU = new ProgramMenu(this);
        menuBar.add(PROGRAM_MENU);

        // PROCESSOR MENU
        PROCESSOR_MENU = new ProcessorMenu(this);
        menuBar.add(PROCESSOR_MENU);

        // ABOUT MENU
        ABOUT_MENU = new AboutMenu(this);
        menuBar.add(ABOUT_MENU);

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

    public int[] compileProgram() {
        if (currentProgram == null) {
            Console.Debug.println("No program specified!");
            return null;
        }

        try {
            return Compiler.compileFile(currentProgram, processorConfig.instructionSet);
        } catch (Exception err) {
            Console.Debug.println("Compilation error (for file @'" + currentProgram.getAbsolutePath() + "'):");
            Console.Debug.printStackTrace(err, false);
        }

        return null;
    }

    public void verifyProgram(ActionEvent e) {
        int[] compiledProgram = compileProgram();

        if (compiledProgram != null)
            Console.Debug.println(
                    String.format("The specified program compiled successfully!\nIt occupies %d Word%s.", compiledProgram.length, compiledProgram.length == 1 ? "" : "s")
            );
    }

    public void obfuscateProgram(ActionEvent e) {
        int[] compiledProgram = compileProgram();

        if (compiledProgram != null) {
            Console.Debug.println("Program obfuscated successfully:");
            Console.Debug.println(Compiler.obfuscateProgram(compiledProgram));
        }
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
            currentProcessor = new Processor(processorConfig);
        } catch (Exception err) {
            Console.Debug.println("Couldn't create processor.");
            Console.Debug.printStackTrace(err, false);
            return;
        }

        // Compile the selected program
        int[] compiledProgram = compileProgram();
        if (compiledProgram == null) return;

        Console.Debug.println(
                "Compiled file (" + currentProgram.getName() + ") occupies "
                        + compiledProgram.length + " / " + currentProcessor.MEMORY.getSize() + " Words"
        );

        // Load compiled program into memory
        try {
            currentProcessor.MEMORY.setValuesAt(0, compiledProgram);
        } catch (Exception err) {
            Console.Debug.println("Error while loading program into memory!");
            Console.Debug.printStackTrace(err, false);
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
                        Console.Debug.printStackTrace(err, false);
                    }
                    Console.Debug.println("Processor stopped!\n");
                }
            };
            processorThread.start();
        } catch (Exception err) {
            Console.Debug.println("Error while starting processor's thread!");
            Console.Debug.printStackTrace(err, false);
        }
    }

    public void stopProcessor(ActionEvent e) {
        if (currentProcessor == null || !currentProcessor.isRunning()) {
            Console.Debug.println("Couldn't stop processor because it isn't currently running!");
            return;
        }
        currentProcessor.stop();
    }

    public void toggleProcessorExecution(ActionEvent e) {
        if (currentProcessor == null || !currentProcessor.isRunning()) {
            Console.Debug.println("Couldn't pause or resume processor because it isn't currently running!");
            return;
        }

        if (currentProcessor.isPaused()) {
            currentProcessor.resume();
            Console.Debug.println("Processor was resumed!");
        } else {
            currentProcessor.pause();
            Console.Debug.println("Processor was paused!");
        }
    }

    public void stepProcessor(ActionEvent e) {
        if (currentProcessor == null || !currentProcessor.isRunning()) {
            Console.Debug.println("Couldn't step processor because it isn't currently running!");
            return;
        }

        currentProcessor.step();
        Console.Debug.println("Processor stepped forward!");
    }

    protected void close(ActionEvent e) {
        System.exit(0);
    }
}
