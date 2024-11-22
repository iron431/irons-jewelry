package io.redspace.ironsjewelry.client;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.api.ModelType;
import io.redspace.ironsjewelry.core.data.JewelryData;
import io.redspace.ironsjewelry.core.data.MaterialDefinition;
import io.redspace.ironsjewelry.core.data.PartDefinition;
import io.redspace.ironsjewelry.core.data.PartIngredient;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.sources.PalettedPermutations;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class JewelryModelType extends ModelType {

    private static PartIngredient get(Holder<PartDefinition> definition, List<PartIngredient> list) {
        for (PartIngredient i : list) {
            if (i.part().equals(definition)) {
                return i;
            }
        }
        return null;
    }

    @Override
    public @NotNull BakingPreparations makePreparations(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int seed) {
        JewelryData jewelryData = JewelryData.get(itemStack);
        if (jewelryData.isValid()) {
            var parts = jewelryData.parts().entrySet();
            if (!parts.isEmpty()) {
                List<Layer> layers = parts.stream().map(part -> {
                    ResourceLocation sprite = getSpriteLocation(part.getKey(), part.getValue());
                    return new Layer(sprite, get(part.getKey(), jewelryData.pattern().value().partTemplate()).drawOrder(), Optional.empty());
                }).toList();

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

    public ResourceLocation getSpriteLocation(Holder<PartDefinition> part, Holder<MaterialDefinition> material) {
        try {
            String base = part.value().baseTextureLocation().toString();
            var permutationName = getPermutationName(material);
            return ResourceLocation.parse(String.format("%s_%s", base, permutationName));
        } catch (ResourceLocationException exception) {
            IronsJewelry.LOGGER.error("Error parsing atlas sprite location: {}", exception.getMessage());
        } catch (Exception ignored) {
        }
        return ResourceLocation.withDefaultNamespace("missingno");
    }

    public ResourceLocation getMenuSpriteLocation(Holder<PartDefinition> partDefinition, boolean bright) {
        return ResourceLocation.parse(String.format("%s_%s", partDefinition.value().baseTextureLocation().toString(), bright ? "menu_bright" : "menu"));
    }

    @Override
    public List<SpriteSource> buildSpriteSources() {
        var resourceManager = Minecraft.getInstance().getResourceManager();
        List<SpriteSource> sources = new ArrayList<>();
        Multimap<ResourceLocation, ResourceLocation> byPaletteKey = LinkedListMultimap.create();
        Map<String, ResourceLocation> permutations = new HashMap<>(Map.of(
                "menu", IronsJewelry.id("palettes/menu"),
                "menu_bright", IronsJewelry.id("palettes/menu_bright")
        ));
        IronsJewelryRegistries.materialRegistry(Minecraft.getInstance().level.registryAccess()).holders().forEach(
                material -> {
                    ResourceLocation palette = material.value().paletteLocation();
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

        for (ResourceLocation paletteKey : byPaletteKey.keySet()) {
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
