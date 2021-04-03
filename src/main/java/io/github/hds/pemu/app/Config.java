package io.github.hds.pemu.app;

import io.github.hds.pemu.utils.KeyValueData;
import io.github.hds.pemu.utils.KeyValueParser;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class Config {

    public static final @NotNull Path CONFIG_PATH = Paths.get(System.getProperty("user.home"), Application.APP_TITLE + ".config");

    private static Config INSTANCE;
    private KeyValueData cfg;

    private Config() {
        resetToDefault();
    }

    public static @NotNull Config getInstance() {
        if (INSTANCE == null) INSTANCE = new Config();
        return INSTANCE;
    }

    public @NotNull KeyValueData getConfig() {
        return cfg;
    }

    private static @NotNull KeyValueData getDefaultConfig() {
        HashMap<String, Object> defaultConfig = new HashMap<>();
        defaultConfig.put("selectedLanguage", "en-us");
        return new KeyValueData(defaultConfig);
    }

    public void resetToDefault() {
        cfg = getDefaultConfig();
    }

    public void loadOrCreate() {
        if (!loadConfig()) saveConfig();
    }

    public boolean loadConfig() {
        File file = CONFIG_PATH.toFile();
        if (file.exists() && file.isFile() && file.canRead()) {
            FileReader reader;
            try {
                reader = new FileReader(file);
            } catch (Exception err) {
                cfg = getDefaultConfig();
                return false;
            }
            cfg = KeyValueParser.parseKeyValuePairs(reader);
            return true;
        }
        return false;
    }

    public boolean saveConfig() {
        File file = CONFIG_PATH.toFile();
        if (file.isDirectory()) return false;
        try {
            PrintWriter writer = new PrintWriter(file, "UTF-8");
            writer.print(cfg.toString());
            writer.close();
            return true;
        } catch (Exception ignored) { }
        return false;
    }

}
