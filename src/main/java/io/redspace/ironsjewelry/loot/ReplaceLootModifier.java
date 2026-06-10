package io.redspace.ironsjewelry.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ReplaceLootModifier extends LootModifier {
    public static final Supplier<MapCodec<ReplaceLootModifier>> CODEC = Suppliers.memoize(()
            -> RecordCodecBuilder.mapCodec(builder -> codecStart(builder).and(
            builder.group(
                    Codec.STRING.fieldOf("key").forGetter(m -> m.IdentifierKey),
                    Codec.DOUBLE.fieldOf("chanceToReplace").forGetter(m -> m.chanceToReplace))

    ).apply(builder, ReplaceLootModifier::new)));
    private final String IdentifierKey;
    private final double chanceToReplace;

    protected ReplaceLootModifier(LootItemCondition[] conditionsIn, int priority, String IdentifierKey, double chanceToReplace) {
        super(conditionsIn, priority);
        this.IdentifierKey = IdentifierKey;
        this.chanceToReplace = Mth.clamp(chanceToReplace, 0, 1.0);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        double roll = context.getRandom().nextDouble();
//        IronsJewelry.LOGGER.debug("InjectPoolLootModifier.doApply {}: {} < {}", IdentifierKey, roll, chanceToReplace);
        if (roll < chanceToReplace) {
            Identifier path = Identifier.parse(IdentifierKey);
            var lootTable = context.getLevel().getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, path));
            ObjectArrayList<ItemStack> objectarraylist = new ObjectArrayList<>();
            //use raw to avoid stack overflow/recursively adding all global loot modifiers
            lootTable.getRandomItemsRaw(context, objectarraylist::add);
            return objectarraylist;
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}