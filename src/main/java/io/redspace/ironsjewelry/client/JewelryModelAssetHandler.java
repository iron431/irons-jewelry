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

public class JewelryModelAssetHandler extends AssetHandler {

    @Override
    public @NotNull BakingPreparations makeBakedModelPreparations(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int seed) {
        return new BakingPreparations(List.of());
    }

    public String getPermutationName(Holder<MaterialDefinition> material) {
        var materialKey = material.getKey().location();
        var materialName = splitEnd(materialKey.getPath());
        return String.format("%s_%s", materialKey.getNamespace(), materialName);
    }

    public Optional<ResourceLocation> getSpriteLocation(Holder<PartDefinition> part, Holder<MaterialDefinition> material) {
        Optional<ResourceLocation> textureOpt = part.value().modelTextureLocation();
        if (textureOpt.isPresent()) {
            try {
                String base = textureOpt.get().toString();
                var permutationName = getPermutationName(material);
                return Optional.of(ResourceLocation.parse(String.format("%s_%s", base, permutationName)));
            } catch (ResourceLocationException exception) {
                IronsJewelry.LOGGER.error("Error parsing atlas sprite location: {}", exception.getMessage());
            } catch (Exception ignored) {
            }
        }
        return Optional.empty();
    }

    @Override
    public List<SpriteSource> buildSpriteSources() {
        var resourceManager = Minecraft.getInstance().getResourceManager();
        List<SpriteSource> sources = new ArrayList<>();
        Multimap<ResourceLocation, ResourceLocation> byPaletteKey = LinkedListMultimap.create();
        Map<String, ResourceLocation> permutations = new HashMap<>();
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
                    var textureOpt = part.value().modelTextureLocation();
                    if (textureOpt.isPresent()) {
                        var texture = textureOpt.get();
                        if (resourceManager.getResource(paletteKey.withPrefix("textures/").withSuffix(".png")).isEmpty()) {
                            IronsJewelry.LOGGER.warn("Invalid palette key: \"{}\" in part: {}", paletteKey, part.key().location());
                        } else if (resourceManager.getResource(texture.withPrefix("textures/").withSuffix(".png")).isEmpty()) {
                            IronsJewelry.LOGGER.warn("Invalid model texture location: \"{}\" in part: {}", texture, part.key().location());
                        } else {
                            byPaletteKey.put(paletteKey, texture);
                        }
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
        throw new UnsupportedOperationException("Non-Item atlas attempting to generate item model id");
    }
}
