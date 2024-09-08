package io.redspace.ironsjewelry.core;

import io.redspace.ironsjewelry.data.PartData;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

//TODO: this cant be record
public record AbstractPattern(ResourceLocation patternId, Map<ResourceLocation, PartData> template) {
}
