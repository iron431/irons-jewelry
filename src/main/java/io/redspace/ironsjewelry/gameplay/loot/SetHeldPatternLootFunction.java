package io.redspace.ironsjewelry.gameplay.loot;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.data.PatternDefinition;
import io.redspace.ironsjewelry.core.data_registry.PatternDataHandler;
import io.redspace.ironsjewelry.registry.ComponentRegistry;
import io.redspace.ironsjewelry.registry.LootRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

import java.util.List;
import java.util.Objects;

public record SetHeldPatternLootFunction(
        Either<List<ResourceLocation>, TagKey<Object>> patternSource) implements LootItemFunction {
    public static MapCodec<SetHeldPatternLootFunction> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.either(Codec.list(ResourceLocation.CODEC/*Utils.byIdCodec(PatternDataHandler::getSafe, PatternDefinition::id)*/), TagKey.codec(ResourceKey.createRegistryKey(IronsJewelry.id("irons_jewelry/patterns")))).fieldOf("patterns").forGetter(SetHeldPatternLootFunction::patternSource)
    ).apply(builder, SetHeldPatternLootFunction::new));

    @Override
    public LootItemFunctionType<? extends LootItemFunction> getType() {
        return LootRegistry.SET_HELD_PATTERN.get();
    }

    @Override
    public ItemStack apply(ItemStack stack, LootContext lootContext) {
        if (this.patternSource.left().isPresent()) {
            List<PatternDefinition> patterns = this.patternSource.left().get().stream().map(PatternDataHandler::get).filter(Objects::nonNull).toList();
            if (!patterns.isEmpty()) {
                var pattern = patterns.get(lootContext.getRandom().nextInt(patterns.size()));
                stack.set(ComponentRegistry.STORED_PATTERN, pattern);
            }
        } else if (this.patternSource.right().isPresent()) {
//            var tagKey = this.patternSource.right().get();
//            tagKey.isFor()
//            if(tagKey instanceof  TagKey<PatternDefinition>){
//
//            }
        }
        return stack;
    }
}
