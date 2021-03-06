package io.github.hds.pemu.plugins;

import io.github.hds.pemu.processor.IDummyProcessor;
import io.github.hds.pemu.processor.IProcessor;
import io.github.hds.pemu.processor.ProcessorConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.StringWriter;

public interface IPlugin {
    /**
     * Returns the ID of this plugin
     * If null or another Plugin with the same ID is present then this Plugin won't be registered
     * @return The ID of this plugin
     */
    default @Nullable String getID() { return null; }

    /**
     * Returns the Name of this plugin
     * @return The Name of this plugin
     */
    default @Nullable String getName() { return null; }

    /**
     * Returns the Version of this plugin
     * @return The Version of this plugin
     */
    default @Nullable String getVersion() { return null; }

    /**
     * Method called when a new {@link IProcessor} needs to be created
     * @param config The config for the {@link IProcessor}
     * @return A new {@link IProcessor}
     */
    default @Nullable IProcessor onCreateProcessor(@NotNull ProcessorConfig config) { return null; }

    /**
     * Method called when a new {@link IDummyProcessor} needs to be created
     * If null is returned then {@link IPlugin#onCreateProcessor} is also called
     * @param config The config for the {@link IDummyProcessor}
     * @return A new {@link IDummyProcessor}
     */
    default @Nullable IDummyProcessor onCreateDummyProcessor(@NotNull ProcessorConfig config) { return null; }


    /**
     * Called when this plugin is being loaded
     * The old one is unloaded
     * @return Whether or not an error was encountered, if false then this plugin won't be fully loaded
     * @throws Exception This method may throw exceptions if any error occurred
     */
    default boolean onLoad() throws Exception { return true; }

    /**
     * Called when this plugin is being unloaded
     * Before the new one is loaded
     */
    default void onUnload() { }
}
