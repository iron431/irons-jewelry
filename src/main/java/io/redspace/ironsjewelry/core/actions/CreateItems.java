package io.redspace.ironsjewelry.core.actions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.core.IAction;
import io.redspace.ironsjewelry.core.data.BonusInstance;
import io.redspace.ironsjewelry.core.data.QualityScalar;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public record CreateItems(
        Optional<QualityScalar> chance,
        Holder<Item> item,
        Optional<Holder<SoundEvent>> sound,
        QualityScalar minCount,
        QualityScalar maxCount
) implements IAction {
    public static final MapCodec<CreateItems> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            QualityScalar.CODEC.optionalFieldOf("chance").forGetter(CreateItems::chance),
            BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("item").forGetter(CreateItems::item),
            BuiltInRegistries.SOUND_EVENT.holderByNameCodec().optionalFieldOf("sound").forGetter(CreateItems::sound),
            QualityScalar.CODEC.fieldOf("minCount").forGetter(CreateItems::minCount),
            QualityScalar.CODEC.fieldOf("maxCount").forGetter(CreateItems::maxCount)
    ).apply(builder, CreateItems::new));

    @Override
    public void apply(ServerLevel serverLevel, double quality, boolean applyToSelf, ServerPlayer wearer, Entity entity) {
        Vec3 origin = applyToSelf ? wearer.getBoundingBox().getCenter() : entity.getBoundingBox().getCenter();
        var random = serverLevel.getRandom();
        if (chance.isPresent()) {
            var threshold = chance.get().sample(quality);
            if (random.nextFloat() > threshold) {
                return;
            }
        }
        int min = (int) minCount.sample(quality);
        int max = (int) maxCount.sample(quality);
        int count = Math.max(random.nextIntBetweenInclusive(min, max), 0);
        for (int i = 0; i < count; i++) {
            ItemStack stack = new ItemStack(this.item);
            float speed = 0.2f + .035f * Math.max(count, 8) * (1 + random.nextFloat()) * .5f;
            Vec3 motion = new Vec3(random.nextFloat() - 0.5, random.nextFloat() * 2 + 1, random.nextFloat() - 0.5);
            motion = motion.normalize().scale(speed);
            var itemEntity = new ItemEntity(serverLevel, origin.x, origin.y, origin.z, stack, motion.x, motion.y, motion.z);
            itemEntity.age = 6000;
            itemEntity.lifespan += 6000; // trick the item entity to be unable to merge, yet still have a regular lifespan of 6000 ticks
            itemEntity.setPickUpDelay(20);
            serverLevel.addFreshEntity(itemEntity);
        }
        this.sound.ifPresent(sound -> serverLevel.playSound(null, origin.x, origin.y, origin.z, sound.value(), SoundSource.PLAYERS, 1.5f, random.nextIntBetweenInclusive(8, 12) * .1f));
    }


    @Override
    public Component formatTooltip(BonusInstance bonusInstance, boolean applyToSelf) {
        var quality = bonusInstance.quality();
        int min = (int) minCount.sample(quality);
        int max = (int) maxCount.sample(quality);
        boolean constant = min == max;
        boolean singular = constant && max == 1;
        Component itemName = Component.translatable(this.item.value().getDescriptionId()).withStyle(ChatFormatting.GREEN);
        if (singular) {
            if (this.chance.isPresent()) {
                var chance = (int) (this.chance.get().sample(quality) * 100);
                return Component.translatable("action.irons_jewelry.create_items.chance.singular", chance, itemName);
            } else {
                return Component.translatable("action.irons_jewelry.create_items.singular", itemName);
            }
        } else {
            Component range = constant ? Component.literal(String.valueOf(min)) : Component.literal(String.format("%s-%s", min, max)).withStyle(ChatFormatting.GOLD);
            if (this.chance.isPresent()) {
                var chance = (int) (this.chance.get().sample(quality) * 100);
                return Component.translatable("action.irons_jewelry.create_items.chance", chance, range, itemName);
            } else {
                return Component.translatable("action.irons_jewelry.create_items", range, itemName);
            }
        }
    }

    @Override
    public MapCodec<? extends IAction> codec() {
        return CODEC;
    }
}
