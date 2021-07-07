package io.github.hds.pemu.processor;

import io.github.hds.pemu.instructions.Instruction;

/**
 * This interface is used to specify if a given {@link IProcessor} should be Dummy
 * This should only be used as a method argument type (see {@link DummyProcessor}'s constructor)
 * (e.g. {@link Instruction}s shouldn't check if a given {@link IProcessor} is Dummy, they should work regardless of the type)
 */
public interface IDummyProcessor extends IProcessor { }
