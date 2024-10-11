package io.redspace.ironsjewelry.gameplay.block.jewelcrafting_station;

import io.redspace.ironsjewelry.core.data.*;
import io.redspace.ironsjewelry.core.data_registry.MaterialDataHandler;
import io.redspace.ironsjewelry.network.packets.SyncJewelcraftingSlotStates;
import io.redspace.ironsjewelry.registry.BlockRegistry;
import io.redspace.ironsjewelry.registry.ComponentRegistry;
import io.redspace.ironsjewelry.registry.MenuRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JewelcraftingStationMenu extends AbstractContainerMenu {
    public static class JewelcraftingInputSlot extends Slot {
        public boolean active;
        public int currentCost;

        public JewelcraftingInputSlot(Container pContainer, int pSlot, int pX, int pY) {
            super(pContainer, pSlot, pX, pY);
        }

        void setup(int x, int y, boolean active) {
            this.x = x;
            this.y = y;
            this.active = active;
        }

        @Override
        public boolean isActive() {
            return active;
        }

        @Override
        public boolean mayPlace(ItemStack pStack) {
            return this.isActive();
        }
    }

    private final SimpleContainer workspaceContainer = new SimpleContainer(10) {
        @Override
        public void setChanged() {
            super.setChanged();
            JewelcraftingStationMenu.this.setupResult();
        }
    };
    public final List<JewelcraftingInputSlot> workspaceSlots = new ArrayList<>();
    private final SimpleContainer resultContainer = new SimpleContainer(1);
    public final Slot resultSlot;

    public JewelcraftingStationMenu(int pContainerId, Inventory pPlayerInventory) {
        this(pContainerId, pPlayerInventory, ContainerLevelAccess.NULL);
    }

    int SCROLL_AREA_OFFSET = 30;

    private final ContainerLevelAccess access;
    private final Player player;
    @Nullable
    private PatternDefinition currentPattern;

    public JewelcraftingStationMenu(int pContainerId, Inventory pPlayerInventory, ContainerLevelAccess pAccess) {
        super(MenuRegistry.JEWELCRAFTING_MENU.get(), pContainerId);
        this.access = pAccess;
        this.player = pPlayerInventory.player;
        //Workspace slots. We allocate 10 as max, although most will be inaccessible at any given time. If a modder wants to add a piece of jewelry with over ten ingredients, good luck.
        for (int i = 0; i < 10; i++) {
            this.workspaceSlots.add((JewelcraftingInputSlot) this.addSlot(new JewelcraftingInputSlot(workspaceContainer, i, -20, -20)));
        }
        this.resultSlot = this.addSlot(new Slot(resultContainer, 0, 173, 33) {
            @Override
            public boolean mayPlace(ItemStack pStack) {
                return false;
            }

            @Override
            public void onTake(Player pPlayer, ItemStack pStack) {
                super.onTake(pPlayer, pStack);
                for (JewelcraftingInputSlot slot : workspaceSlots) {
                    if (!slot.isActive()) {
                        break;
                    }
                    slot.remove(slot.currentCost);
                }
                JewelcraftingStationMenu.this.setupResult();
            }
        });


        //Player Inventory
        for (int k = 0; k < 3; k++) {
            for (int i1 = 0; i1 < 9; i1++) {
                this.addSlot(new Slot(pPlayerInventory, i1 + k * 9 + 9, SCROLL_AREA_OFFSET + 8 + i1 * 18, 84 + k * 18));
            }
        }
        //Player Hotbar
        for (int l = 0; l < 9; l++) {
            this.addSlot(new Slot(pPlayerInventory, l, SCROLL_AREA_OFFSET + 8 + l * 18, 142));
        }
    }

    public boolean isWorkspaceSlot(Slot slot) {
        return slot.index < 10 && slot.container.equals(workspaceContainer);
    }

    private void setupResult() {
        ItemStack result = ItemStack.EMPTY;
        if (this.currentPattern != null) {
            var parts = new HashMap<PartDefinition, MaterialDefinition>();
            var requiredIngredients = currentPattern.partTemplate();
            for (int i = 0; i < requiredIngredients.size(); i++) {
                var ingredient = requiredIngredients.get(i);
                var input = workspaceSlots.get(i).getItem();
                var material = MaterialDataHandler.getMaterialForIngredient(input);
                if (material.isPresent() && input.getCount() >= ingredient.materialCost() && ingredient.part().canUseMaterial(material.get().materialType())) {
                    parts.put(ingredient.part(), material.get());
                    workspaceSlots.get(i).currentCost = ingredient.materialCost();
                }
            }
            var jewelryData = new JewelryData(currentPattern, parts);
            if (jewelryData.isValid()) {
                result = new ItemStack(this.currentPattern.jewelryType().item());
                result.set(ComponentRegistry.JEWELRY_COMPONENT, jewelryData);
            }
        }
        resultSlot.set(result);
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int index) {
        ItemStack tryingToMoveCopy = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack baseItemTryingToMove = slot.getItem();
            tryingToMoveCopy = baseItemTryingToMove.copy();
            int result = 10;
            int invBegin = result + 1;
            int hotbarBegin = invBegin + 27;
            int playerEnd = hotbarBegin + 9;
            if (index > result) {
                // Trying to move item that originates in player inventory
                // First, attempt to place in input slots
                boolean flag = this.moveItemStackTo(baseItemTryingToMove, 0, result, false);
                if (!flag) {
                    // We failed. Move hotbar -> inv, or inv -> hotbar
                    if (index >= hotbarBegin) {
                        if (!this.moveItemStackTo(baseItemTryingToMove, invBegin, hotbarBegin, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else {
                        if (!this.moveItemStackTo(baseItemTryingToMove, hotbarBegin, playerEnd, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
            } else {
                // Trying to put workspace item back into inventory
                if (!this.moveItemStackTo(baseItemTryingToMove, invBegin, playerEnd, false)) {
                    return ItemStack.EMPTY;
                }
            }

            // Below copied from crafting table... looks like state tracking

            if (baseItemTryingToMove.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (baseItemTryingToMove.getCount() == tryingToMoveCopy.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(pPlayer, baseItemTryingToMove);
            if (index == 0) {
                pPlayer.drop(baseItemTryingToMove, false);
            }
        }

        return tryingToMoveCopy;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(this.access, pPlayer, BlockRegistry.JEWELCRAFTING_STATION_BLOCK.value());
    }

    /**
     * @param patternDefinition
     * @return returns true if this pattern is valid and can be crafted, ie the player has learned it
     */
    private boolean validateAvailablePattern(PatternDefinition patternDefinition) {
        return patternDefinition.unlockedByDefault() || this.player instanceof ServerPlayer serverPlayer && PlayerData.get(serverPlayer).isLearned(patternDefinition);
    }

    public boolean handleSetPattern(PatternDefinition patternDefinition) {
        //Reset Workspace
        this.clearContainer(player, workspaceContainer);
        this.workspaceSlots.forEach(slot -> slot.setup(-20, -20, false));
        if (validateAvailablePattern(patternDefinition)) {
            this.currentPattern = patternDefinition;
            int ingredientCount = Math.min(10, currentPattern.partTemplate().size());
            int centerX = 64 + 89 / 2;
            int centerY = 10 + 66 / 2;
            if (ingredientCount == 1) {
                this.workspaceSlots.get(0).setup(centerX - 8, centerY - 8, true);
            } else {
                int maxPerRow = ingredientCount == 10 ? 4 : 3;
                int rows = (ingredientCount - 1) / maxPerRow + 1;
                int verticalSpacing = 8;
                int horizontalSpacing = 14;
                int spriteWidth = 16;
                int totalHeight = rows * spriteWidth + (rows - 1) * verticalSpacing;
                for (int i = 0; i < rows; i++) {
                    int boxesInThisRow = Math.min(maxPerRow, ingredientCount - i * maxPerRow);
                    int rowWidth = boxesInThisRow * spriteWidth + horizontalSpacing * (boxesInThisRow - 1);
                    for (int j = 0; j < boxesInThisRow; j++) {
                        int x = centerX - rowWidth / 2 + (spriteWidth + horizontalSpacing) * j;
                        int y = centerY - totalHeight / 2 + (spriteWidth + verticalSpacing) * i;
                        this.workspaceSlots.get(i * maxPerRow + j).setup(x, y, true);
                    }
                }
            }
        }
        //Update client of new slot availability/positions
        if (this.player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new SyncJewelcraftingSlotStates(this.workspaceSlots.stream().map(slot -> new SyncJewelcraftingSlotStates.SlotState(slot.x, slot.y, slot.active)).toList()));
        }
        return true;
    }

    public void handleClientSideSlotSync(List<SyncJewelcraftingSlotStates.SlotState> states) {
        for (int i = 0; i < states.size(); i++) {
            if (i < 10) {
                var s = states.get(i);
                this.workspaceSlots.get(i).setup(s.x(), s.y(), s.enabled());
            }
        }
    }

    @Override
    public void removed(Player pPlayer) {
        super.removed(pPlayer);
        this.access.execute((p_39371_, p_39372_) -> this.clearContainer(pPlayer, this.workspaceContainer));
    }

}
