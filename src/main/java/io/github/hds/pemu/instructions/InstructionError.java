package io.github.hds.pemu.instructions;

import org.jetbrains.annotations.NotNull;

public class InstructionError extends RuntimeException {
    public InstructionError(@NotNull String name, @NotNull String message, int address) {
        super(
                String.format(
                        "Instruction Error (at %d): \"%s\", %s",
                        address, name,
                        message.endsWith(".") ? message : message + "."
                )
        );
    }
}
