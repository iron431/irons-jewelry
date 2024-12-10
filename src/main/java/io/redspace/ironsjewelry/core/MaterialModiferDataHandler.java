package io.redspace.ironsjewelry.core;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.data.MaterialDefinition;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

public class MaterialModiferDataHandler extends SimpleJsonResourceReloadListener {
    record Modifier(Holder<MaterialDefinition> targetMaterial, Map<IBonusParameterType<?>, Object> parameterOverrides) {
    }

    private static final Codec<Modifier> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            IronsJewelryRegistries.Codecs.MATERIAL_REGISTRY_CODEC.fieldOf("targetMaterial").forGetter(Modifier::targetMaterial),
            IBonusParameterType.BONUS_TO_INSTANCE_CODEC.fieldOf("bonusParameters").forGetter(Modifier::parameterOverrides)
    ).apply(builder, Modifier::new));

    private static Multimap<Holder<MaterialDefinition>, Modifier> INSTANCE;

    public MaterialModiferDataHandler() {
        super(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(), "irons_jewelry/material_modifier");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        IronsJewelry.LOGGER.debug("MaterialDataHandler.apply");
        ImmutableMultimap.Builder<Holder<MaterialDefinition>, Modifier> builder = ImmutableMultimap.builder();
        RegistryOps<JsonElement> registryops = this.makeConditionalOps(); // Neo: add condition context

        for (Map.Entry<ResourceLocation, JsonElement> entry : pObject.entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            if (resourcelocation.getPath().startsWith("_"))
                continue; //Forge: filter anything beginning with "_" as it's used for metadata.
            try {
                var decoded = CODEC.parse(registryops, entry.getValue()).getOrThrow(JsonParseException::new);
                builder.put(decoded.targetMaterial, decoded);
            } catch (IllegalArgumentException | JsonParseException jsonparseexception) {
                IronsJewelry.LOGGER.error("Parsing error loading material {}: {}", resourcelocation, jsonparseexception);
            }
        }

        INSTANCE = builder.build();
        IronsJewelry.LOGGER.debug("MaterialDataHandler Finished Loading: {}", INSTANCE);

    }

    public static Map<IBonusParameterType<?>, Object> getParametersWithOverrides(Holder<MaterialDefinition> material) {
        ImmutableMap.Builder builder = ImmutableMap.builder().putAll(material.value().bonusParameters());
        for (Modifier modifier : INSTANCE.get(material)) {
            var overrides = modifier.parameterOverrides;
            for (Map.Entry<IBonusParameterType<?>, Object> e : overrides.entrySet()) {
                builder.put(e.getKey(), e.getValue());
            }
        }
        var map = builder.buildKeepingLast();
        return (Map<IBonusParameterType<?>, Object>) map;
    }
}