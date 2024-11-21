package io.redspace.ironsjewelry.mixin;

import net.minecraft.client.renderer.texture.atlas.SpriteSourceList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpriteSourceList.class)
public class SpriteSourceListMixin {

    @Inject(method = "load", at = @At(value = "RETURN"))
    private static void addAtlasSources(ResourceManager pResourceManager, ResourceLocation pSprite, CallbackInfoReturnable<SpriteSourceList> cir) {
        if (pSprite.toString().equals("minecraft:blocks")) {
            SpriteSourceList list = cir.getReturnValue();
//            list.sources.addAll(AtlasHelper.getSources());
        }
    }
}
