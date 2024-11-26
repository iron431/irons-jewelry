package io.redspace.ironsjewelry.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.core.IBonusParameterType;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Tuple;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * A pattern represents a piece of jewelry that can be crafted, and contains data for what components are required to craft it and what the resulting item can do
 *
 * @param jewelryType
 * @param partTemplate
 * @param bonuses
 * @param unlockedByDefault
 * @param qualityMultiplier
 */
public record PatternDefinition(String descriptionId,
                                JewelryType jewelryType,
                                List<PartIngredient> partTemplate,
                                Optional<Holder<PartDefinition>> partForQuality,
                                boolean unlockedByDefault,
                                double qualityMultiplier) {
    public static final Codec<PatternDefinition> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.STRING.fieldOf("descriptionId").forGetter(PatternDefinition::descriptionId),
            IronsJewelryRegistries.JEWELRY_TYPE_REGISTRY.byNameCodec().fieldOf("type").forGetter(PatternDefinition::jewelryType),
            Codec.list(PartIngredient.CODEC).fieldOf("parts").forGetter(PatternDefinition::partTemplate),
            IronsJewelryRegistries.Codecs.PART_REGISTRY_CODEC.optionalFieldOf("partForQuality").forGetter(PatternDefinition::partForQuality),
            Codec.BOOL.optionalFieldOf("unlockedByDefault", true).forGetter(PatternDefinition::unlockedByDefault),
            Codec.DOUBLE.optionalFieldOf("qualityMultiplier", 1d).forGetter(PatternDefinition::qualityMultiplier)
    ).apply(builder, PatternDefinition::new));

    public PatternDefinition(String descriptionId, JewelryType jewelryType, List<PartIngredient> partTemplate, Optional<Holder<PartDefinition>> partForQuality,
                             boolean unlockedByDefault,
                             double qualityMultiplier) {
        this.descriptionId = descriptionId;
        this.jewelryType = jewelryType;
        this.partTemplate = partTemplate.stream().sorted(Comparator.comparingInt(PartIngredient::drawOrder)).toList();
        this.partForQuality = partForQuality;
        this.unlockedByDefault = unlockedByDefault;
        this.qualityMultiplier = qualityMultiplier;
    }

    public List<Tuple<PartIngredient, Bonus>> bonuses(){
        return partTemplate.stream().flatMap(part->part.bonuses().stream().map(bonus->(Tuple<PartIngredient, Bonus>)new Tuple(part,bonus))).toList();
    }

    public List<Component> getFullPatternTooltip() {
        var titleStyle = Style.EMPTY.applyFormats(ChatFormatting.GOLD, ChatFormatting.UNDERLINE);
        var headerStyle = Style.EMPTY.applyFormats(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE);
        var infoStyle = ChatFormatting.GRAY;
        Component title = Component.translatable(this.descriptionId()).withStyle(titleStyle);
        Component partHeader = Component.translatable("tooltip.irons_jewelry.parts_header").withStyle(headerStyle);
        var parts = this.partTemplate.stream().map(part -> Component.translatable(part.part().value().descriptionId()).withStyle(infoStyle)).toList();

        Component bonusHeader = Component.translatable(this.bonuses().size() > 1 ? "tooltip.irons_jewelry.bonus_header_plural" : "tooltip.irons_jewelry.bonus_header").withStyle(headerStyle);
        var bonuses = this.bonuses().stream().map(tuple -> {
            MutableComponent component = null;
            if (tuple.getA().parameterValue().isEmpty()) {
                // Value is not hardcoded, it is dependent on the material this part is made from
                // Text returned is: "Bonus x (from part y)"
                component = Component.translatable("tooltip.irons_jewelry.bonus_with_source",
                        Component.translatable(tuple.getB().bonusType().getDescriptionId()), Component.translatable(tuple.getA().part().value().descriptionId()));
            } else {
                // Value is hardcoded. Text returned is: "Bonus x (y)" for some value y
                var entries = tuple.getA().parameterValue().get();
                var bonus = tuple.getB().bonusType();
                IBonusParameterType type = bonus.getParameterType();
                var value = type.resolve(entries);
                if (value.isPresent()) {
                    Optional<String> string = type.getValueDescriptionId(value.get());
                    if (string.isPresent()) {
                        component = Component.translatable("tooltip.irons_jewelry.bonus_with_direct_source", Component.translatable(bonus.getDescriptionId()), Component.translatable(string.get()));
                    }
                }
            }
            if (component == null) {
                component = Component.translatable(tuple.getB().bonusType().getDescriptionId());
            }
            return component.withStyle(infoStyle);
        }).toList();
        var tooltip = new ArrayList<Component>();
        tooltip.add(title);
        tooltip.add(partHeader);
        tooltip.addAll(parts);
        if (!bonuses.isEmpty()) {
            tooltip.add(Component.empty());
            tooltip.add(bonusHeader);
            tooltip.addAll(bonuses);
        }
        return tooltip;
    }
}
