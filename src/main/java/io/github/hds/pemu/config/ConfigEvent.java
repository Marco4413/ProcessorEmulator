package io.github.hds.pemu.config;

import io.github.hds.pemu.tokenizer.keyvalue.KeyValueData;
import io.github.hds.pemu.utils.IStoppable;

public final class ConfigEvent implements IStoppable {
    public enum EVENT_TYPE {
        SAVE, LOAD, DEFAULTS
    }

    private boolean stopping = false;
    private final EVENT_TYPE TYPE;

    /* Might seem odd that this is lower case (since it's final)
     * I've done that so that event listeners don't have to write event.CONFIG to access it
     * But they need to write event.config which is more easy on the eyes
     * Also not wrapping this into a getter to make it easily accessible
     */
    public final KeyValueData config;

    public ConfigEvent(EVENT_TYPE type, KeyValueData config) {
        this.TYPE = type;
        this.config = config;
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
