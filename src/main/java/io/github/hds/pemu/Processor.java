package io.github.hds.pemu;

import io.github.hds.pemu.instructions.Instruction;
import io.github.hds.pemu.instructions.InstructionSet;
import io.github.hds.pemu.memory.Memory;
import io.github.hds.pemu.memory.Registry;

public class Processor {

    private boolean isRunning = false;

    public final Registry IP = new Registry("Instruction Pointer");
    public final Registry SP = new Registry("Stack Pointer");

    public final Memory MEMORY;

    public final InstructionSet INSTRUCTIONSET = new InstructionSet(
            new Instruction[] {
                    new Instruction("NULL", 0),
                    new Instruction("MOV", 2) {
                        @Override
                        public void execute(Processor p, int[] args) {
                            p.MEMORY.setValueAt(args[0], p.MEMORY.getValueAt(args[1]));
                        }
                    },
                    new Instruction("SWP", 2) {
                        @Override
                        public void execute(Processor p, int[] args) {
                            p.MEMORY.setValueAt(args[0], p.MEMORY.setValueAt(args[1], p.MEMORY.getValueAt(args[0])));
                        }
                    },
                    new Instruction("PUSH", 1) {
                        @Override
                        public void execute(Processor p, int[] args) {
                            p.MEMORY.setValueAt(p.SP.getValue(), p.MEMORY.getValueAt(args[0]));
                            p.SP.setValue(p.SP.getValue() - 1);
                        }
                    },
                    new Instruction("POP", 1) {
                        @Override
                        public void execute(Processor p, int[] args) {
                            int lastStackAddress = p.SP.getValue() + 1;
                            p.MEMORY.setValueAt(args[0], p.MEMORY.getValueAt(lastStackAddress));
                            p.SP.setValue(lastStackAddress);
                        }
                    },
                    new Instruction("HLT", 0) {
                        @Override
                        public void execute(Processor p, int[] args) {
                            p.stop();
                        }
                    }
            }
    );

    public Processor() {
        this(256);
    }

    public Processor(int memSize) {
        MEMORY = new Memory(memSize);
        SP.setValue(MEMORY.getSize() - 1);
    }

    public void run() {
        if (isRunning) return;

        isRunning = true;
        while (isRunning) {

            INSTRUCTIONSET.parseAndExecute(this, IP.getValue());

        }
    }

    public void run(int fromAddress) {
        if (isRunning) return;

        IP.setValue(fromAddress);
        run();
    }

    public void stop() { isRunning = false; }

}
