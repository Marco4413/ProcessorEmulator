package io.github.hds.pemu.application;

import io.github.hds.pemu.compiler.CompiledProgram;
import io.github.hds.pemu.compiler.Compiler;
import io.github.hds.pemu.config.ConfigEvent;
import io.github.hds.pemu.config.ConfigManager;
import io.github.hds.pemu.config.IConfigurable;
import io.github.hds.pemu.console.Console;
import io.github.hds.pemu.localization.ITranslatable;
import io.github.hds.pemu.localization.Translation;
import io.github.hds.pemu.localization.TranslationManager;
import io.github.hds.pemu.plugins.DefaultPlugin;
import io.github.hds.pemu.plugins.IPlugin;
import io.github.hds.pemu.plugins.PluginManager;
import io.github.hds.pemu.processor.IProcessor;
import io.github.hds.pemu.processor.ProcessorConfig;
import io.github.hds.pemu.utils.CThread;
import io.github.hds.pemu.utils.IClearable;
import io.github.hds.pemu.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.HashSet;
import java.util.Objects;

public final class Application implements KeyListener, IConfigurable, ITranslatable {
    public static final String APP_NAME = "PEMU";
    public static final String APP_VERSION = "1.12.2";

    private static Application INSTANCE;

    public static final int NONE = 0;
    public static final int DISABLE_CONFIG_AUTO_SAVE = 1;
    public static final int CLOSE_ON_PROCESSOR_STOP = 1 << 1;

    private boolean isRunning = false;
    private IPlugin pluginToLoadOnRun = null;
    private IPlugin loadedPlugin = null;

    private @Nullable File currentProgram = null;
    private @Nullable IProcessor currentProcessor = null;
    private @NotNull ProcessorConfig processorConfig = new ProcessorConfig();
    private @NotNull Translation currentTranslation;

    private boolean disableConfigAutoSave = false;
    private boolean closeOnProcessorStop = false;

    private final HashSet<IApplicationListener> APPLICATION_LISTENERS = new HashSet<>();

    private final boolean HEADLESS;
    private final ApplicationGUI GUI_INSTANCE;

    private Application(boolean headless) {
        HEADLESS = headless || GraphicsEnvironment.isHeadless();

        GUI_INSTANCE = isHeadless() ? null : new ApplicationGUI(this);

        TranslationManager.addTranslationListener(this);
        ConfigManager.addConfigListener(this);
        currentTranslation = TranslationManager.getCurrentTranslation();

        ConfigManager.loadOrCreate();
    }

    public static @NotNull Application getInstance() {
        if (INSTANCE == null) INSTANCE = new Application(false);
        return INSTANCE;
    }

    public static @NotNull Application getInstance(boolean headless) {
        if (INSTANCE == null) INSTANCE = new Application(headless);
        return INSTANCE;
    }

    public @NotNull ApplicationGUI getGUI() {
        if (GUI_INSTANCE == null)
            throw new HeadlessException("Trying to get instance of Application's GUI while in Headless Mode.");
        return GUI_INSTANCE;
    }

    public boolean isHeadless() {
        return HEADLESS;
    }

    public boolean addApplicationListener(@NotNull IApplicationListener listener) {
        return APPLICATION_LISTENERS.add(listener);
    }

    public boolean removeApplicationListener(@NotNull IApplicationListener listener) {
        return APPLICATION_LISTENERS.remove(listener);
    }

    public boolean loadPlugin(@Nullable String pluginID) {
        return loadPlugin(PluginManager.getPlugin(pluginID));
    }

    public boolean loadPlugin(@Nullable IPlugin plugin) {
        // Load the Plugin only if it was registered in the PluginManager
        //  or if it's not the same as the currently loaded one
        if (
                !PluginManager.hasPlugin(plugin) ||
                loadedPlugin != null &&
                Objects.equals(loadedPlugin.getID(), plugin.getID())
        ) return false;

        if (isRunning) {
            if (loadedPlugin != null)
                loadedPlugin.onUnload();

            try {
                loadedPlugin = plugin;
                plugin.onLoad();
            } catch (Throwable err) {
                loadedPlugin = null;
                Console.Debug.println(StringUtils.format(
                        currentTranslation.getOrDefault("messages.pluginLoadFailed"), plugin.getID()
                ));
                Console.Debug.println(StringUtils.format(
                        currentTranslation.getOrDefault("messages.pluginErrorMessage"), StringUtils.stackTraceAsString(err)
                ));
                Console.Debug.println();
            }
        } else {
            pluginToLoadOnRun = plugin;
        }

        return true;
    }

    public @Nullable IPlugin getLoadedPlugin() {
        return loadedPlugin;
    }

    public void setCurrentProgram(@NotNull File program) {
        if (program.canRead())
            currentProgram = program;
        else currentProgram = null;
        APPLICATION_LISTENERS.forEach(l -> l.onProgramChanged(currentProgram));
    }

    public @Nullable File getCurrentProgram() {
        return currentProgram;
    }

    public void setProcessorConfig(@NotNull ProcessorConfig config) {
        processorConfig = config;
        if (currentProcessor != null)
            currentProcessor.getClock().setFrequency(processorConfig.getClockFrequency());
    }

    public @NotNull ProcessorConfig getProcessorConfig() {
        return processorConfig;
    }

    public @Nullable IProcessor createProcessor() {
        IPlugin loadedPlugin = getLoadedPlugin();
        if (loadedPlugin == null) {
            Console.Debug.println(currentTranslation.getOrDefault("messages.noPluginLoaded"));
            Console.Debug.println();
            return null;
        }

        // Create a new Processor with the current ProcessorConfig
        try {
            IProcessor processor = loadedPlugin.onCreateProcessor(processorConfig);
            if (processor == null) {
                Console.Debug.println(StringUtils.format(
                        currentTranslation.getOrDefault("messages.pluginNullProcessor"),
                        loadedPlugin.toString()
                ));
                Console.Debug.println();
            } else return processor;
        } catch (Throwable err) {
            Console.Debug.println(currentTranslation.getOrDefault("messages.processorCreationError"));
            Console.Debug.printStackTrace(err, false);
            Console.Debug.println();
        }

        return null;
    }

    public @Nullable IProcessor createDummyProcessor() {
        IPlugin loadedPlugin = getLoadedPlugin();
        if (loadedPlugin == null) {
            Console.Debug.println(currentTranslation.getOrDefault("messages.noPluginLoaded"));
            Console.Debug.println();
            return null;
        }

        try {
            IProcessor dummyProcessor = loadedPlugin.onCreateDummyProcessor(processorConfig);
            if (dummyProcessor == null) dummyProcessor = loadedPlugin.onCreateProcessor(processorConfig);

            if (dummyProcessor == null) {
                Console.Debug.println(StringUtils.format(
                        currentTranslation.getOrDefault("messages.pluginNullDummyProcessor"),
                        loadedPlugin.toString()
                ));
                Console.Debug.println();
            } else return dummyProcessor;
        } catch (Throwable err) {
            Console.Debug.println(currentTranslation.getOrDefault("messages.dummyProcessorCreationError"));
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
            Console.Debug.println(currentTranslation.getOrDefault("messages.noProgramSpecified"));
        } else if (!currentProgram.exists()) {
            Console.Debug.println(currentTranslation.getOrDefault("messages.programNotFound"));
        } else if (!currentProgram.canRead()) {
            Console.Debug.println(currentTranslation.getOrDefault("messages.programNotReadable"));
        } else {
            try {
                compiledProgram = Compiler.compileFile(currentProgram, processorInstance);
                Console.Debug.println(StringUtils.format(
                        currentTranslation.getOrDefault("messages.compiledSuccessfully"),
                        currentProgram.getName(), compiledProgram.getProgram().length,
                        processorInstance.getMemory().getSize() - processorInstance.getReservedWords(),
                        "Words"
                ));
                Console.Debug.println(StringUtils.format(
                        currentTranslation.getOrDefault("messages.compileTime"),
                        StringUtils.getEngNotation(compiledProgram.getCompileTime(), "s")
                ));
            } catch (Exception err) {
                Console.Debug.println(StringUtils.format(
                        currentTranslation.getOrDefault("messages.compileError"),
                        currentProgram.getName()
                ));
                Console.Debug.printStackTrace(err, false);
            }
        }

        Console.Debug.println();

        return compiledProgram;
    }

    public void verifyProgram() {
        IProcessor dummyProcessor = createDummyProcessor();
        if (dummyProcessor == null) return;

        compileProgram(dummyProcessor);
    }

    public void obfuscateProgram() {
        IProcessor dummyProcessor = createDummyProcessor();
        if (dummyProcessor == null) return;

        CompiledProgram compiledProgram = compileProgram(dummyProcessor);
        if (compiledProgram == null) return;

        Console.Debug.println(currentTranslation.getOrDefault("messages.obfuscatedSuccessfully"));
        Console.Debug.println(Compiler.obfuscateProgram(compiledProgram));
        Console.Debug.println();
    }

    public boolean runProcessor() {
        // Make sure that the last thread is dead
        if (currentProcessor != null && currentProcessor.isRunning()) {
            Console.Debug.println(currentTranslation.getOrDefault("messages.processorAlreadyRunning"));
            Console.Debug.println();
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
            Console.Debug.println(currentTranslation.getOrDefault("messages.programMemoryLoadError"));
            Console.Debug.printStackTrace(err, false);
            Console.Debug.println();
            return false;
        }

        if (loadError != null) {
            Console.Debug.println(currentTranslation.getOrDefault("messages.programLoadError"));
            Console.Debug.println(StringUtils.format(
                    currentTranslation.getOrDefault("messages.processorError"),
                    loadError
            ));
            Console.Debug.println();
            return false;
        }

        // Run the processor
        try {
            Console.Debug.println(currentTranslation.getOrDefault("messages.processorRunning"));
            Console.Debug.println(currentProcessor.getInfo());

            if (Console.ProgramOutput instanceof IClearable)
                ((IClearable) Console.ProgramOutput).clear();

            // We want to make sure that if the Processor fails, details about the error show on the Console
            CThread.runThread(
                    currentProcessor,
                    () -> {
                        Console.Debug.println(currentTranslation.getOrDefault("messages.processorStopped"));
                        Console.Debug.println();

                        if (closeOnProcessorStop) System.exit(0);
                    },
                    err -> {
                        currentProcessor.stop(); // Make sure to stop the processor if it fails
                        Console.Debug.println(currentTranslation.getOrDefault("messages.programRunningError"));
                        Console.Debug.printStackTrace(err, false);
                    }
            );

            return true;
        } catch (Exception err) {
            Console.Debug.println(currentTranslation.getOrDefault("messages.processorThreadError"));
            Console.Debug.printStackTrace(err, false);
            Console.Debug.println();
            return false;
        }
    }

    public void stopProcessor() {
        if (currentProcessor == null || !currentProcessor.isRunning()) {
            Console.Debug.println(currentTranslation.getOrDefault("messages.processorStopNotRunning"));
            Console.Debug.println();
            return;
        }
        currentProcessor.stop();
    }

    public @Nullable IProcessor getCurrentProcessor() {
        return currentProcessor;
    }

    public boolean isProcessorPaused() {
        return currentProcessor == null || currentProcessor.isPaused();
    }

    public boolean isProcessorRunning() {
        return currentProcessor != null && currentProcessor.isRunning();
    }

    public void toggleProcessorExecution() {
        if (currentProcessor == null || !currentProcessor.isRunning()) {
            Console.Debug.println(currentTranslation.getOrDefault("messages.processorPauseResumeNotRunning"));
        } else if (currentProcessor.isPaused()) {
            currentProcessor.resume();
            Console.Debug.println(currentTranslation.getOrDefault("messages.processorResumed"));
        } else {
            currentProcessor.pause();
            Console.Debug.println(currentTranslation.getOrDefault("messages.processorPaused"));
        }

        Console.Debug.println();
    }

    public void stepProcessor() {
        if (currentProcessor == null || !currentProcessor.isRunning()) {
            Console.Debug.println(currentTranslation.getOrDefault("messages.processorStepNotRunning"));
        } else {
            currentProcessor.step();
            Console.Debug.println(currentTranslation.getOrDefault("messages.processorStepped"));
        }

        Console.Debug.println();
    }

    public void setFlags(int flags) {
        disableConfigAutoSave = (flags & DISABLE_CONFIG_AUTO_SAVE) == DISABLE_CONFIG_AUTO_SAVE;
        closeOnProcessorStop = (flags & CLOSE_ON_PROCESSOR_STOP) == CLOSE_ON_PROCESSOR_STOP;
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public void run() {
        if (isRunning) throw new RuntimeException("Application is already running.");
        isRunning = true;

        if (GUI_INSTANCE != null) GUI_INSTANCE.FRAME.setVisible(true);
        loadPlugin(pluginToLoadOnRun);
    }

    public void close() {
        if (!disableConfigAutoSave) ConfigManager.saveConfig();
        System.exit(0);
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

    @Override
    @SuppressWarnings("ConstantConditions")
    public void loadConfig(@NotNull ConfigEvent e) {
        // Check if the config is on the right version, if not reset it to defaults
        String configVersion = e.config.get(String.class, "version");
        if (configVersion == null || StringUtils.compareVersions(configVersion, APP_VERSION) != 0) {
            e.stop();
            ConfigManager.resetToDefault();
            return;
        }

        // We let the app crash if config couldn't be loaded successfully
        processorConfig.setBits(e.config.get(Integer.class, "processorConfig.bits"));
        processorConfig.setMemorySize(e.config.get(Integer.class, "processorConfig.memorySize"));
        processorConfig.setClockFrequency(e.config.get(Integer.class, "processorConfig.clockFrequency"));

        TranslationManager.setCurrentTranslation(
                e.config.get(String.class, "selectedLanguage")
        );

        PluginManager.registerPlugins();
        this.loadPlugin(PluginManager.getPlugin(
                e.config.get(String.class, "loadedPlugin")
        ));
    }

    @Override
    public void saveConfig(@NotNull ConfigEvent e) {
        e.config.put("processorConfig.bits", processorConfig.getBits());
        e.config.put("processorConfig.memorySize", processorConfig.getMemorySize());
        e.config.put("processorConfig.clockFrequency", processorConfig.getClockFrequency());

        String selectedLanguage = TranslationManager.getCurrentTranslation().getShortName();
        e.config.put("selectedLanguage", selectedLanguage);

        IPlugin loadedPlugin = this.getLoadedPlugin();
        if (loadedPlugin != null)
            e.config.put("loadedPlugin", loadedPlugin.getID());
    }

    @Override
    public void setDefaults(@NotNull ConfigEvent e) {
        e.config.put("version", APP_VERSION);
        e.config.put("processorConfig.bits", ProcessorConfig.DEFAULT_BITS);
        e.config.put("processorConfig.memorySize", ProcessorConfig.DEFAULT_MEMORY_SIZE);
        e.config.put("processorConfig.clockFrequency", ProcessorConfig.DEFAULT_FREQUENCY);

        e.config.put("selectedLanguage", "en-us");
        String defaultPluginID = DefaultPlugin.getInstance().getID();
        e.config.put("loadedPlugin", defaultPluginID);
    }

    @Override
    public void updateTranslations(@NotNull Translation translation) {
        currentTranslation = translation;
    }
}
