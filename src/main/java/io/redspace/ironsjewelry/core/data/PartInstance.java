package io.redspace.ironsjewelry.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.data_registry.MaterialDataHandler;
import io.redspace.ironsjewelry.data_registry.PartDataHandler;
import net.minecraft.resources.ResourceLocation;

public record PartInstance(ResourceLocation partId, ResourceLocation materialId) {
    public static final Codec<PartInstance> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf("part").forGetter(PartInstance::partId),
            ResourceLocation.CODEC.fieldOf("material").forGetter(PartInstance::materialId)
    ).apply(builder, PartInstance::new));

    public ResourceLocation atlasResourceLocaction(){
        try{

            String composite = PartDataHandler.INSTANCE.get(partId).baseTextureLocation().toString();
            var components = MaterialDataHandler.INSTANCE.get(materialId).paletteLocation().getPath().split("/");
            composite += "_" + components[components.length - 1];
            return ResourceLocation.parse(composite);
        }catch (Exception e){
            //TODO: something better
            return ResourceLocation.parse("unknown");
        }
    }
}
