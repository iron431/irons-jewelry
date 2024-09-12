package io.redspace.ironsjewelry.core.data_registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.data.PatternDefinition;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;
import java.util.Optional;

public class PatternDataHandler extends SimpleJsonResourceReloadListener {
    private static BiMap<ResourceLocation, PatternDefinition> INSTANCE;

    public PatternDataHandler() {
        super(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(), "irons_jewelry/patterns");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        ImmutableBiMap.Builder<ResourceLocation, PatternDefinition> builder = ImmutableBiMap.builder();
        RegistryOps<JsonElement> registryops = this.makeConditionalOps(); // Neo: add condition context

        for (Map.Entry<ResourceLocation, JsonElement> entry : pObject.entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            if (resourcelocation.getPath().startsWith("_"))
                continue; //Forge: filter anything beginning with "_" as it's used for metadata.

            try {
                var decoded = PatternDefinition.CODEC.parse(registryops, entry.getValue()).getOrThrow(JsonParseException::new);
                builder.put(resourcelocation, decoded);
            } catch (IllegalArgumentException | JsonParseException jsonparseexception) {
                IronsJewelry.LOGGER.error("Parsing error loading pattern {}", resourcelocation, jsonparseexception);
            }
        }

        INSTANCE = builder.build();
        IronsJewelry.LOGGER.debug("PatternDataHandler Finished Loading: {}", INSTANCE);
    }

    public static PatternDefinition get(ResourceLocation resourceLocation) {
        return INSTANCE.get(resourceLocation);
    }

    public static Optional<PatternDefinition> getSafe(ResourceLocation resourceLocation) {
        return INSTANCE.containsKey(resourceLocation) ? Optional.of(INSTANCE.get(resourceLocation)) : Optional.empty();
    }

    public static ResourceLocation getKey(PatternDefinition part) {
        return INSTANCE.inverse().get(part);
    }
}
