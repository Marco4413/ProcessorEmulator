package io.github.hds.pemu.instructions;

import io.github.hds.pemu.Processor;
import org.jetbrains.annotations.NotNull;

public class BasicInstructions {

    public static final Instruction NULL = new Instruction("NULL", 0);

    public static final Instruction MOV = new Instruction("MOV", 2) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            p.DATA.setValueAt(args[0], p.DATA.getValueAt(args[1]));
            return false;
        }
    };

    public static final Instruction SWP = new Instruction("SWP", 2) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            p.DATA.setValueAt(args[0], p.DATA.setValueAt(args[1], p.DATA.getValueAt(args[0])));
            return false;
        }
    };

    public static final Instruction DATA = new Instruction("DATA", 2) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            p.DATA.setValueAt(args[0], args[1]);
            return false;
        }
    };

    public static final Instruction OUTP = new Instruction("OUTP", 1) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            System.out.println(p.PROGRAM.toString(args[0]));
            return false;
        }
    };

    public static final Instruction OUTD = new Instruction("OUTD", 1) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            System.out.println(p.DATA.toString(args[0]));
            return false;
        }
    };

    public static final Instruction JMP = new Instruction("JMP", 1) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            p.IP.value = args[0];
            return true;
        }
    };

    public static final Instruction PUSH = new Instruction("PUSH", 1) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            p.DATA.setValueAt(p.SP.getValue(), p.DATA.getValueAt(args[0]));
            p.SP.value--;
            return false;
        }
    };

    public static final Instruction POP = new Instruction("POP", 1) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            p.DATA.setValueAt(args[0], p.DATA.getValueAt(++p.SP.value));
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
            new Instruction[] { NULL, MOV, SWP, DATA, OUTP, OUTD, JMP, PUSH, POP, HLT }
    );

}
