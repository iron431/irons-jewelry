package io.redspace.ironsjewelry.datagen;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.actions.ApplyDamageAction;
import io.redspace.ironsjewelry.core.data.*;
import io.redspace.ironsjewelry.core.parameters.ActionParameter;
import io.redspace.ironsjewelry.registry.BonusTypeRegistry;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;
import io.redspace.ironsjewelry.registry.JewelryTypeRegistry;
import io.redspace.ironsjewelry.registry.ParameterTypeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JewelryDataRegistryGenerator {


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
    public static Holder<PartDefinition> HAGGLER_STONE;
    public static Holder<PartDefinition> BAND_BANE_RING;
    public static Holder<PartDefinition> SKULL_BANE_RING;
    public static Holder<PartDefinition> CHAIN_RHINESTONE;
    public static Holder<PartDefinition> GEM_RHINESTONE_A;
    public static Holder<PartDefinition> GEM_RHINESTONE_B;
    public static Holder<PartDefinition> GEM_RHINESTONE_C;

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
    public static Holder<PatternDefinition> HAGGLER_RING;
    public static Holder<PatternDefinition> BANE_RING;
    public static Holder<PatternDefinition> RHINESTONE_AMULET;

    private static void bootstrapParts(BootstrapContext<PartDefinition> bootstrap) {
        BAND_SIMPLE = bootstrap.register(partKey(IronsJewelry.id("band_simple")), new PartDefinition(
                "part.irons_jewelry.band_simple",
                IronsJewelry.id("palettes/gold"),
                List.of("metal"),
                IronsJewelry.id("item/base/gold_ring")
        ));
        GEM_ROUND = bootstrap.register(partKey(IronsJewelry.id("gem_round")), new PartDefinition(
                "part.irons_jewelry.gem_round",
                IronsJewelry.id("palettes/diamond"),
                List.of("gem"),
                IronsJewelry.id("item/base/gem_round")
        ));
        BAND_GEM = bootstrap.register(partKey(IronsJewelry.id("band_gem")), new PartDefinition(
                "part.irons_jewelry.band_gem",
                IronsJewelry.id("palettes/gold"),
                List.of("metal"),
                IronsJewelry.id("item/base/gem_band")
        ));
        BAND_BARBED = bootstrap.register(partKey(IronsJewelry.id("band_barbed")), PartDefinition.simpleMetalPart(IronsJewelry.MODID, "band_barbed"));
        BAND_STALWART = bootstrap.register(partKey(IronsJewelry.id("band_stalwart")), PartDefinition.simpleMetalPart(IronsJewelry.MODID, "band_stalwart"));
        BAND_GEM_SUPERIOR = bootstrap.register(partKey(IronsJewelry.id("band_gem_superior")), PartDefinition.simpleMetalPart(IronsJewelry.MODID, "band_gem_superior"));
        BAND_GEM_THIN = bootstrap.register(partKey(IronsJewelry.id("band_gem_thin")), PartDefinition.simpleMetalPart(IronsJewelry.MODID, "band_gem_thin"));
        BAND_TEARSTONE = bootstrap.register(partKey(IronsJewelry.id("band_tearstone")), PartDefinition.simpleMetalPart(IronsJewelry.MODID, "band_tearstone"));
        CHAIN_AMULET_OF_PROTECTION = bootstrap.register(partKey(IronsJewelry.id("chain_amulet_of_protection")), PartDefinition.simpleMetalPart(IronsJewelry.MODID, "chain_amulet_of_protection"));
        CHAIN_SIMPLE = bootstrap.register(partKey(IronsJewelry.id("chain_simple")), PartDefinition.simpleMetalPart(IronsJewelry.MODID, "chain_simple"));
        CHAIN_SIMPLE_AMULET = bootstrap.register(partKey(IronsJewelry.id("chain_simple_amulet")), PartDefinition.simpleMetalPart(IronsJewelry.MODID, "chain_simple_amulet"));
        GEM_AMULET_OF_PROTECTION = bootstrap.register(partKey(IronsJewelry.id("gem_amulet_of_protection")), PartDefinition.simpleGemPart(IronsJewelry.MODID, "gem_amulet_of_protection"));
        GEM_BAND_BARBED = bootstrap.register(partKey(IronsJewelry.id("gem_band_barbed")), PartDefinition.simpleGemPart(IronsJewelry.MODID, "gem_band_barbed"));
        GEM_POINTY = bootstrap.register(partKey(IronsJewelry.id("gem_pointy")), PartDefinition.simpleGemPart(IronsJewelry.MODID, "gem_pointy"));
        GEM_SIMPLE_AMULET = bootstrap.register(partKey(IronsJewelry.id("gem_simple_amulet")), PartDefinition.simpleGemPart(IronsJewelry.MODID, "gem_simple_amulet"));
        GEM_TEARSTONE = bootstrap.register(partKey(IronsJewelry.id("gem_tearstone")), PartDefinition.simpleGemPart(IronsJewelry.MODID, "gem_tearstone"));
        GEMS_SIDE = bootstrap.register(partKey(IronsJewelry.id("gems_side")), PartDefinition.simpleGemPart(IronsJewelry.MODID, "gems_side"));
        PIGLIN_SIGNET = bootstrap.register(partKey(IronsJewelry.id("piglin_signet")), new PartDefinition(
                "part.irons_jewelry.piglin_signet",
                IronsJewelry.id("palettes/gold"),
                List.of("gold"),
                IronsJewelry.id("item/base/piglin_signet")
        ));
        HAGGLER_STONE = bootstrap.register(partKey(IronsJewelry.id("haggler_stone")), new PartDefinition(
                "part.irons_jewelry.haggler_stone",
                IronsJewelry.id("palettes/diamond"),
                List.of("emerald"),
                IronsJewelry.id("item/base/gem_round_large")
        ));
        BAND_BANE_RING = bootstrap.register(partKey(IronsJewelry.id("band_bane_ring")), PartDefinition.simpleMetalPart(IronsJewelry.MODID, "band_bane_ring"));
        SKULL_BANE_RING = bootstrap.register(partKey(IronsJewelry.id("skull_bane_ring")), new PartDefinition(
                "part.irons_jewelry.skull_bane_ring",
                IronsJewelry.id("palettes/gold"),
                List.of("metal", "gem"),
                IronsJewelry.id("item/base/skull_bane_ring")
        ));
        CHAIN_RHINESTONE = bootstrap.register(partKey(IronsJewelry.id("chain_rhinestone")), PartDefinition.simpleMetalPart(IronsJewelry.MODID, "chain_rhinestone"));
        GEM_RHINESTONE_A = bootstrap.register(partKey(IronsJewelry.id("gem_rhinestone_a")), PartDefinition.simpleGemPart(IronsJewelry.MODID, "gem_rhinestone_a"));
        GEM_RHINESTONE_B = bootstrap.register(partKey(IronsJewelry.id("gem_rhinestone_b")), PartDefinition.simpleGemPart(IronsJewelry.MODID, "gem_rhinestone_b"));
        GEM_RHINESTONE_C = bootstrap.register(partKey(IronsJewelry.id("gem_rhinestone_c")), PartDefinition.simpleGemPart(IronsJewelry.MODID, "gem_rhinestone_c"));

    }

    private static void bootstrapPatterns(BootstrapContext<PatternDefinition> bootstrap) {
        HolderGetter<DamageType> damageGetter = bootstrap.lookup(Registries.DAMAGE_TYPE);
        SIMPLE_BAND = bootstrap.register(patternKey(IronsJewelry.id("simple_band")), new PatternDefinition(
                "pattern.irons_jewelry.simple_band",
                JewelryTypeRegistry.RING.get(),
                List.of(
                        new PartIngredient(BAND_SIMPLE, 4, 0, List.of(new Bonus(BonusTypeRegistry.ATTRIBUTE_BONUS.get(), 1)))
                ),
                Optional.empty(),
                true, 0.75
        ));
        GEMSET_RING = bootstrap.register(patternKey(IronsJewelry.id("gemset_ring")), new PatternDefinition(
                "pattern.irons_jewelry.gemset_ring",
                JewelryTypeRegistry.RING.get(),
                List.of(
                        new PartIngredient(BAND_GEM, 4, 0, List.of()),
                        new PartIngredient(GEM_ROUND, 1, 1, List.of(new Bonus(BonusTypeRegistry.ATTRIBUTE_BONUS.get(), 1)))
                ),
                Optional.of(BAND_GEM),
                true, 1
        ));
        AMULET_OF_PROTECTION = bootstrap.register(patternKey(IronsJewelry.id("amulet_of_protection")), new PatternDefinition(
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
        BARBED_BAND = bootstrap.register(patternKey(IronsJewelry.id("barbed_band")), new PatternDefinition(
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
                2.5
        ));
        IMRPOVED_GEMSET_RING = bootstrap.register(patternKey(IronsJewelry.id("improved_gemset_ring")), new PatternDefinition(
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
        SUPERIOR_GEMSET_RING = bootstrap.register(patternKey(IronsJewelry.id("superior_gemset_ring")), new PatternDefinition(
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
                false, 1.5
        ));
        PIGLIN_SIGNET_RING = bootstrap.register(patternKey(IronsJewelry.id("piglin_signet_ring")), new PatternDefinition(
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
        SHARPSHOOTER_LOOP = bootstrap.register(patternKey(IronsJewelry.id("sharpshooter_loop")), new PatternDefinition(
                "pattern.irons_jewelry.sharpshooter_loop",
                JewelryTypeRegistry.RING.get(),
                List.of(
                        new PartIngredient(BAND_GEM_THIN, 6, 0, List.of(
                        )),
                        new PartIngredient(GEM_POINTY, 2, 1, List.of(
                                new Bonus(BonusTypeRegistry.ON_PROJECTILE_HIT_BONUS.get(), 1, Optional.of(new QualityScalar(100, -20)), Map.of())
                        ))
                ),
                Optional.of(BAND_GEM_THIN),
                false,
                1
        ));
        SIMPLE_AMULET = bootstrap.register(patternKey(IronsJewelry.id("simple_amulet")), new PatternDefinition(
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
        SIMPLE_CHAIN = bootstrap.register(patternKey(IronsJewelry.id("simple_chain")), new PatternDefinition(
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
        STALWART_RING = bootstrap.register(patternKey(IronsJewelry.id("stalwart_ring")), new PatternDefinition(
                "pattern.irons_jewelry.stalwart_ring",
                JewelryTypeRegistry.RING.get(),
                List.of(
                        new PartIngredient(BAND_STALWART, 6, 0, List.of(
                                new Bonus(BonusTypeRegistry.ON_SHIELD_BLOCK_BONUS.get(), 1, Optional.of(new QualityScalar(80)), Map.of())
                        ))
                ),
                Optional.empty(),
                false,
                1
        ));
        TEARSTONE_RING = bootstrap.register(patternKey(IronsJewelry.id("tearstone_ring")), new PatternDefinition(
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
        HAGGLER_RING = bootstrap.register(patternKey(IronsJewelry.id("haggler_ring")), new PatternDefinition(
                "pattern.irons_jewelry.haggler_ring",
                JewelryTypeRegistry.RING.get(),
                List.of(
                        new PartIngredient(BAND_GEM, 4, 0, List.of()),
                        new PartIngredient(HAGGLER_STONE, 4, 1, List.of(
                                new Bonus(BonusTypeRegistry.TRADE_DISCOUNT_BONUS.get(), 1)
                        ))
                ),
                Optional.of(BAND_GEM),
                false, 2
        ));
        BANE_RING = bootstrap.register(patternKey(IronsJewelry.id("bane_ring")), new PatternDefinition(
                "pattern.irons_jewelry.bane_ring",
                JewelryTypeRegistry.RING.get(),
                List.of(
                        new PartIngredient(BAND_BANE_RING, 6, 0, List.of(
                        )),
                        new PartIngredient(SKULL_BANE_RING, 4, 1, List.of(
                                new Bonus(BonusTypeRegistry.ON_ATTACK_BONUS.get(), 0.25, Optional.of(new QualityScalar(160)), Map.of())
                        ))
                ),
                Optional.of(BAND_BANE_RING),
                false,
                4
        ));
        RHINESTONE_AMULET = bootstrap.register(patternKey(IronsJewelry.id("rhinestone_amulet")), new PatternDefinition(
                "pattern.irons_jewelry.rhinestone_amulet",
                JewelryTypeRegistry.NECKLACE.get(),
                List.of(
                        new PartIngredient(CHAIN_RHINESTONE, 6, 0, List.of()),
                        new PartIngredient(GEM_RHINESTONE_A, 1, 1, List.of(
                                new Bonus(BonusTypeRegistry.ATTRIBUTE_BONUS.get(), 0.5)
                        )),
                        new PartIngredient(GEM_RHINESTONE_B, 1, 2, List.of(
                                new Bonus(BonusTypeRegistry.ATTRIBUTE_BONUS.get(), 0.5)
                        )),

                        new PartIngredient(GEM_RHINESTONE_C, 1, 3, List.of(
                                new Bonus(BonusTypeRegistry.ATTRIBUTE_BONUS.get(), 0.5)
                        ))
                ),
                Optional.of(CHAIN_RHINESTONE),
                false, 1.25
        ));
    }

    public static final RegistrySetBuilder builder = new RegistrySetBuilder()
            .add(IronsJewelryRegistries.Keys.PART_REGISTRY_KEY, JewelryDataRegistryGenerator::bootstrapParts)
            .add(IronsJewelryRegistries.Keys.PATTERN_REGISTRY_KEY, JewelryDataRegistryGenerator::bootstrapPatterns);

    private static ResourceKey<PatternDefinition> patternKey(ResourceLocation location) {
        return ResourceKey.create(IronsJewelryRegistries.Keys.PATTERN_REGISTRY_KEY, location);
    }

    private static ResourceKey<PartDefinition> partKey(ResourceLocation location) {
        return ResourceKey.create(IronsJewelryRegistries.Keys.PART_REGISTRY_KEY, location);
    }

    private static ResourceKey<MaterialDefinition> materialKey(ResourceLocation location) {
        return ResourceKey.create(IronsJewelryRegistries.Keys.MATERIAL_REGISTRY_KEY, location);
    }
}
