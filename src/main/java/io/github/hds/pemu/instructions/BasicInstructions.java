package io.github.hds.pemu.instructions;

import io.github.hds.pemu.Processor;
import org.jetbrains.annotations.NotNull;

public class BasicInstructions {

    public static final Instruction NULL = new Instruction("NULL", 0);

    public static final Instruction MOV = new Instruction("MOV", 2) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            p.MEMORY.setValueAt(args[0], p.MEMORY.getValueAt(args[1]));
            return false;
        }
    };

    public static final Instruction SWP = new Instruction("SWP", 2) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            p.MEMORY.setValueAt(args[0], p.MEMORY.setValueAt(args[1], p.MEMORY.getValueAt(args[0])));
            return false;
        }
    };

    public static final Instruction JMP = new Instruction("JMP", 1) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            p.IP.setValue(args[0]);

            return true;
        }
    };

    public static final Instruction PUSH = new Instruction("PUSH", 1) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            p.MEMORY.setValueAt(p.SP.getValue(), p.MEMORY.getValueAt(args[0]));
            p.SP.setValue(p.SP.getValue() - 1);
            return false;
        }
    };

    public static final Instruction POP = new Instruction("POP", 1) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            int lastStackAddress = p.SP.getValue() + 1;
            p.MEMORY.setValueAt(args[0], p.MEMORY.getValueAt(lastStackAddress));
            p.SP.setValue(lastStackAddress);
            return false;
        }
    };

    public static final Instruction HLT = new Instruction("HLT", 0) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            p.stop();
            return false;
        }
    };

    public static final InstructionSet BASIC_SET = new InstructionSet(
            new Instruction[] { NULL, MOV, SWP, JMP, PUSH, POP, HLT }
    );

}
