package io.redspace.ironsjewelry.core;

import io.redspace.ironsjewelry.core.data.QualityScalar;

public interface ICooldownHandler {
    //TODO: iron's spellbooks compat handler
    ICooldownHandler INSTANCE = new ICooldownHandler() {
        @Override
        public int getCooldown(QualityScalar baseCooldown, double quality) {
            return (int) baseCooldown.sample(quality);
        }
    };

    int getCooldown(QualityScalar baseCooldown, double quality);
}
