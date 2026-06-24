package io.redspace.ironsjewelry.core.data;

import io.redspace.ironsjewelry.registry.ComponentRegistry;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class StoredPatternData {
    public static boolean has(ItemStack itemStack) {
        return itemStack.has(ComponentRegistry.STORED_PATTERN);
    }

    @Nullable
    public static Holder<PatternDefinition> get(ItemStack itemStack) {
        return itemStack.get(ComponentRegistry.STORED_PATTERN);
    }

    public static void set(ItemStack itemStack, Holder<PatternDefinition> pattern) {
        itemStack.set(ComponentRegistry.STORED_PATTERN, pattern);
    }

    public static void remove(ItemStack itemStack) {
        itemStack.remove(ComponentRegistry.STORED_PATTERN);
    }
}
