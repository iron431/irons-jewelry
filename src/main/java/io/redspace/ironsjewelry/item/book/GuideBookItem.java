package io.redspace.ironsjewelry.item.book;

import io.redspace.ironsjewelry.utils.MinecraftInstanceHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class GuideBookItem extends Item {
    private static final Component DESC = Component.translatable("item.irons_jewelry.jewelcrafting_guide.description").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);

    public GuideBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(DESC);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(getDescriptionId()).withColor(0x547bc6);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        var stack = player.getItemInHand(usedHand);
        if (player instanceof LocalPlayer) {
            MinecraftInstanceHelper.INSTANCE.openGuidebookScreen();
        }
        player.playSound(SoundEvents.BOOK_PAGE_TURN);
        return InteractionResultHolder.success(stack);
    }
}
