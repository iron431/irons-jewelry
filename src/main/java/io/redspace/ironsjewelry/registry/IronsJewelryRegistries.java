package io.redspace.ironsjewelry.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.BonusType;
import io.redspace.ironsjewelry.core.IAction;
import io.redspace.ironsjewelry.core.IBonusParameterType;
import io.redspace.ironsjewelry.core.actions.ApplyDamageAction;
import io.redspace.ironsjewelry.core.data.*;
import io.redspace.ironsjewelry.core.parameters.ActionParameter;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class IronsJewelryRegistries {

    public static final Registry<IBonusParameterType<?>> PARAMETER_TYPE_REGISTRY = new RegistryBuilder<>(Keys.PARAMETER_REGISTRY_KEY).defaultKey(IronsJewelry.id("empty")).create();
    public static final Registry<JewelryType> JEWELRY_TYPE_REGISTRY = new RegistryBuilder<>(Keys.JEWELRY_TYPE_KEY).defaultKey(IronsJewelry.id("empty")).create();
    public static final Registry<BonusType> BONUS_TYPE_REGISTRY = new RegistryBuilder<>(Keys.BONUS_TYPE_REGISTRY_KEY).defaultKey(IronsJewelry.id("empty")).create();
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
        public static final ResourceKey<Registry<IBonusParameterType<?>>> PARAMETER_REGISTRY_KEY = ResourceKey.createRegistryKey(IronsJewelry.id("bonus_parameter_type"));
        public static final ResourceKey<Registry<JewelryType>> JEWELRY_TYPE_KEY = ResourceKey.createRegistryKey(IronsJewelry.id("jewelry_type"));
        public static final ResourceKey<Registry<BonusType>> BONUS_TYPE_REGISTRY_KEY = ResourceKey.createRegistryKey(IronsJewelry.id("bonus_type"));
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
        event.register(BONUS_TYPE_REGISTRY);
        event.register(PARAMETER_TYPE_REGISTRY);
        event.register(ACTION_REGISTRY);
        event.register(JEWELRY_TYPE_REGISTRY);
    }

    private static ResourceKey<PatternDefinition> pnk(ResourceLocation location) {
        return ResourceKey.create(Keys.PATTERN_REGISTRY_KEY, location);
    }

    private static ResourceKey<PartDefinition> prk(ResourceLocation location) {
        return ResourceKey.create(Keys.PART_REGISTRY_KEY, location);
    }

    private static ResourceKey<MaterialDefinition> mk(ResourceLocation location) {
        return ResourceKey.create(Keys.MATERIAL_REGISTRY_KEY, location);
    }

    public static Holder<PartDefinition> BAND_BARBED;
    public static Holder<PartDefinition> BAND_STALWART;
    public static Holder<PartDefinition> BAND_SIMPLE;
    public static Holder<PartDefinition> BAND_GEM;
    public static Holder<PartDefinition> BAND_GEM_SUPERIOR;
    public static Holder<PartDefinition> BAND_GEM_THIN;
    public static Holder<PartDefinition> BAND_TEARSTONE;
    public static Holder<PartDefinition> CHAIN_AMULET_OF_PROTECTION;
    public static Holder<PartDefinition> CHAIN_SIMPLE;
    public static Holder<PartDefinition> CHAIN_SIMPLE_AMULET;
    public static Holder<PartDefinition> GEM_ROUND;
    public static Holder<PartDefinition> GEM_AMULET_OF_PROTECTION;
    public static Holder<PartDefinition> GEM_BAND_BARBED;
    public static Holder<PartDefinition> GEM_POINTY;
    public static Holder<PartDefinition> GEM_SIMPLE_AMULET;
    public static Holder<PartDefinition> GEM_TEARSTONE;
    public static Holder<PartDefinition> GEMS_SIDE;
    public static Holder<PartDefinition> PIGLIN_SIGNET;

    public static Holder<PatternDefinition> GEMSET_RING;
    public static Holder<PatternDefinition> SIMPLE_BAND;
    public static Holder<PatternDefinition> AMULET_OF_PROTECTION;
    public static Holder<PatternDefinition> BARBED_BAND;
    public static Holder<PatternDefinition> IMRPOVED_GEMSET_RING;
    public static Holder<PatternDefinition> SUPERIOR_GEMSET_RING;
    public static Holder<PatternDefinition> STALWART_RING;
    public static Holder<PatternDefinition> PIGLIN_SIGNET_RING;
    public static Holder<PatternDefinition> SHARPSHOOTER_LOOP;
    public static Holder<PatternDefinition> SIMPLE_AMULET;
    public static Holder<PatternDefinition> SIMPLE_CHAIN;
    public static Holder<PatternDefinition> TEARSTONE_RING;

    public static final RegistrySetBuilder builder = new RegistrySetBuilder()
            .add(IronsJewelryRegistries.Keys.PART_REGISTRY_KEY, bootstrap -> {
                BAND_SIMPLE = bootstrap.register(prk(IronsJewelry.id("band_simple")), new PartDefinition(
                        "part.irons_jewelry.band_simple",
                        IronsJewelry.id("palettes/gold"),
                        List.of("metal"),
                        IronsJewelry.id("item/base/gold_ring")
                ));
                GEM_ROUND = bootstrap.register(prk(IronsJewelry.id("gem_round")), new PartDefinition(
                        "part.irons_jewelry.gem_round",
                        IronsJewelry.id("palettes/diamond"),
                        List.of("gem"),
                        IronsJewelry.id("item/base/gem_round")
                ));
                BAND_GEM = bootstrap.register(prk(IronsJewelry.id("band_gem")), new PartDefinition(
                        "part.irons_jewelry.band_gem",
                        IronsJewelry.id("palettes/gold"),
                        List.of("metal"),
                        IronsJewelry.id("item/base/gem_band")
                ));
                BAND_BARBED = bootstrap.register(prk(IronsJewelry.id("band_barbed")), PartDefinition.simpleMetalPart(IronsJewelry.MODID, "band_barbed"));
                BAND_STALWART = bootstrap.register(prk(IronsJewelry.id("band_stalwart")), PartDefinition.simpleMetalPart(IronsJewelry.MODID, "band_stalwart"));
                BAND_GEM_SUPERIOR = bootstrap.register(prk(IronsJewelry.id("band_gem_superior")), PartDefinition.simpleMetalPart(IronsJewelry.MODID, "band_gem_superior"));
                BAND_GEM_THIN = bootstrap.register(prk(IronsJewelry.id("band_gem_thin")), PartDefinition.simpleMetalPart(IronsJewelry.MODID, "band_gem_thin"));
                BAND_TEARSTONE = bootstrap.register(prk(IronsJewelry.id("band_tearstone")), PartDefinition.simpleMetalPart(IronsJewelry.MODID, "band_tearstone"));
                CHAIN_AMULET_OF_PROTECTION = bootstrap.register(prk(IronsJewelry.id("chain_amulet_of_protection")), PartDefinition.simpleMetalPart(IronsJewelry.MODID, "chain_amulet_of_protection"));
                CHAIN_SIMPLE = bootstrap.register(prk(IronsJewelry.id("chain_simple")), PartDefinition.simpleMetalPart(IronsJewelry.MODID, "chain_simple"));
                CHAIN_SIMPLE_AMULET = bootstrap.register(prk(IronsJewelry.id("chain_simple_amulet")), PartDefinition.simpleMetalPart(IronsJewelry.MODID, "chain_simple_amulet"));
                GEM_AMULET_OF_PROTECTION = bootstrap.register(prk(IronsJewelry.id("gem_amulet_of_protection")), PartDefinition.simpleGemPart(IronsJewelry.MODID, "gem_amulet_of_protection"));
                GEM_BAND_BARBED = bootstrap.register(prk(IronsJewelry.id("gem_band_barbed")), PartDefinition.simpleGemPart(IronsJewelry.MODID, "gem_band_barbed"));
                GEM_POINTY = bootstrap.register(prk(IronsJewelry.id("gem_pointy")), PartDefinition.simpleGemPart(IronsJewelry.MODID, "gem_pointy"));
                GEM_SIMPLE_AMULET = bootstrap.register(prk(IronsJewelry.id("gem_simple_amulet")), PartDefinition.simpleGemPart(IronsJewelry.MODID, "gem_simple_amulet"));
                GEM_TEARSTONE = bootstrap.register(prk(IronsJewelry.id("gem_tearstone")), PartDefinition.simpleGemPart(IronsJewelry.MODID, "gem_tearstone"));
                GEMS_SIDE = bootstrap.register(prk(IronsJewelry.id("gems_side")), PartDefinition.simpleGemPart(IronsJewelry.MODID, "gems_side"));
                PIGLIN_SIGNET = bootstrap.register(prk(IronsJewelry.id("piglin_signet")), new PartDefinition(
                        "part.irons_jewelry.piglin_signet",
                        IronsJewelry.id("palettes/gold"),
                        List.of("gold"),
                        IronsJewelry.id("item/base/piglin_signet")
                ));
            })
            .add(IronsJewelryRegistries.Keys.PATTERN_REGISTRY_KEY, bootstrap -> {
                HolderGetter<DamageType> damageGetter = bootstrap.lookup(Registries.DAMAGE_TYPE);
                SIMPLE_BAND = bootstrap.register(pnk(IronsJewelry.id("simple_band")), new PatternDefinition(
                        "pattern.irons_jewelry.simple_band",
                        JewelryTypeRegistry.RING.get(),
                        List.of(
                                new PartIngredient(BAND_SIMPLE, 4, 0, List.of(new Bonus(BonusTypeRegistry.ATTRIBUTE_BONUS.get(), 1)))
                        ),
                        Optional.empty(),
                        true, 0.75
                ));
                GEMSET_RING = bootstrap.register(pnk(IronsJewelry.id("gemset_ring")), new PatternDefinition(
                        "pattern.irons_jewelry.gemset_ring",
                        JewelryTypeRegistry.RING.get(),
                        List.of(
                                new PartIngredient(BAND_GEM, 4, 0, List.of()),
                                new PartIngredient(GEM_ROUND, 1, 1, List.of(new Bonus(BonusTypeRegistry.ATTRIBUTE_BONUS.get(), 1)))
                        ),
                        Optional.of(BAND_GEM),
                        true, 1
                ));
                AMULET_OF_PROTECTION = bootstrap.register(pnk(IronsJewelry.id("amulet_of_protection")), new PatternDefinition(
                        "pattern.irons_jewelry.amulet_of_protection",
                        JewelryTypeRegistry.NECKLACE.get(),
                        List.of(
                                new PartIngredient(CHAIN_AMULET_OF_PROTECTION, 6, 0, List.of()),
                                new PartIngredient(GEM_AMULET_OF_PROTECTION, 2, 1, List.of(
                                        new Bonus(BonusTypeRegistry.ON_TAKE_DAMAGE_BONUS.get(), 1, Optional.of(new QualityScalar(200, -20)), Map.of())
                                ))
                        ),
                        Optional.of(CHAIN_AMULET_OF_PROTECTION),
                        false,
                        1.5
                ));
                BARBED_BAND = bootstrap.register(pnk(IronsJewelry.id("barbed_band")), new PatternDefinition(
                        "pattern.irons_jewelry.barbed_band",
                        JewelryTypeRegistry.RING.get(),
                        List.of(
                                new PartIngredient(BAND_BARBED, 8, 0, List.of(
                                        new Bonus(BonusTypeRegistry.ATTRIBUTE_BONUS.get(), 1),
                                        new Bonus(BonusTypeRegistry.ON_ATTACK_BONUS.get(), 1, Optional.of(new QualityScalar(60)),
                                                Map.of(ParameterTypeRegistry.ACTION_PARAMETER.get(), new ActionParameter.ActionRunnable(
                                                        new ApplyDamageAction(
                                                                damageGetter.getOrThrow(DamageTypes.THORNS),
                                                                new QualityScalar(1, 0.5),
                                                                Optional.empty(),
                                                                Optional.empty()
                                                        ), true)))

                                )),
                                new PartIngredient(GEM_BAND_BARBED, 3, 1, List.of())
                        ),
                        Optional.of(GEM_BAND_BARBED),
                        false,
                        3
                ));
                IMRPOVED_GEMSET_RING = bootstrap.register(pnk(IronsJewelry.id("improved_gemset_ring")), new PatternDefinition(
                        "pattern.irons_jewelry.improved_gemset_ring",
                        JewelryTypeRegistry.RING.get(),
                        List.of(
                                new PartIngredient(BAND_GEM, 6, 0, List.of()),
                                new PartIngredient(GEM_POINTY, 1, 1, List.of(
                                        new Bonus(BonusTypeRegistry.ATTRIBUTE_BONUS.get(), 1)
                                ))
                        ),
                        Optional.of(BAND_GEM),
                        false, 1.5
                ));
                SUPERIOR_GEMSET_RING = bootstrap.register(pnk(IronsJewelry.id("superior_gemset_ring")), new PatternDefinition(
                        "pattern.irons_jewelry.superior_gemset_ring",
                        JewelryTypeRegistry.RING.get(),
                        List.of(
                                new PartIngredient(BAND_GEM_SUPERIOR, 6, 0, List.of()),
                                new PartIngredient(GEMS_SIDE, 1, 1, List.of(
                                        new Bonus(BonusTypeRegistry.ATTRIBUTE_BONUS.get(), 0.5)
                                )),
                                new PartIngredient(GEM_POINTY, 2, 2, List.of(
                                        new Bonus(BonusTypeRegistry.ATTRIBUTE_BONUS.get(), 1)
                                ))
                        ),
                        Optional.of(BAND_GEM_SUPERIOR),
                        false, 1.75
                ));
                PIGLIN_SIGNET_RING = bootstrap.register(pnk(IronsJewelry.id("piglin_signet_ring")), new PatternDefinition(
                        "pattern.irons_jewelry.piglin_signet_ring",
                        JewelryTypeRegistry.RING.get(),
                        List.of(
                                new PartIngredient(BAND_GEM, 4, 0, List.of()),
                                new PartIngredient(PIGLIN_SIGNET, 4, 1, List.of(
                                        new Bonus(BonusTypeRegistry.PIGLIN_NEUTRAL_BONUS.get(), 1)
                                ))
                        ),
                        Optional.empty(),
                        false, 2
                ));
                SHARPSHOOTER_LOOP = bootstrap.register(pnk(IronsJewelry.id("sharpshooter_loop")), new PatternDefinition(
                        "pattern.irons_jewelry.sharpshooter_loop",
                        JewelryTypeRegistry.RING.get(),
                        List.of(
                                new PartIngredient(BAND_GEM_THIN, 6, 0, List.of(
                                )),
                                new PartIngredient(GEM_POINTY, 2, 1, List.of(
                                        new Bonus(BonusTypeRegistry.ON_PROJECTILE_HIT_BONUS.get(), 1, Optional.of(new QualityScalar(100, -20)),Map.of())
                                ))
                        ),
                        Optional.of(BAND_GEM_THIN),
                        false,
                        1
                ));
                SIMPLE_AMULET = bootstrap.register(pnk(IronsJewelry.id("simple_amulet")), new PatternDefinition(
                        "pattern.irons_jewelry.simple_amulet",
                        JewelryTypeRegistry.NECKLACE.get(),
                        List.of(
                                new PartIngredient(CHAIN_SIMPLE_AMULET, 4, 0, List.of()),
                                new PartIngredient(GEM_SIMPLE_AMULET, 1, 1, List.of(
                                        new Bonus(BonusTypeRegistry.ATTRIBUTE_BONUS.get(), 1)
                                ))
                        ),
                        Optional.of(CHAIN_SIMPLE_AMULET),
                        true,
                        1
                ));
                SIMPLE_CHAIN = bootstrap.register(pnk(IronsJewelry.id("simple_chain")), new PatternDefinition(
                        "pattern.irons_jewelry.simple_chain",
                        JewelryTypeRegistry.NECKLACE.get(),
                        List.of(
                                new PartIngredient(CHAIN_SIMPLE, 4, 0, List.of(
                                        new Bonus(BonusTypeRegistry.ATTRIBUTE_BONUS.get(), 1)
                                ))
                        ),
                        Optional.empty(),
                        true,
                        1
                ));
                STALWART_RING = bootstrap.register(pnk(IronsJewelry.id("stalwart_ring")), new PatternDefinition(
                        "pattern.irons_jewelry.stalwart_ring",
                        JewelryTypeRegistry.RING.get(),
                        List.of(
                                new PartIngredient(BAND_STALWART, 6, 0, List.of(
                                        new Bonus(BonusTypeRegistry.ON_SHIELD_BLOCK_BONUS.get(), 1, Optional.of(new QualityScalar(80)),Map.of())
                                ))
                        ),
                        Optional.empty(),
                        false,
                        1
                ));
                TEARSTONE_RING = bootstrap.register(pnk(IronsJewelry.id("tearstone_ring")), new PatternDefinition(
                        "pattern.irons_jewelry.tearstone_ring",
                        JewelryTypeRegistry.RING.get(),
                        List.of(
                                new PartIngredient(BAND_TEARSTONE, 4, 0, List.of(
                                )),
                                new PartIngredient(GEM_TEARSTONE, 4, 1, List.of(
                                        new Bonus(BonusTypeRegistry.EFFECT_IMMUNITY_BONUS.get(), 1)
                                ))
                        ),
                        Optional.empty(),
                        false,
                        1.5
                ));
            });
}
