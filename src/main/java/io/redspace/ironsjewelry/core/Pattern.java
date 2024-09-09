package io.redspace.ironsjewelry.core;

import io.redspace.ironsjewelry.core.data.PartIngredient;

import java.util.List;

public record Pattern(List<PartIngredient> partTemplate, List<BonusSource> bonuses, boolean unlockedByDefault) {
    @Override
    public int hashCode() {
        return (partTemplate.hashCode() * 31 + bonuses().hashCode()) * 10 + (unlockedByDefault ? 1 : 0);
    }
}
