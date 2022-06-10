package io.github.marco4413.pemu.processor;

public final class Clock {

    // 1 billion is the max because it's 1 update each nano second
    //  and we can't update more than 1 time each nano second because
    //  we're using System.nanoTime
    public static final int MAX_FREQUENCY = 1_000_000_000;
    // 1 is the minimum because floating Hz aren't supported
    public static final int MIN_FREQUENCY = 1;

    private int frequency;
    private long interval;
    private long lastUpdated = 0;
    private long lastDelta = 0;

    public Clock(int frequency) {
        setFrequency(frequency);
    }

    /**
     * Sets this {@link Clock}'s frequency to the one specified
     * @param frequency The new frequency of this {@link Clock}
     */
    public void setFrequency(int frequency) {
        if (frequency < MIN_FREQUENCY)
            throw new IllegalArgumentException("Clock's frequency can't be less than " + MIN_FREQUENCY + "Hz.");
        else if (frequency > MAX_FREQUENCY)
            throw new IllegalArgumentException("Clock's frequency can't be more than " + MAX_FREQUENCY + "Hz.");

        this.frequency = frequency;
        // Why 1 billion? Because the interval is stored as nano seconds
        //  It's the same as (1 / (double) frequency) * 1_000_000_000
        this.interval = 1_000_000_000L / frequency;
    }

    /**
     * Returns this {@link Clock}'s frequency
     * @return This {@link Clock}'s frequency
     */
    public int getFrequency() {
        return frequency;
    }

    /**
     * Returns the interval (in seconds) between {@link Clock} updates
     * @return The interval (in seconds) between {@link Clock} updates
     */
    public double getInterval() {
        return this.interval / 1_000_000_000d;
    }

    /**
     * Returns the last update's delta time
     * @return The last update's delta time
     */
    public double getDeltaTime() {
        return lastDelta / 1_000_000_000d;
    }

    /**
     * Updates this {@link Clock} and returns whether or not it triggered
     * @return Whether or not this {@link Clock} triggered
     */
    public boolean update() {
        long currentTime = System.nanoTime();
        long deltaTime = currentTime - lastUpdated;
        if (deltaTime >= interval) {
            lastDelta = deltaTime;
            lastUpdated = currentTime;
            return true;
        }
        return false;
    }

}
