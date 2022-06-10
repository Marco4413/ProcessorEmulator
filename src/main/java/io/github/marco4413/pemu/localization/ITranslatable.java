package io.github.marco4413.pemu.localization;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ITranslatable {
    void updateTranslations(@NotNull Translation translation);
}
