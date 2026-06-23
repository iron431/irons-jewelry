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
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class ItemModelDataGenerator extends ModelProvider {

    public ItemModelDataGenerator(PackOutput output) {
        super(output, IronsJewelry.MODID);
    }

    @Override
    public @NonNull CompletableFuture<?> run(@NonNull CachedOutput cache) {
        return super.run(cache);
    }

    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return ItemModelHelper.simpleBlocks.stream();
    }

    @Override
    protected Stream<? extends Holder<Item>> getKnownItems() {
        return ItemModelHelper.simpleItems.stream();
    }

    @Override
    protected void registerModels(@NonNull BlockModelGenerators blockModels, @NonNull ItemModelGenerators itemModels) {
        ItemModelHelper.simpleItems.forEach(item -> itemModels.generateFlatItem(item.get(), ModelTemplates.FLAT_ITEM));
    }
}
