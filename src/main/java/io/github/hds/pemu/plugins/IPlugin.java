package io.github.hds.pemu.plugins;

import io.github.hds.pemu.processor.IDummyProcessor;
import io.github.hds.pemu.processor.IProcessor;
import io.github.hds.pemu.processor.ProcessorConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IPlugin {
    @NotNull  String getID();
    @Nullable String getName();
    @Nullable String getDescription();
    @Nullable String getVersion();
    @Nullable IProcessor createProcessor(@NotNull ProcessorConfig config);
    @Nullable IDummyProcessor createDummyProcessor(@NotNull ProcessorConfig config);
}
