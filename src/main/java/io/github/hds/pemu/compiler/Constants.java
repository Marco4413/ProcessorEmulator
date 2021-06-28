package io.github.hds.pemu.compiler;

import org.jetbrains.annotations.NotNull;

import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.HashMap;

public final class Constants {

    private static final HashMap<String, Integer> CONSTANTS = new HashMap<>();

    static {
        for (Field field : KeyEvent.class.getFields()) {
            String fieldName = field.getName();
            if (fieldName.startsWith("VK_"))
                try {
                    CONSTANTS.put(fieldName, field.getInt(null));
                } catch (Exception ignored) { }
        }
    }

    public static @NotNull HashMap<String, Integer> getDefaultConstants() {
        return new HashMap<>(CONSTANTS);
    }

}
