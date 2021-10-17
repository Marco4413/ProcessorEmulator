package io.github.hds.pemu.plugins;

import io.github.hds.pemu.Main;
import io.github.hds.pemu.app.*;
import io.github.hds.pemu.files.FileManager;
import io.github.hds.pemu.files.FileUtils;
import io.github.hds.pemu.localization.Translation;
import io.github.hds.pemu.localization.TranslationManager;
import io.github.hds.pemu.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public final class PluginManager {

    protected static final String PLUGIN_EXT = "jar";
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

    private static void registerPlugin(@NotNull Class<?> pluginClass) {
        Translation currentTranslation = TranslationManager.getCurrentTranslation();
        String errorMessage;

        if (IPlugin.class.isAssignableFrom(pluginClass)) {
            try {
                registerPlugin((IPlugin) pluginClass.newInstance());
                return;
            } catch (Exception err) {
                errorMessage = StringUtils.format(
                        currentTranslation.getOrDefault("messages.pluginRegistrationFailed"),
                        err.toString()
                );
            }
        } else errorMessage = StringUtils.format(
                currentTranslation.getOrDefault("messages.pluginInvalidType"),
                IPlugin.class.getSimpleName()
        );

        Plugin pluginAnnotation = pluginClass.getAnnotation(Plugin.class);
        assert pluginAnnotation != null;

        System.err.println(StringUtils.format(
                currentTranslation.getOrDefault("messages.pluginRegistrationFailed"),
                pluginAnnotation.name()
        ));
        System.err.println(errorMessage);
        System.err.println();
    }

    /**
     * Registers all external plugins which can be found at {@link FileManager#getPluginDirectory}.
     * This function doesn't compile any Plugin, Plugins should only be compiled when loaded by {@link Application}
     */
    public static void registerPlugins() {
        File pluginsDir = FileManager.getPluginDirectory();
        if (!pluginsDir.isDirectory()) return;

        ArrayList<URL> classURLs = new ArrayList<>();
        for (File file : pluginsDir.listFiles()) {
            if (!file.isFile() || !FileUtils.getFileExtension(file).equalsIgnoreCase(PLUGIN_EXT)) continue;
            try {
                classURLs.add(file.toURI().toURL());
            } catch (Exception ignored) { }
        }

        URLClassLoader classLoader = null;
        try {
            classLoader = new URLClassLoader(
                    classURLs.toArray(new URL[0]), Main.class.getClassLoader()
            );

            Reflections reflections = new Reflections(
                    new ConfigurationBuilder()
                            // Setting search path of this Jar
                            .forPackage(Main.class.getPackage().getName(), classLoader.getParent())
                            // Setting search path of external Jars
                            .addClassLoaders(classLoader).addUrls(classLoader.getURLs())
                            // Only Scanning Annotated Types
                            .setScanners(Scanners.TypesAnnotated)
            );

            Set<Class<?>> pluginClasses = reflections.getTypesAnnotatedWith(Plugin.class, false);
            pluginClasses.forEach(PluginManager::registerPlugin);
        } catch (Exception err) {
            System.err.println("Error while Registering all Plugins:");
            System.err.println(StringUtils.stackTraceAsString(err));
            System.err.println();
        }

        if (classLoader != null) {
            try {
                classLoader.close();
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
