package io.github.hds.pemu.files;

import io.github.hds.pemu.app.Application;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class FileManager {

    public static final Path HOME_DIR = Paths.get(System.getProperty("user.home"));
    public static final Path PEMU_DIR = HOME_DIR.resolve(Application.APP_TITLE + "/");

    public static final File CONFIG = PEMU_DIR.resolve("pemu.config").toFile();

    public static @NotNull File getConfigFile() {
        if (!FileUtils.createFile(CONFIG, false))
            throw new IllegalStateException("pemu config file couldn't be created.");
        return CONFIG;
    }

}
