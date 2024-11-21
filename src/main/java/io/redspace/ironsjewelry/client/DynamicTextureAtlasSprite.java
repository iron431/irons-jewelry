package io.redspace.ironsjewelry.client;

import com.mojang.blaze3d.platform.NativeImage;
import io.redspace.ironsjewelry.IronsJewelry;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntUnaryOperator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.util.FastColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class DynamicTextureAtlasSprite extends TextureAtlasSprite {
    final ResourceLocation baseTexture, paletteLocation;
    static final ResourceLocation paletteKey = IronsJewelry.id("palettes/gold");

    protected DynamicTextureAtlasSprite(TextureAtlasSprite textureAtlasSprite, ResourceLocation baseTexture, ResourceLocation paletteLocation) {
        super(textureAtlasSprite.atlasLocation(), apply(textureAtlasSprite.contents(),baseTexture, paletteLocation), (int) (textureAtlasSprite.getX() / textureAtlasSprite.getU0())+5, (int) (textureAtlasSprite.getY() / textureAtlasSprite.getV0()), textureAtlasSprite.getX(), textureAtlasSprite.getY());
        this.baseTexture = baseTexture;
        this.paletteLocation = paletteLocation;
    }
    protected DynamicTextureAtlasSprite(TextureAtlasSprite textureAtlasSprite, ResourceLocation baseTexture, ResourceLocation paletteLocation, boolean b) {
        super(textureAtlasSprite.atlasLocation(), apply(textureAtlasSprite.contents(),baseTexture, paletteLocation), 16, 16, 0, 0);
        this.baseTexture = baseTexture;
        this.paletteLocation = paletteLocation;
    }

    @Override
    public SpriteContents contents() {
        return super.contents();
    }

    @Nullable
    static public SpriteContents apply(SpriteContents base, ResourceLocation baseTexture, ResourceLocation paletteLocation) {
        Object object;
        NativeImage baseImage = base.getOriginalImage();
        try {
//            (string, resourceLocation) -> map.put(
//                    string, Suppliers.memoize(() -> createPaletteMapping(supplier.get(), loadPaletteEntryFromImage(Minecraft.getInstance().getResourceManager(), resourceLocation)))
//            )
            var pResourceManager = Minecraft.getInstance().getResourceManager();
            var palette = createPaletteMapping(loadPaletteEntryFromImage(pResourceManager, paletteKey), loadPaletteEntryFromImage(pResourceManager, paletteLocation));

            NativeImage nativeimage = baseImage.mappedCopy(palette);
            var components = paletteLocation.getPath().split("/");
            var name = baseTexture.withSuffix(String.format("_%s", components[components.length - 1]));
            return new SpriteContents(
                    name, new FrameSize(nativeimage.getWidth(), nativeimage.getHeight()), nativeimage, ResourceMetadata.EMPTY
            );
        } catch (IllegalArgumentException e/*| IOException ioexception*/) {
//            LOGGER.error("unable to apply palette to {}", this.permutationLocation, ioexception);
            object = null;
        }

        return base;
    }

    private static IntUnaryOperator createPaletteMapping(int[] p_266839_, int[] p_266776_) {
        if (p_266776_.length != p_266839_.length) {
//            LOGGER.warn("Palette mapping has different sizes: {} and {}", p_266839_.length, p_266776_.length);
            throw new IllegalArgumentException();
        } else {
            Int2IntMap int2intmap = new Int2IntOpenHashMap(p_266776_.length);

            for (int i = 0; i < p_266839_.length; i++) {
                int j = p_266839_[i];
                if (FastColor.ABGR32.alpha(j) != 0) {
                    int2intmap.put(FastColor.ABGR32.transparent(j), p_266776_[i]);
                }
            }

            return p_267899_ -> {
                int k = FastColor.ABGR32.alpha(p_267899_);
                if (k == 0) {
                    return p_267899_;
                } else {
                    int l = FastColor.ABGR32.transparent(p_267899_);
                    int i1 = int2intmap.getOrDefault(l, FastColor.ABGR32.opaque(l));
                    int j1 = FastColor.ABGR32.alpha(i1);
                    return FastColor.ABGR32.color(k * j1 / 255, i1);
                }
            };
        }
    }

    public static int[] loadPaletteEntryFromImage(ResourceManager pResourceMananger, ResourceLocation pPalette) {
        Optional<Resource> optional = pResourceMananger.getResource(SpriteSource.TEXTURE_ID_CONVERTER.idToFile(pPalette));
        if (optional.isEmpty()) {
//            LOGGER.error("Failed to load palette image {}", pPalette);
            throw new IllegalArgumentException();
        } else {
            try {
                int[] aint;
                try (
                        InputStream inputstream = optional.get().open();
                        NativeImage nativeimage = NativeImage.read(inputstream);
                ) {
                    aint = nativeimage.getPixelsRGBA();
                }

                return aint;
            } catch (Exception exception) {
//                LOGGER.error("Couldn't load texture {}", pPalette, exception);
                throw new IllegalArgumentException();
            }
        }
    }
}
