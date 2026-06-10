package io.redspace.ironsjewelry.utils;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.data.BonusInstance;
import io.redspace.ironsjewelry.core.data.JewelryData;
import io.redspace.ironsjewelry.core.data.MaterialDefinition;
import io.redspace.ironsjewelry.core.data.PartIngredient;
import io.redspace.ironsjewelry.core.data.PatternDefinition;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Utils {

    public static <T> Codec<T> byIdCodec(Function<Identifier, Optional<T>> idToObj, Function<T, Identifier> objToId) {
        return Identifier.CODEC
                .comapFlatMap(
                        Identifier -> idToObj.apply(Identifier)
                                .map(DataResult::success)
                                .orElseGet(() -> DataResult.error(() -> "Unknown registry key: " + Identifier)),
                        objToId
                );
    }

    public static <T> StreamCodec<ByteBuf, T> idStreamCodec(Function<Identifier, T> idToObj, Function<T, Identifier> objToId) {
        return Identifier.STREAM_CODEC.map(idToObj, objToId);
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
        return CuriosApi.getCuriosInventory(player).map(inv -> inv.findCurios(JewelryData::has).stream().flatMap(slot -> JewelryData.get(slot.stack()).getBonuses().stream()).toList()).orElse(List.of());
    }

    public static List<ItemStack> getEquippedJewelry(Player player) {
        return CuriosApi.getCuriosInventory(player).map(inv -> inv.findCurios(JewelryData::has).stream().map(SlotResult::stack).toList()).orElse(List.of());
    }

    public static List<FormattedCharSequence> rasterizeComponentList(List<? extends Component> components) {
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

    public static ItemStack createExampleJewelryItem(RegistryAccess registryAccess, Holder<PatternDefinition> patternHolder) {
        var pattern = patternHolder.value();
        ItemStack output = new ItemStack(pattern.jewelryType().item());
        Holder<MaterialDefinition> iron = IronsJewelryRegistries.materialRegistry(registryAccess).get(IronsJewelry.id("example")).orElseThrow();
        var parts = pattern.partTemplate().stream().map(PartIngredient::part).collect(Collectors.toMap(Function.identity(),
                (p) -> iron));
        JewelryData jewelryData = JewelryData.renderable(IronsJewelryRegistries.patternRegistry(Minecraft.getInstance().level.registryAccess()).wrapAsHolder(pattern), parts);
        JewelryData.set(output, jewelryData);
        var bonuses = pattern.getPatternBonusesTooltip();
        if (!bonuses.isEmpty()) {
            bonuses.set(0, Component.translatable("tooltip.irons_jewelry.bonus_crafted_header").withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE)); // replace header
            bonuses.add(0, Component.empty());
            output.set(DataComponents.LORE, new ItemLore(bonuses.stream().map(component -> (Component) component.withStyle(component.getStyle().withItalic(false))).toList()));
        }
        return output;
    }

    public static void spawnParticles(Level level, ParticleOptions particle, double x, double y, double z, int count, double deltaX, double deltaY, double deltaZ, double speed, boolean force) {
        level.getServer().getPlayerList().getPlayers().forEach(player -> ((ServerLevel) level).sendParticles(player, particle, force, false, x, y, z, count, deltaX, deltaY, deltaZ, speed));
    }
}
