package io.github.hds.pemu.processor;

public class Clock {

    public static final int MAX_CLOCK = 1_000_000_000;
    public static final int MIN_CLOCK = 1;

    private int clock;
    private int timeBetweenUpdates;

    private long lastUpdated = 0;

    public Clock(int clock) {
        setClock(clock);
    }

    public void setClock(int clock) {
        if (clock < MIN_CLOCK) throw new IllegalArgumentException("Clock can't be less than " + MIN_CLOCK + "Hz!");
        this.clock = clock;
        this.timeBetweenUpdates = MAX_CLOCK / clock;
    }

    public int getClock() {
        return clock;
    }

    public boolean update() {
        long currentTime = System.nanoTime();
        if (currentTime - lastUpdated >= timeBetweenUpdates) {
            lastUpdated = currentTime;
            return true;
        }
        return false;
    }

}
