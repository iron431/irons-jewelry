package io.redspace.ironsjewelry.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.redspace.ironsjewelry.core.data.JewelryData;
import io.redspace.ironsjewelry.registry.ActionRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.function.Function;

public interface IAction {
    Codec<IAction> CODEC = ActionRegistry.ACTION_REGISTRY
            .byNameCodec()
            .dispatch(IAction::codec, Function.identity());

    void apply(ServerLevel serverLevel, double quality, boolean applyToSelf, ServerPlayer wearer, Entity entity);

    MapCodec<? extends IAction> codec();
}
