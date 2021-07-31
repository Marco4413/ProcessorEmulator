package io.github.hds.pemu.localization;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;

public final class Translation {

    protected final HashMap<String, String> MAP;

    public Translation() {
        this(new HashMap<>());
    }

    public Translation(@NotNull HashMap<String, String> translationMap) {
        MAP = translationMap;
    }

    public static @NotNull Translation mergeTranslations(@NotNull Translation... translations) {
        Translation mergedTranslation = new Translation();
        for (int i = translations.length - 1; i >= 0; i--)
            translations[i].MAP.forEach(mergedTranslation.MAP::put);
        return mergedTranslation;
    }

    public @NotNull Translation merge(@NotNull Translation... others) {
        Translation mergedOthers = mergeTranslations(others);
        this.MAP.forEach(mergedOthers.MAP::put);
        return mergedOthers;
    }

    public @NotNull String getName() {
        return getOrDefault("_longName", getOrDefault("_shortName"));
    }

    public @NotNull String getLongName() {
        return getOrDefault("_longName", toString());
    }

    public @NotNull String getShortName() {
        return getOrDefault("_shortName", toString());
    }

    public @Nullable String get(@NotNull String key) {
        return MAP.get(key);
    }

    public @NotNull String getOrDefault(@NotNull String key) {
        return MAP.getOrDefault(key, key);
    }

    public @NotNull String getOrDefault(@NotNull String key, @NotNull String defaultValue) {
        return MAP.getOrDefault(key, defaultValue);
    }

    public void translateFrame(@NotNull String translationPath, @NotNull JFrame frame) {
        frame.setTitle(getOrDefault(translationPath + "._title"));
    }

    public void translateComponent(@NotNull String translationPath, @NotNull JLabel component) {
        component.setText(getOrDefault(translationPath + "._text"));
    }

    public void translateComponent(@NotNull String translationPath, @NotNull AbstractButton component) {
        component.setText(getOrDefault(translationPath + "._text"));
        component.setMnemonic(getOrDefault(translationPath + "._mnemonic", String.valueOf(KeyEvent.CHAR_UNDEFINED)).charAt(0));
    }

}
