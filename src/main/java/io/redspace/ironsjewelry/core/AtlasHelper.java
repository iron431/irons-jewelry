package io.redspace.ironsjewelry.core;

import io.redspace.ironsjewelry.IronsJewelry;
import net.minecraft.client.renderer.texture.atlas.sources.PalettedPermutations;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AtlasHelper {
    private static final List<PalettedPermutations> sources = new ArrayList<>();

    static {
        //TODO: incorperate this into datagen, automate as much as possible
        /*
        Metal and Gems textures are split because it helps to actually do the art in color instead of black and white
        Thus, they must be separated in the atlas by palette key
        Metals:
         */
        var metalPaletteKey = IronsJewelry.id("palettes/gold");
        var metalTextures = List.of(
                IronsJewelry.id("item/base/gem_band"),
                IronsJewelry.id("item/base/gem_band_thin"),
                IronsJewelry.id("item/base/gold_ring"),
                IronsJewelry.id("item/base/band_flat_top"),
                IronsJewelry.id("item/base/band_tearstone"),
                IronsJewelry.id("item/base/band_stoneplate"),
                IronsJewelry.id("item/base/band_gem_superior"),
                IronsJewelry.id("item/base/piglin_signet"),
                IronsJewelry.id("item/base/twisted_ring_primary"),
                IronsJewelry.id("item/base/twisted_ring_secondary"),
                IronsJewelry.id("item/base/chain_simple"),
                IronsJewelry.id("item/base/chain_simple_amulet"),
                IronsJewelry.id("item/base/chain_amulet_of_protection"),
                IronsJewelry.id("item/base/barbed_band")
        );
        var metalPermutations = new HashMap<String, ResourceLocation>();
        metalPermutations.put("gold", IronsJewelry.id("palettes/gold"));
        metalPermutations.put("test", IronsJewelry.id("palettes/test"));
        metalPermutations.put("silver", IronsJewelry.id("palettes/silver"));
        metalPermutations.put("iron", IronsJewelry.id("palettes/iron"));
        metalPermutations.put("copper", IronsJewelry.id("palettes/copper"));
        metalPermutations.put("netherite", IronsJewelry.id("palettes/netherite"));
        metalPermutations.put("platinum", IronsJewelry.id("palettes/platinum"));
        metalPermutations.put("brass", IronsJewelry.id("palettes/brass"));
        metalPermutations.put("bronze", IronsJewelry.id("palettes/bronze"));
        metalPermutations.put("allthemodium", IronsJewelry.id("palettes/allthemodium"));
        metalPermutations.put("vibranium", IronsJewelry.id("palettes/vibranium"));
        metalPermutations.put("unobtainium", IronsJewelry.id("palettes/unobtainium"));
        metalPermutations.put("menu", IronsJewelry.id("palettes/menu"));
        metalPermutations.put("menu_bright", IronsJewelry.id("palettes/menu_bright"));
        sources.add(new PalettedPermutations(metalTextures, metalPaletteKey, metalPermutations));
        /*
        Gems:
         */
        var gemPaletteKey = IronsJewelry.id("palettes/diamond");
        var gemTextures = List.of(
                IronsJewelry.id("item/base/pointy_gem"),
                IronsJewelry.id("item/base/gem_round"),
                IronsJewelry.id("item/base/gems_side"),
                IronsJewelry.id("item/base/gem_tearstone"),
                IronsJewelry.id("item/base/gem_stoneplate"),
                IronsJewelry.id("item/base/gem_simple_amulet"),
                IronsJewelry.id("item/base/gem_amulet_of_protection"),
                IronsJewelry.id("item/base/barbed_band_gem")
        );
        var gemPermutations = new HashMap<String, ResourceLocation>();
        gemPermutations.put("diamond", IronsJewelry.id("palettes/diamond"));
        gemPermutations.put("amethyst", IronsJewelry.id("palettes/amethyst"));
        gemPermutations.put("emerald", IronsJewelry.id("palettes/emerald"));
        gemPermutations.put("lapis", IronsJewelry.id("palettes/lapis"));
        gemPermutations.put("ruby", IronsJewelry.id("palettes/ruby"));
        gemPermutations.put("sapphire", IronsJewelry.id("palettes/sapphire"));
        gemPermutations.put("moonstone", IronsJewelry.id("palettes/moonstone"));
        gemPermutations.put("peridot", IronsJewelry.id("palettes/peridot"));
        gemPermutations.put("topaz", IronsJewelry.id("palettes/topaz"));
        gemPermutations.put("onyx", IronsJewelry.id("palettes/onyx"));
        gemPermutations.put("menu", IronsJewelry.id("palettes/menu"));
        gemPermutations.put("menu_bright", IronsJewelry.id("palettes/menu_bright"));
        sources.add(new PalettedPermutations(gemTextures, gemPaletteKey, gemPermutations));
    }

    public static void add(PalettedPermutations spriteSource) {
        sources.add(spriteSource);
    }

    public static List<PalettedPermutations> getSources() {
        return sources;
    }
}
