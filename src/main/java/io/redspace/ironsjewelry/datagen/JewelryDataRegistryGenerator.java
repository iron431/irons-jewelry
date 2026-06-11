package io.redspace.ironsjewelry.datagen;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.actions.ApplyDamageAction;
import io.redspace.ironsjewelry.core.actions.ApplyEffectAction;
import io.redspace.ironsjewelry.core.actions.ApplyFreezeAction;
import io.redspace.ironsjewelry.core.actions.CreateItemsAction;
import io.redspace.ironsjewelry.core.actions.ExplodeAction;
import io.redspace.ironsjewelry.core.actions.HealAction;
import io.redspace.ironsjewelry.core.actions.IgniteAction;
import io.redspace.ironsjewelry.core.actions.KnockbackAction;
import io.redspace.ironsjewelry.core.data.AttributeInstance;
import io.redspace.ironsjewelry.core.data.Bonus;
import io.redspace.ironsjewelry.core.data.MaterialDefinition;
import io.redspace.ironsjewelry.core.data.PartDefinition;
import io.redspace.ironsjewelry.core.data.PartIngredient;
import io.redspace.ironsjewelry.core.data.PatternDefinition;
import io.redspace.ironsjewelry.core.data.QualityScalar;
import io.redspace.ironsjewelry.core.parameters.ActionParameter;
import io.redspace.ironsjewelry.registry.BonusTypeRegistry;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;
import io.redspace.ironsjewelry.registry.JewelryTypeRegistry;
import io.redspace.ironsjewelry.registry.ParameterTypeRegistry;
import io.redspace.ironsjewelry.registry.SoundRegistry;
import io.redspace.ironsjewelry.utils.JewelryModTags;
import io.redspace.ironslib.registry.IronsLibRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;

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

    public static Holder<MaterialDefinition> ALLTHEMODIUM;
    public static Holder<MaterialDefinition> AMETHYST;
    public static Holder<MaterialDefinition> BRASS;
    public static Holder<MaterialDefinition> BRONZE;
    public static Holder<MaterialDefinition> COPPER;
    public static Holder<MaterialDefinition> DIAMOND;
    public static Holder<MaterialDefinition> EMERALD;
    public static Holder<MaterialDefinition> EXAMPLE;
    public static Holder<MaterialDefinition> GARNET;
    public static Holder<MaterialDefinition> GOLD;
    public static Holder<MaterialDefinition> IRON;
    public static Holder<MaterialDefinition> LAPIS;
    public static Holder<MaterialDefinition> MOONSTONE;
    public static Holder<MaterialDefinition> NETHERITE;
    public static Holder<MaterialDefinition> ONYX;
    public static Holder<MaterialDefinition> PERIDOT;
    public static Holder<MaterialDefinition> PLATINUM;
    public static Holder<MaterialDefinition> RUBY;
    public static Holder<MaterialDefinition> SAPPHIRE;
    public static Holder<MaterialDefinition> SILVER;
    public static Holder<MaterialDefinition> TOPAZ;
    public static Holder<MaterialDefinition> UNOBTAINIUM;
    public static Holder<MaterialDefinition> VIBRANIUM;

    private static void bootstrapParts(BootstrapContext<PartDefinition> bootstrap) {
        HolderGetter<MaterialDefinition> materialGetter = bootstrap.lookup(IronsJewelryRegistries.Keys.MATERIAL_REGISTRY_KEY);
        HolderSet.Named<MaterialDefinition> metals = materialGetter.getOrThrow(JewelryModTags.METAL);
        HolderSet.Named<MaterialDefinition> gems = materialGetter.getOrThrow(JewelryModTags.GEM);
        HolderSet.Named<MaterialDefinition> golds = materialGetter.getOrThrow(JewelryModTags.GOLD);
        HolderSet.Named<MaterialDefinition> emeralds = materialGetter.getOrThrow(JewelryModTags.EMERALD);
        HolderSet.Named<MaterialDefinition> metalsOrGems = materialGetter.getOrThrow(JewelryModTags.METAL_OR_GEM);
        BAND_SIMPLE = bootstrap.register(partKey(IronsJewelry.id("band_simple")), new PartDefinition(
                "part.irons_jewelry.band_simple",
                IronsJewelry.id("palettes/gold"),
                Optional.of(metals),
                IronsJewelry.id("item/base/gold_ring")
        ));
        GEM_ROUND = bootstrap.register(partKey(IronsJewelry.id("gem_round")), new PartDefinition(
                "part.irons_jewelry.gem_round",
                IronsJewelry.id("palettes/diamond"),
                Optional.of(gems),
                IronsJewelry.id("item/base/gem_round")
        ));
        BAND_GEM = bootstrap.register(partKey(IronsJewelry.id("band_gem")), new PartDefinition(
                "part.irons_jewelry.band_gem",
                IronsJewelry.id("palettes/gold"),
                Optional.of(metals),
                IronsJewelry.id("item/base/gem_band")
        ));
        BAND_BARBED = bootstrap.register(partKey(IronsJewelry.id("band_barbed")), PartDefinition.simpleMetalPart(IronsJewelry.MODID, "band_barbed", metals));
        BAND_STALWART = bootstrap.register(partKey(IronsJewelry.id("band_stalwart")), PartDefinition.simpleMetalPart(IronsJewelry.MODID, "band_stalwart", metals));
        BAND_GEM_SUPERIOR = bootstrap.register(partKey(IronsJewelry.id("band_gem_superior")), PartDefinition.simpleMetalPart(IronsJewelry.MODID, "band_gem_superior", metals));
        BAND_GEM_THIN = bootstrap.register(partKey(IronsJewelry.id("band_gem_thin")), PartDefinition.simpleMetalPart(IronsJewelry.MODID, "band_gem_thin", metals));
        BAND_TEARSTONE = bootstrap.register(partKey(IronsJewelry.id("band_tearstone")), PartDefinition.simpleMetalPart(IronsJewelry.MODID, "band_tearstone", metals));
        CHAIN_AMULET_OF_PROTECTION = bootstrap.register(partKey(IronsJewelry.id("chain_amulet_of_protection")), PartDefinition.simpleMetalPart(IronsJewelry.MODID, "chain_amulet_of_protection", metals));
        CHAIN_SIMPLE = bootstrap.register(partKey(IronsJewelry.id("chain_simple")), PartDefinition.simpleMetalPart(IronsJewelry.MODID, "chain_simple", metals));
        CHAIN_SIMPLE_AMULET = bootstrap.register(partKey(IronsJewelry.id("chain_simple_amulet")), PartDefinition.simpleMetalPart(IronsJewelry.MODID, "chain_simple_amulet", metals));
        GEM_AMULET_OF_PROTECTION = bootstrap.register(partKey(IronsJewelry.id("gem_amulet_of_protection")), PartDefinition.simpleGemPart(IronsJewelry.MODID, "gem_amulet_of_protection", gems));
        GEM_BAND_BARBED = bootstrap.register(partKey(IronsJewelry.id("gem_band_barbed")), PartDefinition.simpleGemPart(IronsJewelry.MODID, "gem_band_barbed", gems));
        GEM_POINTY = bootstrap.register(partKey(IronsJewelry.id("gem_pointy")), PartDefinition.simpleGemPart(IronsJewelry.MODID, "gem_pointy", gems));
        GEM_SIMPLE_AMULET = bootstrap.register(partKey(IronsJewelry.id("gem_simple_amulet")), PartDefinition.simpleGemPart(IronsJewelry.MODID, "gem_simple_amulet", gems));
        GEM_TEARSTONE = bootstrap.register(partKey(IronsJewelry.id("gem_tearstone")), PartDefinition.simpleGemPart(IronsJewelry.MODID, "gem_tearstone", gems));
        GEMS_SIDE = bootstrap.register(partKey(IronsJewelry.id("gems_side")), PartDefinition.simpleGemPart(IronsJewelry.MODID, "gems_side", gems));
        PIGLIN_SIGNET = bootstrap.register(partKey(IronsJewelry.id("piglin_signet")), new PartDefinition(
                "part.irons_jewelry.piglin_signet",
                IronsJewelry.id("palettes/gold"),
                Optional.of(golds),
                IronsJewelry.id("item/base/piglin_signet")
        ));
        HAGGLER_STONE = bootstrap.register(partKey(IronsJewelry.id("haggler_stone")), new PartDefinition(
                "part.irons_jewelry.haggler_stone",
                IronsJewelry.id("palettes/diamond"),
                Optional.of(emeralds),
                IronsJewelry.id("item/base/gem_round_large")
        ));
        BAND_BANE_RING = bootstrap.register(partKey(IronsJewelry.id("band_bane_ring")), PartDefinition.simpleMetalPart(IronsJewelry.MODID, "band_bane_ring", metals));
        SKULL_BANE_RING = bootstrap.register(partKey(IronsJewelry.id("skull_bane_ring")), new PartDefinition(
                "part.irons_jewelry.skull_bane_ring",
                IronsJewelry.id("palettes/gold"),
                Optional.of(metalsOrGems),
                IronsJewelry.id("item/base/skull_bane_ring")
        ));
        CHAIN_RHINESTONE = bootstrap.register(partKey(IronsJewelry.id("chain_rhinestone")), PartDefinition.simpleMetalPart(IronsJewelry.MODID, "chain_rhinestone", metals));
        GEM_RHINESTONE_A = bootstrap.register(partKey(IronsJewelry.id("gem_rhinestone_a")), PartDefinition.simpleGemPart(IronsJewelry.MODID, "gem_rhinestone_a", gems));
        GEM_RHINESTONE_B = bootstrap.register(partKey(IronsJewelry.id("gem_rhinestone_b")), PartDefinition.simpleGemPart(IronsJewelry.MODID, "gem_rhinestone_b", gems));
        GEM_RHINESTONE_C = bootstrap.register(partKey(IronsJewelry.id("gem_rhinestone_c")), PartDefinition.simpleGemPart(IronsJewelry.MODID, "gem_rhinestone_c", gems));

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

    private static void bootstrapMaterials(BootstrapContext<MaterialDefinition> bootstrap) {
        HolderGetter<DamageType> damageGetter = bootstrap.lookup(Registries.DAMAGE_TYPE);
        Holder<SoundEvent> windChargeBurst = SoundEvents.WIND_CHARGE_BURST;

        ALLTHEMODIUM = bootstrap.register(materialKey(IronsJewelry.id("allthemodium")), new MaterialDefinition(
                "material.irons_jewelry.allthemodium",
                Ingredient.of(itemTag("c:ingots/allthemodium")),
                IronsJewelry.id("palettes/allthemodium"),
                Map.of(
                        ParameterTypeRegistry.ATTRIBUTE_PARAMETER.get(), new AttributeInstance(Attributes.ARMOR_TOUGHNESS, 2, AttributeModifier.Operation.ADD_VALUE),
                        ParameterTypeRegistry.POSITIVE_EFFECT_PARAMETER.get(), MobEffects.DAMAGE_RESISTANCE,
                        ParameterTypeRegistry.NEGATIVE_EFFECT_PARAMETER.get(), MobEffects.MOVEMENT_SLOWDOWN,
                        ParameterTypeRegistry.ACTION_PARAMETER.get(), new ActionParameter.ActionRunnable(
                                new ApplyEffectAction(new QualityScalar(120), new QualityScalar(0, 1, 0, Optional.of(2.0)), MobEffects.DAMAGE_BOOST), true)
                ),
                3.0
        ));
        AMETHYST = bootstrap.register(materialKey(IronsJewelry.id("amethyst")), new MaterialDefinition(
                "material.irons_jewelry.amethyst",
                Ingredient.of(itemTag("c:gems/amethyst")),
                IronsJewelry.id("palettes/amethyst"),
                Map.of(
                        ParameterTypeRegistry.ATTRIBUTE_PARAMETER.get(), new AttributeInstance(IronsLibRegistries.AttributeRegistry.ARMOR_PIERCE, 1, AttributeModifier.Operation.ADD_VALUE),
                        ParameterTypeRegistry.POSITIVE_EFFECT_PARAMETER.get(), MobEffects.REGENERATION,
                        ParameterTypeRegistry.NEGATIVE_EFFECT_PARAMETER.get(), MobEffects.POISON,
                        ParameterTypeRegistry.ACTION_PARAMETER.get(), new ActionParameter.ActionRunnable(
                                new ApplyDamageAction(damageGetter.getOrThrow(DamageTypes.THORNS), new QualityScalar(3.0, 1), Optional.empty(), Optional.empty()), false)
                ),
                1
        ));
        BRASS = bootstrap.register(materialKey(IronsJewelry.id("brass")), new MaterialDefinition(
                "material.irons_jewelry.brass",
                Ingredient.of(itemTag("c:ingots/brass")),
                IronsJewelry.id("palettes/brass"),
                Map.of(
                        ParameterTypeRegistry.ATTRIBUTE_PARAMETER.get(), new AttributeInstance(Attributes.KNOCKBACK_RESISTANCE, 0.1, AttributeModifier.Operation.ADD_VALUE)
                ),
                0.75
        ));
        BRONZE = bootstrap.register(materialKey(IronsJewelry.id("bronze")), new MaterialDefinition(
                "material.irons_jewelry.bronze",
                Ingredient.of(itemTag("c:ingots/bronze")),
                IronsJewelry.id("palettes/bronze"),
                Map.of(
                        ParameterTypeRegistry.ATTRIBUTE_PARAMETER.get(), new AttributeInstance(NeoForgeMod.SWIM_SPEED, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                ),
                1.0
        ));
        COPPER = bootstrap.register(materialKey(IronsJewelry.id("copper")), new MaterialDefinition(
                "material.irons_jewelry.copper",
                Ingredient.of(itemTag("c:ingots/copper")),
                IronsJewelry.id("palettes/copper"),
                Map.of(
                        ParameterTypeRegistry.ATTRIBUTE_PARAMETER.get(), new AttributeInstance(Attributes.ATTACK_SPEED, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                        ParameterTypeRegistry.POSITIVE_EFFECT_PARAMETER.get(), MobEffects.DIG_SPEED,
                        ParameterTypeRegistry.NEGATIVE_EFFECT_PARAMETER.get(), MobEffects.POISON,
                        ParameterTypeRegistry.ACTION_PARAMETER.get(), new ActionParameter.ActionRunnable(
                                new ApplyDamageAction(damageGetter.getOrThrow(DamageTypes.LIGHTNING_BOLT), new QualityScalar(3.0, 1), Optional.empty(), Optional.empty()), false)
                ),
                0.5
        ));
        DIAMOND = bootstrap.register(materialKey(IronsJewelry.id("diamond")), new MaterialDefinition(
                "material.irons_jewelry.diamond",
                Ingredient.of(itemTag("c:gems/diamond")),
                IronsJewelry.id("palettes/diamond"),
                Map.of(
                        ParameterTypeRegistry.ATTRIBUTE_PARAMETER.get(), new AttributeInstance(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE),
                        ParameterTypeRegistry.POSITIVE_EFFECT_PARAMETER.get(), MobEffects.DAMAGE_RESISTANCE,
                        ParameterTypeRegistry.NEGATIVE_EFFECT_PARAMETER.get(), MobEffects.WEAKNESS,
                        ParameterTypeRegistry.ACTION_PARAMETER.get(), new ActionParameter.ActionRunnable(
                                new ApplyDamageAction(damageGetter.getOrThrow(DamageTypes.MAGIC), new QualityScalar(1.0, 1), Optional.empty(), Optional.empty()), false)
                ),
                2
        ));
        EMERALD = bootstrap.register(materialKey(IronsJewelry.id("emerald")), new MaterialDefinition(
                "material.irons_jewelry.emerald",
                Ingredient.of(itemTag("c:gems/emerald")),
                IronsJewelry.id("palettes/emerald"),
                Map.of(
                        ParameterTypeRegistry.ATTRIBUTE_PARAMETER.get(), new AttributeInstance(Attributes.MOVEMENT_SPEED, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                        ParameterTypeRegistry.POSITIVE_EFFECT_PARAMETER.get(), MobEffects.MOVEMENT_SPEED,
                        ParameterTypeRegistry.NEGATIVE_EFFECT_PARAMETER.get(), MobEffects.INFESTED,
                        ParameterTypeRegistry.ACTION_PARAMETER.get(), new ActionParameter.ActionRunnable(
                                new CreateItemsAction(
                                        Optional.of(new QualityScalar(0.05, 0.05, 0.01, Optional.of(1.0))),
                                        Items.EMERALD.builtInRegistryHolder(),
                                        Optional.of(SoundRegistry.EMERALDS),
                                        new QualityScalar(1),
                                        new QualityScalar(4, 2)
                                ), false)
                ),
                1.5
        ));
        EXAMPLE = bootstrap.register(materialKey(IronsJewelry.id("example")), new MaterialDefinition(
                "material.irons_jewelry.example",
                Optional.empty(),
                IronsJewelry.id("palettes/silver"),
                Map.of(),
                1.0
        ));
        GARNET = bootstrap.register(materialKey(IronsJewelry.id("garnet")), new MaterialDefinition(
                "material.irons_jewelry.garnet",
                Ingredient.of(itemTag("c:gems/garnet")),
                IronsJewelry.id("palettes/garnet"),
                Map.of(
                        ParameterTypeRegistry.ATTRIBUTE_PARAMETER.get(), new AttributeInstance(Attributes.ATTACK_DAMAGE, 0.04, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                        ParameterTypeRegistry.NEGATIVE_EFFECT_PARAMETER.get(), MobEffects.WITHER,
                        ParameterTypeRegistry.ACTION_PARAMETER.get(), new ActionParameter.ActionRunnable(
                                new ApplyEffectAction(new QualityScalar(80), new QualityScalar(0, 1, 0, Optional.of(2.0)), MobEffects.DAMAGE_RESISTANCE), true)
                ),
                2
        ));
        GOLD = bootstrap.register(materialKey(IronsJewelry.id("gold")), new MaterialDefinition(
                "material.irons_jewelry.gold",
                Ingredient.of(itemTag("c:ingots/gold")),
                IronsJewelry.id("palettes/gold"),
                Map.of(
                        ParameterTypeRegistry.ATTRIBUTE_PARAMETER.get(), new AttributeInstance(IronsLibRegistries.AttributeRegistry.MINING_SPEED, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                        ParameterTypeRegistry.POSITIVE_EFFECT_PARAMETER.get(), MobEffects.FIRE_RESISTANCE,
                        ParameterTypeRegistry.NEGATIVE_EFFECT_PARAMETER.get(), MobEffects.POISON,
                        ParameterTypeRegistry.ACTION_PARAMETER.get(), new ActionParameter.ActionRunnable(
                                new IgniteAction(new QualityScalar(40.0)), false)
                ),
                1.5
        ));
        IRON = bootstrap.register(materialKey(IronsJewelry.id("iron")), new MaterialDefinition(
                "material.irons_jewelry.iron",
                Ingredient.of(itemTag("c:ingots/iron")),
                IronsJewelry.id("palettes/iron"),
                Map.of(
                        ParameterTypeRegistry.ATTRIBUTE_PARAMETER.get(), new AttributeInstance(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE),
                        ParameterTypeRegistry.POSITIVE_EFFECT_PARAMETER.get(), MobEffects.DAMAGE_RESISTANCE,
                        ParameterTypeRegistry.NEGATIVE_EFFECT_PARAMETER.get(), MobEffects.MOVEMENT_SLOWDOWN,
                        ParameterTypeRegistry.ACTION_PARAMETER.get(), new ActionParameter.ActionRunnable(
                                new KnockbackAction(new QualityScalar(2.0, 1.0)), false)
                ),
                0.75
        ));
        LAPIS = bootstrap.register(materialKey(IronsJewelry.id("lapis")), new MaterialDefinition(
                "material.irons_jewelry.lapis",
                Ingredient.of(itemTag("c:gems/lapis")),
                IronsJewelry.id("palettes/lapis"),
                Map.of(
                        ParameterTypeRegistry.ATTRIBUTE_PARAMETER.get(), new AttributeInstance(IronsLibRegistries.AttributeRegistry.EXPERIENCE_GAINED, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                        ParameterTypeRegistry.NEGATIVE_EFFECT_PARAMETER.get(), MobEffects.GLOWING,
                        ParameterTypeRegistry.ACTION_PARAMETER.get(), new ActionParameter.ActionRunnable(
                                new ApplyEffectAction(new QualityScalar(80), new QualityScalar(0, 1, 0, Optional.of(2.0)), MobEffects.MOVEMENT_SLOWDOWN), false)
                ),
                1
        ));
        MOONSTONE = bootstrap.register(materialKey(IronsJewelry.id("moonstone")), new MaterialDefinition(
                "material.irons_jewelry.moonstone",
                Ingredient.of(itemTag("c:gems/moonstone")),
                IronsJewelry.id("palettes/moonstone"),
                Map.of(
                        ParameterTypeRegistry.ATTRIBUTE_PARAMETER.get(), new AttributeInstance(IronsLibRegistries.AttributeRegistry.ARROW_DAMAGE, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                        ParameterTypeRegistry.NEGATIVE_EFFECT_PARAMETER.get(), MobEffects.WEAVING,
                        ParameterTypeRegistry.POSITIVE_EFFECT_PARAMETER.get(), MobEffects.INVISIBILITY,
                        ParameterTypeRegistry.ACTION_PARAMETER.get(), new ActionParameter.ActionRunnable(
                                new ExplodeAction(false, false, "action.irons_jewelry.explode.wind_burst",
                                        Optional.empty(), Optional.of(new QualityScalar(2.5)), Optional.empty(), Vec3.ZERO,
                                        new QualityScalar(2, 1), false, Level.ExplosionInteraction.NONE,
                                        ParticleTypes.GUST_EMITTER_SMALL, ParticleTypes.GUST_EMITTER_LARGE, windChargeBurst), false)
                ),
                2
        ));
        NETHERITE = bootstrap.register(materialKey(IronsJewelry.id("netherite")), new MaterialDefinition(
                "material.irons_jewelry.netherite",
                Ingredient.of(itemTag("c:ingots/netherite")),
                IronsJewelry.id("palettes/netherite"),
                Map.of(
                        ParameterTypeRegistry.ATTRIBUTE_PARAMETER.get(), new AttributeInstance(Attributes.ATTACK_DAMAGE, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                        ParameterTypeRegistry.POSITIVE_EFFECT_PARAMETER.get(), MobEffects.DAMAGE_BOOST,
                        ParameterTypeRegistry.NEGATIVE_EFFECT_PARAMETER.get(), MobEffects.WITHER,
                        ParameterTypeRegistry.ACTION_PARAMETER.get(), new ActionParameter.ActionRunnable(
                                new ApplyEffectAction(new QualityScalar(60), new QualityScalar(0, 1, 0, Optional.of(2.0)), MobEffects.DAMAGE_BOOST), true)
                ),
                2.5
        ));
        ONYX = bootstrap.register(materialKey(IronsJewelry.id("onyx")), new MaterialDefinition(
                "material.irons_jewelry.onyx",
                Ingredient.of(itemTag("c:gems/onyx")),
                IronsJewelry.id("palettes/onyx"),
                Map.of(
                        ParameterTypeRegistry.ATTRIBUTE_PARAMETER.get(), new AttributeInstance(IronsLibRegistries.AttributeRegistry.CRIT_DAMAGE, 0.15, AttributeModifier.Operation.ADD_VALUE),
                        ParameterTypeRegistry.NEGATIVE_EFFECT_PARAMETER.get(), MobEffects.BLINDNESS,
                        ParameterTypeRegistry.ACTION_PARAMETER.get(), new ActionParameter.ActionRunnable(
                                new HealAction(new QualityScalar(4.0, 2.0)), true)
                ),
                2
        ));
        PERIDOT = bootstrap.register(materialKey(IronsJewelry.id("peridot")), new MaterialDefinition(
                "material.irons_jewelry.peridot",
                Ingredient.of(itemTag("c:gems/peridot")),
                IronsJewelry.id("palettes/peridot"),
                Map.of(
                        ParameterTypeRegistry.ATTRIBUTE_PARAMETER.get(), new AttributeInstance(IronsLibRegistries.AttributeRegistry.MINING_SPEED, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                        ParameterTypeRegistry.NEGATIVE_EFFECT_PARAMETER.get(), MobEffects.POISON,
                        ParameterTypeRegistry.POSITIVE_EFFECT_PARAMETER.get(), MobEffects.DIG_SPEED,
                        ParameterTypeRegistry.ACTION_PARAMETER.get(), new ActionParameter.ActionRunnable(
                                new KnockbackAction(new QualityScalar(-2.0, -1.0)), false)
                ),
                2
        ));
        PLATINUM = bootstrap.register(materialKey(IronsJewelry.id("platinum")), new MaterialDefinition(
                "material.irons_jewelry.platinum",
                Ingredient.of(itemTag("c:ingots/platinum")),
                IronsJewelry.id("palettes/platinum"),
                Map.of(
                        ParameterTypeRegistry.ATTRIBUTE_PARAMETER.get(), new AttributeInstance(IronsLibRegistries.AttributeRegistry.ARMOR_PIERCE, 1, AttributeModifier.Operation.ADD_VALUE),
                        ParameterTypeRegistry.POSITIVE_EFFECT_PARAMETER.get(), MobEffects.DAMAGE_RESISTANCE,
                        ParameterTypeRegistry.NEGATIVE_EFFECT_PARAMETER.get(), MobEffects.WEAKNESS,
                        ParameterTypeRegistry.ACTION_PARAMETER.get(), new ActionParameter.ActionRunnable(
                                new KnockbackAction(new QualityScalar(2.0, 1.0)), false)
                ),
                2.0
        ));
        RUBY = bootstrap.register(materialKey(IronsJewelry.id("ruby")), new MaterialDefinition(
                "material.irons_jewelry.ruby",
                Ingredient.of(itemTag("c:gems/ruby")),
                IronsJewelry.id("palettes/ruby"),
                Map.of(
                        ParameterTypeRegistry.ATTRIBUTE_PARAMETER.get(), new AttributeInstance(Attributes.MAX_HEALTH, 2, AttributeModifier.Operation.ADD_VALUE),
                        ParameterTypeRegistry.NEGATIVE_EFFECT_PARAMETER.get(), MobEffects.HUNGER,
                        ParameterTypeRegistry.ACTION_PARAMETER.get(), new ActionParameter.ActionRunnable(
                                new IgniteAction(new QualityScalar(40.0)), false)
                ),
                2
        ));
        SAPPHIRE = bootstrap.register(materialKey(IronsJewelry.id("sapphire")), new MaterialDefinition(
                "material.irons_jewelry.sapphire",
                Ingredient.of(itemTag("c:gems/sapphire")),
                IronsJewelry.id("palettes/sapphire"),
                Map.of(
                        ParameterTypeRegistry.ATTRIBUTE_PARAMETER.get(), new AttributeInstance(IronsLibRegistries.AttributeRegistry.DODGE_CHANCE, 0.03, AttributeModifier.Operation.ADD_VALUE),
                        ParameterTypeRegistry.NEGATIVE_EFFECT_PARAMETER.get(), MobEffects.MOVEMENT_SLOWDOWN,
                        ParameterTypeRegistry.ACTION_PARAMETER.get(), new ActionParameter.ActionRunnable(
                                new ApplyFreezeAction(new QualityScalar(40.0), false), false)
                ),
                2
        ));
        SILVER = bootstrap.register(materialKey(IronsJewelry.id("silver")), new MaterialDefinition(
                "material.irons_jewelry.silver",
                Ingredient.of(itemTag("c:ingots/silver")),
                IronsJewelry.id("palettes/silver"),
                Map.of(
                        ParameterTypeRegistry.ATTRIBUTE_PARAMETER.get(), new AttributeInstance(Attributes.MOVEMENT_SPEED, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                        ParameterTypeRegistry.POSITIVE_EFFECT_PARAMETER.get(), MobEffects.MOVEMENT_SPEED,
                        ParameterTypeRegistry.NEGATIVE_EFFECT_PARAMETER.get(), MobEffects.BAD_OMEN,
                        ParameterTypeRegistry.ACTION_PARAMETER.get(), new ActionParameter.ActionRunnable(
                                new ExplodeAction(false, false, "action.irons_jewelry.explode.wind_burst",
                                        Optional.empty(), Optional.of(new QualityScalar(2.5)), Optional.empty(), Vec3.ZERO,
                                        new QualityScalar(2, 1.5), false, Level.ExplosionInteraction.NONE,
                                        ParticleTypes.GUST_EMITTER_SMALL, ParticleTypes.GUST_EMITTER_LARGE, windChargeBurst), true)
                ),
                1
        ));
        TOPAZ = bootstrap.register(materialKey(IronsJewelry.id("topaz")), new MaterialDefinition(
                "material.irons_jewelry.topaz",
                Ingredient.of(itemTag("c:gems/topaz")),
                IronsJewelry.id("palettes/topaz"),
                Map.of(
                        ParameterTypeRegistry.ATTRIBUTE_PARAMETER.get(), new AttributeInstance(Attributes.ARMOR_TOUGHNESS, 2, AttributeModifier.Operation.ADD_VALUE),
                        ParameterTypeRegistry.NEGATIVE_EFFECT_PARAMETER.get(), MobEffects.LEVITATION,
                        ParameterTypeRegistry.ACTION_PARAMETER.get(), new ActionParameter.ActionRunnable(
                                new KnockbackAction(new QualityScalar(-2.0, -1.0)), true)
                ),
                2
        ));
        UNOBTAINIUM = bootstrap.register(materialKey(IronsJewelry.id("unobtainium")), new MaterialDefinition(
                "material.irons_jewelry.unobtainium",
                Ingredient.of(itemTag("c:ingots/unobtainium")),
                IronsJewelry.id("palettes/unobtainium"),
                Map.of(
                        ParameterTypeRegistry.ATTRIBUTE_PARAMETER.get(), new AttributeInstance(Attributes.ARMOR_TOUGHNESS, 2, AttributeModifier.Operation.ADD_VALUE),
                        ParameterTypeRegistry.POSITIVE_EFFECT_PARAMETER.get(), MobEffects.DAMAGE_RESISTANCE,
                        ParameterTypeRegistry.NEGATIVE_EFFECT_PARAMETER.get(), MobEffects.MOVEMENT_SLOWDOWN,
                        ParameterTypeRegistry.ACTION_PARAMETER.get(), new ActionParameter.ActionRunnable(
                                new ApplyEffectAction(new QualityScalar(120), new QualityScalar(0, 1, 0, Optional.of(2.0)), MobEffects.DAMAGE_BOOST), true)
                ),
                4
        ));
        VIBRANIUM = bootstrap.register(materialKey(IronsJewelry.id("vibranium")), new MaterialDefinition(
                "material.irons_jewelry.vibranium",
                Ingredient.of(itemTag("c:ingots/vibranium")),
                IronsJewelry.id("palettes/vibranium"),
                Map.of(
                        ParameterTypeRegistry.ATTRIBUTE_PARAMETER.get(), new AttributeInstance(Attributes.ARMOR_TOUGHNESS, 2, AttributeModifier.Operation.ADD_VALUE),
                        ParameterTypeRegistry.POSITIVE_EFFECT_PARAMETER.get(), MobEffects.DAMAGE_RESISTANCE,
                        ParameterTypeRegistry.NEGATIVE_EFFECT_PARAMETER.get(), MobEffects.MOVEMENT_SLOWDOWN,
                        ParameterTypeRegistry.ACTION_PARAMETER.get(), new ActionParameter.ActionRunnable(
                                new ApplyEffectAction(new QualityScalar(120), new QualityScalar(0, 1, 0, Optional.of(2.0)), MobEffects.DAMAGE_BOOST), true)
                ),
                3.5
        ));
    }

    public static final RegistrySetBuilder builder = new RegistrySetBuilder()
            .add(IronsJewelryRegistries.Keys.PART_REGISTRY_KEY, JewelryDataRegistryGenerator::bootstrapParts)
            .add(IronsJewelryRegistries.Keys.PATTERN_REGISTRY_KEY, JewelryDataRegistryGenerator::bootstrapPatterns)
            .add(IronsJewelryRegistries.Keys.MATERIAL_REGISTRY_KEY, JewelryDataRegistryGenerator::bootstrapMaterials);

    private static ResourceKey<PatternDefinition> patternKey(ResourceLocation location) {
        return ResourceKey.create(IronsJewelryRegistries.Keys.PATTERN_REGISTRY_KEY, location);
    }

    private static ResourceKey<PartDefinition> partKey(ResourceLocation location) {
        return ResourceKey.create(IronsJewelryRegistries.Keys.PART_REGISTRY_KEY, location);
    }

    private static ResourceKey<MaterialDefinition> materialKey(ResourceLocation location) {
        return ResourceKey.create(IronsJewelryRegistries.Keys.MATERIAL_REGISTRY_KEY, location);
    }

    private static TagKey<Item> itemTag(String tag) {
        return TagKey.create(Registries.ITEM, ResourceLocation.parse(tag));
    }
}
