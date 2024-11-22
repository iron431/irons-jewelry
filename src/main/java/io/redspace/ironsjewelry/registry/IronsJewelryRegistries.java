package io.redspace.ironsjewelry.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.Bonus;
import io.redspace.ironsjewelry.core.IAction;
import io.redspace.ironsjewelry.core.IBonusParameterType;
import io.redspace.ironsjewelry.core.data.JewelryType;
import io.redspace.ironsjewelry.core.data.MaterialDefinition;
import io.redspace.ironsjewelry.core.data.PartDefinition;
import io.redspace.ironsjewelry.core.data.PatternDefinition;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class IronsJewelryRegistries {

    public static final Registry<IBonusParameterType<?>> PARAMETER_TYPE_REGISTRY = new RegistryBuilder<>(Keys.PARAMETER_REGISTRY_KEY).defaultKey(IronsJewelry.id("empty")).create();
    public static final Registry<JewelryType> JEWELRY_TYPE_REGISTRY = new RegistryBuilder<>(Keys.JEWELRY_TYPE_KEY).defaultKey(IronsJewelry.id("empty")).create();
    public static final Registry<Bonus> BONUS_REGISTRY = new RegistryBuilder<>(Keys.BONUS_REGISTRY_KEY).defaultKey(IronsJewelry.id("empty")).create();
    public static final Registry<MapCodec<? extends IAction>> ACTION_REGISTRY = new RegistryBuilder<>(Keys.ACTION_REGISTRY_KEY).defaultKey(IronsJewelry.id("empty")).create();

    public static <T> Registry<T> get(RegistryAccess registryAccess, ResourceKey<Registry<T>> key) {
        return registryAccess.registryOrThrow(key);
    }

    public static Registry<PatternDefinition> patternRegistry(RegistryAccess registryAccess) {
        return get(registryAccess, Keys.PATTERN_REGISTRY_KEY);
    }

    public static Registry<MaterialDefinition> materialRegistry(RegistryAccess registryAccess) {
        return get(registryAccess, Keys.MATERIAL_REGISTRY_KEY);
    }

    public static Registry<PartDefinition> partRegistry(RegistryAccess registryAccess) {
        return get(registryAccess, Keys.PART_REGISTRY_KEY);
    }

    public static class Keys {
        public static final ResourceKey<Registry<PatternDefinition>> PATTERN_REGISTRY_KEY = ResourceKey.createRegistryKey(IronsJewelry.id("pattern"));
        public static final ResourceKey<Registry<MaterialDefinition>> MATERIAL_REGISTRY_KEY = ResourceKey.createRegistryKey(IronsJewelry.id("material"));
        public static final ResourceKey<Registry<PartDefinition>> PART_REGISTRY_KEY = ResourceKey.createRegistryKey(IronsJewelry.id("part"));
        public static final ResourceKey<Registry<IBonusParameterType<?>>> PARAMETER_REGISTRY_KEY = ResourceKey.createRegistryKey(IronsJewelry.id("bonus_parameter"));
        public static final ResourceKey<Registry<JewelryType>> JEWELRY_TYPE_KEY = ResourceKey.createRegistryKey(IronsJewelry.id("jewelry_type"));
        public static final ResourceKey<Registry<Bonus>> BONUS_REGISTRY_KEY = ResourceKey.createRegistryKey(IronsJewelry.id("bonus"));
        public static final ResourceKey<Registry<MapCodec<? extends IAction>>> ACTION_REGISTRY_KEY = ResourceKey.createRegistryKey(IronsJewelry.id("action"));
    }

    public static class Codecs {
        public static final Codec<Holder<PatternDefinition>> PATTERN_REGISTRY_CODEC = RegistryFixedCodec.create(Keys.PATTERN_REGISTRY_KEY);
        public static final Codec<Holder<MaterialDefinition>> MATERIAL_REGISTRY_CODEC = RegistryFixedCodec.create(Keys.MATERIAL_REGISTRY_KEY);
        public static final Codec<Holder<PartDefinition>> PART_REGISTRY_CODEC = RegistryFixedCodec.create(Keys.PART_REGISTRY_KEY);
    }

    public static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(
                Keys.PATTERN_REGISTRY_KEY,
                PatternDefinition.CODEC,
                PatternDefinition.CODEC
        );
        event.dataPackRegistry(
                Keys.MATERIAL_REGISTRY_KEY,
                MaterialDefinition.CODEC,
                MaterialDefinition.CODEC
        );
        event.dataPackRegistry(
                Keys.PART_REGISTRY_KEY,
                PartDefinition.CODEC,
                PartDefinition.CODEC
        );
    }

    public static void registerRegistries(NewRegistryEvent event) {
        event.register(BONUS_REGISTRY);
        event.register(PARAMETER_TYPE_REGISTRY);
        event.register(ACTION_REGISTRY);
        event.register(JEWELRY_TYPE_REGISTRY);
    }
}
