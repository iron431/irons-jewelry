package io.redspace.ironsjewelry.core.data;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.Bonus;
import io.redspace.ironsjewelry.network.packets.SyncPlayerDataPacket;
import io.redspace.ironsjewelry.registry.BonusRegistry;
import io.redspace.ironsjewelry.registry.DataAttachmentRegistry;
import io.redspace.ironsjewelry.registry.JewelryDataRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PlayerData {


    private final Set<Holder<PatternDefinition>> learnedPatterns = new HashSet<>();
    private final Map<ResourceLocation, CooldownInstance> cooldowns = new HashMap<>();

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

    public boolean isOnCooldown(Bonus bonus) {
        return isOnCooldown(BonusRegistry.BONUS_REGISTRY.getKey(bonus));
    }

    public boolean isOnCooldown(ResourceLocation resourceLocation) {
        return cooldowns.containsKey(resourceLocation) && cooldowns.get(resourceLocation).firstTick;
    }

    public void addCooldown(Bonus bonus, int ticks) {
        var k = BonusRegistry.BONUS_REGISTRY.getKey(bonus);
        var cooldown = cooldowns.get(k);
        if (cooldown != null && cooldown.remainingTicks >= ticks) {
            return;
        }
        cooldowns.put(k, new CooldownInstance(ticks));
    }

    public boolean learn(ServerPlayer serverPlayer, Holder<PatternDefinition> patternDefinition) {
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
    public static class Serializer implements IAttachmentSerializer<CompoundTag, PlayerData> {
        private static final String LEARNED_PATTERNS = "learned_patterns";
        private static final String COOLDOWNS = "cooldowns";

        @Override
        public PlayerData read(IAttachmentHolder holder, CompoundTag compoundTag, HolderLookup.Provider provider) {
            var data = new PlayerData();
            var learnedPatterns = compoundTag.getList(LEARNED_PATTERNS, StringTag.TAG_STRING);
            var holderGetter = provider.asGetterLookup().lookupOrThrow(JewelryDataRegistries.PATTERN_REGISTRY_KEY);
            for (Tag stringTag : learnedPatterns) {
                try {
                    var string = stringTag.getAsString();
                    var pattern = holderGetter.get(ResourceKey.create(JewelryDataRegistries.PATTERN_REGISTRY_KEY, ResourceLocation.parse(string)));
                    pattern.ifPresent(data.learnedPatterns::add);
                } catch (Exception e) {
                    continue;
                }
            }
            var cooldowns = compoundTag.getList(COOLDOWNS, CompoundTag.TAG_COMPOUND);
            for (Tag tag : cooldowns) {
                try {
                    var cooldown = (CompoundTag) tag;
                    var id = ResourceLocation.parse(cooldown.getString("id"));
                    var rt = cooldown.getInt("rt");
                    var tt = cooldown.getInt("tt");
                    data.cooldowns.put(id, new CooldownInstance(rt, tt));
                } catch (Exception e) {
                    continue;
                }
            }
            return data;
        }

        @Override
        public @Nullable CompoundTag write(PlayerData attachment, HolderLookup.Provider provider) {
            CompoundTag tag = new CompoundTag();

            var patterns = new ListTag();
            attachment.learnedPatterns.forEach(patternDefinition -> patterns.add(StringTag.valueOf(patternDefinition.getKey().location().toString())));
            tag.put(LEARNED_PATTERNS, patterns);

            var cooldowns = new ListTag();
            attachment.cooldowns.forEach((r, cd) -> {
                var c = new CompoundTag();
                c.put("id", StringTag.valueOf(r.toString()));
                c.put("rt", IntTag.valueOf(cd.remainingTicks));
                c.put("tt", IntTag.valueOf(cd.totalTicks));
                cooldowns.add(c);
            });
            tag.put(COOLDOWNS, cooldowns);

            return tag;
        }

        public static void networkWrite(RegistryFriendlyByteBuf buf, PlayerData playerData) {
            buf.writeInt(playerData.learnedPatterns.size());
            for (Holder<PatternDefinition> pattern : playerData.learnedPatterns) {
                try {
                    buf.writeResourceLocation(Objects.requireNonNull(pattern.getKey()).location());
                } catch (Exception e) {
                    buf.writeResourceLocation(IronsJewelry.id("empty"));
                }
            }
        }

        public static PlayerData networkRead(RegistryFriendlyByteBuf buf) {
            var playerData = new PlayerData();
            int i = buf.readInt();
            var registry = JewelryDataRegistries.patternRegistry(buf.registryAccess());
            for (int j = 0; j < i; j++) {
                try {
                    playerData.learnedPatterns.add(registry.wrapAsHolder(Objects.requireNonNull(registry.get(buf.readResourceLocation()))));
                } catch (Exception e) {
                    continue;
                }
            }
            return playerData;
        }
    }
}
