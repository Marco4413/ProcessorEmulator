package io.github.hds.pemu.instructions;

import io.github.hds.pemu.app.Console;
import io.github.hds.pemu.memory.*;
import io.github.hds.pemu.memory.flags.IFlag;
import io.github.hds.pemu.memory.registers.IRegister;
import io.github.hds.pemu.processor.IProcessor;
import io.github.hds.pemu.utils.MathUtils;
import org.jetbrains.annotations.NotNull;

public final class Instructions {

    private static void updateMathFlags(@NotNull IProcessor p, int value, boolean zero, boolean carry) {
        IFlag ZF = p.getFlag("ZF");
        IFlag CF = p.getFlag("CF");
        if (ZF == null) throw new NullPointerException("Zero Flag isn't present on the Processor.");
        if (CF == null) throw new NullPointerException("Carry Flag isn't present on the Processor.");

        if (zero) ZF.setValue(value == 0);
        if (carry) CF.setValue((value & ~p.getMemory().getWord().BIT_MASK) != 0);
    }

    public static final Instruction NULL = new Instruction("NULL", 0);

    public static final Instruction BRK = new Instruction("BRK", 0) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            if (!p.isPaused()) {
                p.pause();
                Console.Debug.println("Processor encountered a breakpoint.");
            }
        }
    };

    public static final Instruction DATA = new Instruction("DATA", 2) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            p.getMemory().setValueAt(args[0], args[1]);
        }
    };

    public static final Instruction MOV = new Instruction("MOV", 2) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            IMemory memory = p.getMemory();
            memory.setValueAt(args[0], memory.getValueAt(args[1]));
        }
    };

    public static final Instruction SWP = new Instruction("SWP", 2) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            IMemory memory = p.getMemory();
            memory.setValueAt(args[0], memory.setValueAt(args[1], memory.getValueAt(args[0])));
        }
    };

    public static final Instruction XMOV = new Instruction("XMOV", 3) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            IMemory memory = p.getMemory();

            boolean[] options = MathUtils.getBits(args[2], new boolean[3]);
            int firstAddress  = options[0] ? memory.getValueAt(args[0]) : args[0];
            int secondAddress = options[1] ? memory.getValueAt(args[1]) : args[1];
            boolean doSwap = options[2];

            if (doSwap)
                memory.setValueAt(firstAddress, memory.setValueAt(secondAddress, memory.getValueAt(firstAddress)));
            else memory.setValueAt(firstAddress, memory.getValueAt(secondAddress));
        }
    };

    public static final Instruction OUTI = new Instruction("OUTI", 1) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            Console.POutput.print(p.getMemory().getValueAt(args[0]));
        }
    };

    public static final Instruction OUTC = new Instruction("OUTC", 1) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            IMemory memory = p.getMemory();
            char character = (char) memory.getValueAt(args[0]);
            if (character == '\0') Console.POutput.clear();
            else Console.POutput.print((char) memory.getValueAt(args[0]));
        }
    };

    public static final Instruction GETI = new Instruction("GETI", 1) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            p.getMemory().setValueAt(args[0], Math.max(Character.getNumericValue(p.getKeyPressed()), 0));
        }
    };

    public static final Instruction GETC = new Instruction("GETC", 1) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            p.getMemory().setValueAt(args[0], p.getCharPressed());
        }
    };

    public static final Instruction GETK = new Instruction("GETK", 1) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            p.getMemory().setValueAt(args[0], p.getKeyPressed());
        }
    };

    public static final Instruction TS = new Instruction("TS", 1) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            long timeElapsed = p.getTimeRunning();
            p.getMemory().setValueAt(args[0], (int) (timeElapsed / 1000));
        }
    };

    public static final Instruction TMS = new Instruction("TMS", 1) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            long timeElapsed = p.getTimeRunning();
            p.getMemory().setValueAt(args[0], (int) timeElapsed);
        }
    };

    public static final Instruction INC = new Instruction("INC", 1) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            IMemory memory = p.getMemory();
            int result = memory.getValueAt(args[0]) + 1;
            memory.setValueAt(args[0], result);
            updateMathFlags(p, result, true, true);
        }
    };

    public static final Instruction DEC = new Instruction("DEC", 1) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            IMemory memory = p.getMemory();
            int result = memory.getValueAt(args[0]) - 1;
            memory.setValueAt(args[0], result);
            updateMathFlags(p, result, true, true);
        }
    };

    public static final Instruction ADD = new Instruction("ADD", 2) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            IMemory memory = p.getMemory();
            int result = memory.getValueAt(args[0]) + memory.getValueAt(args[1]);
            memory.setValueAt(args[0], result);
            updateMathFlags(p, result, true, true);
        }
    };

    public static final Instruction SUB = new Instruction("SUB", 2) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            IMemory memory = p.getMemory();
            int result = memory.getValueAt(args[0]) - memory.getValueAt(args[1]);
            memory.setValueAt(args[0], result);
            updateMathFlags(p, result, true, true);
        }
    };

    public static final Instruction MUL = new Instruction("MUL", 2) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            IMemory memory = p.getMemory();
            int result = memory.getValueAt(args[0]) * memory.getValueAt(args[1]);
            memory.setValueAt(args[0], result);
            updateMathFlags(p, result, true, true);
        }
    };

    public static final Instruction DIV = new Instruction("DIV", 2) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            IMemory memory = p.getMemory();
            int result = memory.getValueAt(args[0]) / memory.getValueAt(args[1]);
            memory.setValueAt(args[0], result);
            updateMathFlags(p, result, true, true);
        }
    };

    public static final Instruction MOD = new Instruction("MOD", 2) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            IMemory memory = p.getMemory();
            int result = memory.getValueAt(args[0]) % memory.getValueAt(args[1]);
            memory.setValueAt(args[0], result);
            updateMathFlags(p, result, true, true);
        }
    };

    public static final Instruction AND = new Instruction("AND", 2) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            IMemory memory = p.getMemory();
            int res = memory.getValueAt(args[0]) & memory.getValueAt(args[1]);
            memory.setValueAt(args[0], res);
            updateMathFlags(p, res, true, false);
        }
    };

    public static final Instruction OR = new Instruction("OR", 2) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            IMemory memory = p.getMemory();
            int res = memory.getValueAt(args[0]) | memory.getValueAt(args[1]);
            memory.setValueAt(args[0], res);
            updateMathFlags(p, res, true, false);
        }
    };

    public static final Instruction NOT = new Instruction("NOT", 1) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            IMemory memory = p.getMemory();
            int res = ~memory.getValueAt(args[0]);
            memory.setValueAt(args[0], res);
            updateMathFlags(p, res, true, false);
        }
    };

    public static final Instruction XOR = new Instruction("XOR", 2) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            IMemory memory = p.getMemory();
            int res = memory.getValueAt(args[0]) ^ memory.getValueAt(args[1]);
            memory.setValueAt(args[0], res);
            updateMathFlags(p, res, true, false);
        }
    };

    public static final Instruction CMP = new Instruction("CMP", 2) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            IMemory memory = p.getMemory();
            IFlag ZF = p.getFlag("ZF");
            IFlag CF = p.getFlag("CF");
            if (ZF == null) throw new NullPointerException("Zero Flag isn't present on the Processor.");
            if (CF == null) throw new NullPointerException("Carry Flag isn't present on the Processor.");

            ZF.setValue(memory.getValueAt(args[0]) == memory.getValueAt(args[1]));
            CF.setValue(memory.getValueAt(args[0]) <  memory.getValueAt(args[1]));
        }
    };

    public static final Instruction JMP = new Instruction("JMP", 1) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            IRegister IP = p.getRegister("IP");
            if (IP == null) throw new NullPointerException("Instruction Pointer Register isn't present on the Processor.");
            IP.setValue(args[0]);
        }
    };

    public static final Instruction JC = new Instruction("JC", 1) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            IFlag CF = p.getFlag("CF");
            if (CF == null) throw new NullPointerException("Carry Flag isn't present on the Processor.");
            if (CF.getValue()) JMP.execute(p, args);
        }
    };

    public static final Instruction JNC = new Instruction("JNC", 1) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            IFlag CF = p.getFlag("CF");
            if (CF == null) throw new NullPointerException("Carry Flag isn't present on the Processor.");
            if (!CF.getValue()) JMP.execute(p, args);
        }
    };

    public static final Instruction JZ = new Instruction("JZ", 1) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            IFlag ZF = p.getFlag("ZF");
            if (ZF == null) throw new NullPointerException("Zero Flag isn't present on the Processor.");
            if (ZF.getValue()) JMP.execute(p, args);
        }
    };

    public static final Instruction JNZ = new Instruction("JNZ", 1) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            IFlag ZF = p.getFlag("ZF");
            if (ZF == null) throw new NullPointerException("Zero Flag isn't present on the Processor.");
            if (!ZF.getValue()) JMP.execute(p, args);
        }
    };

    public static final Instruction JE = new Instruction("JE", 1) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            JZ.execute(p, args);
        }
    };

    public static final Instruction JNE = new Instruction("JNE", 1) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            JNZ.execute(p, args);
        }
    };

    public static final Instruction JB = new Instruction("JB", 1) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            JC.execute(p, args);
        }
    };

    public static final Instruction JNB = new Instruction("JNB", 1) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            JNC.execute(p, args);
        }
    };

    public static final Instruction JBE = new Instruction("JBE", 1) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            IFlag ZF = p.getFlag("ZF");
            IFlag CF = p.getFlag("CF");
            if (ZF == null) throw new NullPointerException("Zero Flag isn't present on the Processor.");
            if (CF == null) throw new NullPointerException("Carry Flag isn't present on the Processor.");
            if (ZF.getValue() || CF.getValue()) JMP.execute(p, args);
        }
    };

    public static final Instruction JNBE = new Instruction("JNBE", 1) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            IFlag ZF = p.getFlag("ZF");
            IFlag CF = p.getFlag("CF");
            if (ZF == null) throw new NullPointerException("Zero Flag isn't present on the Processor.");
            if (CF == null) throw new NullPointerException("Carry Flag isn't present on the Processor.");
            if (!ZF.getValue() || CF.getValue()) JMP.execute(p, args);
        }
    };

    public static final Instruction JA = new Instruction("JA", 1) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            JNBE.execute(p, args);
        }
    };

    public static final Instruction JNA = new Instruction("JNA", 1) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            JBE.execute(p, args);
        }
    };

    public static final Instruction JAE = new Instruction("JAE", 1) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            JNC.execute(p, args);
        }
    };

    public static final Instruction JNAE = new Instruction("JNAE", 1) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            JC.execute(p, args);
        }
    };

    public static final Instruction CALL = new Instruction("CALL", 1) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            IRegister SP = p.getRegister("SP");
            IRegister IP = p.getRegister("IP");
            if (SP == null) throw new NullPointerException("Stack Pointer Register isn't present on the Processor.");
            if (IP == null) throw new NullPointerException("Instruction Pointer Register isn't present on the Processor.");

            p.getMemory().setValueAt(
                    SP.setValue(SP.getValue() - 1), IP.getValue()
            );
            JMP.execute(p, args);
        }
    };

    public static final Instruction RET = new Instruction("RET", 0) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            IRegister SP = p.getRegister("SP");
            if (SP == null) throw new NullPointerException("Stack Pointer Register isn't present on the Processor.");

            SP.setValue(SP.getValue() + 1);
            JMP.execute(p, new int[] { p.getMemory().getValueAt(SP.getValue()) });
        }
    };

    public static final Instruction PUSH = new Instruction("PUSH", 1) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            IRegister SP = p.getRegister("SP");
            if (SP == null) throw new NullPointerException("Stack Pointer Register isn't present on the Processor.");

            IMemory memory = p.getMemory();
            memory.setValueAt(SP.setValue(SP.getValue() - 1), memory.getValueAt(args[0]));
        }
    };

    public static final Instruction POP = new Instruction("POP", 1) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            IRegister SP = p.getRegister("SP");
            if (SP == null) throw new NullPointerException("Stack Pointer Register isn't present on the Processor.");

            IMemory memory = p.getMemory();
            SP.setValue(SP.getValue() + 1);
            memory.setValueAt(args[0], memory.getValueAt(SP.getValue()));
        }
    };

    public static final Instruction LOOP = new Instruction("LOOP", 2) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            IMemory memory = p.getMemory();
            int counter = memory.getValueAt(args[1]);
            memory.setValueAt(args[1], --counter);

            if (counter != 0) JMP.execute(p, new int[] { args[0] });
        }
    };

    public static final Instruction HLT = new Instruction("HLT", 0) {
        @Override
        public void execute(@NotNull IProcessor p, int[] args) {
            p.stop();
        }
    };

    public static final InstructionSet SET = new InstructionSet(
            new Instruction[] {
                    NULL, BRK , DATA, MOV , SWP , XMOV, OUTI, OUTC,
                    GETI, GETC, GETK, TS  , TMS , INC , DEC , ADD ,
                    SUB , MUL , DIV , MOD , AND , OR  , NOT , XOR ,
                    CMP , JMP , JC  , JNC , JZ  , JNZ , JE  , JNE ,
                    JB  , JNB , JBE , JNBE, JA  , JNA , JAE , JNAE,
                    CALL, RET , PUSH, POP , LOOP, HLT
            }
    );

}
