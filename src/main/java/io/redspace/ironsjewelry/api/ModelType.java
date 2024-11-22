package io.redspace.ironsjewelry.api;

import com.mojang.math.Transformation;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public abstract class ModelType {

    /**
     * @param sprite         Sprite for this layer
     * @param drawOrder      Order to draw. Lower values are drawn first, and thus appear at the bottom
     * @param transformation If specified, a transformation to be applied to this layer when baked. For example, if using 32x32 textures, you can scale by x2 to preverse vanilla pixel density
     */
    public record Layer(TextureAtlasSprite sprite, int drawOrder, Optional<Transformation> transformation) {
        @Override
        public int hashCode() {
            return transformation.map(Transformation::hashCode).orElse(0) * 31 + sprite.atlasLocation().hashCode();
        }
    }

    /**
     * @param layers List of things to be drawn and baked into the final model
     */
    public record BakingPreparations(List<Layer> layers) {
        @Override
        public int hashCode() {
            return layers.hashCode();
        }
    }

    public abstract BakingPreparations makePreparations(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int seed);

    public abstract ResourceLocation getAtlasLocation();

}
