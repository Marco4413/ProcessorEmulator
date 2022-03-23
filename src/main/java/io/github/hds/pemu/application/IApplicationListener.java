package io.github.hds.pemu.application;

import org.jetbrains.annotations.Nullable;

import java.io.File;

public interface IApplicationListener {
    void onProgramChanged(@Nullable File newProgram);
}
