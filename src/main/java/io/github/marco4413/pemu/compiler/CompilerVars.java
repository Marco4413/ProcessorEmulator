package io.github.marco4413.pemu.compiler;

import io.github.marco4413.pemu.utils.IPIntSupplier;
import io.github.marco4413.pemu.console.Console;
import org.jetbrains.annotations.NotNull;

import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.HashMap;

public final class CompilerVars extends HashMap<String, IPIntSupplier> {

    private static final CompilerVars C_VARS = new CompilerVars();

    static {
        for (Field field : KeyEvent.class.getFields()) {
            String fieldName = field.getName();
            if (fieldName.startsWith("VK_")) {
                try {
                    int value = field.getInt(null);
                    C_VARS.put(fieldName, data -> value);
                } catch (IllegalAccessException e) {
                    Console.Debug.printStackTrace(e);
                }
            }
        }
    }

    public static @NotNull CompilerVars getDefaultVars() {
        CompilerVars defaultVars = new CompilerVars();
        defaultVars.putAll(C_VARS);
        return defaultVars;
    }

}
