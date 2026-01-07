package com.astralrealms.classes.model;

import lombok.Getter;

public class JumpState {

    @Getter
    private int jumpCount = 0;
    private long lastDoubleJumpTime = 0;

    public void reset() {
        jumpCount = 0;
    }

    public boolean canDoubleJump(long currentTime, long cooldownMs, int maxJumps) {
        return jumpCount < maxJumps && (jumpCount == 0 || currentTime - lastDoubleJumpTime >= cooldownMs);
    }

    public void recordJump() {
        jumpCount++;
    }

    public void recordDoubleJump(long currentTime) {
        jumpCount++;
        lastDoubleJumpTime = currentTime;
    }
}