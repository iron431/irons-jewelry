package io.redspace.ironsjewelry.core;

import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class MinecraftInstanceHelper implements IMinecraftInstanceHelper {
    /**
     * If we are on the client, this is replaced with an implementation that returns the client host player
     */
    public static IMinecraftInstanceHelper INSTANCE = () -> null;

    @Nullable
    @Override
    public Player player() {
        return INSTANCE.player();
    }

    @Nullable
    public static Player getPlayer() {
        return INSTANCE.player();
    }

    public static void ifPlayerPresent(Consumer<Player> consumer) {
        var player = getPlayer();
        if (player != null) {
            consumer.accept(player);
        }
    }
}
