package io.redspace.ironsjewelry.api;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.math.Transformation;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.client.ClientData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.NeoForgeRenderTypes;
import net.neoforged.neoforge.client.RenderTypeGroup;
import net.neoforged.neoforge.client.model.CompositeModel;
import net.neoforged.neoforge.client.model.EmptyModel;
import net.neoforged.neoforge.client.model.SimpleModelState;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import net.neoforged.neoforge.client.model.geometry.UnbakedGeometryHelper;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class DynamicModel implements IUnbakedGeometry<DynamicModel> {
    final ModelType type;

    public DynamicModel(ModelType type) {
        this.type = type;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides) {
        return new BakedHolder(type, context, baker, modelState);
    }

    public static class BakedHolder implements BakedModel {
        BakedModel model;
        ItemOverrides overrides;
        ModelType type;

        public BakedHolder(ModelType type, IGeometryBakingContext context, ModelBaker baker, ModelState modelState) {
            this.model = Minecraft.getInstance().getModelManager().getMissingModel();
            var blockmodel = (BlockModel) baker.getModel(ModelBakery.MISSING_MODEL_LOCATION);
            this.overrides = new ItemOverrides(baker, blockmodel,
                    List.of()
                    , baker.getModelTextureGetter()) {
                @Nullable
                @Override
                public BakedModel resolve(BakedModel pModel, ItemStack pStack, @Nullable ClientLevel pLevel, @Nullable LivingEntity pEntity, int pSeed) {
                    ModelType.BakingPreparations preparations = type.makePreparations(pStack, pLevel, pEntity, pSeed);
                    return ClientData.MODEL_CACHE.computeIfAbsent(preparations.hashCode(), (i) -> bake(type, preparations, context, modelState, new ItemOverrides(baker, blockmodel, List.of(), baker.getModelTextureGetter())));
                }
            };
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState pState, @Nullable Direction pDirection, RandomSource pRandom) {
            return model.getQuads(pState, pDirection, pRandom);
        }

        @Override
        public boolean useAmbientOcclusion() {
            return model.useAmbientOcclusion();
        }

        @Override
        public boolean isGui3d() {
            return model.isGui3d();
        }

        @Override
        public boolean usesBlockLight() {
            return model.usesBlockLight();
        }

        @Override
        public boolean isCustomRenderer() {
            return model.isCustomRenderer();
        }

        @Override
        public TextureAtlasSprite getParticleIcon() {
            return model.getParticleIcon();
        }

        @Override
        public ItemOverrides getOverrides() {
            return overrides;
        }
    }

    public static BakedModel bake(ModelType type, ModelType.BakingPreparations preparations, IGeometryBakingContext context, ModelState modelState, ItemOverrides overrides) {
        IronsJewelry.LOGGER.debug("JewelryModel bake: {}", preparations);
        var layers = preparations.layers().stream().sorted(Comparator.comparingInt(ModelType.Layer::drawOrder)).toList();
        if (!layers.isEmpty()) {
            TextureAtlasSprite particle = layers.getFirst().sprite();
            CompositeModel.Baked.Builder builder = CompositeModel.Baked.builder(context, particle, overrides, context.getTransforms());
            Transformation rootTransform = context.getRootTransform();
            for (int i = 0; i < layers.size(); i++) {
                var layer = layers.get(i);

                TextureAtlasSprite sprite = layer.sprite();
                Transformation transformation = layer.transformation().orElse(new Transformation(
                        new Vector3f(0, 0, 0),
                        new Quaternionf(), new Vector3f(1, 1, 1),
                        new Quaternionf())
                );

                ModelState subState = new SimpleModelState(modelState.getRotation().compose(rootTransform.compose(transformation)), modelState.isUvLocked());

                List<BlockElement> unbaked = UnbakedGeometryHelper.createUnbakedItemElements(i, sprite);
                List<BakedQuad> quads = UnbakedGeometryHelper.bakeElements(unbaked, (material2) -> sprite, subState);
                RenderTypeGroup renderTypes = new RenderTypeGroup(RenderType.solid(), NeoForgeRenderTypes.getUnsortedTranslucent(type.getAtlasLocation()));

                builder.addQuads(renderTypes, quads);
            }
            return builder.build();
        }
        return EmptyModel.BAKED;

    }

    public static final class Loader implements IGeometryLoader<DynamicModel> {
        public static final Loader INSTANCE = new Loader();

        private Loader() {
        }

        @Override
        public DynamicModel read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) {
            try {
                String typestring = jsonObject.get("type").getAsString();
                ModelType type = Objects.requireNonNull(ModelTypeRegistry.MODEL_TYPE_REGISTRY.get(ResourceLocation.parse(typestring)));
                return new DynamicModel(type);
            } catch (Exception e) {
                throw new JsonParseException(e.getMessage());
            }
        }
    }
}
