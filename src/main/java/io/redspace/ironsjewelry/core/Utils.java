package io.redspace.ironsjewelry.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.function.Function;

public class Utils {

    public static <T> Codec<T> idCodec(Function<ResourceLocation, Optional<T>> idToObj, Function<T, ResourceLocation> objToId) {
        return ResourceLocation.CODEC
                .comapFlatMap(
                        resourceLocation -> idToObj.apply(resourceLocation)
                                .map(DataResult::success)
                                .orElseGet(() -> DataResult.error(() -> "Unknown registry key: " + resourceLocation)),
                        objToId
                );
    }
}
