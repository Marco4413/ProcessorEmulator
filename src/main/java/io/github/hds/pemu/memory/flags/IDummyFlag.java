package io.github.hds.pemu.memory.flags;

import io.github.hds.pemu.instructions.Instruction;
import io.github.hds.pemu.processor.DummyProcessor;

/**
 * This interface is used to specify if a given {@link IFlag} should be Dummy
 * This should only be used as a method argument type (see {@link DummyProcessor}'s constructor)
 * (e.g. {@link Instruction}s shouldn't check if a given {@link IFlag} is Dummy, they should work regardless of the type)
 */
public interface IDummyFlag extends IFlag { }
