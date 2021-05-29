package io.github.hds.pemu.localization;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ITranslatable {
    void updateTranslations(@NotNull Translation translation);
}
