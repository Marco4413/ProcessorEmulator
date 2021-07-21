# Processor Emulator

![](./logo.png)

## What's this project all about?

One day I woke up and decided to challenge myself.
So this project came to life, it's a short story but full of headaches.
Don't expect much from this emulator, it's made just for learning purposes.

## Does this program create any files on disk?

From version 1.0.0 this program creates one file named `PEMU.config` in the user's home dir to save all config data.

On Windows: `%USERPROFILE%/PEMU.config`

## How to run:

There are four main ways to run this project:
 1. By downloading the precompiled jar file and running it.
 2. By opening the project using **IntelliJ IDEA** and building the **project's artifact**.
 3. By opening the project using **IntelliJ IDEA** and creating a new **Run Config**:
    - Java Version: `1.8`
    - Class Path: `Processor_Emulator.main`
    - Main Class: `io.github.hds.pemu.Main`
 4. (**NOTE: This isn't really tested or supported**) By compiling it yourself! Download the latest version from the repo
    and run `gradlew build`, the compiled file should be located in `./build/libs/PEMU-version.jar`.

## Examples:

PEMU Program Examples can be found [@examples/](https://github.com/hds536jhmk/ProcessorEmulator/tree/master/examples)

## PEMU Libraries:

Since version `1.10.0` added the `#INCLUDE` Compiler Instruction (see [docs](#documentation)) libraries became possible,
so I've made a [Standard Library](https://github.com/hds536jhmk/ProcessorEmulator/tree/master/stdlib) Which is separated
into modules (since Memory Management is so important) with all functions that I've found myself rewriting all the time.

You can download the above linked folder and start using it yourself to make your life much easier!

## Documentation:

Can be found [@DOCUMENTATION.md](https://github.com/hds536jhmk/ProcessorEmulator/blob/master/DOCUMENTATION.md)

## Utilities:

### vscode-pemu-language

There's a VSCode Extension which currently adds Icons (Still waiting for VSCode to support extending other themes),
Syntax Highlighting and other utilities to be used with the PEMU Programming Language:

 - https://marketplace.visualstudio.com/items?itemName=hds.pemu-language-extensions
 - https://github.com/hds536jhmk/vscode-pemu-language

### PEMULangUtil.py

There's also a bundled Python script in the jar file that is useful if you want to verify your PEMU programs from the
command line, it's called [PEMULangUtil.py](https://github.com/hds536jhmk/ProcessorEmulator/blob/master/src/main/resources/PEMULangUtil.py).
It doesn't require any dependencies, it just needs the standard Python library and [Python 3.8.X](https://www.python.org/downloads/),
it should also work on newer versions, but it's not tested.

## Dependencies:

There are actually no dependencies except for `JetBrains Annotations` that's used at compile time.

## Screenshots:

The Main Window that contains the Debugging Console (the one on the bottom), and the Processor Console (where the
processor can print to on the top) and other info/settings:

![](./preview_main_window.png)

The Processor Config Panel where you can change your Processor's settings
(Clock can be changed while the Processor's running):

![](./preview_processor_config.png)

The Memory View window where the Processor's Memory is shown to the user, this has also some visual settings:

![](./preview_memory_view.png)
