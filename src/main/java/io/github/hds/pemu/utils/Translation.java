package io.github.hds.pemu.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;

public class Translation {

    private final HashMap<String, String> MAP;

    public Translation() {
        this(new HashMap<>());
    }

    public Translation(@NotNull HashMap<String, String> translationMap) {
        MAP = translationMap;
    }

    public @NotNull String getName() {
        return getOrDefault("_longName", getOrDefault("_shortName"));
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
