package io.github.hds.pemu.plugins;

import io.github.hds.pemu.processor.IDummyProcessor;
import io.github.hds.pemu.processor.IProcessor;
import io.github.hds.pemu.processor.ProcessorConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IPlugin {
    /**
     * Returns the ID of this plugin
     * If null or another Plugin with the same ID is present then this Plugin won't be registered
     * @return The ID of this plugin
     */
    @Nullable String getID();

    /**
     * Returns the Name of this plugin
     * @return The Name of this plugin
     */
    @Nullable String getName();

    /**
     * Returns the Version of this plugin
     * @return The Version of this plugin
     */
    @Nullable String getVersion();

    /**
     * Method called when a new {@link IProcessor} needs to be created
     * @param config The config for the {@link IProcessor}
     * @return A new {@link IProcessor}
     */
    @Nullable IProcessor onCreateProcessor(@NotNull ProcessorConfig config);

    /**
     * Method called when a new {@link IDummyProcessor} needs to be created
     * If null is returned then {@link IPlugin#onCreateProcessor} is also called
     * @param config The config for the {@link IDummyProcessor}
     * @return A new {@link IDummyProcessor}
     */
    @Nullable IDummyProcessor onCreateDummyProcessor(@NotNull ProcessorConfig config);

    /**
     * Called before this plugin is registered
     * @return An error message or null if none
     */
    default @Nullable String onRegister() {
        // You can do your Version Check business here
        return null;
    }

    /**
     * Called when this plugin is being loaded
     * The old one is unloaded
     */
    default void onLoad() { }

    /**
     * Called when this plugin is being unloaded
     * Before the new one is loaded
     */
    default void onUnload() { }
}
