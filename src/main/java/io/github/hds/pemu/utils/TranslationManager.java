package io.github.hds.pemu.utils;

import io.github.hds.pemu.Main;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class TranslationManager {

    private static Translation currentTranslation;
    private static final ArrayList<ITranslatable> LISTENERS = new ArrayList<>();

    public static void addTranslationListener(@NotNull ITranslatable translatable) {
        LISTENERS.add(translatable);
    }

    public static void removeTranslationListener(@NotNull ITranslatable translatable) {
        LISTENERS.remove(translatable);
    }

    public static @NotNull Translation getCurrentTranslation() {
        return currentTranslation == null ? new Translation() : currentTranslation;
    }

    public static void setCurrentTranslation(@NotNull Translation translation) {
        currentTranslation = translation;
        LISTENERS.forEach(listener -> listener.updateTranslations(currentTranslation));
    }

    public static @NotNull Translation loadTranslation(@NotNull String resourcePath) {
        InputStream stream = Main.class.getResourceAsStream(resourcePath);
        return parseTranslation(new InputStreamReader(stream));
    }

    private static @NotNull Translation parseTranslation(@NotNull Readable readable) {
        KeyValueParser.ParsedData parsedData = KeyValueParser.parseKeyValuePairs(readable);
        HashMap<String, String> translationData = new HashMap<>();
        parsedData.forEach(
                (k, v) -> {
                    if (v != null) translationData.put(k, String.valueOf(v));
                }
        );
        return new Translation(translationData);
    }

}