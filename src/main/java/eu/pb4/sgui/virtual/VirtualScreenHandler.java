package eu.pb4.sgui.virtual;

import eu.pb4.sgui.SimpleGui;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;

public class VirtualScreenHandler extends ScreenHandler {
    private final SimpleGui gui;
    private final VirtualInventory inventory;

    public VirtualScreenHandler(@Nullable ScreenHandlerType<?> type, int syncId, SimpleGui gui, PlayerEntity player) {
        super(type, syncId);
        this.gui = gui;

        this.inventory = new VirtualInventory(gui);
        int i = (gui.getHeight() - 4) * 18;

        int n;
        int m;

        for (n = 0; n < this.gui.getVirtualSize(); ++n) {
            Slot slot = this.gui.getSlotRedirect(n);
            if (slot != null) {
                this.addSlot(slot);
            } else {
                this.addSlot(new VirtualSlot(inventory, n, 0, 0));
            }
        }

        if (gui.isIncludingPlayer()) {
            int size = this.gui.getHeight() * this.gui.getWitdh();
            for (n = 0; n < 4; ++n) {
                for (m = 0; m < 9; ++m) {
                    this.addSlot(new VirtualSlot(
                            inventory, m + n * 9 + size, 0, 0));
                }
            }
        } else {
            PlayerInventory playerInventory = player.inventory;
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

    public SimpleGui getGui() {
        return this.gui;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        if (this.gui.getSize() <= slot) {
            this.getSlot(slot).setStack(stack);
        } else {
            this.getSlot(slot).setStack(ItemStack.EMPTY);
        }
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        Slot slot = this.slots.get(index);
        return slot != null && !(slot.inventory instanceof VirtualInventory) ? slot.getStack() : ItemStack.EMPTY;
    }

    @Override
    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
        return this.inventory != slot.inventory;
    }

    @Override
    public void close(PlayerEntity player) {
        super.close(player);
        this.gui.close(true);
    }
}
