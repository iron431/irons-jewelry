package io.redspace.ironsjewelry.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.data.MaterialDefinition;
import io.redspace.ironsjewelry.core.data.PartDefinition;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.sources.PalettedPermutations;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class JewelryAtlas extends TextureAtlasHolder {
    public boolean hasBuilt = false;
    public static final ResourceLocation ATLAS_OUTPUT_LOCATION = IronsJewelry.id("textures/atlas/test_atlas.png");
    public static final ResourceLocation ATLAS_JSON_NAME = IronsJewelry.id("test");

    public JewelryAtlas(TextureManager pTextureManager) {
        super(pTextureManager, ATLAS_OUTPUT_LOCATION, ATLAS_JSON_NAME);
    }

    public void buildCustomContents() {
        var loader = SpriteLoader.create(this.textureAtlas);
        SpriteResourceLoader spriteresourceloader = SpriteResourceLoader.create(SpriteLoader.DEFAULT_METADATA_SECTIONS);

        List<SpriteSource> sources = new ArrayList<>(/*AtlasHelper.getSources()*/);
        Multimap<ResourceLocation, ResourceLocation> byPaletteKey = LinkedListMultimap.create();
        Map<String, ResourceLocation> permutations = new HashMap<>();
        Iterable<MaterialDefinition> materials = IronsJewelryRegistries.materialRegistry(Minecraft.getInstance().level.registryAccess());
        Iterable<PartDefinition> parts = IronsJewelryRegistries.partRegistry(Minecraft.getInstance().level.registryAccess());
        for (PartDefinition part : parts) {
            byPaletteKey.put(part.paletteKey(), part.baseTextureLocation());
        }
        for (MaterialDefinition materialDefinition : materials) {
            ResourceLocation palette = materialDefinition.paletteLocation();
            //String name = String.format("%s$%s", palette.getNamespace(), splitEnd(palette.getPath()));
            permutations.put(splitEnd(palette.toString()), palette);
        }
        for (ResourceLocation paletteKey : byPaletteKey.keySet()) {
            var entries = byPaletteKey.get(paletteKey).stream().toList();
            sources.add(new PalettedPermutations(entries, paletteKey, permutations));
        }

        var factories = list(sources, Minecraft.getInstance().getResourceManager());
        List<SpriteContents> contents = factories.stream().map(factory -> factory.apply(spriteresourceloader)).toList();
        var preparations = loader.stitch(contents, 0, Runnable::run);
        this.textureAtlas.upload(preparations);
        hasBuilt = true;
    }

    private String splitEnd(String string) {
        var a = string.split("/");
        return a[a.length - 1];
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
