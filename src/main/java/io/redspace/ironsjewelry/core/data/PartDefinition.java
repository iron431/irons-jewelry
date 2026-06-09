package io.redspace.ironsjewelry.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public record PartDefinition(String descriptionId,
                             Identifier paletteKey,
                             Optional<HolderSet<MaterialDefinition>> allowedMaterials,
                             Identifier baseTextureLocation) {
    public static final Codec<PartDefinition> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.STRING.fieldOf("descriptionId").forGetter(PartDefinition::descriptionId),
            Identifier.CODEC.fieldOf("paletteKey").forGetter(PartDefinition::paletteKey),
            RegistryCodecs.homogeneousList(IronsJewelryRegistries.Keys.MATERIAL_REGISTRY_KEY).optionalFieldOf("allowedMaterials").forGetter(PartDefinition::allowedMaterials),
            Identifier.CODEC.fieldOf("baseTextureLocation").forGetter(PartDefinition::baseTextureLocation)
    ).apply(builder, PartDefinition::new));

    public boolean canUseMaterial(Holder<MaterialDefinition> material) {
        return allowedMaterials.isEmpty() || allowedMaterials.get().contains(material);
    }

    public static PartDefinition simpleMetalPart(String namespace, String name, HolderSet<MaterialDefinition> allowedMaterials) {
        return new PartDefinition(
                String.format("part.%s.%s", namespace, name),
                IronsJewelry.id("palettes/gold"),
                Optional.of(allowedMaterials),
                Identifier.fromNamespaceAndPath(namespace, String.format("item/base/%s", name))
        );
    }

    public static PartDefinition simpleGemPart(String namespace, String name, HolderSet<MaterialDefinition> allowedMaterials) {
        return new PartDefinition(
                String.format("part.%s.%s", namespace, name),
                IronsJewelry.id("palettes/diamond"),
                Optional.of(allowedMaterials),
                Identifier.fromNamespaceAndPath(namespace, String.format("item/base/%s", name))
        );
    }
}
