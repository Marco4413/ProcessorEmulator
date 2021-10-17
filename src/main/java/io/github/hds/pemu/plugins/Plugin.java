package io.github.hds.pemu.plugins;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface Plugin {
    @NotNull String id();
    @NotNull String name();
    @NotNull String version();
}
