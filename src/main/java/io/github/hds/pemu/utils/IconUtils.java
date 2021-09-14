package io.github.hds.pemu.utils;

import io.github.hds.pemu.Main;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public final class IconUtils {

    public static @NotNull ImageIcon importIcon(@NotNull String resourceLocation, int size) {
        return importIcon(resourceLocation, size, size);
    }

    public static @NotNull ImageIcon importIcon(@NotNull String resourceLocation, int width, int height) {
        return new ImageIcon(
                new ImageIcon(Main.class.getResource(resourceLocation))
                        .getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH)
        );
    }

}
