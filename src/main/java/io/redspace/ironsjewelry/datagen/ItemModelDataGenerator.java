package io.redspace.ironsjewelry.datagen;

import io.redspace.ironsjewelry.IronsJewelry;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.core.Holder;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ItemModelDataGenerator extends ModelProvider {
    public static Map<DeferredHolder<Item, ? extends Item>, Consumer<ItemModelGenerators>> items = new HashMap<>();
    // todo: implement blocks eventually ig
    public static Map<DeferredHolder<Block, ? extends Block>, Consumer<ItemModelGenerators>> blocks = new HashMap<>();

    public ItemModelDataGenerator(PackOutput output) {
        super(output, IronsJewelry.MODID);
    }

    @Override
    public @NonNull CompletableFuture<?> run(@NonNull CachedOutput cache) {
        return super.run(cache);
    }

    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return blocks.keySet().stream();
    }

    @Override
    protected Stream<? extends Holder<Item>> getKnownItems() {
        return items.keySet().stream();
    }

    @Override
    protected void registerModels(@NonNull BlockModelGenerators blockModels, @NonNull ItemModelGenerators itemModels) {
        try {
            items.values().forEach(c -> c.accept(itemModels));
        } catch (Exception e) {
            IronsJewelry.LOGGER.debug("erjgnhorenhoj: {}", e.getMessage());
        }
    }

    public static void simpleItem(DeferredHolder<Item, ? extends Item> item) {
        items.put(item, itemModels -> itemModels.generateFlatItem(item.get(), ModelTemplates.FLAT_ITEM));
    }


}
