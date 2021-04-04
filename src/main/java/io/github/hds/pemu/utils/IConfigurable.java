package io.github.hds.pemu.utils;

public interface IConfigurable {

    public void loadConfig(KeyValueData config);
    public void saveConfig(KeyValueData config);
    public default void setDefaults(KeyValueData defaultConfig) { }

}
