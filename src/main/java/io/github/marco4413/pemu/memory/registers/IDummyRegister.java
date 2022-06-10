package io.github.marco4413.pemu.memory.registers;

import io.github.marco4413.pemu.instructions.Instruction;
import io.github.marco4413.pemu.processor.DummyProcessor;

/**
 * This interface is used to specify if a given {@link IRegister} should be Dummy
 * This should only be used as a method argument type (see {@link DummyProcessor}'s constructor)
 * (e.g. {@link Instruction}s shouldn't check if a given {@link IRegister} is Dummy, they should work regardless of the type)
 */
public interface IDummyRegister extends IRegister { }
