package io.redspace.ironsjewelry.client;

import io.redspace.ironsjewelry.item.book.GuideBookScreen;
import net.minecraft.client.Minecraft;

public final class ClientHelper {

    private ClientHelper() {
    }

    public static void openGuidebookScreen() {
        Minecraft.getInstance().setScreen(new GuideBookScreen());
    }
}
