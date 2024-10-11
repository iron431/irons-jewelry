package io.redspace.ironsjewelry.core.data;

import io.redspace.ironsjewelry.core.data_registry.PatternDataHandler;
import io.redspace.ironsjewelry.network.packets.SyncPlayerDataPacket;
import io.redspace.ironsjewelry.registry.DataAttachmentRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class PlayerData {
    public static class Serializer implements IAttachmentSerializer<CompoundTag, PlayerData> {
        private static final String LEARNED_PATTERNS = "learned_patterns";

        @Override
        public PlayerData read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
            var data = new PlayerData();
            var learnedPatterns = tag.getList(LEARNED_PATTERNS, StringTag.TAG_STRING);
            for (Tag stringTag : learnedPatterns) {
                try {
                    var string = stringTag.getAsString();
                    var pattern = PatternDataHandler.get(ResourceLocation.parse(string));
                    data.learnedPatterns.add(pattern);
                } catch (Exception e) {
                    continue;
                }
            }
            return data;
        }

        @Override
        public @Nullable CompoundTag write(PlayerData attachment, HolderLookup.Provider provider) {
            CompoundTag tag = new CompoundTag();

            var list = new ListTag();
            attachment.learnedPatterns.forEach(patternDefinition -> list.add(StringTag.valueOf(patternDefinition.id().toString())));
            tag.put(LEARNED_PATTERNS, list);

            return tag;
        }

        public static void networkWrite(FriendlyByteBuf buf, PlayerData playerData) {
            buf.writeInt(playerData.learnedPatterns.size());
            for (PatternDefinition pattern : playerData.learnedPatterns) {
                buf.writeResourceLocation(pattern.id());
            }
        }

        public static PlayerData networkRead(FriendlyByteBuf buf) {
            var playerData = new PlayerData();
            int i = buf.readInt();
            for (int j = 0; j < i; j++) {
                try {
                    playerData.learnedPatterns.add(PatternDataHandler.get(buf.readResourceLocation()));
                } catch (Exception e) {
                    continue;
                }
            }
            return playerData;
        }
    }

    private final Set<PatternDefinition> learnedPatterns = new HashSet<>();

    public Set<PatternDefinition> getLearnedPatterns() {
        return learnedPatterns;
    }

    public boolean learn(ServerPlayer serverPlayer, PatternDefinition patternDefinition) {
        if (learnedPatterns.add(patternDefinition)) {
            sync(serverPlayer);
            return true;
        }
        return false;
    }

    public void sync(ServerPlayer serverPlayer) {
        PacketDistributor.sendToPlayer(serverPlayer, new SyncPlayerDataPacket(this));
    }

    public boolean isLearned(PatternDefinition definition) {
        return learnedPatterns.contains(definition);
    }

    public void clear() {
        this.learnedPatterns.clear();
    }

    public static PlayerData get(Player player) {
        return player.getData(DataAttachmentRegistry.PLAYER_DATA);
    }
}
