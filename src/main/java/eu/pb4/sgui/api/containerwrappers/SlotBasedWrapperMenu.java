package eu.pb4.sgui.api.containerwrappers;

import eu.pb4.sgui.api.SguiUtils;
import eu.pb4.sgui.api.gui.SlotBasedGui;
import eu.pb4.sgui.api.containerwrappers.slot.WrappingSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class SlotBasedWrapperMenu extends AbstractWrapperMenu {
    private final SlotBasedGui gui;

    public SlotBasedWrapperMenu(@Nullable MenuType<?> type, int syncId, SlotBasedGui gui, Player player) {
        super(type, syncId, gui);
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
                this.addSlot(new WrappingSlot(gui, n, 0, 0));
            }
        }

        if (gui.isIncludingPlayer()) {
            int size = this.gui.getHeight() * this.gui.getWidth();
            for (n = 0; n < 4; ++n) {
                for (m = 0; m < 9; ++m) {
                    Slot slot = this.gui.getSlotRedirect(m + n * 9 + size);
                    if (slot != null) {
                        this.addSlot(slot);
                    } else {
                        this.addSlot(new WrappingSlot(gui, m + n * 9 + size, 0, 0));
                    }
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
    public void sendAllDataToRemote() {
        super.sendAllDataToRemote();
        // We have to manually sync offhand state
        int index = this.gui.getOffhandSlotIndex();
        ItemStack updated = index >= 0 ? this.getSlot(index).getItem() : ItemStack.EMPTY;
        SguiUtils.sendSlotUpdate(this.gui.getPlayer(), -2, Inventory.SLOT_OFFHAND, updated, this.getStateId());
    }

    @Override
    public SlotBasedGui getBackingGui() {
        return this.gui;
    }

    @Override
    public void setItem(int slot, int i, ItemStack stack) {
        if (this.gui.getSize() <= slot) {
            this.getSlot(slot).setByPlayer(stack);
        } else {
            this.getSlot(slot).setByPlayer(ItemStack.EMPTY);
        }
    }

    @Override
    public @NonNull ItemStack quickMoveStack(Player player, int index) {
        return this.gui.quickMove(index);
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return !(slot instanceof WrappingSlot) && super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public @NonNull Slot addSlot(Slot slot) {
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
