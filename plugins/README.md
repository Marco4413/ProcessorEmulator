# Plugins

## What can a Plugin add to PEMU?

Plugins were added to make it possible to create other implementations of a Processor, so they are designed to do so.

Though plugins have access to all of Java 1.8's Standard Library and also to the classes from PEMU and JRuby,
so **they might be able to do a lot of stuff and could also harm your computer, so it's recommended to pick plugins
with care**.

## What types of PEMU Plugins exist?

There are 2 types of PEMU Plugins:
 1. [Native Plugins](#writing-plugins-using-java) (Written in Java).
 2. [Ruby Plugins](#writing-plugins-using-ruby).

## How do I load a different Plugin?

It's really simple, you just need to open PEMU and go to `File -> Load Plugin` and select one from the drop down menu
and confirm your choice.

## How do I add Plugins to PEMU?

Plugins can be added by going into `User's Home Dir -> PEMU -> plugins` and dropping the Plugin's folder.

 - Windows: `%USERPROFILE%/PEMU/plugins/`
 - Linux: `$HOME/PEMU/plugins/`

## Writing plugins using Java:

### Folder layout

```
[Plugin Folder]
   |
   |--> plugin.info // Plugin info (ID, Name, Version, ClassPath)
   |
   |--> <Plugin's Folder Name>.jar
   |
   <Other Plugin Data>
```

### Example

**WORK IN PROGRESS**

## Writing plugins using Ruby:

### Recommended VSCode Plugins

 1. [Ruby Solargraph](https://marketplace.visualstudio.com/items?itemName=castwide.solargraph)
    which adds intellisense and code completion to Ruby files.
 2. [PEMU Language Extension](https://marketplace.visualstudio.com/items?itemName=hds.pemu-language-extension)
    which adds syntax highlighting to language and plugin.info files.

### Folder layout

```
[Plugin Folder]
   |
   |--> plugin.info // Plugin info (ID, Name, Version)
   |
   |--> init.rb // Main plugin script, the name can also be the
   |            //  same as the folder's with the ".rb" extension
   <Other Plugin Dependencies>
```

### PEMU API

A list of easy-to-access classes from PEMU can be found in the file [PEMUJRubyPluginAPI.rb](https://github.com/hds536jhmk/ProcessorEmulator/blob/master/src/main/resources/PEMUJRubyPluginAPI.rb)

The above mentioned file can be required by `require "PEMUJRubyPluginAPI"`

### Example

A well-documented example can be found in the [PEMU-RubyProcessor](https://github.com/hds536jhmk/pemu-RubyProcessor)
repository.
