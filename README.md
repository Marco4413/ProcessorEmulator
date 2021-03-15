# Processor Emulator

![](./logo.png)

## What's this project all about?

One day I woke up and decided to challenge myself.
So this project came to life, it's a short story but full of headaches.
Don't expect much from this emulator, it's made just for learning purposes.

## How to run:

There are four main ways to run this project:
 1. By downloading the precompiled jar file and running `java -jar PEMU-version.jar -help`.
 2. By compiling it yourself! Download the latest version from the repo and run `gradlew build`,
    the compiled file should be located in `./build/libs/PEMU-version.jar`.
 3. By opening the project using **IntelliJ IDEA** and building the **project's artifact**.
 4. By opening the project using **IntelliJ IDEA** and creating a new **Run Config**:
    - Java Version: `1.8`
    - Class Path: `Processor_Emulator.main`
    - Main Class: `io.github.hds.pemu.Main`

## Examples:

An example of program that the processor can run can be found [@resources/example.pemu](https://github.com/hds536jhmk/ProcessorEmulator/blob/master/src/main/resources/example.pemu)

## Dependencies:

There are actually no dependencies except for `JetBrains Annotations` that's used at compile time.
