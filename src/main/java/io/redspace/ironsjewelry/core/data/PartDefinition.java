package io.redspace.ironsjewelry.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.core.data_registry.PartDataHandler;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record PartDefinition(List<String> allowedMaterials, ResourceLocation baseTextureLocation) {
    public static final Codec<PartDefinition> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.list(Codec.STRING).optionalFieldOf("allowedMaterialTypes", List.of()).forGetter(PartDefinition::allowedMaterials),
            ResourceLocation.CODEC.fieldOf("baseTextureLocation").forGetter(PartDefinition::baseTextureLocation)
    ).apply(builder, PartDefinition::new));

    public boolean canUseMaterial(String materialType) {
        return allowedMaterials.isEmpty() || allowedMaterials.contains(materialType);
    }

    public boolean canUseMaterial(List<String> materialTypes) {
        return allowedMaterials.isEmpty() || materialTypes.stream().anyMatch(allowedMaterials::contains);
    }

    public String getDescriptionId() {
        var id = id();
        if (id == null) {
            return "unregistered_part";
        } else {
            return Util.makeDescriptionId("part", id);
        }
    }

    public ResourceLocation id() {
        return PartDataHandler.getKey(this);
    }
}
