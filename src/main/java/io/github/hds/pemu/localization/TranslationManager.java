package io.github.hds.pemu.localization;

import io.github.hds.pemu.Main;
import io.github.hds.pemu.files.FileUtils;
import io.github.hds.pemu.keyvalue.KeyValueData;
import io.github.hds.pemu.keyvalue.KeyValueParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public final class TranslationManager {

    public static final String LANGUAGE_EXTENSION = "lang";

    private static Translation currentTranslation = new Translation();
    private static final ArrayList<ITranslatable> LISTENERS = new ArrayList<>();

    private static final HashMap<String, Translation> AVAILABLE_TRANSLATIONS = new HashMap<>();

    static {
        Scanner languagesList = new Scanner(
                Main.class.getResourceAsStream("/localization/languages.txt")
        );

        while (languagesList.hasNextLine()) {
            String languageShortName = languagesList.nextLine().trim();
            if (languageShortName.length() == 0 || AVAILABLE_TRANSLATIONS.containsKey(languageShortName)) continue;

            Translation translation = loadTranslation(FileUtils.getPathWithExtension(
                "/localization/" + languageShortName, LANGUAGE_EXTENSION
            ));

            if (translation.getShortName().equals(languageShortName))
                AVAILABLE_TRANSLATIONS.put(languageShortName, translation);
        }
    }

    public static boolean hasTranslation(@NotNull String shortName) {
        return AVAILABLE_TRANSLATIONS.containsKey(shortName);
    }

    public static @Nullable Translation getTranslation(@NotNull String shortName) {
        return AVAILABLE_TRANSLATIONS.get(shortName);
    }

    public static @NotNull Translation[] getAvailableTranslations() {
        return AVAILABLE_TRANSLATIONS.values().toArray(new Translation[0]);
    }

    public static void addTranslationListener(@NotNull ITranslatable translatable) {
        LISTENERS.add(translatable);
    }

    public static void removeTranslationListener(@NotNull ITranslatable translatable) {
        LISTENERS.remove(translatable);
    }

    public static @NotNull Translation getCurrentTranslation() {
        return currentTranslation;
    }

    private static boolean setCurrentTranslation(@NotNull Translation translation) {
        currentTranslation = translation;
        LISTENERS.forEach(listener -> listener.updateTranslations(currentTranslation));
        return true;
    }

    public static boolean setCurrentTranslation(@NotNull String shortName) {
        Translation translation = getTranslation(shortName);
        if (translation != null) {
            setCurrentTranslation(translation);
            return true;
        }
        return false;
    }

    private static @NotNull Translation loadTranslation(@NotNull String resourcePath) {
        InputStream stream = Main.class.getResourceAsStream(resourcePath);
        return parseTranslation(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }

    public static @NotNull Translation parseTranslation(@NotNull Readable readable) {
        KeyValueData parsedData = KeyValueParser.parseKeyValuePairs(readable);
        HashMap<String, String> translationData = new HashMap<>();
        parsedData.forEach(
                (k, v) -> {
                    if (v != null) translationData.put(k, String.valueOf(v));
                }
        );
        return new Translation(translationData);
    }

}
