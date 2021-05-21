package io.github.hds.pemu.instructions;

import io.github.hds.pemu.app.Console;
import io.github.hds.pemu.memory.*;
import io.github.hds.pemu.processor.IProcessor;
import org.jetbrains.annotations.NotNull;

public class Instructions {

    private static void updateMathFlags(@NotNull IProcessor p, int value, boolean zero, boolean carry) {
        IFlag ZF = p.getFlag("ZF");
        IFlag CF = p.getFlag("CF");

        if (ZF != null && zero) ZF.setValue(value == 0);
        if (CF != null && carry) CF.setValue((value & ~p.getMemory().WORD.MASK) != 0);
    }

    public static final Instruction NULL = new Instruction("NULL", 0);

    public static final Instruction BRK = new Instruction("BRK", 0) {
        @Override
        public boolean execute(@NotNull IProcessor p, int[] args) {
            if (!p.isPaused()) {
                p.pause();
                Console.Debug.println("Processor encountered a breakpoint.");
            }
            return false;
        }
    };

    public static final Instruction DATA = new Instruction("DATA", 2) {
        @Override
        public boolean execute(@NotNull IProcessor p, int[] args) {
            p.getMemory().setValueAt(args[0], args[1]);
            return false;
        }
    };

    public static final Instruction MOV = new Instruction("MOV", 2) {
        @Override
        public boolean execute(@NotNull IProcessor p, int[] args) {
            Memory memory = p.getMemory();
            memory.setValueAt(args[0], memory.getValueAt(args[1]));
            return false;
        }
    };

    public static final Instruction SWP = new Instruction("SWP", 2) {
        @Override
        public boolean execute(@NotNull IProcessor p, int[] args) {
            Memory memory = p.getMemory();
            memory.setValueAt(args[0], memory.setValueAt(args[1], memory.getValueAt(args[0])));
            return false;
        }
    };

    public static final Instruction OUTI = new Instruction("OUTI", 1) {
        @Override
        public boolean execute(@NotNull IProcessor p, int[] args) {
            Console.POutput.print(p.getMemory().getValueAt(args[0]));
            return false;
        }
    };

    public static final Instruction OUTC = new Instruction("OUTC", 1) {
        @Override
        public boolean execute(@NotNull IProcessor p, int[] args) {
            Memory memory = p.getMemory();
            char character = (char) memory.getValueAt(args[0]);
            if (character == '\0')
                Console.POutput.clear();
            else
                Console.POutput.print((char) memory.getValueAt(args[0]));
            return false;
        }
    };

    public static final Instruction GETI = new Instruction("GETI", 1) {
        @Override
        public boolean execute(@NotNull IProcessor p, int[] args) {
            p.getMemory().setValueAt(args[0], Math.max(Character.getNumericValue(p.getKeyPressed()), 0));
            return false;
        }
    };

    public static final Instruction GETC = new Instruction("GETC", 1) {
        @Override
        public boolean execute(@NotNull IProcessor p, int[] args) {
            p.getMemory().setValueAt(args[0], p.getCharPressed());
            return false;
        }
    };

    public static final Instruction GETK = new Instruction("GETK", 1) {
        @Override
        public boolean execute(@NotNull IProcessor p, int[] args) {
            p.getMemory().setValueAt(args[0], p.getKeyPressed());
            return false;
        }
    };

    public static final Instruction TS = new Instruction("TS", 1) {
        @Override
        public boolean execute(@NotNull IProcessor p, int[] args) {
            long timeElapsed = p.getTimeRunning();
            p.getMemory().setValueAt(args[0], (int) (timeElapsed / 1000));
            return false;
        }
    };

    public static final Instruction TMS = new Instruction("TMS", 1) {
        @Override
        public boolean execute(@NotNull IProcessor p, int[] args) {
            long timeElapsed = p.getTimeRunning();
            p.getMemory().setValueAt(args[0], (int) timeElapsed);
            return false;
        }
    };

    public static final Instruction INC = new Instruction("INC", 1) {
        @Override
        public boolean execute(@NotNull IProcessor p, int[] args) {
            Memory memory = p.getMemory();
            int result = memory.getValueAt(args[0]) + 1;
            memory.setValueAt(args[0], result);
            updateMathFlags(p, result, true, true);
            return false;
        }
    };

    public static final Instruction DEC = new Instruction("DEC", 1) {
        @Override
        public boolean execute(@NotNull IProcessor p, int[] args) {
            Memory memory = p.getMemory();
            int result = memory.getValueAt(args[0]) - 1;
            memory.setValueAt(args[0], result);
            updateMathFlags(p, result, true, true);
            return false;
        }
    };

    public static final Instruction ADD = new Instruction("ADD", 2) {
        @Override
        public boolean execute(@NotNull IProcessor p, int[] args) {
            Memory memory = p.getMemory();
            int result = memory.getValueAt(args[0]) + memory.getValueAt(args[1]);
            memory.setValueAt(args[0], result);
            updateMathFlags(p, result, true, true);
            return false;
        }
    };

    public static final Instruction SUB = new Instruction("SUB", 2) {
        @Override
        public boolean execute(@NotNull IProcessor p, int[] args) {
            Memory memory = p.getMemory();
            int result = memory.getValueAt(args[0]) - memory.getValueAt(args[1]);
            memory.setValueAt(args[0], result);
            updateMathFlags(p, result, true, true);
            return false;
        }
    };

    public static final Instruction MUL = new Instruction("MUL", 2) {
        @Override
        public boolean execute(@NotNull IProcessor p, int[] args) {
            Memory memory = p.getMemory();
            int result = memory.getValueAt(args[0]) * memory.getValueAt(args[1]);
            memory.setValueAt(args[0], result);
            updateMathFlags(p, result, true, true);
            return false;
        }
    };

    public static final Instruction DIV = new Instruction("DIV", 2) {
        @Override
        public boolean execute(@NotNull IProcessor p, int[] args) {
            Memory memory = p.getMemory();
            int result = memory.getValueAt(args[0]) / memory.getValueAt(args[1]);
            memory.setValueAt(args[0], result);
            updateMathFlags(p, result, true, true);
            return false;
        }
    };

    public static final Instruction MOD = new Instruction("MOD", 2) {
        @Override
        public boolean execute(@NotNull IProcessor p, int[] args) {
            Memory memory = p.getMemory();
            int result = memory.getValueAt(args[0]) % memory.getValueAt(args[1]);
            memory.setValueAt(args[0], result);
            updateMathFlags(p, result, true, true);
            return false;
        }
    };

    public static final Instruction AND = new Instruction("AND", 2) {
        @Override
        public boolean execute(@NotNull IProcessor p, int[] args) {
            Memory memory = p.getMemory();
            int res = memory.getValueAt(args[0]) & memory.getValueAt(args[1]);
            memory.setValueAt(args[0], res);
            updateMathFlags(p, res, true, false);
            return false;
        }
    };

    public static final Instruction OR = new Instruction("OR", 2) {
        @Override
        public boolean execute(@NotNull IProcessor p, int[] args) {
            Memory memory = p.getMemory();
            int res = memory.getValueAt(args[0]) | memory.getValueAt(args[1]);
            memory.setValueAt(args[0], res);
            updateMathFlags(p, res, true, false);
            return false;
        }
    };

    public static final Instruction NOT = new Instruction("NOT", 1) {
        @Override
        public boolean execute(@NotNull IProcessor p, int[] args) {
            Memory memory = p.getMemory();
            int res = ~memory.getValueAt(args[0]);
            memory.setValueAt(args[0], res);
            updateMathFlags(p, res, true, false);
            return false;
        }
    };

    public static final Instruction XOR = new Instruction("XOR", 2) {
        @Override
        public boolean execute(@NotNull IProcessor p, int[] args) {
            Memory memory = p.getMemory();
            int res = memory.getValueAt(args[0]) ^ memory.getValueAt(args[1]);
            memory.setValueAt(args[0], res);
            updateMathFlags(p, res, true, false);
            return false;
        }
    };

    public static final Instruction CMP = new Instruction("CMP", 2) {
        @Override
        public boolean execute(@NotNull IProcessor p, int[] args) {
            Memory memory = p.getMemory();
            IFlag ZF = p.getFlag("ZF");
            IFlag CF = p.getFlag("CF");

            if (ZF != null) ZF.setValue(memory.getValueAt(args[0]) == memory.getValueAt(args[1]));
            if (CF != null) CF.setValue(memory.getValueAt(args[0]) <  memory.getValueAt(args[1]));
            return false;
        }
    };

    public static final Instruction JMP = new Instruction("JMP", 1) {
        @Override
        public boolean execute(@NotNull IProcessor p, int[] args) {
            IRegister IP = p.getRegistry("IP");
            if (IP == null) return false;
            else IP.setValue(args[0]);
            return true;
        }
    };

    public static final Instruction JC = new Instruction("JC", 1) {
        @Override
        public boolean execute(@NotNull IProcessor p, int[] args) {
            IFlag CF = p.getFlag("CF");
            if (CF != null && CF.getValue()) return JMP.execute(p, args);
            return false;
        }
    };

    public static final Instruction JNC = new Instruction("JNC", 1) {
        @Override
        public boolean execute(@NotNull IProcessor p, int[] args) {
            IFlag CF = p.getFlag("CF");
            if (CF != null && !CF.getValue()) return JMP.execute(p, args);
            return false;
        }
    };

    public static final Instruction JZ = new Instruction("JZ", 1) {
        @Override
        public boolean execute(@NotNull IProcessor p, int[] args) {
            IFlag ZF = p.getFlag("ZF");
            if (ZF != null && ZF.getValue()) return JMP.execute(p, args);
            return false;
        }
    };

    public static final Instruction JNZ = new Instruction("JNZ", 1) {
        @Override
        public boolean execute(@NotNull IProcessor p, int[] args) {
            IFlag ZF = p.getFlag("ZF");
            if (ZF != null && !ZF.getValue()) return JMP.execute(p, args);
            return false;
        }
    };

    public static final Instruction JE = new Instruction("JE", 1) {
        @Override
        public boolean execute(@NotNull IProcessor p, int[] args) {
            return JZ.execute(p, args);
        }
    };

    public static final Instruction JNE = new Instruction("JNE", 1) {
        @Override
        public boolean execute(@NotNull IProcessor p, int[] args) {
            return JNZ.execute(p, args);
        }
    };

    public static final Instruction JB = new Instruction("JB", 1) {
        @Override
        public boolean execute(@NotNull IProcessor p, int[] args) {
            return JC.execute(p, args);
        }
    };

    public static final Instruction JNB = new Instruction("JNB", 1) {
        @Override
        public boolean execute(@NotNull IProcessor p, int[] args) {
            return JNC.execute(p, args);
        }
    };

    public static final Instruction JBE = new Instruction("JBE", 1) {
        @Override
        public boolean execute(@NotNull IProcessor p, int[] args) {
            IFlag ZF = p.getFlag("ZF");
            IFlag CF = p.getFlag("CF");
            if ((ZF != null && ZF.getValue()) || (CF != null && CF.getValue())) return JMP.execute(p, args);
            return false;
        }
    };

    public static final Instruction JNBE = new Instruction("JNBE", 1) {
        @Override
        public boolean execute(@NotNull IProcessor p, int[] args) {
            IFlag ZF = p.getFlag("ZF");
            IFlag CF = p.getFlag("CF");
            if (!(ZF != null && ZF.getValue()) || (CF != null && CF.getValue())) return JMP.execute(p, args);
            return false;
        }
    };

    public static final Instruction JA = new Instruction("JA", 1) {
        @Override
        public boolean execute(@NotNull IProcessor p, int[] args) {
            return JNBE.execute(p, args);
        }
    };

    public static final Instruction JNA = new Instruction("JNA", 1) {
        @Override
        public boolean execute(@NotNull IProcessor p, int[] args) {
            return JBE.execute(p, args);
        }
    };

    public static final Instruction JAE = new Instruction("JAE", 1) {
        @Override
        public boolean execute(@NotNull IProcessor p, int[] args) {
            return JNC.execute(p, args);
        }
    };

    public static final Instruction JNAE = new Instruction("JNAE", 1) {
        @Override
        public boolean execute(@NotNull IProcessor p, int[] args) {
            return JC.execute(p, args);
        }
    };

    public static final Instruction CALL = new Instruction("CALL", 1) {
        @Override
        public boolean execute(@NotNull IProcessor p, int[] args) {
            IRegister SP = p.getRegistry("SP");
            IRegister IP = p.getRegistry("IP");
            if (SP == null || IP == null) return false;

            p.getMemory().setValueAt(
                    SP.setValue(SP.getValue() - 1), IP.getValue()
            );
            return JMP.execute(p, args);
        }
    };

    public static final Instruction RET = new Instruction("RET", 0) {
        @Override
        public boolean execute(@NotNull IProcessor p, int[] args) {
            IRegister SP = p.getRegistry("SP");
            if (SP == null) return false;

            SP.setValue(SP.getValue() + 1);
            return JMP.execute(p, new int[] { p.getMemory().getValueAt(SP.getValue()) });
        }
    };

    public static final Instruction PUSH = new Instruction("PUSH", 1) {
        @Override
        public boolean execute(@NotNull IProcessor p, int[] args) {
            IRegister SP = p.getRegistry("SP");
            if (SP == null) return false;

            Memory memory = p.getMemory();
            memory.setValueAt(SP.setValue(SP.getValue() - 1), memory.getValueAt(args[0]));
            return false;
        }
    };

    public static final Instruction POP = new Instruction("POP", 1) {
        @Override
        public boolean execute(@NotNull IProcessor p, int[] args) {
            IRegister SP = p.getRegistry("SP");
            if (SP == null) return false;

            Memory memory = p.getMemory();
            SP.setValue(SP.getValue() + 1);
            memory.setValueAt(args[0], memory.getValueAt(SP.getValue()));
            return false;
        }
    };

    public static final Instruction LOOP = new Instruction("LOOP", 2) {
        @Override
        public boolean execute(@NotNull IProcessor p, int[] args) {
            Memory memory = p.getMemory();
            int counter = memory.getValueAt(args[1]);
            memory.setValueAt(args[1], --counter);

            if (counter != 0) {
                return JMP.execute(p, new int[] { args[0] });
            }
            return false;
        }
    };

    public static final Instruction HLT = new Instruction("HLT", 0) {
        @Override
        public boolean execute(@NotNull IProcessor p, int[] args) {
            p.stop();
            return false;
        }
    };

    public static final InstructionSet SET = new InstructionSet(
            new Instruction[] {
                    NULL, BRK , DATA, MOV , SWP, OUTI, OUTC, GETI,
                    GETC, GETK, TS  , TMS , INC, DEC , ADD , SUB ,
                    MUL , DIV , MOD , AND , OR , NOT , XOR , CMP ,
                    JMP , JC  , JNC , JZ  , JNZ, JE  , JNE , JB  ,
                    JNB , JBE , JNBE, JA  , JNA, JAE , JNAE, CALL,
                    RET , PUSH, POP , LOOP, HLT
            }
    );

}
