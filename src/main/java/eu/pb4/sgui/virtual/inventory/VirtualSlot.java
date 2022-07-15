package eu.pb4.sgui.virtual.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class VirtualSlot extends Slot {

    public VirtualSlot(Inventory inventory, int index, int x, int y) {
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
    public boolean canTakePartial(PlayerEntity player) {
        return false;
    }

    @Override
    public ItemStack insertStack(ItemStack stack, int count) {
        return stack;
    }


    @Override
    public void setStack(ItemStack stack) {

    }

    @Override
    public boolean hasStack() {
        return true;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return false;
    }
}
