package io.github.hds.pemu.config;

import io.github.hds.pemu.tokenizer.keyvalue.KeyValueData;

public interface IConfigurable {

    public void loadConfig(KeyValueData config);
    public void saveConfig(KeyValueData config);
    public default void setDefaults(KeyValueData defaultConfig) { }

}
