package io.redspace.ironsjewelry.gameplay.item;

import io.redspace.ironsjewelry.core.data.PlayerData;
import io.redspace.ironsjewelry.registry.ComponentRegistry;
import io.redspace.ironsjewelry.registry.DataAttachmentRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class PatternRecipeItem extends Item {
    public PatternRecipeItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        var stack = pPlayer.getItemInHand(pUsedHand);
        if (stack.has(ComponentRegistry.STORED_PATTERN)) {
            var pattern = stack.get(ComponentRegistry.STORED_PATTERN);
            var playerData = pPlayer.getData(DataAttachmentRegistry.PLAYER_DATA);
            if (!playerData.isLearned(pattern)) {
                if (pPlayer instanceof ServerPlayer serverPlayer) {
                    playerData.learn(serverPlayer, pattern);
                    if (!pPlayer.isCreative()) {
                        stack.shrink(1);
                        pPlayer.setItemInHand(pUsedHand, stack);
                    }
                } else {
                    pPlayer.playSound(SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 1f, 1f);
                    pPlayer.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1f, 1f);
                }

                return InteractionResultHolder.consume(stack);
            }

        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        super.appendHoverText(stack, pContext, pTooltipComponents, pTooltipFlag);
        if (stack.has(ComponentRegistry.STORED_PATTERN)) {
            var pattern = stack.get(ComponentRegistry.STORED_PATTERN);

            pTooltipComponents.add(Component.translatable("tooltip.irons_jewelry.stored_pattern", Component.translatable(pattern.getDescriptionId()).withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.YELLOW));
            var player = Minecraft.getInstance().player;
            if (player != null) {
                var playerData = PlayerData.get(player);
                if (!playerData.isLearned(pattern)) {
                    pTooltipComponents.add(Component.translatable("tooltip.irons_jewelry.use_to_learn", Component.keybind(Minecraft.getInstance().options.keyUse.getName())).withStyle(ChatFormatting.GREEN));
                } else {
                    pTooltipComponents.add(Component.translatable("tooltip.irons_jewelry.already_learned").withStyle(ChatFormatting.RED));
                }
            }
        }
    }
}
