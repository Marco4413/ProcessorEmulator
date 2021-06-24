package io.github.hds.pemu.app;

import io.github.hds.pemu.compiler.CompiledProgram;
import io.github.hds.pemu.compiler.Compiler;
import io.github.hds.pemu.config.ConfigEvent;
import io.github.hds.pemu.config.ConfigManager;
import io.github.hds.pemu.config.IConfigurable;
import io.github.hds.pemu.localization.ITranslatable;
import io.github.hds.pemu.localization.Translation;
import io.github.hds.pemu.localization.TranslationManager;
import io.github.hds.pemu.processor.IProcessor;
import io.github.hds.pemu.processor.ProcessorConfig;
import io.github.hds.pemu.utils.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.function.Function;

public class Application extends JFrame implements KeyListener, ITranslatable, IConfigurable {

    public static final String APP_TITLE = "PEMU";
    public static final String APP_VERSION = "1.6.0";
    public static final int FRAME_WIDTH = 800;
    public static final int FRAME_HEIGHT = 600;
    public static final int FRAME_ICON_SIZE = 32;
    public static final int MENU_ITEM_ICON_SIZE = 20;

    private static Application INSTANCE;

    protected final FileMenu FILE_MENU;
    protected final ProgramMenu PROGRAM_MENU;
    protected final ProcessorMenu PROCESSOR_MENU;
    protected final AboutMenu ABOUT_MENU;

    protected @Nullable File currentProgram = null;
    protected @Nullable Function<ProcessorConfig, IProcessor> processorProducer = null;
    protected @Nullable IProcessor currentProcessor = null;
    protected @NotNull ProcessorConfig processorConfig;

    protected final MemoryView MEMORY_VIEW;

    private @NotNull String localeNoProgramSelected = "";
    private @NotNull String localeProgramSelected = "";

    private Application() throws HeadlessException {
        super();
        processorConfig = new ProcessorConfig();
        setTitle(APP_TITLE);

        setIconImage(IconUtils.importIcon("/assets/icon.png", FRAME_ICON_SIZE).getImage());

        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                ConfigManager.saveConfig();
            }
        });

        ConfigManager.addConfigListener(this);
        TranslationManager.addTranslationListener(this);
        GFileDialog.getInstance();

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

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(Console.POutput), new JScrollPane(Console.Debug));
        splitPane.setResizeWeight(0.5d);
        splitPane.resetToPreferredSizes();
        add(splitPane);

        Console.POutput.addKeyListener(this);

        ConfigManager.loadOrCreate();
    }

    public static @NotNull Application getInstance() {
        if (INSTANCE == null) INSTANCE = new Application();
        return INSTANCE;
    }

    @Override
    public void updateTranslations(@NotNull Translation translation) {
        localeNoProgramSelected = translation.getOrDefault("application.noProgramSelected");
        localeProgramSelected = translation.getOrDefault("application.programSelected");
        updateTitle();
    }

    @Override
    public void loadConfig(@NotNull ConfigEvent e) {
        // Check if the config is on the right version, if not reset it to defaults
        String configVersion = e.CONFIG.get(String.class, "version");
        if (configVersion == null || StringUtils.compareVersions(configVersion, APP_VERSION) != 0) {
            e.stop();
            ConfigManager.resetToDefault();
        } else {
            // We let the app crash if config couldn't be loaded successfully
            processorConfig.setBits(e.CONFIG.get(Integer.class, "processorConfig.bits"));
            processorConfig.setMemorySize(e.CONFIG.get(Integer.class, "processorConfig.memSize"));
            processorConfig.setClock(e.CONFIG.get(Integer.class, "processorConfig.clock"));
        }
    }

    @Override
    public void saveConfig(@NotNull ConfigEvent e) {
        e.CONFIG.put("processorConfig.bits", processorConfig.getBits());
        e.CONFIG.put("processorConfig.memSize", processorConfig.getMemorySize());
        e.CONFIG.put("processorConfig.clock", processorConfig.getClock());
    }

    @Override
    public void setDefaults(@NotNull ConfigEvent e) {
        e.CONFIG.put("version", APP_VERSION);
        e.CONFIG.put("processorConfig.bits", ProcessorConfig.DEFAULT_BITS);
        e.CONFIG.put("processorConfig.memSize", ProcessorConfig.DEFAULT_MEMORY_SIZE);
        e.CONFIG.put("processorConfig.clock", ProcessorConfig.DEFAULT_CLOCK);
    }

    public void updateTitle() {
        if (currentProgram == null)
            setTitle(APP_TITLE + " " + APP_VERSION + " " + localeNoProgramSelected);
        else
            setTitle(APP_TITLE + " " + APP_VERSION + " " + StringUtils.format(localeProgramSelected, currentProgram.getAbsolutePath()));
    }

    public void setProducer(@NotNull Function<ProcessorConfig, IProcessor> producer) {
        processorProducer = producer;
    }

    public void setCurrentProgram(@NotNull File program) {
        if (program.exists() && program.canRead())
            currentProgram = program;
        else currentProgram = null;
        updateTitle();
    }

    public void setProcessorConfig(@NotNull ProcessorConfig config) {
        processorConfig = config;
        if (currentProcessor != null) currentProcessor.getClock().setClock(processorConfig.getClock());
    }

    public @NotNull ProcessorConfig getProcessorConfig() {
        return processorConfig;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (currentProcessor == null) return;

        char typed = e.getKeyChar();
        if (typed == KeyEvent.CHAR_UNDEFINED)
            currentProcessor.setCharPressed('\0');
        else
            currentProcessor.setCharPressed(typed);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (currentProcessor == null) return;
        currentProcessor.setKeyPressed(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (currentProcessor == null) return;

        char character = e.getKeyChar();
        if (character != KeyEvent.CHAR_UNDEFINED && Character.toLowerCase(character) == Character.toLowerCase(currentProcessor.getCharPressed()))
            currentProcessor.setCharPressed('\0');

        if (e.getKeyCode() == currentProcessor.getKeyPressed())
            currentProcessor.setKeyPressed(KeyEvent.VK_UNDEFINED);
    }

    public @Nullable IProcessor createProcessor() {
        if (processorProducer == null) {
            Console.Debug.println("Processor Producer was never specified (Try downloading the latest version of the app)!");
            return null;
        }

        // Create a new Processor with the current ProcessorConfig
        try {
            return processorProducer.apply(processorConfig);
        } catch (Exception err) {
            Console.Debug.println("Couldn't create processor.");
            Console.Debug.printStackTrace(err, false);
        }

        return null;
    }

    public CompiledProgram compileProgram(@Nullable IProcessor processorInstance) {
        if (processorInstance == null) processorInstance = createProcessor();
        if (processorInstance == null) return null;

        if (currentProgram == null) {
            Console.Debug.println("No program specified!");
            return null;
        }

        CompiledProgram compiledProgram = null;
        try {
            compiledProgram = Compiler.compileFile(currentProgram, processorInstance);
            Console.Debug.println(
                    String.format(
                            "File was compiled successfully (%s), it occupies %d/%d Words",
                            currentProgram.getName(), compiledProgram.getProgram().length, processorInstance.getMemory().getSize() - processorInstance.getReservedWords()
                    )
            );
            Console.Debug.println(
                    String.format("Compilation took %.2fms", compiledProgram.getCompileTimeMillis())
            );
        } catch (Exception err) {
            Console.Debug.println("Compilation error (for file @'" + currentProgram.getAbsolutePath() + "'):");
            Console.Debug.printStackTrace(err, false);
        }

        return compiledProgram;
    }

    public void verifyProgram(ActionEvent e) {
        compileProgram(null);
    }

    public void obfuscateProgram(ActionEvent e) {
        CompiledProgram compiledProgram = compileProgram(null);

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
        currentProcessor = createProcessor();
        if (currentProcessor == null) return;

        // Compile the selected program
        CompiledProgram compiledProgram = compileProgram(currentProcessor);
        if (compiledProgram == null) return;

        // Load compiled program into memory
        String loadError = null;
        try {
            loadError = currentProcessor.loadProgram(compiledProgram.getProgram());
        } catch (Exception err) {
            Console.Debug.println("Error while loading program into memory!");
            Console.Debug.printStackTrace(err, false);
        }

        if (loadError != null) {
            Console.Debug.println("Error while loading program!");
            Console.Debug.println("Processor Error: " + loadError);
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
