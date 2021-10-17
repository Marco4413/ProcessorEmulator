package io.github.hds.pemu.plugins;

import io.github.hds.pemu.Main;
import io.github.hds.pemu.console.IConsole;
import io.github.hds.pemu.files.FileManager;
import io.github.hds.pemu.localization.Translation;
import io.github.hds.pemu.localization.TranslationManager;
import io.github.hds.pemu.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jruby.Profile;
import org.jruby.RubyInstanceConfig;
import org.jruby.embed.*;

import java.io.File;
import java.io.Writer;
import java.net.URL;
import java.util.Collections;

public final class RubyPlugin extends AbstractExternalPlugin {

    protected RubyPlugin(@NotNull File file, @Nullable String id, @Nullable String name, @Nullable String version) {
        super(file, PluginType.RUBY, id, name, version);
    }

    private static final URL RESOURCE_PATH_URL = Main.class.getClassLoader().getResource(".");
    private static @NotNull ScriptingContainer getRubyScriptingContainer(@NotNull Writer stdout, @NotNull Writer stderr) {
        ScriptingContainer container = new ScriptingContainer(LocalContextScope.THREADSAFE, LocalVariableBehavior.TRANSIENT);
        if (RESOURCE_PATH_URL != null) {
            container.setLoadPaths(
                    Collections.singletonList(RESOURCE_PATH_URL.getPath())
            );
        }

        container.setOutput(stdout);
        container.setError(stderr);
        container.setProfile(Profile.ALL);
        container.setAttribute(AttributeName.SHARING_VARIABLES, false);
        container.setCompileMode(RubyInstanceConfig.CompileMode.JIT);
        container.put("$stdin", null);

        return container;
    }

    @Override
    protected @Nullable IPlugin compile(@NotNull IConsole outputConsole) {
        File file = getFile();
        if (!file.canRead()) return null;
        String relativePluginPath = FileManager.getPluginDirectory().toPath().relativize(file.toPath()).toString();

        Translation currentTranslation = TranslationManager.getCurrentTranslation();
        Writer errorWriter = outputConsole.toWriter();

        try {
            errorWriter.write(
                    StringUtils.format(currentTranslation.getOrDefault("messages.pluginCompilationFailed"), relativePluginPath)
            );
            errorWriter.write('\n');
        } catch (Exception ignored) { }

        try {
            // Creating Scripting container and setting stdout to the given IConsole
            ScriptingContainer container = getRubyScriptingContainer(errorWriter, errorWriter);

            // Getting the return value from the script
            Object rubyPlugin = container.runScriptlet(PathType.ABSOLUTE, file.getAbsolutePath());

            // If it's not null and is assignable to IPlugin then return it casted to IPlugin
            if (rubyPlugin instanceof IPlugin)
                return (IPlugin) rubyPlugin;

            // Else write to stderr the description of the error
            errorWriter.write(StringUtils.format(
                    currentTranslation.getOrDefault("messages.pluginInvalidType"),
                    "IPlugin"
            ));
        } catch (Exception ignored) { }

        try {
            errorWriter.write('\n');
            errorWriter.flush();
        } catch (Exception ignored) { }

        return null;
    }

}
