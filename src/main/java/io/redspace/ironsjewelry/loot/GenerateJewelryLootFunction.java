package io.redspace.ironsjewelry.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.core.data.JewelryData;
import io.redspace.ironsjewelry.core.data.MaterialDefinition;
import io.redspace.ironsjewelry.core.data.PartDefinition;
import io.redspace.ironsjewelry.core.data.PartIngredient;
import io.redspace.ironsjewelry.core.data.PatternDefinition;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;
import io.redspace.ironsjewelry.registry.LootRegistry;
import io.redspace.ironsjewelry.utils.JewelryModTags;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public record GenerateJewelryLootFunction(
        HolderSet<PatternDefinition> patternSource,
        Optional<Map<TagKey<MaterialDefinition>, HolderSet<MaterialDefinition>>> materialFilter

) implements LootItemFunction {
    public static MapCodec<GenerateJewelryLootFunction> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            RegistryCodecs.homogeneousList(IronsJewelryRegistries.Keys.PATTERN_REGISTRY_KEY).fieldOf("patterns").forGetter(GenerateJewelryLootFunction::patternSource),
            Codec.unboundedMap(TagKey.codec(IronsJewelryRegistries.Keys.MATERIAL_REGISTRY_KEY), RegistryCodecs.homogeneousList(IronsJewelryRegistries.Keys.MATERIAL_REGISTRY_KEY)).optionalFieldOf("materials").forGetter(GenerateJewelryLootFunction::materialFilter)

    ).apply(builder, GenerateJewelryLootFunction::new));

    @Override
    public @NotNull LootItemFunctionType<? extends LootItemFunction> getType() {
        return LootRegistry.GENERATE_JEWELRY.get();
    }

    @Override
    public ItemStack apply(ItemStack stack, LootContext lootContext) {
        TreeMap<Integer, Holder<PatternDefinition>> weightedPatterns = new TreeMap<>();
        //Automatically restrict type from itemstack
        var typeOpt = IronsJewelryRegistries.JEWELRY_TYPE_REGISTRY.stream().filter(jewelryType -> jewelryType.item().value().equals(stack.getItem())).findFirst();
        if (typeOpt.isEmpty()) {
            // unable to find jewelry category for this item
            return ItemStack.EMPTY;
        }
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
            // Precompute all potential materials by excluding all blacklisted materials, unless a material filter is set which will override the blacklist
            List<Holder.Reference<MaterialDefinition>> allMaterials = registry.holders().filter(material ->
                    !material.value().ingredient().hasNoItems() && (!material.is(JewelryModTags.JEWELRY_LOOT_MATERIAL_BLACKLIST) || !materialFilter.isEmpty())
            ).toList();

            for (PartIngredient part : pattern.value().partTemplate()) {
                // Find applicable materials by
                // a): the material exists in this world (allMaterials)
                // b): the material can be used for this part
                // c): the material filter is empty, or this material passes each matching tag filter
                List<Holder.Reference<MaterialDefinition>> applicableMaterials = allMaterials.stream().filter(
                        (material) -> part.part().value().canUseMaterial(material) && matchesMaterialFilter(material)
                ).toList();
                if (!applicableMaterials.isEmpty()) {
                    materials.put(part.part(), getRandomWeightedMaterial(applicableMaterials, lootContext.getRandom()));
                }
            }
            var jewelryData = new JewelryData(pattern, materials);
            if (jewelryData.isValid()) {
                JewelryData.set(stack, jewelryData);
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private boolean matchesMaterialFilter(Holder<MaterialDefinition> materialHolder) {
        return materialFilter.isEmpty() || materialFilter.get().entrySet().stream()
                .filter(entry -> materialHolder.is(entry.getKey()))
                .allMatch(entry -> entry.getValue().contains(materialHolder));
    }

    private static Holder<MaterialDefinition> getRandomWeightedMaterial(List<? extends Holder<MaterialDefinition>> applicableMaterials, RandomSource randomSource) {
        TreeMap<Integer, Holder<MaterialDefinition>> weightedMaterials = new TreeMap<>();
        int total = 0;
        for (Holder<MaterialDefinition> material : applicableMaterials) {
            weightedMaterials.put(total, material);
            total += (int) (100 / (1 + material.value().quality()));
        }
        return weightedMaterials.lowerEntry(randomSource.nextInt(total) + 1).getValue();
    }

    public static class Builder implements LootItemFunction.Builder {
        List<Holder<PatternDefinition>> patterns = new ArrayList<>();
        Map<TagKey<MaterialDefinition>, HolderSet<MaterialDefinition>> materials = new HashMap<>();
        HolderSet<PatternDefinition> holderSet = null;

        public GenerateJewelryLootFunction.Builder withMaterial(TagKey<MaterialDefinition> tag, HolderSet<MaterialDefinition> materials) {
            this.materials.put(tag, materials);
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
            Optional<Map<TagKey<MaterialDefinition>, HolderSet<MaterialDefinition>>> materialOpt = materials.isEmpty() ? Optional.empty() : Optional.of(materials);
            if (holderSet != null) {
                return new GenerateJewelryLootFunction(holderSet, materialOpt);
            } else {
                return new GenerateJewelryLootFunction(new HolderSet.Direct<>(patterns), materialOpt);
            }
        }
    }
}
