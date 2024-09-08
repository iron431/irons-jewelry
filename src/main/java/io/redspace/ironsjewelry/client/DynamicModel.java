package io.redspace.ironsjewelry.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.math.Transformation;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.AbstractPattern;
import io.redspace.ironsjewelry.data.JewelryData;
import io.redspace.ironsjewelry.data.MaterialData;
import io.redspace.ironsjewelry.data.PartData;
import io.redspace.ironsjewelry.data.PartInstance;
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
import net.minecraft.util.GsonHelper;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DynamicModel implements IUnbakedGeometry<DynamicModel> {
    List<Material> textures;

    public DynamicModel(List<ResourceLocation> textures) {
        ResourceLocation atlasToUse = InventoryMenu.BLOCK_ATLAS;
        this.textures = textures.stream().map((resourceLocation -> new Material(atlasToUse, resourceLocation))).collect(Collectors.toList());
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides) {
        return new BakedHolder(context, baker, modelState);
    }

    public static class BakedHolder implements BakedModel {
        BakedModel model;
        ItemOverrides overrides;

        public BakedHolder(IGeometryBakingContext context, ModelBaker baker, ModelState modelState) {
            //fixme: will the missing model have been baked yet?
            this.model = Minecraft.getInstance().getModelManager().getMissingModel();
            var blockmodel = (BlockModel) baker.getModel(ModelBakery.MISSING_MODEL_LOCATION);
            this.overrides = new ItemOverrides(baker, blockmodel,
                    List.of()
                    , baker.getModelTextureGetter()) {
                @Nullable
                @Override
                public BakedModel resolve(BakedModel pModel, ItemStack pStack, @Nullable ClientLevel pLevel, @Nullable LivingEntity pEntity, int pSeed) {
                    JewelryData data = tempJewelryData();//pStack.get(ComponentRegistry.JEWELRY_COMPONENT);
                    if (data != null) {
                        return ClientModelCache.MODEL_CACHE.computeIfAbsent(data.hashCode(), (i) -> bake(data, context, baker.getModelTextureGetter(), modelState, new ItemOverrides(baker, blockmodel, List.of(), baker.getModelTextureGetter())));
                    }
                    return EmptyModel.BAKED;
                }
            };
        }

        private static JewelryData tempJewelryData() {
            ResourceLocation patternId = IronsJewelry.id("simple_ring");
            ResourceLocation bandPartId = IronsJewelry.id("unadorned_band");
            PartData unadornedBand = new PartData(bandPartId, IronsJewelry.id("base/gold_ring"));
            MaterialData materialData = new MaterialData(IronsJewelry.id("wip_material"), null, IronsJewelry.id("palettes/test"), null, 1);
            return new JewelryData(
                    new AbstractPattern(patternId, Map.of(bandPartId, unadornedBand)),
                    Map.of(bandPartId, new PartInstance(unadornedBand, materialData))
            );
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
        var partInstance = jewelryData.getFirstPart();
        if (partInstance.isPresent()) {
            TextureAtlasSprite particle = spriteGetter.apply(new Material(InventoryMenu.BLOCK_ATLAS, partInstance.get().atlasResourceLocaction()));
            CompositeModel.Baked.Builder builder = CompositeModel.Baked.builder(context, particle, overrides, context.getTransforms());
            Transformation rootTransform = context.getRootTransform();


            var parts = jewelryData.parts().values().stream().toList();
//            var list2 = new ArrayList<Drawable>();
//            for (int i = 0; i < parts.size(); i++) {
//                list2.add(new Drawable(parts.get(i), 1f + i * .001f));
//            }
            for (int i = 0; i < parts.size(); i++) {
                PartInstance part = parts.get(i);
                TextureAtlasSprite sprite = spriteGetter.apply(new Material(InventoryMenu.BLOCK_ATLAS, part.atlasResourceLocaction()));

                ModelState subState = new SimpleModelState(modelState.getRotation().compose(
                        rootTransform.compose(new Transformation(
                                new Vector3f(0, 0, 0), //translate texture here
                                new Quaternionf(), new Vector3f(1, 1, 1), //scale texture here (ie 32x32)
                                new Quaternionf())
                        )), modelState.isUvLocked());

                List<BlockElement> unbaked = UnbakedGeometryHelper.createUnbakedItemElements(i, sprite);
                List<BakedQuad> quads = UnbakedGeometryHelper.bakeElements(unbaked, (material2) -> sprite, subState);

                //TODO: custom render type to custom atlas here???
                RenderTypeGroup renderTypes = new RenderTypeGroup(RenderType.solid(), NeoForgeRenderTypes.ITEM_UNSORTED_TRANSLUCENT.get());
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
            JsonArray tempTextureLocations = GsonHelper.getAsJsonArray(jsonObject, "temp");
            ArrayList<ResourceLocation> textureLocations = new ArrayList<>();
            for (JsonElement object : tempTextureLocations) {
                textureLocations.add(ResourceLocation.parse(object.getAsString()));
            }
            //TODO: allow people to load nbt-translator here too?
            IronsJewelry.LOGGER.debug("PalettedModel.Loader.read textureLocations ({}):", textureLocations.size());
            textureLocations.forEach(r -> IronsJewelry.LOGGER.debug("\t" + r.toString()));
            return new DynamicModel(textureLocations);
        }
    }
}
