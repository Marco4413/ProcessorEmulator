package io.github.hds.pemu.config;

import io.github.hds.pemu.tokenizer.keyvalue.KeyValueData;
import io.github.hds.pemu.utils.IStoppable;

public final class ConfigEvent implements IStoppable {
    public enum EVENT_TYPE {
        SAVE, LOAD, DEFAULTS
    }

    private boolean stopping = false;
    private final EVENT_TYPE TYPE;
    public final KeyValueData CONFIG;

    public ConfigEvent(EVENT_TYPE type, KeyValueData config) {
        TYPE = type;
        CONFIG = config;
    }

    @Override
    public void stop() {
        if (canStop()) stopping = true;
    }

    public boolean canStop() {
        return !stopping && TYPE != EVENT_TYPE.DEFAULTS;
    }

    public boolean isStopping() {
        return stopping;
    }

    public EVENT_TYPE getEventType() {
        return TYPE;
    }
}
