package eu.pb4.sgui.virtual.inventory;

import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import eu.pb4.sgui.virtual.VirtualScreenHandlerInterface;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class VirtualScreenHandler extends AbstractContainerMenu implements VirtualScreenHandlerInterface {
    private final SlotGuiInterface gui;

    public VirtualScreenHandler(@Nullable MenuType<?> type, int syncId, SlotGuiInterface gui, Player player) {
        super(type, syncId);
        this.gui = gui;

        setupSlots(player);
    }

    protected void setupSlots(Player player) {
        int n;
        int m;

        for (n = 0; n < this.gui.getVirtualSize(); ++n) {
            Slot slot = this.gui.getSlotRedirect(n);
            if (slot != null) {
                this.addSlot(slot);
            } else {
                this.addSlot(new VirtualSlot(gui, n, 0, 0));
            }
        }

        if (gui.isIncludingPlayer()) {
            int size = this.gui.getHeight() * this.gui.getWidth();
            for (n = 0; n < 4; ++n) {
                for (m = 0; m < 9; ++m) {
                    this.addSlot(new VirtualSlot(gui, m + n * 9 + size, 0, 0));
                }
            }
        } else {
            Inventory playerInventory = player.getInventory();
            for (n = 0; n < 3; ++n) {
                for (m = 0; m < 9; ++m) {
                    this.addSlot(new Slot(playerInventory, m + n * 9 + 9, 0, 0));
                }
            }

            for (n = 0; n < 9; ++n) {
                this.addSlot(new Slot(playerInventory, n, 0, 0));
            }
        }
    }

    @Override
    public void addSlotListener(ContainerListener listener) {
        super.addSlotListener(listener);
        this.gui.afterOpen();
    }

    @Override
    public void sendAllDataToRemote() {
        super.sendAllDataToRemote();
        // We have to manually sync offhand state
        int index = this.getGui().getOffhandSlotIndex();
        ItemStack updated = index >= 0 ? this.getSlot(index).getItem() : ItemStack.EMPTY;
        GuiHelpers.sendSlotUpdate(this.gui.getPlayer(), -2, Inventory.SLOT_OFFHAND, updated, this.getStateId());
    }

    @Override
    public SlotGuiInterface getGui() {
        return this.gui;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void setItem(int slot, int i, ItemStack stack) {
        if (this.gui.getSize() <= slot) {
            this.getSlot(slot).set(stack);
        } else {
            this.getSlot(slot).set(ItemStack.EMPTY);
        }
    }

    @Override
    public void broadcastChanges() {
        try {
            this.gui.onTick();
        } catch (Exception e) {
            this.gui.handleException(e);
        }
        super.broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return this.gui.quickMoveStack(index);
    }

    @Override
    public boolean canDragTo(Slot slot) {
        return !(slot instanceof VirtualSlot) && super.canDragTo(slot);
    }

    @Override
    public Slot addSlot(Slot slot) {
        return super.addSlot(slot);
    }

    public void setSlot(int index, Slot slot) {
        this.slots.set(index, slot);
    }

    @Override
    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean fromLast) {
        return this.gui.insertItem(stack, startIndex, endIndex, fromLast);
    }
}
