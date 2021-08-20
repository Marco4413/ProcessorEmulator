package io.github.hds.pemu.plugins;

import io.github.hds.pemu.Main;
import io.github.hds.pemu.app.*;
import io.github.hds.pemu.files.FileManager;
import io.github.hds.pemu.files.FileUtils;
import io.github.hds.pemu.localization.Translation;
import io.github.hds.pemu.localization.TranslationManager;
import io.github.hds.pemu.tokenizer.keyvalue.KeyValueData;
import io.github.hds.pemu.tokenizer.keyvalue.KeyValueParser;
import io.github.hds.pemu.console.IConsole;
import io.github.hds.pemu.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jruby.Profile;
import org.jruby.RubyInstanceConfig;
import org.jruby.embed.*;

import java.io.*;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;

public final class PluginManager {

    protected static final String PLUGIN_INFO_FILE_NAME = "plugin.info";
    private static final URL RESOURCE_PATH_URL = Main.class.getClassLoader().getResource(".");
    private static @NotNull ScriptingContainer getScriptingContainer(@NotNull Writer stdout, @NotNull Writer stderr) {
        ScriptingContainer container = new ScriptingContainer(LocalContextScope.THREADSAFE, LocalVariableBehavior.TRANSIENT);
        if (RESOURCE_PATH_URL != null) {
            container.setLoadPaths(
                    Collections.singletonList(RESOURCE_PATH_URL.getPath())
            );
        }

        container.setOutput(stdout);
        container.setError(stderr);
        container.setProfile(Profile.ALL);
        container.setAttribute(AttributeName.SHARING_VARIABLES, false);
        container.setCompileMode(RubyInstanceConfig.CompileMode.JIT);
        container.put("$stdin", null);

        return container;
    }

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

    /**
     * Registers all external plugins which can be found at {@link FileManager#getPluginDirectory}.
     * This function doesn't compile any Plugin, Plugins should only be compiled when loaded by {@link Application}
     */
    public static void registerExternalPlugins() {
        File pluginsDir = FileManager.getPluginDirectory();
        if (!pluginsDir.isDirectory()) return;

        for (File pluginDirectory : pluginsDir.listFiles()) {
            if (!pluginDirectory.isDirectory()) continue;
            File pluginFile = FileUtils.getFileFromDirectory(pluginDirectory, "init.rb", pluginDirectory.getName() + ".rb");
            File pluginInfo = FileUtils.getFileFromDirectory(pluginDirectory, PLUGIN_INFO_FILE_NAME);
            if (pluginFile == null || pluginInfo == null) continue;

            try {
                KeyValueData infoData = KeyValueParser.parseKeyValuePairs(new FileReader(pluginInfo));
                registerPlugin(
                        new ExternalPlugin(
                                pluginFile, PluginType.RUBY,
                                infoData.get(String.class, "plugin.id"),
                                infoData.get(String.class, "plugin.name"),
                                infoData.get(String.class, "plugin.version")
                        )
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

    public static @NotNull IPlugin[] getRegisteredPlugins() {
        return PLUGINS.values().toArray(new IPlugin[0]);
    }

    private static @Nullable IPlugin compileRubyPlugin(@Nullable IConsole outputConsole, @NotNull File file) {
        if (!file.canRead()) return null;

        Translation currentTranslation = TranslationManager.getCurrentTranslation();
        Writer stdout, stderr;
        if (outputConsole == null) {
            stdout = new StringWriter();
            stderr = stdout;
        } else {
            stdout = outputConsole.toWriter();
            stderr = outputConsole.toWriter();

            String relativePluginPath = FileManager.getPluginDirectory().toPath().relativize(file.toPath()).toString();
            try {
                stderr.write(StringUtils.format(currentTranslation.getOrDefault("messages.pluginCompilationFailed"), relativePluginPath));
                stderr.write('\n');
            } catch (Exception ignored) { }
        }

        try {
            // Creating Scripting container and setting stdout to the given IConsole
            ScriptingContainer container = getScriptingContainer(stdout, stderr);

            // Getting the return value from the script
            Object rubyPlugin = container.runScriptlet(PathType.ABSOLUTE, file.getAbsolutePath());

            // If it's not null and is assignable to IPlugin then return it casted to IPlugin
            if (rubyPlugin != null && IPlugin.class.isAssignableFrom(rubyPlugin.getClass()))
                return (IPlugin) rubyPlugin;

            // Else write to stderr the description of the error
            stderr.write(
                    StringUtils.format(
                            currentTranslation.getOrDefault("messages.pluginInvalidType"),
                            "IPlugin"
                    )
            );
            stderr.write('\n');
        } catch (Exception ignored) { }

        try {
            stderr.write('\n');
            stderr.flush();
        } catch (Exception ignored) { }

        return null;
    }

    /**
     * Compiles and returns an instance of the specified plugin main file
     * NOTE: This doesn't register the compiled plugin, see {@link PluginManager#registerPlugin} instead
     * @param outputConsole The console to print stdout and stderr to
     * @param file The main file of the plugin to compile
     * @param pluginType The type of the plugin, for now only {@link PluginType#RUBY} is supported
     * @return An instance of the compiled plugin or null if it failed to load
     */
    public static @Nullable IPlugin compilePlugin(@Nullable IConsole outputConsole, @Nullable File file, @NotNull PluginType pluginType) {
        if (file == null) return null;
        if (pluginType == PluginType.RUBY)
            return compileRubyPlugin(outputConsole, file);
        return null;
    }
}
