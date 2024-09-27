package io.redspace.ironsjewelry.gameplay.block.jewelcrafting_station;

import io.redspace.ironsjewelry.registry.BlockRegistry;
import io.redspace.ironsjewelry.registry.MenuRegistry;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class JewelcraftingStationMenu extends AbstractContainerMenu {
    private SimpleContainer inputContainer;
    private final ResultContainer resultSlots = new ResultContainer();

    public JewelcraftingStationMenu(int pContainerId, Inventory pPlayerInventory) {
        this(pContainerId, pPlayerInventory, ContainerLevelAccess.NULL);
    }
    int SCROLL_AREA_OFFSET = 30;

    private final ContainerLevelAccess access;
    private final Player player;
    public JewelcraftingStationMenu(int pContainerId, Inventory pPlayerInventory, ContainerLevelAccess pAccess) {
        super(MenuRegistry.JEWELCRAFTING_MENU.get(), pContainerId);
        this.access = pAccess;
        this.player = pPlayerInventory.player;

//        this.addSlot(new ResultSlot(pPlayerInventory.player, this.craftSlots, this.resultSlots, 0, 124, 35));

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

    //TODO: implement me!
    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return null;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(this.access, pPlayer, BlockRegistry.JEWELCRAFTING_STATION_BLOCK.value());
    }
}
