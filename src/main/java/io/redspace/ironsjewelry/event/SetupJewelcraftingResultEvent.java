package io.redspace.ironsjewelry.event;

import io.redspace.ironsjewelry.core.data.PatternDefinition;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * SetupJewelcraftingResultEvent is fired when a potential piece of jewelry is created in the Jewelcrafting Menu or Jewelcrafting Scren Preview
 * This event is fired even if the result is empty
 * This event is not fired if no pattern is selected
 */
public class SetupJewelcraftingResultEvent extends Event implements ICancellableEvent {
    private final Holder<PatternDefinition> pattern;
    private final Player player;
    private final ItemStack originalResult;
    private ItemStack result;

    public SetupJewelcraftingResultEvent(
            Holder<PatternDefinition> pattern,
            Player player,
            ItemStack result) {
        this.pattern = pattern;
        this.player = player;
        this.originalResult = result;
        this.result = originalResult;
    }

    public Holder<PatternDefinition> getPattern() {
        return pattern;
    }

    public Player getPlayer() {
        return player;
    }

    public ItemStack getResult() {
        return result;
    }

    public ItemStack getOriginalResult() {
        return originalResult;
    }

    public void setResult(ItemStack result) {
        this.result = result;
    }
}
