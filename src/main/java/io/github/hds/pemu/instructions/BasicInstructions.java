package io.github.hds.pemu.instructions;

import io.github.hds.pemu.processor.Processor;
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

    public static final Instruction OUTI = new Instruction("OUTI", 1) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            System.out.print(p.DATA.getValueAt(args[0]));
            return false;
        }
    };

    public static final Instruction OUTC = new Instruction("OUTC", 1) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            System.out.print((char) p.DATA.getValueAt(args[0]));
            return false;
        }
    };

    public static final Instruction ADD = new Instruction("ADD", 2) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            int sum = p.DATA.getValueAt(args[0]) + p.DATA.getValueAt(args[1]);

            p.CARRY.value = sum >= p.DATA.MAX_VALUE;
            p.ZERO.value  = (byte) sum == 0;

            p.DATA.setValueAt(args[0], sum);
            return false;
        }
    };

    public static final Instruction SUB = new Instruction("SUB", 2) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            int sub = p.DATA.getValueAt(args[0]) + ~((byte) p.DATA.getValueAt(args[1])) + 1;

            p.CARRY.value = sub >= p.DATA.MAX_VALUE;
            p.ZERO.value  = (byte) sub == 0;

            p.DATA.setValueAt(args[0], sub);
            return false;
        }
    };

    public static final Instruction MUL = new Instruction("MUL", 2) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            int mult = p.DATA.getValueAt(args[0]) * p.DATA.getValueAt(args[1]);

            p.CARRY.value = mult >= p.DATA.MAX_VALUE;
            p.ZERO.value  = (byte) mult == 0;

            p.DATA.setValueAt(args[0], mult);
            return false;
        }
    };

    public static final Instruction DIV = new Instruction("DIV", 2) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            int div = p.DATA.getValueAt(args[0]) / p.DATA.getValueAt(args[1]);

            p.CARRY.value = div >= p.DATA.MAX_VALUE;
            p.ZERO.value  = (byte) div == 0;

            p.DATA.setValueAt(args[0], div);
            return false;
        }
    };

    public static final Instruction CMP = new Instruction("CMP", 2) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            p.ZERO.value  = p.DATA.getValueAt(args[0]) == p.DATA.getValueAt(args[1]);
            p.CARRY.value = p.DATA.getValueAt(args[0]) <  p.DATA.getValueAt(args[1]);
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

    public static final Instruction JC = new Instruction("JC", 1) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            if (p.CARRY.value) return JMP.execute(p, args);
            return false;
        }
    };

    public static final Instruction JNC = new Instruction("JNC", 1) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            if (!p.CARRY.value) return JMP.execute(p, args);
            return false;
        }
    };

    public static final Instruction JZ = new Instruction("JZ", 1) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            if (p.ZERO.value) return JMP.execute(p, args);
            return false;
        }
    };

    public static final Instruction JNZ = new Instruction("JNZ", 1) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            if (!p.ZERO.value) return JMP.execute(p, args);
            return false;
        }
    };

    public static final Instruction CALL = new Instruction("CALL", 1) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            p.DATA.setValueAt(p.SP.value--, p.IP.value + WORDS);
            return JMP.execute(p, args);
        }
    };

    public static final Instruction RET = new Instruction("RET", 0) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            return JMP.execute(p, new int[] { p.DATA.getValueAt(++p.SP.value) });
        }
    };

    public static final Instruction PUSH = new Instruction("PUSH", 1) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            p.DATA.setValueAt(p.SP.value--, p.DATA.getValueAt(args[0]));
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
            new Instruction[] { NULL, MOV, SWP, DATA, OUTP, OUTD, OUTI, OUTC, ADD, SUB, MUL, DIV, CMP, JMP, JC, JNC, JZ, JNZ, CALL, RET, PUSH, POP, HLT }
    );

}
