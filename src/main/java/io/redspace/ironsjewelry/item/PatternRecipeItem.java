package io.redspace.ironsjewelry.item;

import io.redspace.ironsjewelry.core.data.PatternDefinition;
import io.redspace.ironsjewelry.core.data.PlayerData;
import io.redspace.ironsjewelry.core.data.StoredPatternData;
import io.redspace.ironsjewelry.registry.DataAttachmentRegistry;
import io.redspace.ironsjewelry.registry.ItemRegistry;
import io.redspace.ironsjewelry.utils.MinecraftInstanceHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Consumer;

public class PatternRecipeItem extends Item {
    public PatternRecipeItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        var stack = pPlayer.getItemInHand(pUsedHand);
        if (StoredPatternData.has(stack)) {
            var pattern = StoredPatternData.get(stack);
            var playerData = pPlayer.getData(DataAttachmentRegistry.PLAYER_DATA);
            if (!playerData.isLearned(pattern)) {
                if (pPlayer instanceof ServerPlayer serverPlayer) {
                    playerData.learnAndSync(serverPlayer, pattern);
                    if (!pPlayer.isCreative()) {
                        stack.shrink(1);
                        pPlayer.setItemInHand(pUsedHand, stack);
                    }
                } else {
                    pPlayer.playSound(SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 1f, 1f);
                    pPlayer.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1f, 1f);
                }
                return InteractionResult.CONSUME;
            } else {
                if (pPlayer instanceof ServerPlayer player) {
                    player.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("tooltip.irons_jewelry.pattern_already_learned", Component.translatable(pattern.value().descriptionId())).withStyle(ChatFormatting.RED)));
                } else {
                    pPlayer.playSound(SoundEvents.BOOK_PAGE_TURN, 1f, 1f);
                }
            }
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void appendHoverText(ItemStack stack, TooltipContext pContext, TooltipDisplay display, Consumer<Component> builder, TooltipFlag pTooltipFlag) {
        super.appendHoverText(stack, pContext, display, builder, pTooltipFlag);
        if (StoredPatternData.has(stack)) {
            var pattern = StoredPatternData.get(stack);
            builder.accept(Component.translatable("tooltip.irons_jewelry.stored_pattern", Component.translatable(pattern.value().descriptionId()).withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.GRAY));
            var player = MinecraftInstanceHelper.getPlayer();
            if (player != null) {
                var playerData = PlayerData.get(player);
                if (!playerData.isLearned(pattern)) {
                    builder.accept(Component.translatable("tooltip.irons_jewelry.use_to_learn", Component.keybind("key.use")).withStyle(ChatFormatting.BLUE));
                } else {
                    builder.accept(Component.translatable("tooltip.irons_jewelry.already_learned").withStyle(ChatFormatting.RED));
                }
            }
        }
    }

    public static ItemStack of(Holder<PatternDefinition> patternDefinitionHolder) {
        ItemStack stack = new ItemStack(ItemRegistry.RECIPE);
        StoredPatternData.set(stack, patternDefinitionHolder);
        return stack;
    }
}
