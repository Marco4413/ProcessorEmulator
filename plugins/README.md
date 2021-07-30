# Plugins

## What can a Plugin add to PEMU?

Plugins were added to make it possible to create other implementations of a Processor, so they are designed to do so.

Though plugins have access to all of Java 1.8's Standard Library and also to the classes from PEMU and JRuby,
so **they might be able to do a lot of stuff and could also harm your computer, so it's recommended to pick plugins
with care**.

## How can I write a plugin for PEMU?

There are 2 ways to add plugins to PEMU, a safe one and a risky one:
 1. The safe one (Written in Java), obviously, is the more complicated one because you need to modify the source code
    of the program.
 2. The risky one (Written in Ruby). You might be asking, Why is it risky? Well, it's because it's as simple as dropping
    a folder into another one, which is pretty easy and malicious programs could add plugins without you knowing.

**NOTE: Ruby plugins aren't loaded until applied to the Application (Confirmed the selection on the plugin selection
menu).**

## How do I load a different Plugin?

It's really simple, you just need to open PEMU and go to `File -> Load Plugin` and select one from the drop down menu
and confirm your choice.

## Writing plugins using Ruby:

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

A list of easy-to-access classes from PEMU can be found [@PEMUJRubyPluginAPI.rb](https://github.com/hds536jhmk/ProcessorEmulator/blob/master/src/main/resources/PEMUJRubyPluginAPI.rb)

The above mentioned file can be required by `require "PEMUJRubyPluginAPI"`


### Example

A well-documented example can be found [@RubyProcessor/](https://github.com/hds536jhmk/ProcessorEmulator/tree/latest/plugins/RubyProcessor)

### Adding Ruby Plugins

Ruby Plugins can be added by going into `User's Home Dir -> PEMU -> plugins` and dropping the Plugin's folder.

On Windows: `%USERPROFILE%/PEMU/plugins/`
