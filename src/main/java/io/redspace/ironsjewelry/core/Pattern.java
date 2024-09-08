package io.redspace.ironsjewelry.core;

import io.redspace.ironsjewelry.core.data.PartIngredient;

import java.util.List;

public record Pattern(List<PartIngredient> partTemplate, List<BonusSource> bonuses, boolean unlockedByDefault){
}
