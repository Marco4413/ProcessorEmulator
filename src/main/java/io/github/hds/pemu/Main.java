package io.github.hds.pemu;

import io.github.hds.pemu.instructions.InstructionSet;
import io.github.hds.pemu.memory.Memory;

public class Main {

    public static void main(String[] args) {

        Processor proc = new Processor(32);

        Memory mem = proc.MEMORY;
        InstructionSet is = proc.INSTRUCTIONSET;

        int SWP  = is.getKeyCode("SWP");
        int HLT  = is.getKeyCode("HLT");
        mem.setValuesAt(0, new int[] {
                10, 20, SWP, 0, 1, HLT
        });

        System.out.println("Initial Memory:");
        System.out.println(proc.MEMORY.toString(16));

        proc.run(2);

        System.out.println("Final Memory:");
        System.out.println(proc.MEMORY.toString(16));

    }

}

