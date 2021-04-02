package eu.pb4.sgui.virtual;

import eu.pb4.sgui.GuiElement;
import eu.pb4.sgui.SimpleGui;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public class VirtualInventory implements Inventory {
    private final SimpleGui gui;

    VirtualInventory(SimpleGui gui) {
        this.gui = gui;
    }

    @Override
    public int size() {
        return this.gui.getSize();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public int getMaxCountPerStack() {
        return 1;
    }

    @Override
    public ItemStack getStack(int index) {
        GuiElement element = this.gui.getSlot(index);
        if (element == null) {
            return ItemStack.EMPTY;
        }
        return element.getItem();
    }

    @Override
    public ItemStack removeStack(int index, int count) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int index) {
        return ItemStack.EMPTY;
    }


    @Override
    public void setStack(int slot, ItemStack stack) {
    }

    @Override
    public void markDirty() {
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
    }
}
