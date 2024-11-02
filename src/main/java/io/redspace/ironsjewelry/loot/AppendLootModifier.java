package io.redspace.ironsjewelry.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class AppendLootModifier extends LootModifier {
    public static final Supplier<MapCodec<AppendLootModifier>> CODEC = Suppliers.memoize(()
            -> RecordCodecBuilder.mapCodec(builder -> codecStart(builder).and(
            Codec.list(Codec.STRING).fieldOf("keys").forGetter(m -> m.resourceLocationKeys)).apply(builder, AppendLootModifier::new)));
    private final List<String> resourceLocationKeys;

    protected AppendLootModifier(LootItemCondition[] conditionsIn, List<String> resourceLocationKey) {
        super(conditionsIn);
        this.resourceLocationKeys = resourceLocationKey;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        for (String resourceLocation : resourceLocationKeys) {
            ResourceLocation path = ResourceLocation.parse(resourceLocation);
            var lootTable = context.getLevel().getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, path));
            ObjectArrayList<ItemStack> objectarraylist = new ObjectArrayList<>();
            //use raw to avoid recursively adding all global loot modifiers again
            lootTable.getRandomItemsRaw(context, objectarraylist::add);
            generatedLoot.addAll(objectarraylist);
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}