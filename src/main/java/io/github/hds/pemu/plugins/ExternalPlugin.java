package io.github.hds.pemu.plugins;

import io.github.hds.pemu.files.FileManager;
import io.github.hds.pemu.processor.IDummyProcessor;
import io.github.hds.pemu.processor.IProcessor;
import io.github.hds.pemu.processor.ProcessorConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.StringWriter;
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
    public boolean onLoad(@NotNull StringWriter stderr) {
        pluginInstance = PluginManager.compilePlugin(stderr, PLUGIN_FILE, PLUGIN_TYPE);
        if (pluginInstance == null) return false;

        String mismatchType = null;
        if (!Objects.equals(getID(), pluginInstance.getID()))
            mismatchType = "ID";
        else if (!Objects.equals(getName(), pluginInstance.getName()))
            mismatchType = "Name";
        else if (!Objects.equals(getVersion(), pluginInstance.getVersion()))
            mismatchType = "Version";

        if (mismatchType == null) return pluginInstance.onLoad(stderr);

        String relativePluginPath = FileManager.getPluginDirectory().toPath().relativize(PLUGIN_FILE.toPath()).toString();
        stderr.write("Failed to load plugin: \"" + relativePluginPath + "\"\n");
        stderr.write(" - " + mismatchType + " mismatch between " + PluginManager.PLUGIN_INFO_FILE_NAME + " and given Plugin instance.\n");
        return false;
    }

    @Override
    public void onUnload() {
        pluginInstance.onUnload();
        pluginInstance = null;
    }

    @Override
    public String toString() {
        try {
            return pluginInstance.toString();
        } catch (Exception ignored) { }
        return super.toString();
    }
}
