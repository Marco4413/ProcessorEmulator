package io.github.hds.pemu.memory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IRegister {

    @Nullable String getFullName();
    @NotNull String getShortName();

    int getValue();
    int setValue(int value);

}
