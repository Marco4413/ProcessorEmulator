package io.github.hds.pemu.plugins;

import io.github.hds.pemu.app.Application;
import io.github.hds.pemu.instructions.Instructions;
import io.github.hds.pemu.processor.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DefaultPlugin extends AbstractPlugin {
    private static DefaultPlugin instance;
    private boolean active = false;

    // This Plugin doesn't use the @Plugin Annotation and is registered Manually,
    //  so only this class instantiates itself (hence the private modifier)
    private DefaultPlugin() {
        super("io.github.hds.pemu:default_processor", "Default Processor", Application.APP_VERSION);
    }

    public static @NotNull DefaultPlugin getInstance() {
        if (instance == null) instance = new DefaultPlugin();
        return instance;
    }

    @Override
    public IProcessor onCreateProcessor(@NotNull ProcessorConfig config) {
        return new Processor(config, Instructions.SET);
    }

    @Override
    public IDummyProcessor onCreateDummyProcessor(@NotNull ProcessorConfig config) {
        return Processor.getDummyProcessor(config, Instructions.SET);
    }

    @Override
    public @Nullable String onRegister() {
        return null;
    }

    @Override
    public void onLoad() {
        active = true;
    }

    @Override
    public void onUnload() {
        active = false;
    }

    @Override
    public String toString() {
        return super.toString() + (active ? " | Active" : "");
    }
}
