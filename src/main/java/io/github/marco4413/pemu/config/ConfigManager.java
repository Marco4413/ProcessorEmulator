package io.github.marco4413.pemu.config;

import io.github.marco4413.pemu.files.FileManager;
import io.github.marco4413.pemu.keyvalue.KeyValueData;
import io.github.marco4413.pemu.keyvalue.KeyValueParser;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;

public final class ConfigManager {

    private static KeyValueData config = new KeyValueData();
    private static KeyValueData defaultConfig = null;
    private static boolean defaultOnLoadError = false;
    private static final int MAX_ERRORS_ON_LOAD = 10;

    private static final ArrayList<IConfigurable> LISTENERS = new ArrayList<>();

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
            sendEvent(ConfigEvent.EVENT_TYPE.DEFAULTS);
        }
        return new KeyValueData(defaultConfig);
    }

    public static void resetToDefault() {
        config = getDefaultConfig();
        sendEvent(ConfigEvent.EVENT_TYPE.LOAD);
    }

    public static void loadOrCreate() {
        if (!loadConfig()) {
            saveConfig(true);
            sendEvent(ConfigEvent.EVENT_TYPE.LOAD);
        }
    }

    public static boolean loadConfig() {
        File file = FileManager.getConfigFile();
        if (file.isFile() && file.canRead()) {
            FileReader reader;
            try {
                reader = new FileReader(file);
            } catch (Exception err) {
                config = getDefaultConfig();
                return false;
            }
            config = KeyValueParser.parseKeyValuePairs(reader);
            sendEvent(ConfigEvent.EVENT_TYPE.LOAD);
            return true;
        }
        return false;
    }

    public static boolean saveConfig() {
        return saveConfig(false);
    }

    public static boolean saveConfig(boolean skipListeners) {
        if (!skipListeners) sendEvent(ConfigEvent.EVENT_TYPE.SAVE);

        File file = FileManager.getConfigFile();
        if (file.isDirectory() || !file.canWrite()) return false;
        try {
            PrintWriter writer = new PrintWriter(file, "UTF-8");
            writer.print(config.toString());
            writer.close();
            return true;
        } catch (Exception ignored) { }
        return false;
    }

    public static void setDefaultOnLoadError(boolean value) {
        defaultOnLoadError = value;
    }

    // They ARE NOT replaceable, do you understand that the Array MIGHT change? IDEA, please don't make me hate you
    // It can change because Plugins ( which are registered while loading the config ) might register themselves to
    //  listen for Config events and using foreach this method would throw
    @SuppressWarnings("ForLoopReplaceableByForEach")
    private static void sendEvent(ConfigEvent.EVENT_TYPE type) {
        switch (type) {
            case LOAD: {
                int loadAttempts = 0;
                while (true) {
                    // Create a new event
                    ConfigEvent event = new ConfigEvent(type, config);
                    try {
                        // Send the event to all listeners
                        for (int i = 0; i < LISTENERS.size(); i++) {
                            LISTENERS.get(i).loadConfig(event);
                            if (event.isStopping()) break;
                        }
                        // If all events were successfully sent then break
                        break;
                    } catch (Exception err) {
                        // If an error was thrown then reset to defaults if specified
                        //  Or throw after MAX_ERRORS_ON_LOAD attempts
                        if (loadAttempts++ >= MAX_ERRORS_ON_LOAD || !defaultOnLoadError) throw err;
                        resetToDefault();
                    }
                }
                break;
            }
            case SAVE: {
                ConfigEvent event = new ConfigEvent(type, config);
                for (int i = 0; i < LISTENERS.size(); i++) {
                    LISTENERS.get(i).saveConfig(event);
                    if (event.isStopping()) break;
                }
                break;
            }
            case DEFAULTS: {
                for (int i = 0; i < LISTENERS.size(); i++) {
                    LISTENERS.get(i).setDefaults(
                            new ConfigEvent(type, defaultConfig)
                    );
                }
                break;
            }
        }
    }
}
