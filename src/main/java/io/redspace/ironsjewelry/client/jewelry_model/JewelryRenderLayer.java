package io.redspace.ironsjewelry.client.jewelry_model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.data.JewelryData;
import io.redspace.ironsjewelry.registry.AssetHandlerRegistry;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

import java.util.ArrayList;
import java.util.List;

public class JewelryRenderLayer implements ICurioRenderer {
    private static final RenderType JEWELRY_MODEL_ATLAS_RENDERTYPE = RenderType.createArmorDecalCutoutNoCull(AssetHandlerRegistry.JEWELRY_MODEL_HANDLER.get().getAtlasLocation());
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(IronsJewelry.id("jewelry_humanoid_layer"), "main");

    final HumanoidModel<LivingEntity> model;

    public JewelryRenderLayer(HumanoidModel<LivingEntity> model) {
        this.model = model;
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack poseStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource renderTypeBuffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        JewelryData data = JewelryData.get(stack);
        if (!data.isValid()) {
            return;
        }
        List<TextureAtlasSprite> modelLayers = createJewelryModel(data);
        if (modelLayers.isEmpty()) {
            return;
        }
        ((HumanoidModel<T>)renderLayerParent.getModel()).copyPropertiesTo((HumanoidModel<T>) model);
        poseStack.pushPose();
        for (TextureAtlasSprite sprite : modelLayers) {
            VertexConsumer consumer = sprite.wrap(renderTypeBuffer.getBuffer(RenderType.entityCutoutNoCull(AssetHandlerRegistry.JEWELRY_MODEL_HANDLER.get().getAtlasLocation())));
            model.renderToBuffer(poseStack, consumer, light, OverlayTexture.NO_OVERLAY);
            poseStack.scale(1.001f, 1.001f, 1.001f); // prevent z-fighting
        }
        poseStack.popPose();
    }

    List<TextureAtlasSprite> createJewelryModel(JewelryData data) {
        List<TextureAtlasSprite> partModelTextures = new ArrayList<>();
        for (var ingredient : data.pattern().value().partTemplate()) {
            var part = ingredient.part();
            var material = data.parts().get(part);
            var opt = AssetHandlerRegistry.JEWELRY_MODEL_HANDLER.get().getSpriteLocation(part, material);
            if (opt.isEmpty()) {
                // all parts must support models, or nothing gets rendered
                return List.of();
            }
            partModelTextures.add(AssetHandlerRegistry.JEWELRY_MODEL_HANDLER.get().getSprite(opt.get()));
        }
        return partModelTextures;
    }
}
