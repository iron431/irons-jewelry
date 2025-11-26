package io.redspace.ironsjewelry.utils;

import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public interface IMinecraftInstanceHelper {
    @Nullable
    Player player();

    default void openGuidebookScreen() {

    }

    default boolean isLocalInstance() {
        return false;
    }

}
