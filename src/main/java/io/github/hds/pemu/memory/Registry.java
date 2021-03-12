package io.github.hds.pemu.memory;

import org.jetbrains.annotations.NotNull;

public class Registry {

    public final String NAME;
    public final String SHORT;

    private int value = 0;

    public Registry(@NotNull String name) {
        NAME = name;

        StringBuilder shortName = new StringBuilder();
        String[] words = name.split("\\s");
        for (String word : words) {
            shortName.append( word.charAt(0) );
        }

        SHORT = shortName.toString().toUpperCase();
    }

    public Registry(@NotNull String name, @NotNull String shortName) {
        NAME = name;
        SHORT = shortName;
    }

    public int getValue() { return value; }

    public int setValue(int value) {
        int oldValue = this.value;
        this.value = value;
        return oldValue;
    }


}
