package io.redspace.ironsjewelry.core.data;

import net.minecraft.resources.ResourceLocation;

public record PartInstance(PartDefinition part, MaterialData material) {

    public ResourceLocation atlasResourceLocaction(){
        String composite = part.baseTextureLocation().toString();
        var components = material.paletteLocation().getPath().split("/");
        composite += "_" + components[components.length - 1];
        return ResourceLocation.parse(composite);
    }
}
