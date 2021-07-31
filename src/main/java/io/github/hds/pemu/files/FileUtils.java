package io.github.hds.pemu.files;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.nio.file.Files;

public final class FileUtils {

    public static boolean createFile(@NotNull File file) {
        return createFile(file, file.isDirectory());
    }

    @SuppressWarnings("all")
    public static boolean createFile(@NotNull File file, boolean isDirectory) {
        try {
            if (isDirectory) {
                Files.createDirectories(file.toPath());
            } else {
                Files.createDirectories(file.getParentFile().toPath());
                file.createNewFile();
            }
            return true;
        } catch (Exception err) {
            err.printStackTrace();
            return false;
        }
    }

    public static @Nullable File getFileFromDirectory(@NotNull File folder, String... fileNames) {
        File[] files = folder.listFiles();
        for (String fileName : fileNames) {
            for (File file : files) {
                if (file.isFile() && file.getName().equals(fileName)) return file;
            }
        }
        return null;
    }

    public static @NotNull String[] getFileExtensionFromFilter(@NotNull FileFilter filter) {
        if (filter instanceof FileNameExtensionFilter)
            return ((FileNameExtensionFilter) filter).getExtensions();
        return new String[0];
    }

    public static @NotNull File getFilePathWithExtension(@NotNull File file, @NotNull String... extensions) {
        return new File(getPathWithExtension(file.getAbsolutePath(), extensions));
    }

    public static @NotNull String getPathWithExtension(@NotNull String path, @NotNull String... extensions) {
        if (extensions.length == 0) return path;

        for (String extension : extensions) {
            if (path.endsWith("." + extension)) return path;
        }
        return path + "." + extensions[0];
    }
}
