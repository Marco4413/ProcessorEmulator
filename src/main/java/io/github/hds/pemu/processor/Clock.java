package io.github.hds.pemu.processor;

public class Clock {

    public final int CLOCK;

    private long lastUpdated = 0;

    public Clock(int clock) {
        CLOCK = clock;
    }

    public boolean update() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdated >= 1000.0f / (float) CLOCK) {
            lastUpdated = currentTime;
            return true;
        }
        return false;
    }

}
