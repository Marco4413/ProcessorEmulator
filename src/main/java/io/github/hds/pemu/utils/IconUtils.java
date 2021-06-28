package io.github.hds.pemu.utils;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public final class IconUtils {

    public static @NotNull ImageIcon importIcon(@NotNull String resourceLocation, int size) {
        return new ImageIcon(
                new ImageIcon(System.class.getResource(resourceLocation))
                        .getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH)
        );
    }

}
