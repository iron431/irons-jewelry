package io.redspace.ironsjewelry.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * @param patternId Holds the structure for the piece of jewelry, and how its parts yield a bonus
 * @param parts     The partId instances this piece of jewelry is made of
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
            buf.writeResourceLocation(data.parts.get(i).partId());
            buf.writeResourceLocation(data.parts.get(i).materialId());
        }
    }, (buf) -> {
        List<PartInstance> parts = new ArrayList<>();
        ResourceLocation patternid = buf.readResourceLocation();
        int i = buf.readInt();
        for (int j = 0; j < i; j++) {
            parts.add(new PartInstance(buf.readResourceLocation(), buf.readResourceLocation()));
        }
        return new JewelryData(patternid, parts);
    });

    @Override
    public int hashCode() {
        return patternId.hashCode() * 31 + parts.hashCode();
    }
}
