package io.redspace.ironsjewelry.data;

import net.minecraft.resources.ResourceLocation;

public record PartInstance(PartData part, MaterialData material) {

    public ResourceLocation atlasResourceLocaction(){
        String composite = part.baseTextureLocation().toString();
        var components = material.paletteLocation().getPath().split("/");
        composite += "_" + components[components.length - 1];
        return ResourceLocation.parse(composite);
    }
}
