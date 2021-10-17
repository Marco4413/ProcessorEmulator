package io.github.hds.pemu.plugins;

import io.github.hds.pemu.app.*;
import io.github.hds.pemu.files.FileManager;
import io.github.hds.pemu.files.FileUtils;
import io.github.hds.pemu.keyvalue.KeyValueData;
import io.github.hds.pemu.keyvalue.KeyValueParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.HashMap;

public final class PluginManager {

    protected static final String PLUGIN_INFO_FILE_NAME = "plugin.info";

    private static final HashMap<String, IPlugin> PLUGINS = new HashMap<>();

    /**
     * Registers the specified plugin
     * @param plugin The plugin to register
     * @param <T> The type of plugin (Must implement {@link IPlugin}), it's used to return the same type as the given plugin
     * @return The registered plugin
     */
    public static <T extends IPlugin> @Nullable T registerPlugin(@Nullable T plugin) {
        if (plugin == null) return null;

        String pluginID = plugin.getID();
        if (pluginID == null || PLUGINS.containsKey(pluginID))
            return null;

        PLUGINS.put(plugin.getID(), plugin);
        return plugin;
    }

    private static @Nullable IPlugin getPluginInstance(@NotNull File file, @NotNull KeyValueData pluginInfo) {
        String pluginID = pluginInfo.get(String.class, "plugin.id");
        String pluginName = pluginInfo.get(String.class, "plugin.name");
        String pluginVersion = pluginInfo.get(String.class, "plugin.version");

        switch (FileUtils.getFileExtension(file).toLowerCase()) {
            case "jar": {
                String classPath = pluginInfo.get(String.class, "plugin.classPath");
                if (classPath == null) return null;
                return new NativePlugin(
                        file, pluginID, pluginName, pluginVersion, classPath
                );
            }
            case "rb":
                return new RubyPlugin(
                        file, pluginID, pluginName, pluginVersion
                );
            default:
                return null;
        }
    }

    /**
     * Registers all external plugins which can be found at {@link FileManager#getPluginDirectory}.
     * This function doesn't compile any Plugin, Plugins should only be compiled when loaded by {@link Application}
     */
    public static void registerExternalPlugins() {
        File pluginsDir = FileManager.getPluginDirectory();
        if (!pluginsDir.isDirectory()) return;

        for (File pluginDirectory : pluginsDir.listFiles()) {
            if (!pluginDirectory.isDirectory()) continue;

            String pluginBaseName = pluginDirectory.getName();
            File pluginFile = FileUtils.getFileFromDirectory(pluginDirectory, "init.rb", pluginBaseName + ".rb", pluginBaseName + ".jar");
            File pluginInfo = FileUtils.getFileFromDirectory(pluginDirectory, PLUGIN_INFO_FILE_NAME);

            if (pluginFile == null || pluginInfo == null) continue;

            try {
                KeyValueData infoData = KeyValueParser.parseKeyValuePairs(new FileReader(pluginInfo));
                registerPlugin(
                        getPluginInstance(pluginFile, infoData)
                );
            } catch (Exception ignored) { }
        }
    }

    /**
     * Checks if a plugin with the specified ID was registered
     * @param pluginID The id of the plugin to check for
     * @return Whether or not the specified plugin was registered
     */
    public static boolean hasPlugin(@Nullable String pluginID) {
        if (pluginID == null) return false;
        return PLUGINS.containsKey(pluginID);
    }

    /**
     * Checks if the specified plugin instance was registered
     * @param plugin The plugin instance to check for
     * @return Whether or not the specified plugin was registered
     */
    public static boolean hasPlugin(@Nullable IPlugin plugin) {
        if (plugin == null) return false;
        return hasPlugin(plugin.getID());
    }

    /**
     * Returns a registered plugin with the specified id or null if none
     * @param pluginID The id of the plugin to search for
     * @return The instance of the specified plugin or null if none
     */
    public static @Nullable IPlugin getPlugin(@Nullable String pluginID) {
        if (hasPlugin(pluginID)) return PLUGINS.get(pluginID);
        return null;
    }

    /**
     * Returns all registered plugins
     * @return All registered plugins
     */
    public static @NotNull IPlugin[] getRegisteredPlugins() {
        return PLUGINS.values().toArray(new IPlugin[0]);
    }
}
