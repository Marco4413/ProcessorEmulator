package io.github.hds.pemu.app;

import io.github.hds.pemu.compiler.CompiledProgram;
import io.github.hds.pemu.compiler.Compiler;
import io.github.hds.pemu.config.ConfigEvent;
import io.github.hds.pemu.config.ConfigManager;
import io.github.hds.pemu.config.IConfigurable;
import io.github.hds.pemu.localization.ITranslatable;
import io.github.hds.pemu.localization.Translation;
import io.github.hds.pemu.localization.TranslationManager;
import io.github.hds.pemu.processor.Clock;
import io.github.hds.pemu.processor.IDummyProcessor;
import io.github.hds.pemu.processor.IProcessor;
import io.github.hds.pemu.processor.ProcessorConfig;
import io.github.hds.pemu.utils.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.function.Function;

public final class Application extends JFrame implements KeyListener, ITranslatable, IConfigurable {

    public static final int NONE = 0;
    public static final int CLOSE_ON_PROCESSOR_STOP   = 1;
    public static final int PREVENT_VISIBILITY_CHANGE = 1 << 1;
    public static final int DISABLE_CONFIG_AUTO_SAVE  = 1 << 2;

    public static final String APP_TITLE = "PEMU";
    public static final String APP_VERSION = "1.11.5";
    public static final int FRAME_WIDTH = 800;
    public static final int FRAME_HEIGHT = 600;
    public static final int FRAME_ICON_SIZE = 32;
    public static final int MENU_ITEM_ICON_SIZE = 20;

    public static final int PERFORMANCE_UPDATE_INTERVAL = 1000;

    private static Application INSTANCE;

    private boolean closeOnProcessorStop = false;
    private boolean allowVisibilityChange = true;
    private boolean disableConfigAutoSave = false;

    protected final FileMenu FILE_MENU;
    protected final ProgramMenu PROGRAM_MENU;
    protected final ProcessorMenu PROCESSOR_MENU;
    protected final AboutMenu ABOUT_MENU;

    protected final JLabel PERFORMANCE_LABEL;
    protected final Timer UPDATE_TIMER;

    protected @Nullable Function<ProcessorConfig, IDummyProcessor> dummyProcessorProducer = null;

    protected @Nullable File currentProgram = null;
    protected @Nullable Function<ProcessorConfig, IProcessor> processorProducer = null;
    protected @Nullable IProcessor currentProcessor = null;
    protected @NotNull ProcessorConfig processorConfig;

    protected final MemoryView MEMORY_VIEW;

    private @NotNull String localeNoProgramSelected = "";
    private @NotNull String localeProgramSelected = "";
    private @NotNull String localePerformanceLabel = "";
    private @NotNull String localeNoProcessorRunning = "";

    private Application() throws HeadlessException {
        super();
        setTitle(APP_TITLE);

        setIconImage(IconUtils.importIcon("/assets/icon.png", FRAME_ICON_SIZE).getImage());

        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                close(null);
            }
        });

        ConfigManager.addConfigListener(this);
        TranslationManager.addTranslationListener(this);

        setLayout(new BorderLayout());

        // Making instances of these to allow them to get their translation
        GFileDialog.getInstance();
        ConsoleContextualMenu.getInstance();

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
        processorConfig = PROCESSOR_MENU.CONFIG_PANEL.getConfig();

        // ABOUT MENU
        ABOUT_MENU = new AboutMenu(this);
        menuBar.add(ABOUT_MENU);

        ConsoleComponent programComponent = Console.getProgramComponent();
        ConsoleComponent debugComponent = Console.getDebugComponent();

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(programComponent), new JScrollPane(debugComponent));
        splitPane.setResizeWeight(0.5d);
        splitPane.resetToPreferredSizes();
        add(splitPane, BorderLayout.CENTER);

        PERFORMANCE_LABEL = new JLabel();
        PERFORMANCE_LABEL.setBorder(new EmptyBorder(2, 10, 2, 10));
        PERFORMANCE_LABEL.setHorizontalAlignment(SwingConstants.RIGHT);
        add(PERFORMANCE_LABEL, BorderLayout.PAGE_END);

        programComponent.addKeyListener(this);

        UPDATE_TIMER = new Timer(PERFORMANCE_UPDATE_INTERVAL, this::updateFrame);
        UPDATE_TIMER.start();

        ConfigManager.loadOrCreate();
    }

    public static @NotNull Application getInstance() {
        if (INSTANCE == null) INSTANCE = new Application();
        return INSTANCE;
    }

    private void updateFrame(ActionEvent actionEvent) {
        if (!isVisible() || currentProcessor == null || currentProcessor.isPaused() || !currentProcessor.isRunning()) {
            PERFORMANCE_LABEL.setText(localeNoProcessorRunning);
            return;
        }

        Clock clock = currentProcessor.getClock();
        double interval  = clock.getInterval();
        double deltaTime = clock.getDeltaTime();
        PERFORMANCE_LABEL.setText(
                StringUtils.format(
                        localePerformanceLabel,
                        StringUtils.getEngNotation(interval, "s"),
                        StringUtils.getEngNotation(deltaTime, "s"),
                        StringUtils.getEngNotation(deltaTime - interval, "s")
                )
        );
    }

    public void setFlags(int flags) {
        closeOnProcessorStop  = (flags & CLOSE_ON_PROCESSOR_STOP  ) == CLOSE_ON_PROCESSOR_STOP  ;
        allowVisibilityChange = (flags & PREVENT_VISIBILITY_CHANGE) != PREVENT_VISIBILITY_CHANGE;
        disableConfigAutoSave = (flags & DISABLE_CONFIG_AUTO_SAVE ) == DISABLE_CONFIG_AUTO_SAVE ;
    }

    @Override
    public void updateTranslations(@NotNull Translation translation) {
        localeNoProgramSelected = translation.getOrDefault("application.noProgramSelected");
        localeProgramSelected = translation.getOrDefault("application.programSelected");

        localePerformanceLabel = translation.getOrDefault("application.performanceLabel");
        localeNoProcessorRunning = translation.getOrDefault("application.noProcessorRunning");
        updateTitle();
    }

    @Override
    public void loadConfig(@NotNull ConfigEvent e) {
        // Check if the config is on the right version, if not reset it to defaults
        String configVersion = e.config.get(String.class, "version");
        if (configVersion == null || StringUtils.compareVersions(configVersion, APP_VERSION) != 0) {
            e.stop();
            ConfigManager.resetToDefault();
        } else {
            // We let the app crash if config couldn't be loaded successfully
            processorConfig.setBits(e.config.get(Integer.class, "processorConfig.bits"));
            processorConfig.setMemorySize(e.config.get(Integer.class, "processorConfig.memorySize"));
            processorConfig.setClockFrequency(e.config.get(Integer.class, "processorConfig.clockFrequency"));
        }
    }

    @Override
    public void saveConfig(@NotNull ConfigEvent e) {
        e.config.put("processorConfig.bits", processorConfig.getBits());
        e.config.put("processorConfig.memorySize", processorConfig.getMemorySize());
        e.config.put("processorConfig.clockFrequency", processorConfig.getClockFrequency());
    }

    @Override
    public void setDefaults(@NotNull ConfigEvent e) {
        e.config.put("version", APP_VERSION);
        e.config.put("processorConfig.bits", ProcessorConfig.DEFAULT_BITS);
        e.config.put("processorConfig.memorySize", ProcessorConfig.DEFAULT_MEMORY_SIZE);
        e.config.put("processorConfig.clockFrequency", ProcessorConfig.DEFAULT_FREQUENCY);
    }

    public void updateTitle() {
        setTitle(
                StringUtils.format(
                        "{0} {1} {2}",
                        APP_TITLE, APP_VERSION,
                        currentProgram == null ?
                                localeNoProgramSelected :
                                StringUtils.format(localeProgramSelected, currentProgram.getAbsolutePath())
                )
        );
    }

    public void setProducer(@NotNull Function<ProcessorConfig, IProcessor> producer) {
        processorProducer = producer;
    }

    public void setDummyProducer(@NotNull Function<ProcessorConfig, IDummyProcessor> producer) {
        dummyProcessorProducer = producer;
    }

    public void setCurrentProgram(@NotNull File program) {
        if (program.exists() && program.canRead())
            currentProgram = program;
        else currentProgram = null;
        updateTitle();
    }

    public @Nullable File getCurrentProgram() {
        return currentProgram;
    }

    public void setProcessorConfig(@NotNull ProcessorConfig config) {
        processorConfig = config;
        if (currentProcessor != null) currentProcessor.getClock().setFrequency(processorConfig.getClockFrequency());
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
            Console.Debug.println("Processor Producer was never specified (Try downloading the latest version of the app)!\n");
            return null;
        }

        // Create a new Processor with the current ProcessorConfig
        try {
            return processorProducer.apply(processorConfig);
        } catch (Exception err) {
            Console.Debug.println("Couldn't create Processor.");
            Console.Debug.printStackTrace(err, false);
            Console.Debug.println();
        }

        return null;
    }

    public @Nullable IProcessor createDummyProcessor() {
        // If no DummyProcessor was specified then create a full Processor
        if (dummyProcessorProducer == null)
            return createProcessor();

        try {
            return dummyProcessorProducer.apply(processorConfig);
        } catch (Exception err) {
            Console.Debug.println("Couldn't create Dummy Processor.");
            Console.Debug.printStackTrace(err, false);
            Console.Debug.println();
        }

        return null;
    }

    public CompiledProgram compileProgram(@Nullable IProcessor processorInstance) {
        if (processorInstance == null) processorInstance = createProcessor();
        if (processorInstance == null) return null;

        CompiledProgram compiledProgram = null;

        if (currentProgram == null) {
            Console.Debug.println("No program specified!");
        } else if (!currentProgram.exists()) {
            Console.Debug.println("The specified program doesn't exist!");
        } else if (!currentProgram.canRead()) {
            Console.Debug.println("The specified program can't be read!");
        } else {
            try {
                compiledProgram = Compiler.compileFile(currentProgram, processorInstance);
                Console.Debug.println(
                        String.format(
                                "File was compiled successfully (%s), it occupies %d/%d Words",
                                currentProgram.getName(), compiledProgram.getProgram().length, processorInstance.getMemory().getSize() - processorInstance.getReservedWords()
                        )
                );
                Console.Debug.println(
                        "Compilation took " + StringUtils.getEngNotation(compiledProgram.getCompileTime(), "s")
                );
            } catch (Exception err) {
                Console.Debug.println("Compilation error (for file @'" + currentProgram.getAbsolutePath() + "'):");
                Console.Debug.printStackTrace(err, false);
            }
        }

        Console.Debug.println();

        return compiledProgram;
    }

    public void verifyProgram(ActionEvent e) {
        IProcessor dummyProcessor = createDummyProcessor();
        if (dummyProcessor == null) return;

        compileProgram(dummyProcessor);
    }

    public void obfuscateProgram(ActionEvent e) {
        IProcessor dummyProcessor = createDummyProcessor();
        if (dummyProcessor == null) return;

        CompiledProgram compiledProgram = compileProgram(dummyProcessor);
        if (compiledProgram == null) return;

        Console.Debug.println("Program obfuscated successfully:");
        Console.Debug.println(Compiler.obfuscateProgram(compiledProgram));
        Console.Debug.println();
    }

    public boolean runProcessor(ActionEvent e) {
        // Make sure that the last thread is dead
        if (currentProcessor != null && currentProcessor.isRunning()) {
            Console.Debug.println("Processor is already running!\n");
            return false;
        }

        // Clear Debug console and check if a program is specified
        if (Console.Debug instanceof IClearable) ((IClearable) Console.Debug).clear();

        // Create a new Processor with the specified values
        currentProcessor = createProcessor();
        if (currentProcessor == null) return false;

        // Compile the selected program
        CompiledProgram compiledProgram = compileProgram(currentProcessor);
        if (compiledProgram == null) return false;

        // Load compiled program into memory
        String loadError = null;
        try {
            loadError = currentProcessor.loadProgram(compiledProgram.getProgram());
        } catch (Exception err) {
            Console.Debug.println("Error while loading program into memory!");
            Console.Debug.printStackTrace(err, false);
            Console.Debug.println();
            return false;
        }

        if (loadError != null) {
            Console.Debug.println("Error while loading program!");
            Console.Debug.println("Processor Error: " + loadError + "\n");
            return false;
        }

        // Run the processor
        try {
            Console.Debug.println("Running Processor:\n" + currentProcessor.getInfo());

            if (Console.ProgramOutput instanceof IClearable)
                ((IClearable) Console.ProgramOutput).clear();

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

                    if (closeOnProcessorStop) Application.this.close(null);
                }
            };
            processorThread.start();

            return true;
        } catch (Exception err) {
            Console.Debug.println("Error while starting processor's thread!");
            Console.Debug.printStackTrace(err, false);
            Console.Debug.println();
            return false;
        }
    }

    public void stopProcessor(ActionEvent e) {
        if (currentProcessor == null || !currentProcessor.isRunning()) {
            Console.Debug.println("Couldn't stop processor because it isn't currently running!");
            Console.Debug.println();
            return;
        }
        currentProcessor.stop();
    }

    public void toggleProcessorExecution(ActionEvent e) {
        if (currentProcessor == null || !currentProcessor.isRunning()) {
            Console.Debug.println("Couldn't pause or resume processor because it isn't currently running!");
        } else if (currentProcessor.isPaused()) {
            currentProcessor.resume();
            Console.Debug.println("Processor was resumed!");
        } else {
            currentProcessor.pause();
            Console.Debug.println("Processor was paused!");
        }

        Console.Debug.println();
    }

    public void stepProcessor(ActionEvent e) {
        if (currentProcessor == null || !currentProcessor.isRunning()) {
            Console.Debug.println("Couldn't step processor because it isn't currently running!");
        } else {
            currentProcessor.step();
            Console.Debug.println("Processor stepped forward!");
        }

        Console.Debug.println();
    }

    @Override
    public void setVisible(boolean b) {
        if (allowVisibilityChange) super.setVisible(b);
    }

    public void close(ActionEvent e) {
        if (!disableConfigAutoSave) ConfigManager.saveConfig();
        System.exit(0);
    }
}
