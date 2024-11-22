package io.redspace.ironsjewelry.api.internal;

import com.google.common.collect.ImmutableList;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.api.ModelType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class DynamicAtlas extends TextureAtlas {
    public boolean hasBuilt = false;

    public void reset() {
        if (hasBuilt) {
            clearTextureData();
            hasBuilt = false;
        }
    }

    final ModelType type;

    public DynamicAtlas(ModelType type, TextureManager pTextureManager) {
        super(type.getAtlasLocation());
        this.type = type;
        pTextureManager.register(this.location(), this);
    }

    public void buildCustomContents() {
        IronsJewelry.LOGGER.info("Atlas {}: Building custom contents start", this.location());
        var loader = SpriteLoader.create(this);
        SpriteResourceLoader spriteresourceloader = SpriteResourceLoader.create(SpriteLoader.DEFAULT_METADATA_SECTIONS);
        List<SpriteSource> sources = type.buildSpriteSources();
        var factories = list(sources, Minecraft.getInstance().getResourceManager());
        List<SpriteContents> contents = factories.stream().map(factory -> factory.apply(spriteresourceloader)).filter(Objects::nonNull).toList();
        var preparations = loader.stitch(contents, 0, Runnable::run);
        this.upload(preparations);
        IronsJewelry.LOGGER.info("Atlas {}: Building custom contents finish ({} sprites)", this.location(), preparations.regions().size());
        hasBuilt = true;
    }

    public List<Function<SpriteResourceLoader, SpriteContents>> list(List<SpriteSource> sources, ResourceManager pResourceManager) {
        final Map<ResourceLocation, SpriteSource.SpriteSupplier> map = new HashMap<>();
        SpriteSource.Output spritesource$output = new SpriteSource.Output() {
            @Override
            public void add(ResourceLocation p_296060_, SpriteSource.SpriteSupplier p_296385_) {
                SpriteSource.SpriteSupplier spritesource$spritesupplier = map.put(p_296060_, p_296385_);
                if (spritesource$spritesupplier != null) {
                    spritesource$spritesupplier.discard();
                }
            }

            @Override
            public void removeAll(Predicate<ResourceLocation> p_296294_) {
                Iterator<Map.Entry<ResourceLocation, SpriteSource.SpriteSupplier>> iterator = map.entrySet().iterator();

                while (iterator.hasNext()) {
                    Map.Entry<ResourceLocation, SpriteSource.SpriteSupplier> entry = iterator.next();
                    if (p_296294_.test(entry.getKey())) {
                        entry.getValue().discard();
                        iterator.remove();
                    }
                }
            }
        };
        sources.forEach(p_295860_ -> p_295860_.run(pResourceManager, spritesource$output));
        ImmutableList.Builder<Function<SpriteResourceLoader, SpriteContents>> builder = ImmutableList.builder();
        builder.add(p_295583_ -> MissingTextureAtlasSprite.create());
        builder.addAll(map.values());
        return builder.build();
    }

    @Override
    public TextureAtlasSprite getSprite(ResourceLocation pLocation) {
        if (!hasBuilt) {
            buildCustomContents();
            hasBuilt = true;
        }
        return super.getSprite(pLocation);
    }


}
