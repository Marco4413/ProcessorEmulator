package io.github.hds.pemu.memory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IFlag {

    @Nullable String getFullName();
    @NotNull String getShortName();

    boolean getValue();
    boolean setValue(boolean value);

}
