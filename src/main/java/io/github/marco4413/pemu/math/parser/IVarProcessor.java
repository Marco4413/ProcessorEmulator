package io.github.marco4413.pemu.math.parser;

import io.github.marco4413.pemu.tokenizer.Token;

import java.util.function.BiFunction;

@FunctionalInterface
public interface IVarProcessor extends BiFunction<Token, Object, Double> { }
