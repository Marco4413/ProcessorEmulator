package io.github.hds.pemu;

import io.github.hds.pemu.instructions.InstructionSet;
import io.github.hds.pemu.memory.Memory;

public class Main {

    public static void main(String[] args) {

        Processor proc = new Processor();

        Memory mem = proc.MEMORY;
        InstructionSet is = proc.INSTRUCTIONSET;

        int SWP = is.getKeyCode("SWP");
        int JMP = is.getKeyCode("JMP");
        int HLT = is.getKeyCode("HLT");
        mem.setValuesAt(0, new int[] {
                JMP, 4, 10, 20, SWP, 2, 3, HLT
        });

        System.out.println("Initial Memory:");
        System.out.println(proc.MEMORY.toString(16));

        proc.run();

        System.out.println("Final Memory:");
        System.out.println(proc.MEMORY.toString(16));
    }

}

