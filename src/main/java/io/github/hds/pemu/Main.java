package io.github.hds.pemu;

public class Main {

    public static void main(String[] args) {
        Processor proc = new Processor(64);

        proc.PROGRAM.setValuesAt(
                0, Compiler.compileFile("program.txt", proc)
        );

        proc.run();
    }

}

