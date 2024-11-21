package io.redspace.ironsjewelry.client;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.math.Transformation;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.data.JewelryData;
import io.redspace.ironsjewelry.core.data.MaterialDefinition;
import io.redspace.ironsjewelry.core.data.PartDefinition;
import io.redspace.ironsjewelry.core.data.PartIngredient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
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
import java.util.function.Function;

public class DynamicModel implements IUnbakedGeometry<DynamicModel> {

    public DynamicModel() {
        ResourceLocation atlasToUse = InventoryMenu.BLOCK_ATLAS;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides) {
        return new BakedHolder(context, baker, modelState);
    }

    public static class BakedHolder implements BakedModel {
        BakedModel model;
        ItemOverrides overrides;

        public BakedHolder(IGeometryBakingContext context, ModelBaker baker, ModelState modelState) {
            this.model = Minecraft.getInstance().getModelManager().getMissingModel();
            var blockmodel = (BlockModel) baker.getModel(ModelBakery.MISSING_MODEL_LOCATION);
            this.overrides = new ItemOverrides(baker, blockmodel,
                    List.of()
                    , baker.getModelTextureGetter()) {
                @Nullable
                @Override
                public BakedModel resolve(BakedModel pModel, ItemStack pStack, @Nullable ClientLevel pLevel, @Nullable LivingEntity pEntity, int pSeed) {
                    JewelryData data = JewelryData.get(pStack);
                    if (data.isValid()) {
                        return ClientData.MODEL_CACHE.computeIfAbsent(data.hashCode(), (i) -> bake(data, context, baker.getModelTextureGetter(), modelState, new ItemOverrides(baker, blockmodel, List.of(), baker.getModelTextureGetter())));
                    }
                    return EmptyModel.BAKED;
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

    public static BakedModel bake(JewelryData jewelryData, IGeometryBakingContext context, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides) {
        IronsJewelry.LOGGER.debug("JewelryModel bake: {}", jewelryData);
        var parts = jewelryData.parts().entrySet().stream().sorted(Comparator.comparingInt(entry -> get(entry.getKey(), jewelryData.pattern().value().partTemplate()).drawOrder())).toList();
        if (!parts.isEmpty()) {
            TextureAtlasSprite particle = spriteGetter.apply(new Material(InventoryMenu.BLOCK_ATLAS, atlasResourceLocaction(parts.getFirst().getKey(), parts.getFirst().getValue())));
            CompositeModel.Baked.Builder builder = CompositeModel.Baked.builder(context, particle, overrides, context.getTransforms());
            Transformation rootTransform = context.getRootTransform();

            for (int i = 0; i < parts.size(); i++) {
                TextureAtlasSprite sprite = new DynamicTextureAtlasSprite(
                        spriteGetter.apply(new Material(InventoryMenu.BLOCK_ATLAS, parts.get(i).getKey().value().baseTextureLocation()/*atlasResourceLocaction(parts.get(i).getKey(), parts.get(i).getValue())*/)),
                        parts.get(i).getKey().value().baseTextureLocation(),
                        parts.get(i).getValue().value().paletteLocation(),
                        true
                );

                ModelState subState = new SimpleModelState(modelState.getRotation().compose(
                        rootTransform.compose(new Transformation(
                                new Vector3f(0, 0, 0), //translate texture here
                                new Quaternionf(), new Vector3f(1, 1, 1), //scale texture here (ie 32x32)
                                new Quaternionf())
                        )), modelState.isUvLocked());

                List<BlockElement> unbaked = UnbakedGeometryHelper.createUnbakedItemElements(i, sprite);
                List<BakedQuad> quads = UnbakedGeometryHelper.bakeElements(unbaked, (material2) -> sprite, subState);

                //TODO: custom render type to custom atlas here???
                // yes
                // how the fuck does that point to an atlas if an atlas is virtual?
                // public DynamicTexture(NativeImage pPixels)
                // texturemanager#byname
                ResourceLocation location = atlasResourceLocaction(parts.get(i).getKey(), parts.get(i).getValue());
                Minecraft.getInstance().getTextureManager().register(location, new DynamicTexture(sprite.contents().getOriginalImage()));
                RenderTypeGroup renderTypes = new RenderTypeGroup(RenderType.solid(),NeoForgeRenderTypes.getUnsortedTranslucent(location));
                if(!IronsJewelry.JEWELRY_ATLAS.hasBuilt){
                    IronsJewelry.JEWELRY_ATLAS.buildCustomContents();
                }
                builder.addQuads(renderTypes, quads);
            }
            return builder.build();
        }
        return EmptyModel.BAKED;

    }

    private static PartIngredient get(Holder<PartDefinition> definition, List<PartIngredient> list) {
        for (PartIngredient i : list) {
            if (i.part().equals(definition)) {
                return i;
            }
        }
        return null;
    }

    public static ResourceLocation atlasResourceLocaction(Holder<PartDefinition> part, Holder<MaterialDefinition> material) {
        try {
            String composite = part.value().baseTextureLocation().toString();
            var components = material.value().paletteLocation().getPath().split("/");
            composite += "_" + components[components.length - 1];
            return ResourceLocation.parse(composite);
        } catch (Exception e) {
            //TODO: something better
            return ResourceLocation.parse("unknown");
        }
    }

    public static ResourceLocation atlasResourceLocaction(Holder<PartDefinition> part, String paletteName) {
        try {
            String composite = part.value().baseTextureLocation().toString();
            composite += "_" + paletteName;
            return ResourceLocation.parse(composite);
        } catch (Exception e) {
            //TODO: something better
            return ResourceLocation.parse("unknown");
        }
    }

    public static final class Loader implements IGeometryLoader<DynamicModel> {
        public static final Loader INSTANCE = new Loader();

        private Loader() {
        }

        @Override
        public DynamicModel read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) {
            return new DynamicModel();
        }
    }
}
