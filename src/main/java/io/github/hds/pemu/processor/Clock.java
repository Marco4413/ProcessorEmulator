package io.github.hds.pemu.processor;

public final class Clock {

    // 1 billion is the max because it's 1 update each nano second
    //  and we can't update more than 1 time each nano second because
    //  we're using System.nanoTime
    public static final int MAX_CLOCK = 1_000_000_000;
    // 1 is the minimum because floating Hz aren't supported
    public static final int MIN_CLOCK = 1;

    private int clock;
    private long interval;
    private long lastUpdated = 0;

    public Clock(int clock) {
        setClock(clock);
    }

    public void setClock(int clock) {
        if (clock < MIN_CLOCK) throw new IllegalArgumentException("Clock can't be less than " + MIN_CLOCK + "Hz.");
        else if (clock > MAX_CLOCK) throw new IllegalArgumentException("Clock can't be more than " + MAX_CLOCK + "Hz.");

        this.clock = clock;
        // Why 1 billion? Because the interval is stored as nano seconds
        //  It's the same as (1 / (double) clock) * 1_000_000_000
        this.interval = 1_000_000_000L / clock;
    }

    public int getClock() {
        return clock;
    }

    public boolean update() {
        long currentTime = System.nanoTime();
        if (currentTime - lastUpdated >= interval) {
            lastUpdated = currentTime;
            return true;
        }
        return false;
    }

}
