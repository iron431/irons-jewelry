package io.redspace.ironsjewelry.gameplay.block.jewelcrafting_station;

import io.redspace.ironsjewelry.core.data.JewelryData;
import io.redspace.ironsjewelry.core.data.MaterialDefinition;
import io.redspace.ironsjewelry.core.data.PartDefinition;
import io.redspace.ironsjewelry.core.data.PatternDefinition;
import io.redspace.ironsjewelry.core.data_registry.MaterialDataHandler;
import io.redspace.ironsjewelry.network.packets.SyncJewelcraftingSlotStates;
import io.redspace.ironsjewelry.registry.BlockRegistry;
import io.redspace.ironsjewelry.registry.ComponentRegistry;
import io.redspace.ironsjewelry.registry.ItemRegistry;
import io.redspace.ironsjewelry.registry.MenuRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
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
        this.resultSlot = this.addSlot(new Slot(resultContainer, 0, 173, 33) {
            @Override
            public boolean mayPlace(ItemStack pStack) {
                return false;
            }
        });
        //Workspace slots. We allocate 10 as max, although most will be inaccessible at any given time. If a modder wants to add a piece of jewelry with over ten ingredients, good luck.
        for (int i = 0; i < 10; i++) {
            this.workspaceSlots.add((JewelcraftingInputSlot) this.addSlot(new JewelcraftingInputSlot(workspaceContainer, i, -20, -20)));
        }

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
                }
            }
            var jewelryData = new JewelryData(currentPattern, parts);
            if (jewelryData.isValid()) {
                result = new ItemStack(ItemRegistry.RING.get());
                result.set(ComponentRegistry.JEWELRY_COMPONENT, jewelryData);
            }
        }
        resultSlot.set(result);
    }

//    private void refreshWorkspace() {
//        PatternDefinition pattern = availablePatterns.get(selectedPattern);
//        //clear previous
//        //TODO: implemenmt me
//
//        //setup new
//        int ingredientCount = pattern.partTemplate().size();
//        workspaceContainer = new SimpleContainer(ingredientCount);
//        List<Vec2> locations;
//        int centerX = leftPos + 64 + 89 / 2;
//        int centerY = topPos + 10 + 66 / 2;
//        for (int i = 0; i < ingredientCount; i++) {
//            workspaceSlots.add(new Slot(workspaceContainer, i, centerX, centerY));
//        }
//    }

    //TODO: implement me!
    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return null;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(this.access, pPlayer, BlockRegistry.JEWELCRAFTING_STATION_BLOCK.value());
    }

    //TODO: return true if this pattern is able to be crafted here
    private boolean validateAvailablePattern(PatternDefinition patternDefinition) {
        return true;
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
                //want to make spiral pattern, starting from negative x-axis
                int radius = (int) Mth.lerp(ingredientCount / 10f, 16, 36 + 1);
                int anglePerSlot = 360 / ingredientCount;
                for (int i = 0; i < ingredientCount; i++) {
                    int x = (int) (radius * -Mth.cos(i * anglePerSlot * Mth.DEG_TO_RAD)) + centerX - 8;
                    int y = (int) (radius * 0.5 * -Mth.sin(i * anglePerSlot * Mth.DEG_TO_RAD)) + centerY - 8;
                    this.workspaceSlots.get(i).setup(x, y, true);
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
}
