package io.github.hds.pemu.instructions;

import io.github.hds.pemu.app.Console;
import io.github.hds.pemu.processor.Processor;
import org.jetbrains.annotations.NotNull;

public class Instructions {

    public static final Instruction NULL = new Instruction("NULL", 0);

    public static final Instruction BRK = new Instruction("BRK", 0) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            if (!p.isPaused()) {
                p.pause();
                Console.Debug.println("Processor encountered a breakpoint.");
            }
            return false;
        }
    };

    public static final Instruction DATA = new Instruction("DATA", 2) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            p.MEMORY.setValueAt(args[0], args[1]);
            return false;
        }
    };

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

    public static final Instruction OUTI = new Instruction("OUTI", 1) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            Console.POutput.print(p.MEMORY.getValueAt(args[0]));
            return false;
        }
    };

    public static final Instruction OUTC = new Instruction("OUTC", 1) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            char character = (char) p.MEMORY.getValueAt(args[0]);
            if (character == '\0')
                Console.POutput.clear();
            else
                Console.POutput.print((char) p.MEMORY.getValueAt(args[0]));
            return false;
        }
    };

    public static final Instruction GETI = new Instruction("GETI", 1) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            p.MEMORY.setValueAt(args[0], Math.max(Character.getNumericValue(p.pressedKey), 0));
            return false;
        }
    };

    public static final Instruction GETC = new Instruction("GETC", 1) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            p.MEMORY.setValueAt(args[0], p.pressedChar);
            return false;
        }
    };

    public static final Instruction GETK = new Instruction("GETK", 1) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            p.MEMORY.setValueAt(args[0], p.pressedKey);
            return false;
        }
    };

    public static final Instruction TS = new Instruction("TS", 1) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            long timeElapsed = p.getTimeRunning();
            p.MEMORY.setValueAt(args[0], (int) (timeElapsed / 1000));
            return false;
        }
    };

    public static final Instruction TMS = new Instruction("TMS", 1) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            long timeElapsed = p.getTimeRunning();
            p.MEMORY.setValueAt(args[0], (int) timeElapsed);
            return false;
        }
    };

    public static final Instruction INC = new Instruction("INC", 1) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            int result = p.MEMORY.getValueAt(args[0]) + 1;
            p.MEMORY.setValueAt(args[0], result);
            p.updateFlags(result, true, true);
            return false;
        }
    };

    public static final Instruction DEC = new Instruction("DEC", 1) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            int result = p.MEMORY.getValueAt(args[0]) - 1;
            p.MEMORY.setValueAt(args[0], result);
            p.updateFlags(result, true, true);
            return false;
        }
    };

    public static final Instruction ADD = new Instruction("ADD", 2) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            int result = p.MEMORY.getValueAt(args[0]) + p.MEMORY.getValueAt(args[1]);
            p.MEMORY.setValueAt(args[0], result);
            p.updateFlags(result, true, true);
            return false;
        }
    };

    public static final Instruction SUB = new Instruction("SUB", 2) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            int result = p.MEMORY.getValueAt(args[0]) - p.MEMORY.getValueAt(args[1]);
            p.MEMORY.setValueAt(args[0], result);
            p.updateFlags(result, true, true);
            return false;
        }
    };

    public static final Instruction MUL = new Instruction("MUL", 2) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            int result = p.MEMORY.getValueAt(args[0]) * p.MEMORY.getValueAt(args[1]);
            p.MEMORY.setValueAt(args[0], result);
            p.updateFlags(result, true, true);
            return false;
        }
    };

    public static final Instruction DIV = new Instruction("DIV", 2) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            int result = p.MEMORY.getValueAt(args[0]) / p.MEMORY.getValueAt(args[1]);
            p.MEMORY.setValueAt(args[0], result);
            p.updateFlags(result, true, true);
            return false;
        }
    };

    public static final Instruction MOD = new Instruction("MOD", 2) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            int result = p.MEMORY.getValueAt(args[0]) % p.MEMORY.getValueAt(args[1]);
            p.MEMORY.setValueAt(args[0], result);
            p.updateFlags(result, true, true);
            return false;
        }
    };

    public static final Instruction AND = new Instruction("AND", 2) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            int res = p.MEMORY.getValueAt(args[0]) & p.MEMORY.getValueAt(args[1]);
            p.MEMORY.setValueAt(args[0], res);
            p.updateFlags(res, true, false);
            return false;
        }
    };

    public static final Instruction OR = new Instruction("OR", 2) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            int res = p.MEMORY.getValueAt(args[0]) | p.MEMORY.getValueAt(args[1]);
            p.MEMORY.setValueAt(args[0], res);
            p.updateFlags(res, true, false);
            return false;
        }
    };

    public static final Instruction NOT = new Instruction("NOT", 1) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            int res = ~p.MEMORY.getValueAt(args[0]);
            p.MEMORY.setValueAt(args[0], res);
            p.updateFlags(res, true, false);
            return false;
        }
    };

    public static final Instruction XOR = new Instruction("XOR", 2) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            int res = p.MEMORY.getValueAt(args[0]) ^ p.MEMORY.getValueAt(args[1]);
            p.MEMORY.setValueAt(args[0], res);
            p.updateFlags(res, true, false);
            return false;
        }
    };

    public static final Instruction CMP = new Instruction("CMP", 2) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            p.ZERO.value  = p.MEMORY.getValueAt(args[0]) == p.MEMORY.getValueAt(args[1]);
            p.CARRY.value = p.MEMORY.getValueAt(args[0]) <  p.MEMORY.getValueAt(args[1]);
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

    public static final Instruction JE = new Instruction("JE", 1) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            return JZ.execute(p, args);
        }
    };

    public static final Instruction JNE = new Instruction("JNE", 1) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            return JNZ.execute(p, args);
        }
    };

    public static final Instruction JB = new Instruction("JB", 1) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            return JC.execute(p, args);
        }
    };

    public static final Instruction JNB = new Instruction("JNB", 1) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            return JNC.execute(p, args);
        }
    };

    public static final Instruction JBE = new Instruction("JBE", 1) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            if (p.ZERO.value || p.CARRY.value) return JMP.execute(p, args);
            return false;
        }
    };

    public static final Instruction JNBE = new Instruction("JNBE", 1) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            if (!(p.ZERO.value || p.CARRY.value)) return JMP.execute(p, args);
            return false;
        }
    };

    public static final Instruction JA = new Instruction("JA", 1) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            return JNBE.execute(p, args);
        }
    };

    public static final Instruction JNA = new Instruction("JNA", 1) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            return JBE.execute(p, args);
        }
    };

    public static final Instruction JAE = new Instruction("JAE", 1) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            return JNC.execute(p, args);
        }
    };

    public static final Instruction JNAE = new Instruction("JNAE", 1) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            return JC.execute(p, args);
        }
    };

    public static final Instruction CALL = new Instruction("CALL", 1) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            p.MEMORY.setValueAt(p.SP.value--, p.IP.value + getWords());
            return JMP.execute(p, args);
        }
    };

    public static final Instruction RET = new Instruction("RET", 0) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            return JMP.execute(p, new int[] { p.MEMORY.getValueAt(++p.SP.value) });
        }
    };

    public static final Instruction PUSH = new Instruction("PUSH", 1) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            p.MEMORY.setValueAt(p.SP.value--, p.MEMORY.getValueAt(args[0]));
            return false;
        }
    };

    public static final Instruction POP = new Instruction("POP", 1) {
        @Override
        public boolean execute(@NotNull Processor p, int[] args) {
            p.MEMORY.setValueAt(args[0], p.MEMORY.getValueAt(++p.SP.value));
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

    public static final InstructionSet SET = new InstructionSet(
            new Instruction[] {
                    NULL, BRK , DATA, MOV, SWP, OUTI, OUTC, GETI,
                    GETC, GETK, TS  , TMS, INC, DEC , ADD , SUB ,
                    MUL , DIV , MOD , AND, OR , NOT , XOR , CMP ,
                    JMP , JC  , JNC , JZ , JNZ, JE  , JNE , JB  ,
                    JNB , JBE , JNBE, JA , JNA, JAE , JNAE, CALL,
                    RET , PUSH, POP , HLT
            }
    );

}
