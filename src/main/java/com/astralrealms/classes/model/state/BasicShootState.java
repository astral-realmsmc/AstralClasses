package com.astralrealms.classes.model.state;

import com.astralrealms.classes.model.Tickable;

import lombok.Getter;

@Getter
public class BasicShootState implements Tickable {

    private int hits;
    private long lastHitTimestamp;
    private long lastLostTimestamp;

    public BasicShootState() {
        this.hits = 0;
        this.lastHitTimestamp = 0;
    }

    public void recordHit() {
        lastHitTimestamp = System.currentTimeMillis();

        if (hits < 6)
            hits++;
    }

    public void resetHits() {
        this.hits = 0;
        this.lastHitTimestamp = 0;
        this.lastLostTimestamp = 0;
    }

    @Override
    public void tick() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastHitTimestamp < 1000)
            return;

        if (hits > 0 && currentTime - lastLostTimestamp >= 1000) {
            hits--;
            lastLostTimestamp = currentTime;
        }
    }
}
