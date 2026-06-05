package io.redspace.ironsjewelry.compat.attribute;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class AttributeRemapRegistry {
    private static final Map<Holder<Attribute>, Holder<Attribute>> REMAPS = new HashMap<>();

    private AttributeRemapRegistry() {
    }

    public static void register(Holder<Attribute> from, Holder<Attribute> to) {
        REMAPS.put(from, to);
    }

    public static void register(ResourceLocation from, ResourceLocation to) {
        Optional<Holder.Reference<Attribute>> fromHolder = BuiltInRegistries.ATTRIBUTE.getHolder(from);
        Optional<Holder.Reference<Attribute>> toHolder = BuiltInRegistries.ATTRIBUTE.getHolder(to);
        if (fromHolder.isEmpty() || toHolder.isEmpty()) {
            return;
        }
        register(fromHolder.get(), toHolder.get());
    }

    public static void register(ResourceLocation from, Holder<Attribute> to) {
        Optional<Holder.Reference<Attribute>> fromHolder = BuiltInRegistries.ATTRIBUTE.getHolder(from);
        if (fromHolder.isEmpty()) {
            return;
        }
        register(fromHolder.get(), to);
    }

    public static void register(Holder<Attribute> from, ResourceLocation to) {
        Optional<Holder.Reference<Attribute>> toHolder = BuiltInRegistries.ATTRIBUTE.getHolder(to);
        if (toHolder.isEmpty()) {
            return;
        }
        register(from, toHolder.get());
    }

    public static Optional<Holder<Attribute>> findTarget(Holder<Attribute> source) {
        return Optional.ofNullable(REMAPS.get(source));
    }
}
