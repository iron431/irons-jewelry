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
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

import java.util.*;

public record GenerateJewelryLootFunction(
        HolderSet<PatternDefinition> patternSource,
        Optional<Map<String, HolderSet<MaterialDefinition>>> materialFilter

) implements LootItemFunction {
    public static MapCodec<GenerateJewelryLootFunction> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            RegistryCodecs.homogeneousList(IronsJewelryRegistries.Keys.PATTERN_REGISTRY_KEY).fieldOf("patterns").forGetter(GenerateJewelryLootFunction::patternSource),
            Codec.unboundedMap(Codec.STRING, RegistryCodecs.homogeneousList(IronsJewelryRegistries.Keys.MATERIAL_REGISTRY_KEY)).optionalFieldOf("materials").forGetter(GenerateJewelryLootFunction::materialFilter)

    ).apply(builder, GenerateJewelryLootFunction::new));

    @Override
    public LootItemFunctionType<? extends LootItemFunction> getType() {
        return LootRegistry.GENERATE_JEWELRY.get();
    }

    @Override
    public ItemStack apply(ItemStack stack, LootContext lootContext) {
        TreeMap<Integer, Holder<PatternDefinition>> weightedPatterns = new TreeMap<>();
        //Automatically restrict type from itemstack
        var typeOpt = IronsJewelryRegistries.JEWELRY_TYPE_REGISTRY.stream().filter(jewelryType -> jewelryType.item().value().equals(stack.getItem())).findFirst();
        if (typeOpt.isPresent()) {
            int total = 0;
            //build weighted map based inversely on the quality of the pattern (higher quality patterns have a lower chance to roll)
            for (int i = 0; i < this.patternSource.size(); i++) {
                var pattern = this.patternSource.get(i);
                if (pattern.value().jewelryType().equals(typeOpt.get())) {
                    weightedPatterns.put(total, pattern);
                    total += (int) (100 / (1 + pattern.value().qualityMultiplier()));
                }
            }
            if (!weightedPatterns.isEmpty()) {
                var pattern = weightedPatterns.lowerEntry(lootContext.getRandom().nextInt(total) + 1).getValue();
                HashMap<Holder<PartDefinition>, Holder<MaterialDefinition>> materials = new HashMap<>();
                var registry = IronsJewelryRegistries.materialRegistry(lootContext.getLevel().registryAccess());
                for (PartIngredient part : pattern.value().partTemplate()) {
                    // Find applicable materials by
                    // a): the material exists in this world
                    // b): the material can be used for this part
                    // c): the material filter is empty, or the material filter does not specify this material type, or this material is specified by type in the material filter
                    List<MaterialDefinition> applicableMaterials = registry.stream().filter(
                            (material) ->
                                    !material.ingredient().hasNoItems() &&
                                            part.part().value().canUseMaterial(material.materialType()) &&
                                            (materialFilter.isEmpty() ||
                                                    material.materialType().stream().anyMatch(type -> !materialFilter.get().containsKey(type) || materialFilter.get().get(type).contains(registry.wrapAsHolder(material))))
                    ).toList();
                    if (!applicableMaterials.isEmpty()) {
                        materials.put(part.part(), registry.wrapAsHolder(getRandomWeightedMaterial(applicableMaterials, lootContext.getRandom())));
                    }
                }
                var jewelryData = new JewelryData(pattern, materials);
                if (jewelryData.isValid()) {
                    stack.set(ComponentRegistry.JEWELRY_COMPONENT, jewelryData);
                    return stack;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private static MaterialDefinition getRandomWeightedMaterial(List<MaterialDefinition> applicableMaterials, RandomSource randomSource) {
        TreeMap<Integer, MaterialDefinition> weightedMaterials = new TreeMap<>();
        int total = 0;
        for (MaterialDefinition material : applicableMaterials) {
            weightedMaterials.put(total, material);
            total += (int) (100 / (1 + material.quality()));
        }
        return weightedMaterials.lowerEntry(randomSource.nextInt(total) + 1).getValue();
    }

    public static class Builder implements LootItemFunction.Builder {
        List<Holder<PatternDefinition>> patterns = new ArrayList<>();
        Map<String, HolderSet<MaterialDefinition>> materials = new HashMap<>();
        HolderSet<PatternDefinition> holderSet = null;

        public GenerateJewelryLootFunction.Builder withMaterial(String name, HolderSet<MaterialDefinition> materials) {
            this.materials.put(name, materials);
            return this;
        }

        public GenerateJewelryLootFunction.Builder withPatterns(HolderSet<PatternDefinition> patterns) {
            this.holderSet = patterns;
            return this;
        }

        public GenerateJewelryLootFunction.Builder withPattern(Holder<PatternDefinition> pattern) {
            this.patterns.add(pattern);
            return this;
        }

        @Override
        public LootItemFunction build() {
            Optional<Map<String, HolderSet<MaterialDefinition>>> materialOpt = materials.isEmpty() ? Optional.empty() : Optional.of(materials);
            if (holderSet != null) {
                return new GenerateJewelryLootFunction(holderSet, materialOpt);
            } else {
                return new GenerateJewelryLootFunction(new HolderSet.Direct<>(patterns), materialOpt);
            }
        }
    }
}
