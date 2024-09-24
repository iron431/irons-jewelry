package io.redspace.ironsjewelry.core.data_registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.data.MaterialDefinition;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;
import java.util.Optional;

public class MaterialDataHandler extends SimpleJsonResourceReloadListener {
    private static BiMap<ResourceLocation, MaterialDefinition> INSTANCE;

    public MaterialDataHandler() {
        super(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(), "irons_jewelry/materials");
    }


    @Override
    protected Map<ResourceLocation, JsonElement> prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        IronsJewelry.LOGGER.debug("MaterialDataHandler.prepare");
        //TODO: soft-override system like TagLoader#52
        // While TogLoader is a custom implementation, it is still driven by a PreparableReloadListener (TagManager)
        // Our own custom implementation would be difficuly because its not the SimpleJsonManager#scan that needs to account for our "soft-overrides", but the ResourceManager#list that does
        // Would the tagloader style of filetoidconverter.listMatchingResourceStacks work instead?
        return super.prepare(pResourceManager, pProfiler);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        IronsJewelry.LOGGER.debug("MaterialDataHandler.apply");
        ImmutableBiMap.Builder<ResourceLocation, MaterialDefinition> builder = ImmutableBiMap.builder();
        RegistryOps<JsonElement> registryops = this.makeConditionalOps(); // Neo: add condition context

        for (Map.Entry<ResourceLocation, JsonElement> entry : pObject.entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            if (resourcelocation.getPath().startsWith("_"))
                continue; //Forge: filter anything beginning with "_" as it's used for metadata.

            try {
                var decoded = MaterialDefinition.CODEC.parse(registryops, entry.getValue()).getOrThrow(JsonParseException::new);
                builder.put(resourcelocation, decoded);
            } catch (IllegalArgumentException | JsonParseException jsonparseexception) {
                IronsJewelry.LOGGER.error("Parsing error loading material {}: {}", resourcelocation, jsonparseexception);
            }
        }

        INSTANCE = builder.build();
        IronsJewelry.LOGGER.debug("MaterialDataHandler Finished Loading: {}", INSTANCE);

    }

    public static MaterialDefinition get(ResourceLocation resourceLocation) {
        return INSTANCE.get(resourceLocation);
    }

    public static Optional<MaterialDefinition> getSafe(ResourceLocation resourceLocation) {
        return INSTANCE.containsKey(resourceLocation) ? Optional.of(INSTANCE.get(resourceLocation)) : Optional.empty();
    }

    public static ResourceLocation getKey(MaterialDefinition part) {
        return INSTANCE.inverse().get(part);
    }
}
