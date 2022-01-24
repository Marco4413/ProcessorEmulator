# Plugins

## What can a Plugin add to PEMU?

Plugins were added to make it possible to create other implementations of a Processor, so they are designed to do so.
Though they can also add stuff to the Application itself.

**NOTE: Plugins are written in Java, so they can use EVERYTHING that a normal Java App can use, so they might be able
to harm your computer, so it's recommended to pick plugins with care.**

## How do I load a different Plugin?

It's really simple, you just need to open PEMU and go to `File -> Load Plugin` and select one from the drop down menu
and confirm your choice.

## How do I add Plugins to PEMU?

Plugins can be added by going into `User's Home Dir -> .pemu -> plugins` and dropping the Plugin's File.

 - Windows: `%USERPROFILE%/.pemu/plugins/`
 - Linux: `$HOME/.pemu/plugins/`

## Writing Plugins:

### Recommended VSCode Plugins

 - [PEMU Language Extension](https://marketplace.visualstudio.com/items?itemName=hds.pemu-language-extension)
   which adds syntax highlighting to `lang` files.

### Example

A "well-documented" example can be found here:

https://github.com/hds536jhmk/pemu-ExamplePlugin
