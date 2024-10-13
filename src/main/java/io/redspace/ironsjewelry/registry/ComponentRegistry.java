package io.redspace.ironsjewelry.registry;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.data.JewelryData;
import io.redspace.ironsjewelry.core.data.PatternDefinition;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.UnaryOperator;

public class ComponentRegistry {
    private static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, IronsJewelry.MODID);

    public static void register(IEventBus eventBus) {
        COMPONENTS.register(eventBus);
    }

    private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String pName, UnaryOperator<DataComponentType.Builder<T>> pBuilder) {
        return COMPONENTS.register(pName, () -> pBuilder.apply(DataComponentType.builder()).build());
    }

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<JewelryData>> JEWELRY_COMPONENT = register("jewelry_data", (builder) -> builder.persistent(JewelryData.CODEC).networkSynchronized(JewelryData.STREAM_CODEC).cacheEncoding());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Holder<PatternDefinition>>> STORED_PATTERN = register("stored_pattern", (builder) -> builder.persistent(JewelryDataRegistries.PATTERN_REGISTRY_CODEC).networkSynchronized(ByteBufCodecs.holderRegistry(JewelryDataRegistries.PATTERN_REGISTRY_KEY)).cacheEncoding());
}
