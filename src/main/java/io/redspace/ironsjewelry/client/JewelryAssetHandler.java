package io.redspace.ironsjewelry.client;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import io.redspace.atlasapi.api.AssetHandler;
import io.redspace.atlasapi.api.data.BakingPreparations;
import io.redspace.atlasapi.api.data.ModelLayer;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.data.JewelryData;
import io.redspace.ironsjewelry.core.data.MaterialDefinition;
import io.redspace.ironsjewelry.core.data.PartDefinition;
import io.redspace.ironsjewelry.core.data.PartIngredient;
import io.redspace.ironsjewelry.core.data.PatternDefinition;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;
import net.minecraft.IdentifierException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.sources.PalettedPermutations;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JewelryAssetHandler extends AssetHandler {

    private static int extractDrawOrder(Holder<PartDefinition> definition, List<PartIngredient> list) {
        for (PartIngredient i : list) {
            if (i.part().equals(definition)) {
                return i.drawOrder();
            }
        }
        return 0;
    }

    @Override
    public @NotNull BakingPreparations makeBakedModelPreparations(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int seed) {
        JewelryData jewelryData = JewelryData.get(itemStack);
        if (jewelryData.isValid()) {
            // iterate over the parts and grab sprites based on the material it is made from
            var parts = jewelryData.parts().entrySet();
            if (!parts.isEmpty()) {
                List<ModelLayer> layers = parts.stream().map(part -> {
                    Identifier sprite = getSpriteLocation(part.getKey(), part.getValue());
                    return new ModelLayer(sprite, extractDrawOrder(part.getKey(), jewelryData.pattern().value().partTemplate()), Optional.empty());
                }).toList();

                return new BakingPreparations(layers);
            }
        } else {
            Holder<PatternDefinition> pattern = jewelryData.pattern();
            if (pattern == null && clientLevel != null) {
                // replace invalid pattern with simple band so something always renders
                pattern = IronsJewelryRegistries.patternRegistry(clientLevel.registryAccess()).getHolderOrThrow(ResourceKey.create(IronsJewelryRegistries.Keys.PATTERN_REGISTRY_KEY, IronsJewelry.id("simple_band")));
            }
            if (pattern != null) {
                // iterate over the part template and grab sad menu sprite
                var patternValue = pattern.value();
                List<ModelLayer> layers = patternValue.partTemplate().stream().map(
                        part -> {
                            Identifier sprite = getMenuSpriteLocation(part.part(), true);
                            return new ModelLayer(sprite, extractDrawOrder(part.part(), patternValue.partTemplate()), Optional.empty());
                        }
                ).toList();
                return new BakingPreparations(layers);
            }
        }
        return new BakingPreparations(List.of());
    }

    public String getPermutationName(Holder<MaterialDefinition> material) {
        var materialKey = material.getKey().location();
        var materialName = splitEnd(materialKey.getPath());
        return String.format("%s_%s", materialKey.getNamespace(), materialName);
    }

    public Identifier getSpriteLocation(Holder<PartDefinition> part, Holder<MaterialDefinition> material) {
        try {
            String base = part.value().baseTextureLocation().toString();
            var permutationName = getPermutationName(material);
            return Identifier.parse(String.format("%s_%s", base, permutationName));
        } catch (IdentifierException exception) {
            IronsJewelry.LOGGER.error("Error parsing atlas sprite location: {}", exception.getMessage());
        } catch (Exception ignored) {
        }
        return Identifier.withDefaultNamespace("missingno");
    }

    public Identifier getMenuSpriteLocation(Holder<PartDefinition> partDefinition, boolean bright) {
        return Identifier.parse(String.format("%s_%s", partDefinition.value().baseTextureLocation().toString(), bright ? "menu_bright" : "menu"));
    }

    @Override
    public List<SpriteSource> buildSpriteSources() {
        var resourceManager = Minecraft.getInstance().getResourceManager();
        List<SpriteSource> sources = new ArrayList<>();
        Multimap<Identifier, Identifier> byPaletteKey = LinkedListMultimap.create();
        Map<String, Identifier> permutations = new HashMap<>(Map.of(
                "menu", IronsJewelry.id("palettes/menu"),
                "menu_bright", IronsJewelry.id("palettes/menu_bright")
        ));
        IronsJewelryRegistries.materialRegistry(Minecraft.getInstance().level.registryAccess()).holders().forEach(
                material -> {
                    Identifier palette = material.value().paletteLocation();
                    if (resourceManager.getResource(palette.withPrefix("textures/").withSuffix(".png")).isEmpty()) {
                        IronsJewelry.LOGGER.warn("Invalid palette: \"{}\" in material: {}", palette, material.key().location());
                    } else {
                        permutations.put(getPermutationName(material), palette);
                    }
                }
        );
        IronsJewelryRegistries.partRegistry(Minecraft.getInstance().level.registryAccess()).holders().forEach(part -> {
                    var paletteKey = part.value().paletteKey();
                    var texture = part.value().baseTextureLocation();
                    if (resourceManager.getResource(paletteKey.withPrefix("textures/").withSuffix(".png")).isEmpty()) {
                        IronsJewelry.LOGGER.warn("Invalid palette key: \"{}\" in part: {}", paletteKey, part.key().location());
                    } else if (resourceManager.getResource(texture.withPrefix("textures/").withSuffix(".png")).isEmpty()) {
                        IronsJewelry.LOGGER.warn("Invalid texture location: \"{}\" in part: {}", texture, part.key().location());
                    } else {
                        byPaletteKey.put(paletteKey, texture);
                    }
                }
        );

        for (Identifier paletteKey : byPaletteKey.keySet()) {
            var entries = byPaletteKey.get(paletteKey).stream().toList();
            sources.add(new PalettedPermutations(entries, paletteKey, permutations));
        }
        return sources;
    }

    private static String splitEnd(String string) {
        var a = string.split("/");
        return a[a.length - 1];
    }

    @Override
    public int modelId(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int seed) {
        return JewelryData.get(itemStack).hashCode();
    }
}
