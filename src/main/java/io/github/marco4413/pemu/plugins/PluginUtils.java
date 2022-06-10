package io.github.marco4413.pemu.plugins;

import io.github.marco4413.pemu.localization.Translation;
import io.github.marco4413.pemu.localization.TranslationManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public final class PluginUtils {

    /**
     * Gets the resource of a Plugin following the PEMU convention:
     * 'plugins/PluginName/MyResource'
     * @param plugin The plugin to get the resource of
     * @param resourcePath The path to the plugin's resource (Can also use '../' to access other plugins' resources)
     * @return The {@link InputStream} of the Resource or null if none
     */
    public static @Nullable InputStream getPluginResource(@NotNull IPlugin plugin, @NotNull String resourcePath) {
        try {
            String pluginName = plugin.getName();
            URI resourceURI = new URI(
                    pluginName == null ?
                            resourcePath :
                            ( "/plugins/" + pluginName.replaceAll("\\s", "_") + "/" + resourcePath )
            ).normalize();

            return plugin.getClass().getResourceAsStream(
                    resourceURI.toString()
            );
        } catch (Exception err) {
            System.err.println("URI Error for Resource of Plugin: " + plugin.getID());
            err.printStackTrace();
            return null;
        }
    }

    /**
     * Gets the translation for the specified Plugin:
     * 'plugins/PluginName/localization/en-us.lang'
     * Basically it loads the plugin's translation file (If Present) and merges
     * it with the current PEMU translation of {@link TranslationManager}
     * @param plugin The plugin to get the {@link Translation} for
     * @return The {@link Translation} for the specified plugin
     */
    public static @NotNull Translation getPluginTranslation(@NotNull IPlugin plugin) {
        return getPluginTranslation(plugin, "en-us");
    }

    /**
     * Gets the translation for the specified Plugin:
     * 'plugins/PluginName/localization/en-us.lang'
     * Basically it loads the plugin's translation file (If Present) and merges
     * it with the current PEMU translation of {@link TranslationManager}
     * @param plugin The plugin to get the {@link Translation} for
     * @param defaultTranslation The name of the default translation which will be used if there's no {@link Translation}
     *                           matching the current {@link Translation#getShortName()}
     * @return The {@link Translation} for the specified plugin
     */
    public static @NotNull Translation getPluginTranslation(@NotNull IPlugin plugin, @NotNull String defaultTranslation) {
        Translation translation = TranslationManager.getCurrentTranslation();
        String shortName = translation.getShortName();

        InputStream stream = getPluginResource(plugin, "localization/" + shortName + ".lang");
        if (stream == null) stream = getPluginResource(plugin, "localization/" + defaultTranslation + ".lang");
        if (stream == null) return translation;

        Translation pluginTranslation = TranslationManager.parseTranslation(new InputStreamReader(stream, StandardCharsets.UTF_8));
        return pluginTranslation.merge(translation);
    }

}
