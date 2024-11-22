package io.redspace.ironsjewelry.client;

import io.redspace.ironsjewelry.api.ModelType;
import io.redspace.ironsjewelry.core.data.JewelryData;
import io.redspace.ironsjewelry.core.data.PartDefinition;
import io.redspace.ironsjewelry.core.data.PartIngredient;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

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
                    TextureAtlasSprite sprite = ClientData.JEWELRY_ATLAS.getSprite(part.getKey(), part.getValue());
                    return new Layer(sprite, get(part.getKey(), jewelryData.pattern().value().partTemplate()).drawOrder(), Optional.empty());
                }).toList();

                return new BakingPreparations(layers);
            }
        }
        return new BakingPreparations(List.of());

    }

    @Override
    public ResourceLocation getAtlasLocation() {
        return JewelryAtlas.ATLAS_OUTPUT_LOCATION;
    }

    @Override
    public int modelId(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int seed) {
        return JewelryData.get(itemStack).hashCode();
    }
}
