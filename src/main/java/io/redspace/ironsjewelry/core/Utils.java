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
        String time = "";
        int seconds = ticks / 20;
        int minutes = seconds / 60;
        int hours = minutes / 60;
        if (minutes >= 60) {
            time += String.format("%s:", hours);
        }
        if (seconds >= 60) {
            time += String.format("%s:", minutes % 60);
        }
        if (seconds >= 10) {
            time += (seconds % 60) / 10;
        }
        time += seconds % 10;
        return time;
    }
}
