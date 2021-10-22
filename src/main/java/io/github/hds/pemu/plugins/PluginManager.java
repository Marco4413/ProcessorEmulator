package io.github.hds.pemu.plugins;

import io.github.hds.pemu.Main;
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
    private static final ArrayList<IPlugin> REGISTER_QUEUE = new ArrayList<>();
    private static final HashMap<String, IPlugin> PLUGINS = new HashMap<>();

    /**
     * Queues the specified plugin for register when {@link PluginManager#registerPlugins} is called
     * @param plugin The plugin to queue for register
     */
    public static void queueForRegister(@Nullable IPlugin plugin) {
        if (plugin != null) REGISTER_QUEUE.add(plugin);
    }

    private static void registerPlugin(@NotNull IPlugin plugin) {
        Translation currentTranslation = TranslationManager.getCurrentTranslation();
        String errorMessage;

        String pluginID = plugin.getID();
        if (pluginID == null)
            return;
        else if (PLUGINS.containsKey(pluginID)) {
            errorMessage = StringUtils.format(
                    currentTranslation.getOrDefault("messages.pluginDuplicateID"),
                    plugin.getID()
            );
        } else errorMessage = plugin.onRegister();

        if (errorMessage != null) {
            System.err.println(StringUtils.format(
                    currentTranslation.getOrDefault("messages.pluginRegistrationFailed"),
                    plugin.getName()
            ));
            System.err.println(StringUtils.format(
                    currentTranslation.getOrDefault("messages.pluginErrorMessage"),
                    errorMessage
            ));
            System.err.println();
            return;
        }

        PLUGINS.put(plugin.getID(), plugin);
    }

    private static void registerPlugin(@NotNull Class<?> pluginClass) {
        Translation currentTranslation = TranslationManager.getCurrentTranslation();
        String errorMessage;

        // A Plugin Annotation must be present if this method is called
        Plugin pluginAnnotation = pluginClass.getAnnotation(Plugin.class);
        assert pluginAnnotation != null;

        // Checking using the ID on the Annotation if this Plugin has already been registered
        //  This is done so that no new instance is created uselessly
        if (hasPlugin(pluginAnnotation.id())) {
            // Unlike the other registerPlugin overload this has an error message for duplicate
            //  Plugins because these are registered automatically so it's nice to know if it failed
            errorMessage = StringUtils.format(
                    currentTranslation.getOrDefault("messages.pluginDuplicateID"),
                    pluginAnnotation.id()
            );
        // If the given class is an instance of IPlugin
        } else if (IPlugin.class.isAssignableFrom(pluginClass)) {
            try {
                // Register a new Instance of this class
                registerPlugin((IPlugin) pluginClass.newInstance());
                return;
            } catch (Exception err) {
                // Errors caught here are for example if the Constructor is protected,
                //  throws or there isn't one that takes no arguments
                errorMessage = err.toString();
            }
        } else errorMessage = StringUtils.format(
                currentTranslation.getOrDefault("messages.pluginInvalidType"),
                IPlugin.class.getSimpleName()
        );

        // If we get here then the Plugin couldn't be registered
        System.err.println(StringUtils.format(
                currentTranslation.getOrDefault("messages.pluginRegistrationFailed"),
                pluginAnnotation.name()
        ));
        System.err.println(StringUtils.format(
                currentTranslation.getOrDefault("messages.pluginErrorMessage"),
                errorMessage
        ));
        System.err.println();
    }

    /**
     * Registers all external and queued ( by {@link PluginManager#queueForRegister} ) plugins which can be found at
     * {@link FileManager#getPluginDirectory}, external Plugins are loaded each time while queued Plugins only once.
     */
    public static void registerPlugins() {
        if (REGISTER_QUEUE.size() > 0) {
            REGISTER_QUEUE.forEach(PluginManager::registerPlugin);
            REGISTER_QUEUE.clear();
        }

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
            // This is very unlikely so it's not translated
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
