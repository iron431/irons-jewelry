package io.redspace.ironsjewelry.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.core.data_registry.PatternDataHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @param patternId Holds the structure for the piece of jewelry, and how its parts yield a bonus
 * @param parts     The part instances this piece of jewelry is made of
 */
public record JewelryData(ResourceLocation patternId, List<PartInstance> parts) {
    public static final Codec<JewelryData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf("pattern").forGetter(JewelryData::patternId),
            Codec.list(PartInstance.CODEC).fieldOf("parts").forGetter(JewelryData::parts)
    ).apply(builder, JewelryData::new));

    public static final StreamCodec<FriendlyByteBuf, JewelryData> STREAM_CODEC = StreamCodec.of((buf, data) -> {
        buf.writeResourceLocation(data.patternId);
        buf.writeInt(data.parts.size());
        for (int i = 0; i < data.parts.size(); i++) {
            buf.writeResourceLocation(data.parts.get(i).part().id());
            buf.writeResourceLocation(data.parts.get(i).material().id());
        }
    }, (buf) -> {
        List<PartInstance> parts = new ArrayList<>();
        ResourceLocation patternid = buf.readResourceLocation();
        int i = buf.readInt();
        for (int j = 0; j < i; j++) {
            parts.add(PartInstance.fromResource(buf.readResourceLocation(), buf.readResourceLocation()));
        }
        return new JewelryData(patternid, parts);
    });

    public List<BonusInstance> getBonuses() {
        var pattern = PatternDataHandler.INSTANCE.get(patternId);
        var materials = this.parts.stream().collect(Collectors.toMap(instance -> instance.part().id(), instance -> instance.material()));
        return pattern().bonuses().stream().flatMap(source -> materials.get(source.partIdForBonus()).bonuses().stream().map(bonus -> new BonusInstance(bonus, materials.get(source.partIdForQuality()).quality() * pattern.qualityMultiplier()))).toList();
    }

    public PatternDefinition pattern() {
        return PatternDataHandler.INSTANCE.get(this.patternId);
    }

    @Override
    public int hashCode() {
        return patternId.hashCode() * 31 + parts.hashCode();
    }
}
