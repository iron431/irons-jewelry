package io.redspace.ironsjewelry.api.internal;

import io.redspace.ironsjewelry.api.ModelTypeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class AtlasHandler implements PreparableReloadListener {
    private static final Map<ResourceLocation, DynamicAtlas> ATLASES = new HashMap<>();
    public static final HashMap<Integer, BakedModel> MODEL_CACHE = new HashMap<>();

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier pPreparationBarrier, ResourceManager pResourceManager, ProfilerFiller pPreparationsProfiler, ProfilerFiller pReloadProfiler, Executor pBackgroundExecutor, Executor pGameExecutor) {
        return CompletableFuture.runAsync(() -> {
                    //no preparations
                }).thenCompose(pPreparationBarrier::wait)
                .thenRun(() -> {
                    for (DynamicAtlas atlas : ATLASES.values()) {
                        // If we have already built, rebuild. If not, then the game is still loading and we do nothing
                        if (atlas.hasBuilt) {
                            atlas.buildCustomContents();
                        }
                    }
                    MODEL_CACHE.clear();
                });
    }

    public static DynamicAtlas getAtlas(ResourceLocation resourceLocation) {
        return ATLASES.computeIfAbsent(resourceLocation, r -> new DynamicAtlas(Objects.requireNonNull(ModelTypeRegistry.MODEL_TYPE_REGISTRY.get(r)), Minecraft.getInstance().getTextureManager()));
    }

    public static void clear() {
        ATLASES.values().forEach(DynamicAtlas::reset);
        MODEL_CACHE.clear();
    }
}
