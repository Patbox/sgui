package eu.pb4.sgui.impl.virtual.book;

import eu.pb4.sgui.api.containerwrappers.AbstractWrapperMenu;
import eu.pb4.sgui.api.gui.BookGui;
import eu.pb4.sgui.api.containerwrappers.slot.WrappingSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BookScreenHandler extends AbstractWrapperMenu {
    private final BookGui gui;

    public BookScreenHandler(int syncId, BookGui gui, Player player) {
        super(MenuType.LECTERN, syncId, gui);
        this.gui = gui;

        this.addSlot(new BookSlot(new BookInventory(gui), 0, 0, 0));
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (this.gui.onButtonClick(id)) {
            return true;
        }
        switch (id) {
            case 1 -> {
                this.gui.onPreviousPageButton();
                return true;
            }
            case 2 -> {
                this.gui.onNextPageButton();
                return true;
            }
            case 3 -> {
                this.gui.onTakeBookButton();
                return true;
            }
        }
        if (id >= 100) {
            this.gui.onPageSelected(id - 100 + 1);
            return true;
        }
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void setItem(int slot, int i, ItemStack stack) {
        if (slot == 0) {
            this.getSlot(slot).setByPlayer(stack);
        } else {
            this.getSlot(slot).setByPlayer(ItemStack.EMPTY);
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
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return !(slot instanceof WrappingSlot) && super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean fromLast) {
        return false;
    }

    @Override
    public BookGui getBackingGui() {
        return gui;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        try {
            this.getBackingGui().onRemoved();
        } catch (Throwable e) {
            this.getBackingGui().handleException(e);
        }
    }
}
