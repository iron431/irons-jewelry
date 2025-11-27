package io.redspace.ironsjewelry.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.IronsJewelry;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record PartDefinition(String descriptionId,
                             ResourceLocation paletteKey,
                             List<String> allowedMaterials,
                             ResourceLocation baseTextureLocation,
                             Optional<ResourceLocation> modelTextureLocation) {
    public static final Codec<PartDefinition> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.STRING.fieldOf("descriptionId").forGetter(PartDefinition::descriptionId),
            ResourceLocation.CODEC.fieldOf("paletteKey").forGetter(PartDefinition::paletteKey),
            Codec.list(Codec.STRING).optionalFieldOf("allowedMaterialTypes", List.of()).forGetter(PartDefinition::allowedMaterials),
            ResourceLocation.CODEC.fieldOf("baseTextureLocation").forGetter(PartDefinition::baseTextureLocation),
            ResourceLocation.CODEC.optionalFieldOf("modelTextureLocation").forGetter(PartDefinition::modelTextureLocation)
    ).apply(builder, PartDefinition::new));

    public boolean canUseMaterial(String materialType) {
        return allowedMaterials.isEmpty() || allowedMaterials.contains(materialType);
    }

    public boolean canUseMaterial(List<String> materialTypes) {
        return allowedMaterials.isEmpty() || materialTypes.stream().anyMatch(allowedMaterials::contains);
    }

    /**
     * Use {@link Builder#simpleMetalPart(String, String)} instead
     */
    @Deprecated(forRemoval = true)
    public static PartDefinition simpleMetalPart(String namespace, String name) {
        return new PartDefinition(
                String.format("part.%s.%s", namespace, name),
                IronsJewelry.id("palettes/gold"),
                List.of("metal"),
                ResourceLocation.fromNamespaceAndPath(namespace, String.format("item/base/%s", name)),
                Optional.empty()
        );
    }

    /**
     * Use {@link Builder#simpleGemPart(String, String)} instead
     */
    @Deprecated(forRemoval = true)
    public static PartDefinition simpleGemPart(String namespace, String name) {
        return new PartDefinition(
                String.format("part.%s.%s", namespace, name),
                IronsJewelry.id("palettes/diamond"),
                List.of("gem"),
                ResourceLocation.fromNamespaceAndPath(namespace, String.format("item/base/%s", name)),
                Optional.empty()
        );
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String descriptionId;
        private ResourceLocation paletteKey;
        private List<String> allowedMaterials = new ArrayList<>();
        private ResourceLocation baseTextureLocation;
        private Optional<ResourceLocation> modelTextureLocation = Optional.empty();

        private Builder() {

        }

        // --- Builder methods ---
        public Builder descriptionId(String descriptionId) {
            this.descriptionId = descriptionId;
            return this;
        }

        public Builder paletteKey(ResourceLocation paletteKey) {
            this.paletteKey = paletteKey;
            return this;
        }

        public Builder allowedMaterials(List<String> allowedMaterials) {
            this.allowedMaterials = allowedMaterials;
            return this;
        }

        public Builder addAllowedMaterial(String material) {
            this.allowedMaterials.add(material);
            return this;
        }

        public Builder baseTextureLocation(ResourceLocation baseTextureLocation) {
            this.baseTextureLocation = baseTextureLocation;
            return this;
        }

        public Builder modelTextureLocation(ResourceLocation modelTextureLocation) {
            this.modelTextureLocation = Optional.ofNullable(modelTextureLocation);
            return this;
        }

        public PartDefinition build() {
            if (descriptionId == null || paletteKey == null || baseTextureLocation == null) {
                throw new IllegalStateException("descriptionId, paletteKey, and baseTextureLocation must be set");
            }
            return new PartDefinition(
                    descriptionId,
                    paletteKey,
                    allowedMaterials,
                    baseTextureLocation,
                    modelTextureLocation
            );
        }

        public static Builder simpleMetalPart(String namespace, String name) {
            return new Builder()
                    .descriptionId(String.format("part.%s.%s", namespace, name))
                    .paletteKey(IronsJewelry.id("palettes/gold"))
                    .allowedMaterials(List.of("metal"))
                    .baseTextureLocation(ResourceLocation.fromNamespaceAndPath(namespace, String.format("item/base/%s", name)));
        }

        public static Builder simpleGemPart(String namespace, String name) {
            return new Builder()
                    .descriptionId(String.format("part.%s.%s", namespace, name))
                    .paletteKey(IronsJewelry.id("palettes/diamond"))
                    .allowedMaterials(List.of("gem"))
                    .baseTextureLocation(ResourceLocation.fromNamespaceAndPath(namespace, String.format("item/base/%s", name)));
        }
    }

}
