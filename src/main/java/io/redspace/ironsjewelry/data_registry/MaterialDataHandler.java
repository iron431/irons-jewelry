package io.redspace.ironsjewelry.data_registry;

import com.google.common.collect.ImmutableMap;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.data.MaterialData;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.Map;

public class MaterialDataHandler extends SimpleJsonResourceReloadListener {
    private static Map<ResourceLocation, MaterialData> INSTANCE;
    //TODO: figure out how to record bonuses
    public static final Codec<MaterialData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(MaterialData::ingredient),
            ResourceLocation.CODEC.fieldOf("paletteLocation").forGetter(MaterialData::paletteLocation),
            //Codecs.li.CODEC.fieldOf("paletteLocation").forGetter(MaterialData::bonuses),
            Codec.DOUBLE.fieldOf("quality").forGetter(MaterialData::quality)
    ).apply(builder, (ingredient, paletteLocation, quality) -> new MaterialData(ingredient, paletteLocation, List.of(), quality)));

    public MaterialDataHandler() {
        super(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(), "irons_jewelry/materials");
    }

    @Override
    protected Map<ResourceLocation, JsonElement> prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        IronsJewelry.LOGGER.debug("MaterialDataHandler.prepare");
        return super.prepare(pResourceManager, pProfiler);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        IronsJewelry.LOGGER.debug("MaterialDataHandler.apply");
        ImmutableMap.Builder<ResourceLocation, MaterialData> builder = ImmutableMap.builder();
        RegistryOps<JsonElement> registryops = this.makeConditionalOps(); // Neo: add condition context

        for (Map.Entry<ResourceLocation, JsonElement> entry : pObject.entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            if (resourcelocation.getPath().startsWith("_"))
                continue; //Forge: filter anything beginning with "_" as it's used for metadata.

            try {
                var decoded = CODEC.parse(registryops, entry.getValue()).getOrThrow(JsonParseException::new);
                builder.put(resourcelocation, decoded);
            } catch (IllegalArgumentException | JsonParseException jsonparseexception) {
                IronsJewelry.LOGGER.error("Parsing error loading material {}", resourcelocation, jsonparseexception);
            }
        }

        INSTANCE = builder.build();
    }
}
