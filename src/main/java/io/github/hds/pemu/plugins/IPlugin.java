package io.github.hds.pemu.plugins;

import io.github.hds.pemu.processor.IDummyProcessor;
import io.github.hds.pemu.processor.IProcessor;
import io.github.hds.pemu.processor.ProcessorConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IPlugin {
    default @Nullable String getID() { return null; }
    default @Nullable String getName() { return null; }
    default @Nullable String getVersion() { return null; }
    default @Nullable IProcessor createProcessor(@NotNull ProcessorConfig config) { return null; }
    default @Nullable IDummyProcessor createDummyProcessor(@NotNull ProcessorConfig config) { return null; }
}
