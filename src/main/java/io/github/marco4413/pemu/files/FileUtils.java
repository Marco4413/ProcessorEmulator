package io.github.marco4413.pemu.files;

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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static boolean createFile(@NotNull File file, boolean isDirectory) {
        try {
            if (isDirectory) {
                Files.createDirectories(file.toPath());
            } else {
                Files.createDirectories(file.toPath().normalize().getParent());
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
        assert files != null;
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

    public static @NotNull String getFileExtension(@NotNull File file) {
        String fileName = file.getName();
        int extIndex = fileName.lastIndexOf('.');
        if (extIndex <= 0) return "";
        return fileName.substring(extIndex + 1);
    }

    public static @NotNull File getFileWithExtension(@NotNull File file, @NotNull String... extensions) {
        return new File(getPathWithExtension(file.getAbsolutePath(), extensions));
    }

    public static @NotNull String getPathWithExtension(@NotNull String path, @NotNull String... extensions) {
        if (extensions.length == 0) return path;

        for (String extension : extensions) {
            if (path.endsWith("." + extension)) return path;
        }
        return path + "." + extensions[0];
    }

    public static @NotNull String tryGetCanonicalPath(@NotNull File file) {
        try {
            return file.getCanonicalPath();
        } catch (Exception ignored) { }
        return file.toPath().normalize().toFile().getAbsolutePath();
    }
}
