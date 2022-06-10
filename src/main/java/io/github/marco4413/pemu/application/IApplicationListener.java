package io.github.marco4413.pemu.application;

import org.jetbrains.annotations.Nullable;

import java.io.File;

public interface IApplicationListener {
    void onProgramChanged(@Nullable File newProgram);
}
