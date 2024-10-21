package io.redspace.ironsjewelry.datagen;

import io.redspace.ironsjewelry.IronsJewelry;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ItemModelDataGenerator extends ItemModelProvider {

    public static List<Consumer<ItemModelDataGenerator>> toRegister = new ArrayList<>();

    public ItemModelDataGenerator(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, IronsJewelry.MODID, exFileHelper);
    }

    @Override
    protected void registerModels() {
        toRegister.forEach(c -> c.accept(this));
    }

    public ItemModelBuilder simpleItem(DeferredHolder<Item, ? extends Item> item) {
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.withDefaultNamespace("item/generated")).texture("layer0",
                ResourceLocation.fromNamespaceAndPath(IronsJewelry.MODID, "item/" + item.getId().getPath()));
    }
}
