package io.github.hds.pemu.processor;

public class Clock {

    public static final int MAX_CLOCK = 1_000_000_000;
    public static final int MIN_CLOCK = 1;

    public final int CLOCK;
    private final int TIME_BETWEEN_UPDATES;

    private long lastUpdated = 0;

    public Clock(int clock) {
        if (clock < MIN_CLOCK) throw new IllegalArgumentException("Clock can't be less than " + MIN_CLOCK + "Hz!");
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
