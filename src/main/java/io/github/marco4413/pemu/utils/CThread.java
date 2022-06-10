package io.github.marco4413.pemu.utils;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class CThread {

    public static @NotNull Thread runThread(@NotNull Runnable worker, @NotNull Runnable onFinish, @NotNull Consumer<Throwable> onError) {
        return runThread(worker, onFinish, onError, true);
    }

    public static @NotNull Thread runThread(@NotNull Runnable worker, @NotNull Runnable onFinish, @NotNull Consumer<Throwable> onError, boolean finishAfterError) {
        Thread thread = new Thread(worker) {
            @Override
            public void run() {
                try {
                    super.run();
                } catch (Throwable err) {
                    onError.accept(err);
                    if (!finishAfterError)
                        return;
                }

                onFinish.run();
            }
        };

        thread.start();
        return thread;
    }

}
