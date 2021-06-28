package io.github.hds.pemu.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;

public final class StringUtils {

    public static class SpecialCharacters {
        public static final HashMap<Character, Character> MAP = new HashMap<>();
        public static final HashMap<Character, Character> INVERSE_MAP = new HashMap<>();

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

            MAP.forEach((k, v) -> INVERSE_MAP.put(v, k));
        }

        public static boolean isSpecialCharacter(int codePoint) {
            return INVERSE_MAP.containsKey((char) codePoint);
        }

        public static boolean isSpecialCharacter(char c) {
            return INVERSE_MAP.containsKey(c);
        }

        public static @NotNull String toString(char c) {
            return toString(c, "\\");
        }

        public static @NotNull String toString(char c, @NotNull String escapeCharacter) {
            if (isSpecialCharacter(c))
                return escapeCharacter + INVERSE_MAP.get(c);
            else return String.valueOf(c);
        }

        public static @NotNull String escapeAll(@NotNull String str) {
            StringBuilder escapedStr = new StringBuilder();
            for (int i = 0; i < str.length(); i++) {
                char currentChar = str.charAt(i);
                if (INVERSE_MAP.containsKey(currentChar)) {
                    escapedStr.append("\\").append(INVERSE_MAP.get(currentChar));
                } else escapedStr.append(currentChar);
            }
            return escapedStr.toString();
        }

        public static @NotNull String escapeAll(char character) {
            if (INVERSE_MAP.containsKey(character))
                return "\\" + INVERSE_MAP.get(character);
            return String.valueOf(character);
        }
    }

    public static int parseInt(@NotNull String str) {
        return (int) parseLong(str);
    }

    public static long parseLong(@NotNull String str) {
        int radix = 10;
        if (str.startsWith("0x")) radix = 16;
        else if (str.startsWith("0o")) radix =  8;
        else if (str.startsWith("0b")) radix =  2;
        if (radix != 10) str = str.substring(2);
        return Long.parseLong(str, radix);
    }

    public static int compareVersions(@NotNull String version1, @NotNull String version2) {
        int[] version1Components = Arrays.stream(version1.split("\\.")).mapToInt(StringUtils::parseInt).toArray();
        int[] version2Components = Arrays.stream(version2.split("\\.")).mapToInt(StringUtils::parseInt).toArray();

        for (int i = 0; i < Math.max(version1Components.length, version2Components.length); i++) {
            int v1Component = i < version1Components.length ? version1Components[i] : 0;
            int v2Component = i < version2Components.length ? version2Components[i] : 0;

            if (v1Component > v2Component) return 1;
            else if (v1Component < v2Component) return -1;
        }

        return 0;
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

    private static final String[] ENG_NOTATION_PREFIXES = new String[] { "", "k", "M", "G", "T", "P", "E", "Z", "Y" };

    public static @NotNull String getEngNotationInt(int number) {
        return getEngNotationLong(number);
    }

    public static @NotNull String getEngNotationLong(long number) {
        String numberAsString = String.valueOf(number);
        int zeroCount = 0;
        for (int i = numberAsString.length() - 1; i >= 0; i--) {
            if (numberAsString.charAt(i) == '0') zeroCount++;
            else break;
        }

        int suffixIndex = Math.min(zeroCount / 3, ENG_NOTATION_PREFIXES.length - 1);
        return (number / (long) Math.pow(10, suffixIndex * 3)) + ENG_NOTATION_PREFIXES[suffixIndex];
    }
}
