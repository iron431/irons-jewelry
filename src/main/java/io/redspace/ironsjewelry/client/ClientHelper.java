package io.redspace.ironsjewelry.client;

import io.redspace.ironsjewelry.item.book.GuideBookScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ClientHelper {

    private ClientHelper() {
    }

    public static void openGuidebookScreen() {
        Minecraft.getInstance().setScreen(new GuideBookScreen());
    }
}
