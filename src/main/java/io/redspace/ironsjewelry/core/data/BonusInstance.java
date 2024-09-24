package io.redspace.ironsjewelry.core.data;

import io.redspace.ironsjewelry.core.IBonus;
import io.redspace.ironsjewelry.core.IBonusParameterType;

import java.util.Map;

public record BonusInstance(IBonus bonus, double quality, Map<IBonusParameterType<?>, Object> parameter) {
}
