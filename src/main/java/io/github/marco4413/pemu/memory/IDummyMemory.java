package io.github.marco4413.pemu.memory;

import io.github.marco4413.pemu.instructions.Instruction;
import io.github.marco4413.pemu.processor.DummyProcessor;

/**
 * This interface is used to specify if a given {@link IMemory} should be Dummy
 * This should only be used as a method argument type (see {@link DummyProcessor}'s constructor)
 * (e.g. {@link Instruction}s shouldn't check if a given {@link IMemory} is Dummy, they should work regardless of the type)
 */
public interface IDummyMemory extends IMemory { }
