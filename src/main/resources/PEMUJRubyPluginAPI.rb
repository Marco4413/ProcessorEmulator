
# Copyright (c) 2021 [hds536jhmk](https://github.com/hds536jhmk/ProcessorEmulator)
# 
# Permission is hereby granted, free of charge, to any person
# obtaining a copy of this software and associated documentation
# files (the "Software"), to deal in the Software without
# restriction, including without limitation the rights to use,
# copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the
# Software is furnished to do so, subject to the following
# conditions:
# 
# The above copyright notice and this permission notice shall be
# included in all copies or substantial portions of the Software.
# 
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
# EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
# OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
# NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
# HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
# WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
# FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
# OTHER DEALINGS IN THE SOFTWARE.

module PEMU

    include_package Java::IoGithubHdsPemuFiles
    include_package Java::IoGithubHdsPemuInstructions
    include_package Java::IoGithubHdsPemuLocalization
    include_package Java::IoGithubHdsPemuMemoryFlags
    include_package Java::IoGithubHdsPemuMemoryRegisters
    include_package Java::IoGithubHdsPemuMemory
    include_package Java::IoGithubHdsPemuProcessor
    java_import     Java::IoGithubHdsPemuApp::Application
    java_import     Java::IoGithubHdsPemuConsole::Console
    java_import     Java::IoGithubHdsPemuPlugins::IPlugin
    java_import     Java::IoGithubHdsPemuPlugins::Plugin
    java_import     Java::IoGithubHdsPemuUtils::MathUtils
    java_import     Java::IoGithubHdsPemuUtils::StringUtils
    java_import     Java::JavaAwtEvent::KeyEvent

end

# This section is used to keep compatibility between PEMU versions,
#  if you use these functions you're sure that everything will work right
# These functions will never get removed and will keep their intended
#  behaviour, though they could be @deprecated

# Logs the specified arguments to the Debug Console
# @param [Any] *args The things to write to the Debug Console
def PEMU::debug_log(*args)
    PEMU::Console.Debug.println *args
end

# Logs the specified arguments to the Program Output Console
# @param [Any] *args The things to write to the Program Output Console
def PEMU::program_log(*args)
    PEMU::Console.ProgramOutput.println *args
end

# Checks if the two translations are from the same language
# @param [PEMU::Translation] a
# @param [PEMU::Translation] b
# @return [Boolean] Whether or not the two translations are from the same language
def PEMU::translations_equal?(a, b)
    return a.getShortName == b.getShortName
end

# Parses a translation from the specified folder
# @param [String] path The folder to parse the translation from
# @param [String] short_name The short name of the translation to get
# @return [PEMU::Translation, nil] The specified translation or nil if the file couldn't be read or it's not valid
def PEMU::parse_translation!(path, short_name)
    translation_file_name = PEMU::FileUtils.getPathWithExtension short_name, PEMU::TranslationManager::LANGUAGE_EXTENSION
    translation_file = Paths::get(path, translation_file_name).toFile
    return nil if !translation_file.canRead
    
    translation_file_reader = FileReader.new translation_file
    translation = PEMU::TranslationManager.parseTranslation translation_file_reader
    return translation if translation.getShortName == short_name
    return nil
end

# Gets the value at the specified key from the specified translation
# @param [PEMU::Translation] The translation to get the key from
# @param [String] key The key to get the wanted value from
# @return [String] The value at the specified key or key if not present
def PEMU::get_from_translation(translation, key)
    return translation.getOrDefault key
end

# Returns the file name of the specified translation
# @param [PEMU::Translation] translation The translation to get the name from
# @return [String] The translation's file name
def PEMU::get_translation_file_name(translation)
    return translation.getShortName
end

# Gets the currently loaded translation
# @return [PEMU::Translation] The translation currently in use
def PEMU::get_current_translation()
    return PEMU::TranslationManager.getCurrentTranslation
end

# Merges the two specified translations
# @param [PEMU::Translation] a The base translation
# @param [PEMU::Translation] *others Translations to be merged with the base one
# @return [PEMU::Translation] The merged translation
def PEMU::merge_translations(a, *others)
    return a.merge *others
end

# Formats the specified string using the specified formats
# @param [String] str The string to format (of type "This is a {0}!")
# @param [Any] *formats The formats to format the string with
# @return [String] The formatted String
def PEMU::format_string(str, *formats)
    return PEMU::StringUtils.format(str, *formats)
end

# Returns the current version of the application
# @return [String] The current version of the app
def PEMU::get_version()
    return PEMU::Application::APP_VERSION
end

# Checks if the specified version is between min and max
# @param [String] v The version to check
# @param [String, nil] v_min The minimum required version, if nil it's ignored
# @param [String, nil] v_max The maximum required version, if nil it's ignored
# @return [Boolean] Whether or not the specified version is in the specified range
def PEMU::version_in_range?(v, v_min, v_max)
    return false if v_min != nil && PEMU::StringUtils.compareVersions(v, v_min) < 0
    return false if v_max != nil && PEMU::StringUtils.compareVersions(v, v_max) > 0
    return true
end
