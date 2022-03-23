package io.github.hds.pemu.application.gui;

import io.github.hds.pemu.application.Application;
import io.github.hds.pemu.application.IApplicationListener;
import io.github.hds.pemu.console.Console;
import io.github.hds.pemu.console.ConsoleComponent;
import io.github.hds.pemu.files.FileUtils;
import io.github.hds.pemu.localization.ITranslatable;
import io.github.hds.pemu.localization.Translation;
import io.github.hds.pemu.localization.TranslationManager;
import io.github.hds.pemu.processor.Clock;
import io.github.hds.pemu.processor.IProcessor;
import io.github.hds.pemu.utils.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

/**
 * This class is not thread-safe by any means
 */
public final class ApplicationGUI implements ITranslatable, IApplicationListener {
    public static final int FRAME_WIDTH = 800;
    public static final int FRAME_HEIGHT = 600;
    public static final int FRAME_ICON_SIZE = 32;
    public static final int MENU_ITEM_ICON_SIZE = 20;

    public static final int PERFORMANCE_UPDATE_INTERVAL = 1000;

    private static ApplicationGUI INSTANCE;

    public final JFrame FRAME;
    public final JMenuBar MENU_BAR;

    protected final Application APP;

    private final FileMenu FILE_MENU;
    private final ProgramMenu PROGRAM_MENU;
    private final ProcessorMenu PROCESSOR_MENU;
    private final AboutMenu ABOUT_MENU;

    private final JLabel PERFORMANCE_LABEL;
    private final Timer UPDATE_TIMER;

    protected final MemoryView MEMORY_VIEW;

    private @NotNull Translation currentTranslation = TranslationManager.getCurrentTranslation();

    private ApplicationGUI(@NotNull Application parentApp) {
        APP = parentApp;

        FRAME = new JFrame();
        FRAME.setTitle(Application.APP_NAME);

        FRAME.setIconImage(IconUtils.importIcon("/assets/icon.png", FRAME_ICON_SIZE).getImage());

        FRAME.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        FRAME.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        FRAME.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                close(null);
            }
        });

        TranslationManager.addTranslationListener(this);
        APP.addApplicationListener(this);

        FRAME.setLayout(new BorderLayout());

        // Making instances of this class to allow it to get its translation
        GFileDialog.getInstance();

        MEMORY_VIEW = new MemoryView(this);

        MENU_BAR = new JMenuBar();
        FRAME.setJMenuBar(MENU_BAR);

        // FILE MENU
        FILE_MENU = new FileMenu(this);
        MENU_BAR.add(FILE_MENU);

        // PROGRAM MENU
        PROGRAM_MENU = new ProgramMenu(this);
        MENU_BAR.add(PROGRAM_MENU);

        // PROCESSOR MENU
        PROCESSOR_MENU = new ProcessorMenu(this);
        MENU_BAR.add(PROCESSOR_MENU);
        PROCESSOR_MENU.CONFIG_PANEL.setConfig(APP.getProcessorConfig());

        // ABOUT MENU
        ABOUT_MENU = new AboutMenu(this);
        MENU_BAR.add(ABOUT_MENU);

        ConsoleComponent programComponent = Console.getProgramComponent();
        ConsoleComponent debugComponent = Console.getDebugComponent();

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(programComponent), new JScrollPane(debugComponent));
        splitPane.setResizeWeight(0.5d);
        splitPane.resetToPreferredSizes();
        FRAME.add(splitPane, BorderLayout.CENTER);

        PERFORMANCE_LABEL = new JLabel();
        PERFORMANCE_LABEL.setBorder(new EmptyBorder(2, 10, 2, 10));
        PERFORMANCE_LABEL.setHorizontalAlignment(SwingConstants.RIGHT);
        FRAME.add(PERFORMANCE_LABEL, BorderLayout.PAGE_END);

        programComponent.addKeyListener(APP);

        UPDATE_TIMER = new Timer(PERFORMANCE_UPDATE_INTERVAL, this::updateFrame);
        UPDATE_TIMER.start();
    }

    public static @NotNull ApplicationGUI getInstance() {
        if (INSTANCE == null) INSTANCE = new ApplicationGUI(Application.getInstance());
        return INSTANCE;
    }

    private void updateFrame(ActionEvent actionEvent) {
        if (!FRAME.isVisible()) return;

        if (APP.isProcessorPaused() || !APP.isProcessorRunning()) {
            PERFORMANCE_LABEL.setText(currentTranslation.getOrDefault("application.noProcessorRunning"));
            return;
        }

        IProcessor processor = APP.getCurrentProcessor();
        if (processor == null) return;

        Clock clock = processor.getClock();
        double interval  = clock.getInterval();
        double deltaTime = clock.getDeltaTime();
        PERFORMANCE_LABEL.setText(
                StringUtils.format(
                        currentTranslation.getOrDefault("application.performanceLabel"),
                        StringUtils.getEngNotation(interval, "s"),
                        StringUtils.getEngNotation(deltaTime, "s"),
                        StringUtils.getEngNotation(deltaTime - interval, "s")
                )
        );
    }

    public void close(ActionEvent e) {
        APP.close();
    }

    @Override
    public void updateTranslations(@NotNull Translation translation) {
        currentTranslation = translation;
        onProgramChanged(APP.getCurrentProgram());
    }

    @Override
    public void onProgramChanged(@Nullable File newProgram) {
        FRAME.setTitle(
                StringUtils.format(
                        "{0} {1} {2}",
                        Application.APP_NAME, Application.APP_VERSION,
                        newProgram == null ?
                                currentTranslation.getOrDefault("application.noProgramSelected") :
                                StringUtils.format(
                                        currentTranslation.getOrDefault("application.programSelected"),
                                        FileUtils.tryGetCanonicalPath(newProgram)
                                )
                )
        );
    }
}
