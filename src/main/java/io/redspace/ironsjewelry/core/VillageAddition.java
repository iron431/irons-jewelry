package io.redspace.ironsjewelry.core;

import com.mojang.datafixers.util.Pair;
import io.redspace.ironsjewelry.IronsJewelry;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber()
public class VillageAddition {

    private static void addBuildingToPool(Registry<StructureTemplatePool> templatePoolRegistry,
                                          Registry<StructureProcessorList> processorListRegistry,
                                          ResourceKey<StructureProcessorList> proccessor,
                                          ResourceLocation poolRL,
                                          String nbtPieceRL,
                                          int weight) {

        // Grabs the processor list we want to use along with our piece.
        // This is a requirement as using the ProcessorLists.EMPTY field will cause the game to throw errors.
        // The reason why is the empty processor list in the world's registry is not the same instance as in that field once the world is started up.
        Holder<StructureProcessorList> processor = processorListRegistry.getHolderOrThrow(proccessor);

        // Grab the pool we want to add to
        StructureTemplatePool pool = templatePoolRegistry.get(poolRL);
        if (pool == null) return;

        // Grabs the nbt piece and creates a SinglePoolElement of it that we can add to a structure's pool.
        // Use .legacy( for villages/outposts and .single( for everything else
        SinglePoolElement piece = SinglePoolElement.legacy(IronsJewelry.id(nbtPieceRL).toString(),
                processor).apply(StructureTemplatePool.Projection.RIGID);
        // Use AccessTransformer or Accessor Mixin to make StructureTemplatePool's templates field public for us to see.
        // Weight is handled by how many times the entry appears in this list.
        // We do not need to worry about immutability as this field is created using Lists.newArrayList(); which makes a mutable list.
        for (int i = 0; i < weight; i++) {
            pool.templates.add(piece);
        }

        // Use AccessTransformer or Accessor Mixin to make StructureTemplatePool's rawTemplates field public for us to see.
        // This list of pairs of pieces and weights is not used by vanilla by default but another mod may need it for efficiency.
        // So lets add to this list for completeness. We need to make a copy of the array as it can be an immutable list.
        List<Pair<StructurePoolElement, Integer>> listOfPieceEntries = new ArrayList<>(pool.rawTemplates);
        listOfPieceEntries.add(new Pair<>(piece, weight));
        pool.rawTemplates = listOfPieceEntries;
//        pool.rawTemplates.forEach((pair) ->
//                IronsSpellbooks.LOGGER.debug("{}: {}", pair.getFirst().toString(), pair.getSecond()));
    }

    /**
     * We use FMLServerAboutToStartEvent as the dynamic registry exists now and all JSON worldgen files were parsed.
     * Mod compat is best done here.
     */
    @SubscribeEvent
    public static void addNewVillageBuilding(final ServerAboutToStartEvent event) {
        Registry<StructureTemplatePool> templatePoolRegistry = event.getServer().registryAccess().registry(Registries.TEMPLATE_POOL).orElseThrow();
        Registry<StructureProcessorList> processorListRegistry = event.getServer().registryAccess().registry(Registries.PROCESSOR_LIST).orElseThrow();

        //TODO: configurable weight?
        int weight = 2; // two is common weight for artisan building

        addBuildingToPool(templatePoolRegistry, processorListRegistry,
                ProcessorLists.MOSSIFY_70_PERCENT,
                ResourceLocation.parse("minecraft:village/plains/houses"),
                "village/plains/house_jeweler",
                weight);
        addBuildingToPool(templatePoolRegistry, processorListRegistry,
                ProcessorLists.MOSSIFY_70_PERCENT,
                ResourceLocation.parse("minecraft:village/desert/houses"),
                "village/desert/house_jeweler", weight);

        addBuildingToPool(templatePoolRegistry, processorListRegistry,
                ProcessorLists.MOSSIFY_70_PERCENT,
                ResourceLocation.parse("minecraft:village/taiga/houses"),
                "village/taiga/house_jeweler", weight);

        addBuildingToPool(templatePoolRegistry, processorListRegistry,
                ProcessorLists.MOSSIFY_70_PERCENT,
                ResourceLocation.parse("minecraft:village/savanna/houses"),
                "village/savanna/house_jeweler", weight);

        addBuildingToPool(templatePoolRegistry, processorListRegistry,
                ProcessorLists.MOSSIFY_70_PERCENT,
                ResourceLocation.parse("minecraft:village/snowy/houses"),
                "village/snowy/house_jeweler", weight);


    }
}
