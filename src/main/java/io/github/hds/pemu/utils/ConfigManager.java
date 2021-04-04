package io.github.hds.pemu.utils;

import io.github.hds.pemu.app.Application;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ConfigManager {

    public static final @NotNull Path CONFIG_PATH = Paths.get(System.getProperty("user.home"), Application.APP_TITLE + ".config");

    private static KeyValueData config = new KeyValueData();
    private static KeyValueData defaultConfig = null;

    private static ArrayList<IConfigurable> LISTENERS = new ArrayList<>();

    public static void addConfigListener(@NotNull IConfigurable configurable) {
        LISTENERS.add(configurable);
    }

    public static void removeConfigListener(@NotNull IConfigurable configurable) {
        LISTENERS.remove(configurable);
    }

    public static @NotNull KeyValueData getConfig() {
        return config;
    }

    public static @NotNull KeyValueData getDefaultConfig() {
        if (defaultConfig == null) {
            defaultConfig = new KeyValueData();
            LISTENERS.forEach(listener -> listener.setDefaults(defaultConfig));
        }
        return new KeyValueData(defaultConfig);
    }

    public static void resetToDefault() {
        config = getDefaultConfig();
    }

    public static void loadOrCreate() {
        if (!loadConfig()) {
            saveConfig(true);
            LISTENERS.forEach(listener -> listener.loadConfig(config));
        }
    }

    public static boolean loadConfig() {
        File file = CONFIG_PATH.toFile();
        if (file.exists() && file.isFile() && file.canRead()) {
            FileReader reader;
            try {
                reader = new FileReader(file);
            } catch (Exception err) {
                config = getDefaultConfig();
                return false;
            }
            config = KeyValueParser.parseKeyValuePairs(reader);
            LISTENERS.forEach(listener -> listener.loadConfig(config));
            return true;
        }
        return false;
    }

    public static boolean saveConfig() {
        return saveConfig(false);
    }

    public static boolean saveConfig(boolean skipListeners) {
        if (!skipListeners)
            LISTENERS.forEach(listener -> listener.saveConfig(config));

        File file = CONFIG_PATH.toFile();
        if (file.isDirectory()) return false;
        try {
            PrintWriter writer = new PrintWriter(file, "UTF-8");
            writer.print(config.toString());
            writer.close();
            return true;
        } catch (Exception ignored) { }
        return false;
    }

}
