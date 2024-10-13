package io.redspace.ironsjewelry.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.core.data.PatternDefinition;
import io.redspace.ironsjewelry.registry.ComponentRegistry;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;
import io.redspace.ironsjewelry.registry.LootRegistry;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

public record SetHeldPatternLootFunction(
        HolderSet<PatternDefinition> patternSource) implements LootItemFunction {
    public static MapCodec<SetHeldPatternLootFunction> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            RegistryCodecs.homogeneousList(IronsJewelryRegistries.Keys.PATTERN_REGISTRY_KEY).fieldOf("patterns").forGetter(SetHeldPatternLootFunction::patternSource)
    ).apply(builder, SetHeldPatternLootFunction::new));

    @Override
    public LootItemFunctionType<? extends LootItemFunction> getType() {
        return LootRegistry.SET_HELD_PATTERN.get();
    }

    @Override
    public ItemStack apply(ItemStack stack, LootContext lootContext) {
        //if (this.patternSource.left().isPresent()) {
        //    List<Holder<PatternDefinition>> patterns = this.patternSource.left().get();
        //    if (!patterns.isEmpty()) {
        //        var pattern = patterns.get(lootContext.getRandom().nextInt(patterns.size()));
        //        stack.set(ComponentRegistry.STORED_PATTERN, pattern);
        //    }
        //} else if (this.patternSource.right().isPresent()) {
        //    var tagKey = this.patternSource.right().get();
        //    var registry = IronsJewelryRegistries.patternRegistry(lootContext.getLevel().registryAccess());
        //    registry.getRandomElementOf(tagKey, lootContext.getRandom()).ifPresent(pattern -> stack.set(ComponentRegistry.STORED_PATTERN, pattern));
        //}
        this.patternSource.getRandomElement(lootContext.getRandom()).ifPresent(pattern -> stack.set(ComponentRegistry.STORED_PATTERN, pattern));
        return stack;
    }
}
