package io.redspace.ironsjewelry.datagen;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.List;

public class ItemModelHelper  {
    public static List<DeferredHolder<Item, ? extends Item>> simpleItems = new ArrayList<>();
    // todo: implement blocks eventually
    public static List<DeferredHolder<Block, ? extends Block>> simpleBlocks = new ArrayList<>();

    public static void simpleItem(DeferredHolder<Item, ? extends Item> item) {
        simpleItems.add(item);
    }


}
