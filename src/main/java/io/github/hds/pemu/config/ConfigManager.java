package io.github.hds.pemu.config;

import io.github.hds.pemu.app.Application;
import io.github.hds.pemu.tokenizer.keyvalue.KeyValueData;
import io.github.hds.pemu.tokenizer.keyvalue.KeyValueParser;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ConfigManager {

    private enum EVENT_TYPE {
        SAVE, LOAD, DEFAULTS
    }

    public static final @NotNull Path CONFIG_PATH = Paths.get(System.getProperty("user.home"), Application.APP_TITLE + ".config");

    private static KeyValueData config = new KeyValueData();
    private static KeyValueData defaultConfig = null;

    private static ArrayList<IConfigurable> LISTENERS = new ArrayList<>();
    private static boolean stoppingEvent = false;

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
            sendEvent(EVENT_TYPE.DEFAULTS);
        }
        return new KeyValueData(defaultConfig);
    }

    public static void resetToDefault() {
        config = getDefaultConfig();
        sendEvent(EVENT_TYPE.LOAD);
    }

    public static void loadOrCreate() {
        if (!loadConfig()) {
            saveConfig(true);
            sendEvent(EVENT_TYPE.LOAD);
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
            sendEvent(EVENT_TYPE.LOAD);
            return true;
        }
        return false;
    }

    public static boolean saveConfig() {
        return saveConfig(false);
    }

    public static boolean saveConfig(boolean skipListeners) {
        if (!skipListeners)
            sendEvent(EVENT_TYPE.SAVE);

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

    private static void sendEvent(EVENT_TYPE type) {
        stoppingEvent = false;
        switch (type) {
            case LOAD:
                for (IConfigurable listener : LISTENERS) {
                    if (stoppingEvent) break;
                    listener.loadConfig(config);
                }
                break;
            case SAVE:
                for (IConfigurable listener : LISTENERS) {
                    if (stoppingEvent) break;
                    listener.saveConfig(config);
                }
                break;
            case DEFAULTS:
                LISTENERS.forEach(listener -> listener.setDefaults(defaultConfig));
                break;
        }
    }

    public static void stopEvent() {
        stoppingEvent = true;
    }

}
