package io.github.hds.pemu;

import io.github.hds.pemu.compiler.Compiler;

public class Main {

    public static void main(String[] args) {
        Processor proc = new Processor(64);

        proc.PROGRAM.setValuesAt(
                0, Compiler.compileFile("program.txt", proc)
        );

        proc.run();
    }

}

