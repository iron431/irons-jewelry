package io.redspace.ironsjewelry.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.IronsJewelry;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record PartDefinition(String descriptionId,
                             ResourceLocation paletteKey,
                             List<String> allowedMaterials,
                             ResourceLocation baseTextureLocation) {
    public static final Codec<PartDefinition> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.STRING.fieldOf("descriptionId").forGetter(PartDefinition::descriptionId),
            ResourceLocation.CODEC.fieldOf("paletteKey").forGetter(PartDefinition::paletteKey),
            Codec.list(Codec.STRING).optionalFieldOf("allowedMaterialTypes", List.of()).forGetter(PartDefinition::allowedMaterials),
            ResourceLocation.CODEC.fieldOf("baseTextureLocation").forGetter(PartDefinition::baseTextureLocation)
    ).apply(builder, PartDefinition::new));

    public boolean canUseMaterial(String materialType) {
        return allowedMaterials.isEmpty() || allowedMaterials.contains(materialType);
    }

    public boolean canUseMaterial(List<String> materialTypes) {
        return allowedMaterials.isEmpty() || materialTypes.stream().anyMatch(allowedMaterials::contains);
    }

    public static PartDefinition simpleMetalPart(String namespace, String name) {
        return new PartDefinition(
                String.format("part.%s.%s", namespace, name),
                IronsJewelry.id("palettes/gold"),
                List.of("metal"),
                ResourceLocation.fromNamespaceAndPath(namespace, String.format("item/base/%s", name))
        );
    }
    public static PartDefinition simpleGemPart(String namespace, String name) {
        return new PartDefinition(
                String.format("part.%s.%s", namespace, name),
                IronsJewelry.id("palettes/diamond"),
                List.of("gem"),
                ResourceLocation.fromNamespaceAndPath(namespace, String.format("item/base/%s", name))
        );
    }
}
