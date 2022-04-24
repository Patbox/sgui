package eu.pb4.sgui.virtual.book;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class BookSlot extends Slot {
    public BookSlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public ItemStack takeStack(int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canTakeItems(PlayerEntity playerEntity) {
        return false;
    }

    @Override
    public boolean hasStack() {
        return true;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return false;
    }

    @Override
    protected void onTake(int amount) {
    }

    @Override
    protected void onCrafted(ItemStack stack) {
    }

    @Override
    public ItemStack getStack() {
        return this.inventory.getStack(0);
    }

    @Override
    public void setStack(ItemStack stack) {
    }

    @Override
    public void markDirty() {
    }
}
