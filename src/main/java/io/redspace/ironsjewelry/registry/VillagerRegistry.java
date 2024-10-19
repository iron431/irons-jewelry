package io.redspace.ironsjewelry.registry;

import com.google.common.collect.ImmutableSet;
import io.redspace.ironsjewelry.IronsJewelry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import javax.annotation.Nullable;

public class VillagerRegistry {
    private static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(BuiltInRegistries.VILLAGER_PROFESSION, IronsJewelry.MODID);
    private static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(BuiltInRegistries.POINT_OF_INTEREST_TYPE, IronsJewelry.MODID);

    public static final DeferredHolder<PoiType, PoiType> JEWELCRAFTING_POI = POI_TYPES.register("jeweler", () -> new PoiType(ImmutableSet.copyOf(BlockRegistry.JEWELCRAFTING_STATION_BLOCK.get().getStateDefinition().getPossibleStates()), 1, 1));
    public static final DeferredHolder<VillagerProfession, VillagerProfession> JEWELER_PROFESSSION = registerProfession("jeweler", JEWELCRAFTING_POI.getKey(), SoundEvents.VILLAGER_WORK_ARMORER);

    private static DeferredHolder<VillagerProfession, VillagerProfession> registerProfession(
            String pName, ResourceKey<PoiType> pJobSite, @Nullable SoundEvent pWorkSound
    ) {
        return PROFESSIONS.register(pName, () -> new VillagerProfession(pName, p_219668_ -> p_219668_.is(pJobSite), p_219640_ -> p_219640_.is(pJobSite), ImmutableSet.of(), ImmutableSet.of(), pWorkSound));
    }

    public static void register(IEventBus eventBus) {
        PROFESSIONS.register(eventBus);
        POI_TYPES.register(eventBus);
    }
}

