package eu.pb4.sgui.virtual.book;

import eu.pb4.sgui.api.gui.BookGui;
import eu.pb4.sgui.virtual.VirtualScreenHandlerInterface;
import eu.pb4.sgui.virtual.inventory.VirtualInventory;
import eu.pb4.sgui.virtual.inventory.VirtualSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;

public class BookScreenHandler extends ScreenHandler implements VirtualScreenHandlerInterface {
    private final BookGui gui;

    public BookScreenHandler(int syncId, BookGui gui, PlayerEntity player) {
        super(ScreenHandlerType.LECTERN, syncId);
        this.gui = gui;

        this.addSlot(new BookSlot(new BookInventory(gui), 0, 0, 0, gui.getBook()));
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        switch (id) {
            case 1 -> {
                this.gui.setPage(gui.getPage() - 1);
                return true;
            }
            case 2 -> {
                this.gui.setPage(gui.getPage() + 1);
                return true;
            }
            case 3 -> {
                this.gui.onTakeBookButton();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }
    @Override
    public void setStackInSlot(int slot, int i, ItemStack stack) {
        if (slot == 0) {
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
            e.printStackTrace();
        }
        super.sendContentUpdates();
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
        return !(slot instanceof VirtualSlot) && super.canInsertIntoSlot(stack, slot);
    }

    @Override
    protected boolean insertItem(ItemStack stack, int startIndex, int endIndex, boolean fromLast) {
        return false;
    }

    @Override
    public BookGui getGui() {
        return gui;
    }
}
