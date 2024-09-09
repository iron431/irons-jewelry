package io.redspace.ironsjewelry.data_registry;

import com.google.common.collect.ImmutableMap;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.data.PartDefinition;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

public class PartDataHandler extends SimpleJsonResourceReloadListener {
    public static Map<ResourceLocation, PartDefinition> INSTANCE;


    public PartDataHandler() {
        super(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(), "irons_jewelry/parts");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        ImmutableMap.Builder<ResourceLocation, PartDefinition> builder = ImmutableMap.builder();
        RegistryOps<JsonElement> registryops = this.makeConditionalOps(); // Neo: add condition context

        for (Map.Entry<ResourceLocation, JsonElement> entry : pObject.entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            if (resourcelocation.getPath().startsWith("_"))
                continue; //Forge: filter anything beginning with "_" as it's used for metadata.

            try {
                var decoded = PartDefinition.CODEC.parse(registryops, entry.getValue()).getOrThrow(JsonParseException::new);
                builder.put(resourcelocation, decoded);
            } catch (IllegalArgumentException | JsonParseException jsonparseexception) {
                IronsJewelry.LOGGER.error("Parsing error loading partId {}", resourcelocation, jsonparseexception);
            }
        }

        INSTANCE = builder.build();
    }
}
