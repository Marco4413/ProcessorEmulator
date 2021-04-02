package io.github.hds.pemu.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.regex.Matcher;

public class StringUtils {

    public static class SpecialCharacters {
        public static final HashMap<Character, Character> MAP = new HashMap<>();

        static {
            MAP.put('\'', '\''); // Quotes
            MAP.put('\"', '\"'); // Double quotes
            MAP.put('\\', '\\'); // Backslash
            MAP.put('t' , '\t'); // Tab character
            MAP.put('b' , '\b'); // Backspace
            MAP.put('r' , '\r'); // Carriage return
            MAP.put('f' , '\f'); // Form feed
            MAP.put('n' , '\n'); // Newline
            MAP.put('0' , '\0'); // NULL
        }
    }

    public static int parseInt(@NotNull String str) {
        int radix = 10;
        if (str.startsWith("0x")) radix = 16;
        else if (str.startsWith("0o")) radix =  8;
        else if (str.startsWith("0b")) radix =  2;
        if (radix != 10) str = str.substring(2);
        return Integer.parseInt(str, radix);
    }

    public static @NotNull String format(@Nullable String str, @NotNull String... formats) {
        if (str == null) return "";
        for (int i = 0; i < formats.length; i++) {
            str = str.replaceAll("\\{" + i + "}", Matcher.quoteReplacement(formats[i]));
        }
        return str;
    }

    public static @NotNull String toShortName(@NotNull String name) {
        StringBuilder shortName = new StringBuilder();
        String[] words = name.split("\\s+");
        for (String word : words) {
            shortName.append( word.charAt(0) );
        }

        return shortName.toString().toUpperCase();
    }

    public static @NotNull String stackTraceAsString(@NotNull Exception err) {
        StringWriter str = new StringWriter();
        err.printStackTrace(new PrintWriter(str));
        return str.toString();
    }

    public static @NotNull String[] getFileExtFromFilter(@NotNull FileFilter filter) {
        if (filter instanceof FileNameExtensionFilter)
            return ((FileNameExtensionFilter) filter).getExtensions();
        return new String[0];
    }

    public static @NotNull String getFilePathWExt(@NotNull File file, @NotNull String... extensions) {
        return getPathWExt(file.getAbsolutePath(), extensions);
    }

    public static @NotNull String getPathWExt(@NotNull String path, @NotNull String... extensions) {
        if (extensions.length == 0) return path;

        for (String extension : extensions) {
            if (path.endsWith("." + extension)) return path;
        }
        return path + "." + extensions[0];
    }
}
