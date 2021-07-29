package io.github.hds.pemu.plugins;

import io.github.hds.pemu.Main;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.HashMap;

public final class PluginManager {

    protected static final URL RESOURCE_PATH_URL = Main.class.getClassLoader().getResource(".");

    private static final HashMap<String, IPlugin> PLUGINS = new HashMap<>();
    public static <T extends IPlugin> @Nullable T registerPlugin(@Nullable T plugin) {
        if (plugin == null) return null;

        String pluginID = plugin.getID();
        if (pluginID == null || PLUGINS.containsKey(pluginID))
            return null;

        PLUGINS.put(plugin.getID(), plugin);
        return plugin;
    }

    public static boolean hasPlugin(@Nullable String pluginID) {
        if (pluginID == null) return false;
        else if (PLUGINS.containsKey(pluginID)) return true;
        return PLUGINS.containsKey(pluginID);
    }

    public static boolean hasPlugin(@Nullable IPlugin plugin) {
        if (plugin == null) return false;
        return hasPlugin(plugin.getID());
    }

    public static @Nullable IPlugin getPlugin(@Nullable String pluginID) {
        if (hasPlugin(pluginID)) return PLUGINS.get(pluginID);
        return null;
    }

    public static @NotNull IPlugin[] getAllPlugins() {
        return PLUGINS.values().toArray(new IPlugin[0]);
    }
}
