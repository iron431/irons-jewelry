package io.redspace.ironsjewelry.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.core.MaterialModifierDataHandler;
import io.redspace.ironsjewelry.core.bonuses.BonusType;
import io.redspace.ironsjewelry.core.parameters.IBonusParameterType;
import io.redspace.ironsjewelry.registry.ComponentRegistry;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;
import io.redspace.ironsjewelry.registry.ParameterTypeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Primary Data Object for the Jewelry Data Component
 * Consists of:
 * - Pattern Definition, the template for what this piece of jewelry is
 * - Map of Parts to Materials, the instantiated parts and what material they're made of
 * - Validity Boolean (whether all parts are in place, etc)
 * - List of Bonus Instances, which are the buffs this piece of jewelry gives when worn
 */
public class JewelryData {
    public static final Codec<JewelryData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            IronsJewelryRegistries.Codecs.PATTERN_REGISTRY_CODEC.fieldOf("pattern").forGetter(JewelryData::pattern),
            Codec.unboundedMap(
                    IronsJewelryRegistries.Codecs.PART_REGISTRY_CODEC,
                    IronsJewelryRegistries.Codecs.MATERIAL_REGISTRY_CODEC).fieldOf("parts").forGetter(JewelryData::parts)
    ).apply(builder, JewelryData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, JewelryData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(IronsJewelryRegistries.Keys.PATTERN_REGISTRY_KEY),
            jewelryData -> jewelryData.pattern,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.holderRegistry(IronsJewelryRegistries.Keys.PART_REGISTRY_KEY), ByteBufCodecs.holderRegistry(IronsJewelryRegistries.Keys.MATERIAL_REGISTRY_KEY)),
            jewelryData -> jewelryData.parts,
            JewelryData::new
    );

    // data
    private final Holder<PatternDefinition> pattern;
    private final Map<Holder<PartDefinition>, Holder<MaterialDefinition>> parts;
    // cache
    private final boolean valid;
    private final List<BonusInstance> bonuses;
    private final int hashCode;

    public JewelryData(Holder<PatternDefinition> pattern, Map<Holder<PartDefinition>, Holder<MaterialDefinition>> parts) {
        this.pattern = pattern;
        this.parts = parts;
        this.valid = validate();
        this.bonuses = cacheBonuses();
        this.hashCode = Objects.hash(pattern, parts);
    }

    private JewelryData(Holder<PatternDefinition> pattern, Map<Holder<PartDefinition>, Holder<MaterialDefinition>> parts, boolean valid, List<BonusInstance> bonuses) {
        this.pattern = pattern;
        this.parts = parts;
        this.valid = valid;
        this.bonuses = bonuses;
        this.hashCode = Objects.hash(pattern, parts);
    }

    /**
     * @param pattern
     * @param parts
     * @return Returns a special use-case of jewelry data for rendering. returned JewelryData is not practically useful, but is able to render as an item for intermediary previews.
     */
    public static JewelryData renderable(Holder<PatternDefinition> pattern, Map<Holder<PartDefinition>, Holder<MaterialDefinition>> parts) {
        return new JewelryData(pattern, parts, true, List.of());
    }

    private JewelryData() {
        this.pattern = null;
        this.valid = false;
        this.parts = Map.of();
        this.bonuses = List.of();
        this.hashCode = 0;
    }

    public static JewelryData NONE = new JewelryData();

    public static boolean has(ItemStack itemStack) {
        return itemStack.has(ComponentRegistry.JEWELRY_COMPONENT);
    }

    @NotNull
    public static JewelryData get(ItemStack itemStack) {
        return itemStack.getOrDefault(ComponentRegistry.JEWELRY_COMPONENT, NONE);
    }

    @Nullable
    public static JewelryData getNullable(ItemStack itemStack) {
        return itemStack.get(ComponentRegistry.JEWELRY_COMPONENT);
    }

    public static void set(ItemStack itemStack, JewelryData data) {
        itemStack.set(ComponentRegistry.JEWELRY_COMPONENT, data);
    }

    public static void remove(ItemStack itemStack) {
        itemStack.remove(ComponentRegistry.JEWELRY_COMPONENT);
    }

    public static void ifPresent(ItemStack itemStack, Consumer<JewelryData> consumer) {
        var data = getNullable(itemStack);
        if (data != null) {
            consumer.accept(data);
        }
    }

    public <T> void forBonuses(BonusType bonusType, Class<T> clazz, BiConsumer<BonusInstance, T> consumer) {
        var bonuses = this.getBonuses();
        for (BonusInstance instance : bonuses) {
            if (instance.bonusType().equals(bonusType)) {
                instance.bonusType().getParameterType().resolve(instance).ifPresent(param ->
                        {
                            if (clazz.isInstance(param)) {
                                consumer.accept(instance, (T) param);
                            }
                        }
                );
            }
        }
    }

    private boolean validate() {
        if (this.pattern == null || this.parts.size() != this.pattern.value().partTemplate().size()) {
            return false;
        }
        for (PartIngredient part : this.pattern.value().partTemplate()) {
            if (!this.parts.containsKey(part.part())) {
                //Ensure our parts contain everything specified by the pattern
                return false;
            }
        }
        return true;
    }

    public Map<Holder<PartDefinition>, Holder<MaterialDefinition>> parts() {
        return this.parts;
    }

    public boolean isValid() {
        return valid;
    }

    private List<BonusInstance> cacheBonuses() {
        if (!valid) {
            return List.of();
        }
        return pattern.value().bonuses().stream().map(this::getBonusFor).filter(inst -> inst.bonusType().getParameterType().equals(ParameterTypeRegistry.EMPTY.get()) || !inst.parameter().isEmpty()).toList();
    }

    public BonusInstance getBonusFor(Tuple<PartIngredient, Bonus> tuple) {
        Bonus bonus = tuple.getB();
        // Get type directly from bonus instance
        BonusType type = bonus.bonusType();
        // Calculate total quality based on the pattern's multiplier, the bonus instance multiplier, and the partForQuality's quality
        double totalQuality = pattern.value().qualityMultiplier() *
                bonus.qualityMultiplier() *
                pattern.value().partForQuality().map(PartDefinition -> this.parts.get(PartDefinition).value().quality()).orElse(1d);
        // Get cooldown directly from bonus instance
        Optional<QualityScalar> cooldown = bonus.cooldown();
        // Get all the parameters from either the bonus instance override (if present), or this part's material's bonuses
        Map<IBonusParameterType<?>, Object> allParameters = bonus.parameterValue().containsKey(bonus.bonusType().getParameterType()) ?
                bonus.parameterValue() :
                MaterialModifierDataHandler.getParametersWithOverrides(this.parts.get(tuple.getA().part()));
        Map<IBonusParameterType<?>, Object> parameter;
        // Filter data into just the bonus we need, or empty if it is not present
        if (allParameters.containsKey(type.getParameterType())) {
            parameter = Map.of(type.getParameterType(), allParameters.get(type.getParameterType()));
        } else {
            parameter = Map.of();
        }

        return new BonusInstance(
                type,
                totalQuality,
                parameter,
                cooldown
        );
    }

    public Component getItemName() {
        if (!this.isValid()) {
            return Component.translatable("item.irons_jewelry.invalid_jewelry");
        }
        var parts = pattern.value().partTemplate();
        Component[] ids = new Component[parts.size()];
        for (int i = 0; i < parts.size(); i++) {
            // Fill arguments in reverse (pinnacle piece, ie gem, will be first translation argument)
            // This order is most commonly preferred, although language index indicators can always work too (%2$s for the second argument)
            ids[parts.size() - 1 - i] = Component.translatable(this.parts.get(parts.get(i).part()).value().descriptionId());
        }
        var descriptionId = this.pattern.value().descriptionId();
        Map<Holder<MaterialDefinition>, Integer> duplicates = new HashMap<>();
        this.parts.forEach((part, material) -> {
            if (!pattern.value().partForQuality().map(ignore -> ignore.value().equals(part.value())).orElse(false)) {
                var count = duplicates.getOrDefault(material, 0);
                count++;
                duplicates.put(material, count);
            }
        });
        boolean dirty = false;
        MutableComponent directTranslation = Component.translatable(descriptionId + ".item", ids);
        String rasterizedTranslation = directTranslation.getString();
        for (var entry : duplicates.entrySet()) {
            var material = entry.getKey();
            var count = entry.getValue();
            if (count > 1) {
                for (int i = 0; i < count - 1; i++) {
                    rasterizedTranslation = rasterizedTranslation.replaceFirst(String.format("%s-", Component.translatable(material.value().descriptionId()).getString()), "");
                    dirty = true;
                }
            }
        }
        if (dirty) {
            return Component.literal(rasterizedTranslation);
        } else {
            return directTranslation;
        }
    }

    public List<BonusInstance> getBonuses() {
        return this.bonuses;
    }

    public Holder<PatternDefinition> pattern() {
        return this.pattern;
    }

    private static net.minecraft.resources.ResourceLocation getHolderId(net.minecraft.core.Holder<?> holder) {
        return holder.unwrapKey()
                .map(net.minecraft.resources.ResourceKey::location)
                .orElse(net.minecraft.resources.ResourceLocation.parse("irons_jewelry:unknown"));
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(getHolderId(this.pattern));
        for (Map.Entry<net.minecraft.core.Holder<PartDefinition>, net.minecraft.core.Holder<MaterialDefinition>> entry : this.parts.entrySet()) {
            result += Objects.hashCode(getHolderId(entry.getKey())) ^ Objects.hashCode(getHolderId(entry.getValue()));
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof JewelryData other)) return false;

        if (!Objects.equals(getHolderId(this.pattern), getHolderId(other.pattern))) return false;

        if (this.parts.size() != other.parts.size()) return false;

        for (Map.Entry<net.minecraft.core.Holder<PartDefinition>, net.minecraft.core.Holder<MaterialDefinition>> entry : this.parts.entrySet()) {
            net.minecraft.resources.ResourceLocation thisPartId = getHolderId(entry.getKey());
            net.minecraft.resources.ResourceLocation thisMaterialId = getHolderId(entry.getValue());

            net.minecraft.resources.ResourceLocation otherMaterialId = other.parts.entrySet().stream()
                    .filter(e -> getHolderId(e.getKey()).equals(thisPartId))
                    .map(e -> getHolderId(e.getValue()))
                    .findFirst()
                    .orElse(null);

            if (otherMaterialId == null || !thisMaterialId.equals(otherMaterialId)) return false;
        }

        return true;
    }
}
