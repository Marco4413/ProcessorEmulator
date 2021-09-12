package io.github.hds.pemu.plugins;

import io.github.hds.pemu.console.Console;
import io.github.hds.pemu.files.FileManager;
import io.github.hds.pemu.localization.Translation;
import io.github.hds.pemu.localization.TranslationManager;
import io.github.hds.pemu.processor.IDummyProcessor;
import io.github.hds.pemu.processor.IProcessor;
import io.github.hds.pemu.processor.ProcessorConfig;
import io.github.hds.pemu.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Objects;

public final class ExternalPlugin extends Plugin {

    private final File PLUGIN_FILE;
    private final PluginType PLUGIN_TYPE;
    private IPlugin pluginInstance = null;

    private final String ID;
    private final String NAME;
    private final String VERSION;

    protected ExternalPlugin(@NotNull File file, @NotNull PluginType pluginType, @Nullable String id, @Nullable String name, @Nullable String version) {
        PLUGIN_FILE = file;
        PLUGIN_TYPE = pluginType;
        ID      = id;
        NAME    = name;
        VERSION = version;
    }

    @Override
    public @Nullable String getID() {
        return ID;
    }

    @Override
    public @Nullable String getName() {
        return NAME;
    }

    @Override
    public @Nullable String getVersion() {
        return VERSION;
    }

    @Override
    public @Nullable IProcessor onCreateProcessor(@NotNull ProcessorConfig config) {
        return pluginInstance == null ? null : pluginInstance.onCreateProcessor(config);
    }

    @Override
    public @Nullable IDummyProcessor onCreateDummyProcessor(@NotNull ProcessorConfig config) {
        return pluginInstance == null ? null : pluginInstance.onCreateDummyProcessor(config);
    }

    @Override
    public boolean onLoad() throws Exception {
        pluginInstance = PluginManager.compilePlugin(Console.Debug, PLUGIN_FILE, PLUGIN_TYPE);
        if (pluginInstance == null) return false;

        Translation currentTranslation = TranslationManager.getCurrentTranslation();

        String mismatchType = null;
        if (!Objects.equals(getID(), pluginInstance.getID()))
            mismatchType = currentTranslation.getOrDefault("messages.pluginID");
        else if (!Objects.equals(getName(), pluginInstance.getName()))
            mismatchType = currentTranslation.getOrDefault("messages.pluginName");
        else if (!Objects.equals(getVersion(), pluginInstance.getVersion()))
            mismatchType = currentTranslation.getOrDefault("messages.pluginVersion");

        if (mismatchType == null) return pluginInstance.onLoad();

        String relativePluginPath = FileManager.getPluginDirectory().toPath().relativize(PLUGIN_FILE.toPath()).toString();
        Console.Debug.println(StringUtils.format(currentTranslation.getOrDefault("messages.pluginLoadFailed"), relativePluginPath));
        Console.Debug.println(StringUtils.format(currentTranslation.getOrDefault("messages.pluginInfoMismatch"), mismatchType, PluginManager.PLUGIN_INFO_FILE_NAME));
        Console.Debug.println();
        return false;
    }

    @Override
    public void onUnload() {
        if (pluginInstance != null) {
            pluginInstance.onUnload();
            pluginInstance = null;
        }
    }

    @Override
    public String toString() {
        try {
            return pluginInstance.toString();
        } catch (Exception ignored) { }
        return super.toString();
    }
}
