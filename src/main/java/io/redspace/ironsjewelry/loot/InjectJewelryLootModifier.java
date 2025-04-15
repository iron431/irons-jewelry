package io.redspace.ironsjewelry.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.IronsJewelry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ChainedJsonException;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class InjectJewelryLootModifier extends LootModifier {
    public static final Supplier<MapCodec<InjectJewelryLootModifier>> CODEC = Suppliers.memoize(()
            -> RecordCodecBuilder.mapCodec(builder -> codecStart(builder).apply(builder, InjectJewelryLootModifier::new)));

    protected InjectJewelryLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (LootInjectionHandler.TRACKED_LOOT_TABLES.containsKey(context.getQueriedLootTableId())) {
            float chance = LootInjectionHandler.TRACKED_LOOT_TABLES.get(context.getQueriedLootTableId());
            if (context.getRandom().nextFloat() <= chance || true) {
                var lootTable = context.getLevel().getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, IronsJewelry.id("modifiers/inject_jewelry")));
                ObjectArrayList<ItemStack> objectarraylist = new ObjectArrayList<>();
                //use raw to avoid recursively adding all global loot modifiers again
                lootTable.getRandomItemsRaw(context, objectarraylist::add);
                generatedLoot.addAll(objectarraylist);
            }
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}