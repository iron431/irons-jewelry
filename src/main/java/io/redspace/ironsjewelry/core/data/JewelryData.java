package io.redspace.ironsjewelry.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.core.Utils;
import io.redspace.ironsjewelry.core.data_registry.MaterialDataHandler;
import io.redspace.ironsjewelry.core.data_registry.PartDataHandler;
import io.redspace.ironsjewelry.core.data_registry.PatternDataHandler;
import io.redspace.ironsjewelry.registry.ComponentRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            Utils.idCodec(PatternDataHandler::getSafe, PatternDefinition::id).fieldOf("pattern").forGetter(JewelryData::pattern),
            Codec.unboundedMap(
                    Utils.idCodec(PartDataHandler::getSafe, PartDefinition::id),
                    Utils.idCodec(MaterialDataHandler::getSafe, MaterialDefinition::id)).fieldOf("parts").forGetter(JewelryData::parts)
    ).apply(builder, JewelryData::new));

    public static final StreamCodec<FriendlyByteBuf, JewelryData> STREAM_CODEC = StreamCodec.of((buf, data) -> {
        buf.writeResourceLocation(data.pattern.id());
        var parts = data.parts.entrySet();
        buf.writeInt(parts.size());
        for (Map.Entry<PartDefinition, MaterialDefinition> entry : parts) {
            buf.writeResourceLocation(entry.getKey().id());
            buf.writeResourceLocation(entry.getValue().id());
        }
    }, (buf) -> {
        Map<PartDefinition, MaterialDefinition> parts = new HashMap<>();
        ResourceLocation patternid = buf.readResourceLocation();
        int i = buf.readInt();
        for (int j = 0; j < i; j++) {
            var opt1 = PartDataHandler.getSafe(buf.readResourceLocation());
            var opt2 = MaterialDataHandler.getSafe(buf.readResourceLocation());
            if (opt1.isPresent() && opt2.isPresent()) {
                parts.put(opt1.get(), opt2.get());
            }
        }
        return new JewelryData(PatternDataHandler.get(patternid), parts);
    });

    private final PatternDefinition pattern;
    private final Map<PartDefinition, MaterialDefinition> parts;
    private final boolean valid;
    private final List<BonusInstance> bonuses;

    public JewelryData(PatternDefinition pattern, Map<PartDefinition, MaterialDefinition> parts) {
        this.pattern = pattern;
        this.parts = parts;
        this.valid = validate();
        this.bonuses = cacheBonuses();
    }

    private JewelryData() {
        this.pattern = null;
        this.valid = false;
        this.parts = Map.of();
        this.bonuses = List.of();
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


    private boolean validate() {
        if (this.pattern == null) {
            return false;
        }
        for (PartIngredient part : this.pattern.partTemplate()) {
            if (!this.parts.containsKey(part.part())) {
                //Ensure our parts contain everything specified by the pattern
                //TODO: ensure parts dont contain extra things not specifiied by pattern?
                return false;
            }
        }
        return true;
    }

    public Map<PartDefinition, MaterialDefinition> parts() {
        return this.parts;
    }

    public boolean isValid() {
        return valid;
    }

    private List<BonusInstance> cacheBonuses() {
        if (!valid) {
            return List.of();
        }
        return pattern.bonuses().stream().map(source -> source.getBonusFor(this)).toList();
        //return pattern.bonuses().stream().flatMap(source -> parts.get(source.partForBonus()).bonuses().stream().map(bonus -> new BonusInstance(bonus, parts.get(source.partForQuality()).qualityOrSource() * pattern.qualityMultiplier()))).toList();
    }

    public Component getItemName() {
        if (this == NONE) {
            return Component.translatable("item.irons_jewelry.invalid_jewelry");
        }
        //technically, this isn't ordered. but like, it kinda is. essentially uses material arguments in reverse-draw-order, meaning the pinnacle component (ie gem) will be the first argument
        var values = parts.values();
        Component[] ids = new Component[values.size()];
        var itr = values.iterator();
        for (int i = parts.size() - 1; i >= 0; i--) {
            ids[i] = Component.translatable(itr.next().getDescriptionId());
        }
        return Component.translatable(this.pattern.getDescriptionId() + ".item", ids);
    }

    public List<BonusInstance> getBonuses() {
        return this.bonuses;
    }

    public PatternDefinition pattern() {
        return this.pattern;
    }

    @Override
    public int hashCode() {
        return pattern.id().hashCode() * 31 + parts.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && this.hashCode() == obj.hashCode();
    }
}
