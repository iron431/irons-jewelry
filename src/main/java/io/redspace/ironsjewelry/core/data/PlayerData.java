package io.redspace.ironsjewelry.core.data;

import com.mojang.serialization.Codec;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.bonuses.BonusType;
import io.redspace.ironsjewelry.network.packets.SyncPlayerDataPacket;
import io.redspace.ironsjewelry.registry.DataAttachmentRegistry;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class PlayerData {


    private final Set<Holder<PatternDefinition>> learnedPatterns = new HashSet<>();
    private final Map<Identifier, CooldownInstance> cooldowns = new HashMap<>();
    private int bookmarkIndex = -1;

    public int getBookmarkIndex() {
        return bookmarkIndex;
    }

    public boolean hasBookmark() {
        return bookmarkIndex >= 0;
    }

    public void removeBookmark() {
        bookmarkIndex = -1;
    }

    public void setBookmarkIndex(int bookmarkIndex) {
        this.bookmarkIndex = bookmarkIndex;
    }

    public Set<Holder<PatternDefinition>> getLearnedPatterns() {
        return learnedPatterns;
    }

    public void tickCooldowns(int actualTicks) {
        if (!cooldowns.isEmpty()) {
            var spells = cooldowns.entrySet().stream().filter(x -> decrementCooldown(x.getValue(), actualTicks)).toList();
            spells.forEach(spell -> cooldowns.remove(spell.getKey()));
        }
    }

    public boolean decrementCooldown(CooldownInstance c, int amount) {
        c.decrementBy(amount);
        return c.getRemainingTicks() <= 0;
    }

    public boolean isOnCooldown(BonusType bonusType) {
        return isOnCooldown(IronsJewelryRegistries.BONUS_TYPE_REGISTRY.getKey(bonusType));
    }

    public boolean isOnCooldown(Identifier Identifier) {
        return cooldowns.containsKey(Identifier) && cooldowns.get(Identifier).firstTick;
    }

    public void addCooldown(BonusType bonusType, int ticks) {
        var k = IronsJewelryRegistries.BONUS_TYPE_REGISTRY.getKey(bonusType);
        var cooldown = cooldowns.get(k);
        if (cooldown != null && cooldown.remainingTicks >= ticks) {
            return;
        }
        cooldowns.put(k, new CooldownInstance(ticks));
    }


    public boolean learn(Holder<PatternDefinition> patternDefinition) {
        return learnedPatterns.add(patternDefinition);
    }

    public boolean learnAndSync(ServerPlayer serverPlayer, Holder<PatternDefinition> patternDefinition) {
        if (learnedPatterns.add(patternDefinition)) {
            sync(serverPlayer);
            return true;
        }
        return false;
    }

    public void sync(ServerPlayer serverPlayer) {
        PacketDistributor.sendToPlayer(serverPlayer, new SyncPlayerDataPacket(this));
    }

    public boolean isLearned(Holder<PatternDefinition> definition) {
        return learnedPatterns.contains(definition);
    }

    public void clear() {
        this.learnedPatterns.clear();
    }

    public static PlayerData get(Player player) {
        return player.getData(DataAttachmentRegistry.PLAYER_DATA);
    }

    /**
     * Serializer
     */
    public static class Serializer implements IAttachmentSerializer<PlayerData> {
        private static final String LEARNED_PATTERNS = "learned_patterns";
        private static final String COOLDOWNS = "cooldowns";

        @Override
        public PlayerData read(IAttachmentHolder holder, ValueInput input) {
            var data = new PlayerData();
            var learnedPatternsList = input.listOrEmpty(LEARNED_PATTERNS, Codec.STRING);
            if (holder instanceof Player player) {
                var holderGetter = player.registryAccess().lookupOrThrow(IronsJewelryRegistries.Keys.PATTERN_REGISTRY_KEY);
                for (String string : learnedPatternsList) {
                    try {
                        var pattern = holderGetter.get(ResourceKey.create(IronsJewelryRegistries.Keys.PATTERN_REGISTRY_KEY, Identifier.parse(string)));
                        pattern.ifPresent(data.learnedPatterns::add);
                    } catch (Exception e) {
                        continue;
                    }
                }
            }
            var cooldownsList = input.childrenListOrEmpty(COOLDOWNS);
            for (ValueInput child : cooldownsList) {
                try {
                    var id = Identifier.parse(child.getStringOr("id", ""));
                    var rt = child.getIntOr("rt", 0);
                    var tt = child.getIntOr("tt", 0);
                    data.cooldowns.put(id, new CooldownInstance(rt, tt));
                } catch (Exception e) {
                    continue;
                }
            }
            data.bookmarkIndex = input.getIntOr("bookmark", -1);
            return data;
        }

        @Override
        public boolean write(PlayerData attachment, ValueOutput output) {
            var patternsList = output.list(LEARNED_PATTERNS, Codec.STRING);
            attachment.learnedPatterns.forEach(patternDefinition -> patternsList.add(patternDefinition.getKey().identifier().toString()));

            var cooldownsList = output.childrenList(COOLDOWNS);
            attachment.cooldowns.forEach((r, cd) -> {
                var child = cooldownsList.addChild();
                child.putString("id", r.toString());
                child.putInt("rt", cd.remainingTicks);
                child.putInt("tt", cd.totalTicks);
            });

            if (attachment.hasBookmark()) {
                output.putInt("bookmark", attachment.getBookmarkIndex());
            }
            return !output.isEmpty();
        }

        public static void networkWrite(RegistryFriendlyByteBuf buf, PlayerData playerData) {
            buf.writeInt(playerData.learnedPatterns.size());
            for (Holder<PatternDefinition> pattern : playerData.learnedPatterns) {
                try {
                    buf.writeIdentifier(Objects.requireNonNull(pattern.getKey()).identifier());
                } catch (Exception e) {
                    buf.writeIdentifier(IronsJewelry.id("empty"));
                }
            }
            buf.writeInt(playerData.getBookmarkIndex());
        }

        public static PlayerData networkRead(RegistryFriendlyByteBuf buf) {
            var playerData = new PlayerData();
            int i = buf.readInt();
            var registry = IronsJewelryRegistries.patternRegistry(buf.registryAccess());
            for (int j = 0; j < i; j++) {
                try {
                    registry.get(buf.readIdentifier()).ifPresent(playerData.learnedPatterns::add);
                } catch (Exception e) {
                    continue;
                }
            }
            playerData.setBookmarkIndex(buf.readInt());
            return playerData;
        }
    }
}
