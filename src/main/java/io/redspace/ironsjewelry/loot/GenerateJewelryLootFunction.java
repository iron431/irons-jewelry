package io.redspace.ironsjewelry.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.core.data.*;
import io.redspace.ironsjewelry.registry.ComponentRegistry;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;
import io.redspace.ironsjewelry.registry.LootRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;

public record GenerateJewelryLootFunction(
        Holder<JewelryType> type,
        HolderSet<PatternDefinition> patternSource,
        double qualityMin, double qualityMax,
        Optional<HolderSet<MaterialDefinition>> materialFilter

) implements LootItemFunction {
    public static MapCodec<GenerateJewelryLootFunction> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            IronsJewelryRegistries.JEWELRY_TYPE_REGISTRY.holderByNameCodec().fieldOf("type").forGetter(GenerateJewelryLootFunction::type),
            RegistryCodecs.homogeneousList(IronsJewelryRegistries.Keys.PATTERN_REGISTRY_KEY).fieldOf("patterns").forGetter(GenerateJewelryLootFunction::patternSource),
            Codec.DOUBLE.fieldOf("qualityMin").forGetter(GenerateJewelryLootFunction::qualityMin),
            Codec.DOUBLE.fieldOf("qualityMax").forGetter(GenerateJewelryLootFunction::qualityMax),
            RegistryCodecs.homogeneousList(IronsJewelryRegistries.Keys.MATERIAL_REGISTRY_KEY).optionalFieldOf("materialFilter").forGetter(GenerateJewelryLootFunction::materialFilter)

    ).apply(builder, GenerateJewelryLootFunction::new));

    @Override
    public LootItemFunctionType<? extends LootItemFunction> getType() {
        return LootRegistry.GENERATE_JEWELRY.get();
    }

    @Override
    public ItemStack apply(ItemStack stack, LootContext lootContext) {
        TreeMap<Integer, Holder<PatternDefinition>> weightedPatterns = new TreeMap<>();
        int total = 0;
        for (int i = 0; i < this.patternSource.size(); i++) {
            var pattern = this.patternSource.get(i);
            if (pattern.value().jewelryType().equals(this.type.value())) {
                weightedPatterns.put(total, pattern);
                total += (int) (pattern.value().qualityMultiplier() * 100);
            }
        }
        var pattern = weightedPatterns.lowerEntry(lootContext.getRandom().nextInt(total)).getValue();
        HashMap<Holder<PartDefinition>, Holder<MaterialDefinition>> materials = new HashMap<>();
        var registry = IronsJewelryRegistries.materialRegistry(lootContext.getLevel().registryAccess());
        for (PartIngredient part : pattern.value().partTemplate()) {
            List<MaterialDefinition> applicableMaterials = registry.stream().filter(
                    (material) -> material.quality() >= this.qualityMin && material.quality() <= this.qualityMax &&
                            part.part().value().canUseMaterial(material.materialType()) &&
                            //Fixme: if you wanted to limit say, only metal, this would require you to list all gem types now
                            // could just literally save this as a map of strings to holderset instead
                            (materialFilter.isEmpty() || materialFilter.get().contains(registry.wrapAsHolder(material)))
            ).toList();
            if (!applicableMaterials.isEmpty()) {
                materials.put(part.part(), registry.wrapAsHolder(applicableMaterials.get(lootContext.getRandom().nextInt(applicableMaterials.size()))));
            }
        }
        var jewelryData = new JewelryData(pattern, materials);
        if (jewelryData.isValid()) {
            stack.set(ComponentRegistry.JEWELRY_COMPONENT, jewelryData);
            return stack;
        } else {
            return ItemStack.EMPTY;
        }
    }
}
