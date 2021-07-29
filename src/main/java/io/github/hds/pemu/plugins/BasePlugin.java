package io.github.hds.pemu.plugins;

import io.github.hds.pemu.Main;
import io.github.hds.pemu.app.Application;
import io.github.hds.pemu.processor.*;
import org.jetbrains.annotations.NotNull;

public final class BasePlugin extends Plugin {
    public BasePlugin() { }

    @Override
    public @NotNull String getID() {
        return Main.class.getPackage().getName() + ":default_processor";
    }

    @Override
    public String getName() {
        return "Default Processor";
    }

    @Override
    public String getVersion() {
        return Application.APP_VERSION;
    }

    @Override
    public IProcessor createProcessor(@NotNull ProcessorConfig config) {
        return new Processor(config);
    }

    @Override
    public IDummyProcessor createDummyProcessor(@NotNull ProcessorConfig config) {
        return Processor.getDummyProcessor(config);
    }
}
