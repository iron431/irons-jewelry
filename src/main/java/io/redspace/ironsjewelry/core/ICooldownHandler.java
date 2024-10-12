package io.redspace.ironsjewelry.core;

public interface ICooldownHandler {
    //TODO: iron's spellbooks compat handler
    ICooldownHandler INSTANCE = new ICooldownHandler() {
        @Override
        public int getCooldown(int baseCooldown, double quality) {
            return baseCooldown;
        }
    };

    int getCooldown(int baseCooldown, double quality);
}
