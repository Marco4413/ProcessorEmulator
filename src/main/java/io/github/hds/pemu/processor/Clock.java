package io.github.hds.pemu.processor;

public class Clock {

    public static final double MAX_CLOCK = 1e9d;

    public final double CLOCK;
    private final double TIME_BETWEEN_UPDATES;

    private long lastUpdated = 0;

    public Clock(double clock) {
        CLOCK = clock;
        TIME_BETWEEN_UPDATES = MAX_CLOCK / CLOCK;
    }

    public boolean update() {
        long currentTime = System.nanoTime();
        if (currentTime - lastUpdated >= TIME_BETWEEN_UPDATES) {
            lastUpdated = currentTime;
            return true;
        }
        return false;
    }

}
