package io.github.hds.pemu.config;

import io.github.hds.pemu.tokenizer.keyvalue.KeyValueData;
import org.jetbrains.annotations.NotNull;

public interface IConfigurable {

    void loadConfig(@NotNull KeyValueData config);
    void saveConfig(@NotNull KeyValueData config);
    default void setDefaults(@NotNull KeyValueData defaultConfig) { }

}
