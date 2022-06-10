package io.github.marco4413.pemu.config;

import org.jetbrains.annotations.NotNull;

public interface IConfigurable {

    void loadConfig(@NotNull ConfigEvent e);
    void saveConfig(@NotNull ConfigEvent e);
    default void setDefaults(@NotNull ConfigEvent e) { }

}
