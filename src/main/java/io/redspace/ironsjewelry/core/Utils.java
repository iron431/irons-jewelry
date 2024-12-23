package io.redspace.ironsjewelry.core;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import io.redspace.ironsjewelry.core.data.BonusInstance;
import io.redspace.ironsjewelry.core.data.JewelryData;
import io.redspace.ironsjewelry.core.data.MaterialDefinition;
import io.redspace.ironsjewelry.registry.ComponentRegistry;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;

public class Utils {

    public static <T> Codec<T> byIdCodec(Function<ResourceLocation, Optional<T>> idToObj, Function<T, ResourceLocation> objToId) {
        return ResourceLocation.CODEC
                .comapFlatMap(
                        resourceLocation -> idToObj.apply(resourceLocation)
                                .map(DataResult::success)
                                .orElseGet(() -> DataResult.error(() -> "Unknown registry key: " + resourceLocation)),
                        objToId
                );
    }

    public static <T> StreamCodec<ByteBuf, T> idStreamCodec(Function<ResourceLocation, T> idToObj, Function<T, ResourceLocation> objToId) {
        return ResourceLocation.STREAM_CODEC.map(idToObj, objToId);
    }

    public static <L, R, T> T mapEither(Either<L, R> either, Function<L, T> leftToValue, Function<R, T> rightToValue) {
        if (either.left().isPresent()) {
            return leftToValue.apply(either.left().get());
        } else if (either.right().isPresent()) {
            return rightToValue.apply(either.right().get());
        } else {
            throw new NoSuchElementException("Neither right nor left present in either");
        }
    }

    public static Optional<Holder<MaterialDefinition>> getMaterialForIngredient(RegistryAccess access, ItemStack ingredient) {
        var r = IronsJewelryRegistries.materialRegistry(access);
        return r.stream().filter(material -> material.ingredient().test(ingredient)).map(r::wrapAsHolder).findFirst();
    }

    public static List<BonusInstance> getEquippedBonuses(Player player) {
        return CuriosApi.getCuriosInventory(player).map(inv -> inv.findCurios(stack -> stack.has(ComponentRegistry.JEWELRY_COMPONENT)).stream().flatMap(slot -> JewelryData.get(slot.stack()).getBonuses().stream()).toList()).orElse(List.of());
    }

    public static List<ItemStack> getEquippedJewelry(Player player) {
        return CuriosApi.getCuriosInventory(player).map(inv -> inv.findCurios(stack -> stack.has(ComponentRegistry.JEWELRY_COMPONENT)).stream().map(SlotResult::stack).toList()).orElse(List.of());
    }

    public static List<? extends FormattedCharSequence> rasterizeComponentList(List<Component> components) {
        return components.stream().map(component -> FormattedCharSequence.forward(component.getString(), component.getStyle())).toList();
    }


    public static String timeFromTicks(float ticks, int decimalPlaces) {
        float ticks_to_seconds = 20;
        float seconds_to_minutes = 60;
        String affix = "s";
        float time = ticks / ticks_to_seconds;
        if (time > seconds_to_minutes) {
            time /= seconds_to_minutes;
            affix = "m";
        }
        return stringTruncation(time, decimalPlaces) + affix;
    }

    public static String stringTruncation(double f, int decimalPlaces) {
        if (f == Math.floor(f)) {
            return Integer.toString((int) f);
        }

        double multiplier = Math.pow(10, decimalPlaces);
        double truncatedValue = Math.floor(f * multiplier) / multiplier;

        // Convert the truncated value to a string
        String result = Double.toString(truncatedValue);

        // Remove trailing zeros
        result = result.replaceAll("0*$", "");

        // Remove the decimal point if there are no decimal places
        result = result.endsWith(".") ? result.substring(0, result.length() - 1) : result;

        return result;
    }

    public static String digitalTimeFromTicks(int ticks) {
        return digitalTimeFromTicks(ticks, false);
    }

    public static String digitalTimeFromTicks(int ticks, boolean showZeroMinutes) {
        String time = "";
        int seconds = ticks / 20;
        int minutes = seconds / 60;
        int hours = minutes / 60;
        if (minutes >= 60) {
            time += String.format("%s:", hours);
        }
        if (seconds >= 60 || showZeroMinutes) {
            time += String.format("%s:", minutes % 60);
        }
        if (seconds >= 10) {
            time += (seconds % 60) / 10;
        } else if (minutes > 0 || showZeroMinutes) {
            time += "0";
        }
        time += seconds % 10;
        return time;
    }
}
