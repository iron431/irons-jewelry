package io.redspace.ironsjewelry.core;

public class CooldownHandler {
    public static ICooldownHandler INSTANCE = (wearer, baseCooldown, quality) -> (int) baseCooldown.sample(quality);
}
