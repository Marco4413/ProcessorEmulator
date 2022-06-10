package io.github.marco4413.pemu.plugins;

import io.github.marco4413.pemu.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractPlugin implements IPlugin {
    private final String ID;
    private final String NAME;
    private final String VERSION;

    protected AbstractPlugin() {
        this(null);
    }

    protected AbstractPlugin(@Nullable Plugin annotation) {
        Class<?> clazz = this.getClass();
        if (annotation == null)
            annotation = clazz.getAnnotation(Plugin.class);

        if (annotation == null) {
            ID = StringUtils.format("{0}:{1}", clazz.getPackage().getName(), clazz.getSimpleName());
            NAME = clazz.getSimpleName();
            VERSION = null;
        } else {
            ID = annotation.id();
            NAME = annotation.name();
            VERSION = annotation.version();
        }
    }

    protected AbstractPlugin(@NotNull String id, @Nullable String name, @Nullable String version) {
        assert this.getClass().getAnnotation(Plugin.class) == null;
        ID = id;
        NAME = name;
        VERSION = version;
    }

    @Override
    public final @NotNull String getID() {
        return ID;
    }

    @Override
    public final @Nullable String getName() {
        return NAME;
    }

    @Override
    public final @Nullable String getVersion() {
        return VERSION;
    }

    @Override
    public String toString() {
        return StringUtils.format("{0}:{1}", getName(), getVersion());
    }
}
