package io.redspace.ironsjewelry.core.data;

import net.minecraft.resources.ResourceLocation;

public record PartDefinition(ResourceLocation baseTextureLocation) {
    @Override
    public int hashCode() {
        return baseTextureLocation.hashCode();
    }
}
