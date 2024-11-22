package io.redspace.ironsjewelry.api;

import com.google.common.collect.ImmutableList;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.client.ClientData;
import io.redspace.ironsjewelry.core.data.MaterialDefinition;
import io.redspace.ironsjewelry.core.data.PartDefinition;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class DynamicAtlas extends TextureAtlas implements PreparableReloadListener, AutoCloseable {
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

    public TextureAtlasSprite getSprite(Holder<PartDefinition> part, Holder<MaterialDefinition> material) {
        return getSprite(getSpriteLocation(part, material));
    }

    public ResourceLocation getMenuSpriteLocation(Holder<PartDefinition> partDefinition, boolean bright) {
        return ResourceLocation.parse(String.format("%s_%s", partDefinition.value().baseTextureLocation().toString(), bright ? "menu_bright" : "menu"));
    }

    public boolean hasBuilt = false;
    public static final ResourceLocation ATLAS_OUTPUT_LOCATION = IronsJewelry.id("textures/atlas/jewelry.png");

    public void reset() {
        if (hasBuilt) {
            clearTextureData();
            hasBuilt = false;
        }
    }

    public DynamicAtlas(TextureManager pTextureManager) {
        super(ATLAS_OUTPUT_LOCATION);
        pTextureManager.register(ATLAS_OUTPUT_LOCATION, this);
    }

    abstract List<SpriteSource> buildSpriteSources();

    public void buildCustomContents() {
        IronsJewelry.LOGGER.info("Atlas {}: Building custom contents start", this.location());
        var loader = SpriteLoader.create(this);
        SpriteResourceLoader spriteresourceloader = SpriteResourceLoader.create(SpriteLoader.DEFAULT_METADATA_SECTIONS);
        List<SpriteSource> sources = buildSpriteSources();
        var factories = list(sources, Minecraft.getInstance().getResourceManager());
        List<SpriteContents> contents = factories.stream().map(factory -> factory.apply(spriteresourceloader)).filter(Objects::nonNull).toList();
        var preparations = loader.stitch(contents, 0, Runnable::run);
        this.upload(preparations);
        IronsJewelry.LOGGER.info("Atlas {}: Building custom contents finish ({} sprites)", this.location(), preparations.regions().size());
        hasBuilt = true;
    }

    private static String splitEnd(String string) {
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

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier pPreparationBarrier, ResourceManager pResourceManager, ProfilerFiller pPreparationsProfiler, ProfilerFiller pReloadProfiler, Executor pBackgroundExecutor, Executor pGameExecutor) {
        return CompletableFuture.runAsync(() -> {
                    //no preparations
                }).thenCompose(pPreparationBarrier::wait)
                .thenRun(() -> {
                    // If we have already built, rebuild. If not, then the game is still loading and we do nothing
                    if (hasBuilt) {
                        buildCustomContents();
                        //TODO: really this should be in its own handler but i dont want to make a new one rn
                        ClientData.clear();
                    }
                });
    }

    @Override
    public void close() {
        this.hasBuilt = false;
        this.clearTextureData();
    }
}
