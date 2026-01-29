package com.astralrealms.classes.model;

import org.bukkit.Input;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerInput;

public class SimpleInput implements Input {

    private final boolean forward;
    private final boolean backward;
    private final boolean left;
    private final boolean right;
    private final boolean jump;
    private final boolean sneak;
    private final boolean sprint;

    public SimpleInput(WrapperPlayClientPlayerInput packet) {
        this.forward = packet.isForward();
        this.backward = packet.isBackward();
        this.left = packet.isLeft();
        this.right = packet.isRight();
        this.jump = packet.isJump();
        this.sneak = packet.isShift();
        this.sprint = packet.isSprint();
    }

    @Override
    public boolean isForward() {
        return this.forward;
    }

    @Override
    public boolean isBackward() {
        return this.backward;
    }

    @Override
    public boolean isLeft() {
        return this.left;
    }

    @Override
    public boolean isRight() {
        return this.right;
    }

    @Override
    public boolean isJump() {
        return this.jump;
    }

    @Override
    public boolean isSneak() {
        return this.sneak;
    }

    @Override
    public boolean isSprint() {
        return this.sprint;
    }
}
