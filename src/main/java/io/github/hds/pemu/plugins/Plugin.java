package io.github.hds.pemu.plugins;

import io.github.hds.pemu.utils.StringUtils;

public abstract class Plugin implements IPlugin {
    @Override
    public String toString() {
        return StringUtils.format("{0}:{1}", getName(), getVersion());
    }
}
