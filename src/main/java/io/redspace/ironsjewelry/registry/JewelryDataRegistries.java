package io.redspace.ironsjewelry.registry;

import com.mojang.serialization.Codec;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.data.PatternDefinition;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

public class JewelryDataRegistries {

    private static <T> Registry<T> get(RegistryAccess registryAccess, ResourceKey<Registry<T>> key) {
        return registryAccess.registryOrThrow(key);
    }

    public static Registry<PatternDefinition> patternRegistry(RegistryAccess registryAccess) {
        return get(registryAccess, PATTERN_REGISTRY_KEY);
    }

    public static final ResourceKey<Registry<PatternDefinition>> PATTERN_REGISTRY_KEY = ResourceKey.createRegistryKey(IronsJewelry.id("patterns"));
//    public static final ResourceKey<Registry<MaterialDefinition>> MATERIAL_REGISTRY_KEY = ResourceKey.createRegistryKey(IronsJewelry.id("irons_jewelry/material"));
//    public static final ResourceKey<Registry<PartDefinition>> PART_REGISTRY_KEY = ResourceKey.createRegistryKey(IronsJewelry.id("irons_jewelry/part"));

    public static final Codec<Holder<PatternDefinition>> PATTERN_REGISTRY_CODEC = RegistryFixedCodec.create(PATTERN_REGISTRY_KEY)/*.xmap(Holder::value, Holder::direct)*/;
//    public static final Codec<Holder<MaterialDefinition>> MATERIAL_REGISTRY_CODEC = RegistryFixedCodec.create(MATERIAL_REGISTRY_KEY);
//    public static final Codec<Holder<PartDefinition>> PART_REGISTRY_CODEC = RegistryFixedCodec.create(PART_REGISTRY_KEY);

    public static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(
                PATTERN_REGISTRY_KEY,
                PatternDefinition.CODEC,
                PatternDefinition.CODEC
        );
//        event.dataPackRegistry(
//                MATERIAL_REGISTRY_KEY,
//                MaterialDefinition.CODEC,
//                MaterialDefinition.CODEC
//        );
//        event.dataPackRegistry(
//                PART_REGISTRY_KEY,
//                PartDefinition.CODEC,
//                PartDefinition.CODEC
//        );
    }
}
