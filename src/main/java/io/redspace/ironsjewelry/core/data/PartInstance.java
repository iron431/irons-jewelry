package io.redspace.ironsjewelry.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.core.data_registry.MaterialDataHandler;
import io.redspace.ironsjewelry.core.data_registry.PartDataHandler;
import net.minecraft.resources.ResourceLocation;

/**
 * A part instance is a combination of a part and a material, ie Gold Band or Ruby Gemstone
 *
 * @param part     Id of the part definition
 * @param material Id of the material definition
 */
public record PartInstance(PartDefinition part, MaterialDefinition material) {
    public static final Codec<PartInstance> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf("part").forGetter(data -> PartDataHandler.getKey(data.part)),
            ResourceLocation.CODEC.fieldOf("material").forGetter(data -> MaterialDataHandler.getKey(data.material))
    ).apply(builder, PartInstance::fromResource));

    public ResourceLocation atlasResourceLocaction() {
        try {
            String composite = part.baseTextureLocation().toString();
            var components = material.paletteLocation().getPath().split("/");
            composite += "_" + components[components.length - 1];
            return ResourceLocation.parse(composite);
        } catch (Exception e) {
            //TODO: something better
            return ResourceLocation.parse("unknown");
        }
    }

    public static PartInstance fromResource(ResourceLocation part, ResourceLocation material) {
        return new PartInstance(PartDataHandler.get(part), MaterialDataHandler.get(material));
    }
}
