package io.github.hds.pemu;

import io.github.hds.pemu.instructions.InstructionSet;
import io.github.hds.pemu.memory.Memory;

public class Main {

    public static void main(String[] args) {
        Processor proc = new Processor(64);

        Memory program = proc.PROGRAM;
        Memory data = proc.DATA;
        InstructionSet is = proc.INSTRUCTIONSET;

        int SWP = is.getKeyCode("SWP");
        int HLT = is.getKeyCode("HLT");
        program.setValuesAt(0, new int[] {
                SWP, 0, 1, HLT
        });

        data.setValuesAt(0, new int[] {
                10, 20
        });

        System.out.println("Initial Memory:");
        System.out.println(data.toString(8));

        proc.run();

        System.out.println("Final Memory:");
        System.out.println(data.toString(8));
    }

}

