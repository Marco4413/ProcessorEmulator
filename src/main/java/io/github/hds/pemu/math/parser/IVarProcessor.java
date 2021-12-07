package io.github.hds.pemu.math.parser;

import io.github.hds.pemu.tokenizer.Token;

import java.util.function.BiFunction;

@FunctionalInterface
public interface IVarProcessor extends BiFunction<Token, Object, Double> { }
