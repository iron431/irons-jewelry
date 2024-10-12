package io.redspace.ironsjewelry.core.data;

public class CooldownInstance {
    int remainingTicks, totalTicks;
    boolean firstTick;
    public CooldownInstance(int ticks) {
        this.remainingTicks = ticks;
        this.totalTicks = ticks;
    }

    public CooldownInstance(int remainingTicks, int totalTicks) {
        this.remainingTicks = remainingTicks;
        this.totalTicks = totalTicks;
    }

    public void decrementBy(int amount) {
        firstTick = true;
        remainingTicks -= amount;
    }

    public int getRemainingTicks(){
        return remainingTicks;
    }
}
