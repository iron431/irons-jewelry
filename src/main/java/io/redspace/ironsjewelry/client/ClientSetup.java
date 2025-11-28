package io.redspace.ironsjewelry.client;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.block.jewelcrafting_station.JewelcraftingStationScreen;
import io.redspace.ironsjewelry.client.jewelry_model.JewelryRenderLayer;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;
import io.redspace.ironsjewelry.registry.ItemRegistry;
import io.redspace.ironsjewelry.registry.JewelryTypeRegistry;
import io.redspace.ironsjewelry.registry.MenuRegistry;
import io.redspace.ironsjewelry.utils.IMinecraftInstanceHelper;
import io.redspace.ironsjewelry.utils.MinecraftInstanceHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.ShieldModel;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.layers.EnergySwirlLayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import top.theillusivec4.curios.api.client.ICurioRenderer;

import java.util.function.Supplier;

@EventBusSubscriber(modid = IronsJewelry.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(MenuRegistry.JEWELCRAFTING_MENU.get(), JewelcraftingStationScreen::new);
    }

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(JewelryRenderLayer.LAYER_LOCATION, () -> LayerDefinition.create(HumanoidModel.createMesh(new CubeDeformation(0.26F), 0.0F), 64, 32));
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MinecraftInstanceHelper.INSTANCE = new IMinecraftInstanceHelper() {
                @Nullable
                @Override
                public Player player() {
                    return Minecraft.getInstance().player;
                }

                @Override
                public boolean isLocalInstance() {
                    return true;
                }
            };
            Supplier<ICurioRenderer> jewelryRenderLayerSupplier = () -> new JewelryRenderLayer(new HumanoidArmorModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(JewelryRenderLayer.LAYER_LOCATION)));
            IronsJewelryRegistries.JEWELRY_TYPE_REGISTRY.forEach(
                    type -> CuriosRendererRegistry.register(type.item().value(), jewelryRenderLayerSupplier)
            );
        });
    }
}
