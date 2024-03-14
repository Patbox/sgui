package eu.pb4.sgui.virtual.inventory;

import eu.pb4.sgui.api.gui.SlotGuiInterface;
import eu.pb4.sgui.virtual.VirtualScreenHandlerInterface;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;

public class VirtualScreenHandler extends ScreenHandler implements VirtualScreenHandlerInterface {
    private final SlotGuiInterface gui;

    public VirtualScreenHandler(@Nullable ScreenHandlerType<?> type, int syncId, SlotGuiInterface gui, PlayerEntity player) {
        super(type, syncId);
        this.gui = gui;

        setupSlots(player);
    }

    protected void setupSlots(PlayerEntity player) {
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
            PlayerInventory playerInventory = player.getInventory();
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
    public void addListener(ScreenHandlerListener listener) {
        super.addListener(listener);
        this.gui.afterOpen();
    }

    @Override
    public SlotGuiInterface getGui() {
        return this.gui;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void setStackInSlot(int slot, int i, ItemStack stack) {
        if (this.gui.getSize() <= slot) {
            this.getSlot(slot).setStack(stack);
        } else {
            this.getSlot(slot).setStack(ItemStack.EMPTY);
        }
    }

    @Override
    public void sendContentUpdates() {
        try {
            this.gui.onTick();
        } catch (Exception e) {
            this.gui.handleException(e);
        }
        super.sendContentUpdates();
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        return this.gui.quickMove(index);
    }

    @Override
    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
        return !(slot instanceof VirtualSlot) && super.canInsertIntoSlot(stack, slot);
    }

    @Override
    public Slot addSlot(Slot slot) {
        return super.addSlot(slot);
    }

    public void setSlot(int index, Slot slot) {
        this.slots.set(index, slot);
    }

    @Override
    protected boolean insertItem(ItemStack stack, int startIndex, int endIndex, boolean fromLast) {
        return this.gui.insertItem(stack, startIndex, endIndex, fromLast);
    }
}
