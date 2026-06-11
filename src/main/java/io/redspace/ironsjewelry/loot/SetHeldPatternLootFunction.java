package io.redspace.ironsjewelry.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.core.data.PatternDefinition;
import io.redspace.ironsjewelry.core.data.StoredPatternData;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.Optional;

public record SetHeldPatternLootFunction(
        HolderSet<PatternDefinition> patternSource) implements LootItemFunction {
    public static final MapCodec<SetHeldPatternLootFunction> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            RegistryCodecs.homogeneousList(IronsJewelryRegistries.Keys.PATTERN_REGISTRY_KEY).fieldOf("patterns").forGetter(SetHeldPatternLootFunction::patternSource)
    ).apply(builder, SetHeldPatternLootFunction::new));

    @Override
    public MapCodec<SetHeldPatternLootFunction> codec() {
        return MAP_CODEC;
    }

    @Override
    public ItemStack apply(ItemStack stack, LootContext lootContext) {
        Optional<Holder<PatternDefinition>> patternOpt = this.patternSource.getRandomElement(lootContext.getRandom());
        if (patternOpt.isPresent()) {
            var pattern = patternOpt.get();
            StoredPatternData.set(stack, pattern);
            stack.set(DataComponents.ADDITIONAL_TRADE_COST, (int) Math.min(64, (18 + pattern.value().partTemplate().size() * 2) * pattern.value().qualityMultiplier()));
        }
        return stack;
    }
}
