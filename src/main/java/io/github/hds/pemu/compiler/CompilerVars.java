package io.github.hds.pemu.compiler;

import org.jetbrains.annotations.NotNull;

import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.HashMap;

public final class CompilerVars extends HashMap<String, Integer> {

    private static final CompilerVars C_VARS = new CompilerVars();

    static {
        for (Field field : KeyEvent.class.getFields()) {
            String fieldName = field.getName();
            if (fieldName.startsWith("VK_")) {
                try {
                    C_VARS.put(fieldName, field.getInt(null));
                } catch (Exception ignored) { }
            }
        }
    }

    public static @NotNull CompilerVars getDefaultVars() {
        CompilerVars defaultVars = new CompilerVars();
        C_VARS.forEach(defaultVars::put);
        return defaultVars;
    }

}
