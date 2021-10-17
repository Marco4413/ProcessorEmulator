package io.github.hds.pemu.plugins;

import io.github.hds.pemu.Main;
import io.github.hds.pemu.console.IConsole;
import io.github.hds.pemu.files.FileManager;
import io.github.hds.pemu.localization.Translation;
import io.github.hds.pemu.localization.TranslationManager;
import io.github.hds.pemu.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

public final class NativePlugin extends AbstractExternalPlugin {

    private final String CLASS_PATH;

    protected NativePlugin(@NotNull File file, @Nullable String id, @Nullable String name, @Nullable String version, @NotNull String classPath) {
        super(file, PluginType.NATIVE, id, name, version);
        CLASS_PATH = classPath;
    }

    @Override
    protected @Nullable IPlugin compile(@NotNull IConsole outputConsole) {
        File file = getFile();
        if (!file.canRead()) return null;
        String relativePluginPath = FileManager.getPluginDirectory().toPath().relativize(getFile().toPath()).toString();

        Translation currentTranslation = TranslationManager.getCurrentTranslation();
        String errorMessage = StringUtils.format(currentTranslation.getOrDefault("messages.pluginCompilationFailed"), relativePluginPath);
        String errorDescription;

        try {
            // Loading Jar File (Putting Error Message before the action so that if it throws the message is set)
            URLClassLoader jarURL = new URLClassLoader(
                    new URL[] { getFile().toURI().toURL() },
                    Main.class.getClassLoader()
            );

            // Getting class from specified classpath
            Class<?> classInstance = Class.forName(CLASS_PATH, false, jarURL);

            // Create a new Instance of the Plugin
            Object pluginInstance = classInstance.newInstance();

            // If the Plugin is an instance of IPlugin then return it
            if (pluginInstance instanceof IPlugin)
                return (IPlugin) pluginInstance;

            errorDescription = StringUtils.format(
                    currentTranslation.getOrDefault("messages.pluginInvalidType"),
                    "IPlugin"
            );
        } catch (LinkageError err) {
            errorDescription = StringUtils.format(
                    currentTranslation.getOrDefault("messages.pluginNativeLinkError"),
                    err
            );
        } catch (ClassNotFoundException err) {
            errorDescription = StringUtils.format(
                    currentTranslation.getOrDefault("messages.pluginNativeNoClassFound"),
                    CLASS_PATH
            );
        } catch (Throwable err) {
            errorDescription = StringUtils.format(
                    currentTranslation.getOrDefault("messages.pluginNativeLoadError"),
                    err
            );
        }

        outputConsole.println(errorMessage);
        outputConsole.println(errorDescription);

        // If we get here compilation has failed
        return null;
    }

}
