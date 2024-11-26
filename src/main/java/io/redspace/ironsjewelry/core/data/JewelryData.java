package io.redspace.ironsjewelry.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.core.BonusType;
import io.redspace.ironsjewelry.registry.ComponentRegistry;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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

    private final Holder<PatternDefinition> pattern;
    private final Map<Holder<PartDefinition>, Holder<MaterialDefinition>> parts;
    private final boolean valid;
    private final List<BonusInstance> bonuses;
    private final int hashCode;

    public JewelryData(Holder<PatternDefinition> pattern, Map<Holder<PartDefinition>, Holder<MaterialDefinition>> parts) {
        this.pattern = pattern;
        this.parts = parts;
        this.valid = validate();
        this.bonuses = cacheBonuses();
        this.hashCode =
                pattern.getKey().location().hashCode() * 31 +
                        parts.entrySet().stream().collect(
                                Collectors.toMap(entry -> entry.getKey().getKey().location(), entry -> entry.getValue().getKey().location())
                        ).hashCode();
    }

    private JewelryData(Holder<PatternDefinition> pattern, Map<Holder<PartDefinition>, Holder<MaterialDefinition>> parts, boolean valid, List<BonusInstance> bonuses) {
        this.pattern = pattern;
        this.parts = parts;
        this.valid = valid;
        this.bonuses = bonuses;
        this.hashCode = parts.hashCode();
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

    @NotNull
    public static JewelryData get(ItemStack itemStack) {
        return itemStack.getOrDefault(ComponentRegistry.JEWELRY_COMPONENT, NONE);
    }

    public static void ifPresent(ItemStack itemStack, Consumer<JewelryData> consumer) {
        var data = itemStack.get(ComponentRegistry.JEWELRY_COMPONENT);
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
        return pattern.value().bonuses().stream().map(this::getBonusFor).toList();
    }

    public BonusInstance getBonusFor(Tuple<PartIngredient, Bonus> tuple) {
        return new BonusInstance(
                tuple.getB().bonusType(),
                pattern.value().qualityMultiplier() * tuple.getB().qualityMultiplier() * pattern.value().partForQuality().map(PartDefinition -> this.parts.get(PartDefinition).value().quality()).orElse(1d),
                tuple.getB().parameterValue().containsKey(tuple.getB().bonusType().getParameterType()) ? tuple.getB().parameterValue() : (this.parts.get(tuple.getA().part()).value().bonusParameters()),
                tuple.getB().cooldown()
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
        return Component.translatable(descriptionId + ".item", ids);
    }

    public List<BonusInstance> getBonuses() {
        return this.bonuses;
    }

    public Holder<PatternDefinition> pattern() {
        return this.pattern;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && this.hashCode() == obj.hashCode();
    }
}
