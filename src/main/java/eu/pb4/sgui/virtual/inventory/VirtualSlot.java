package eu.pb4.sgui.virtual.inventory;

import eu.pb4.sgui.api.gui.SlotGuiInterface;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

import java.util.Optional;

public class VirtualSlot extends Slot {

    private final SlotGuiInterface gui;

    public VirtualSlot(SlotGuiInterface gui, int index, int x, int y) {
        super(VirtualInventory.INSTANCE, index, x, y);
        this.gui = gui;
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
    public Optional<ItemStack> tryTakeStackRange(int min, int max, PlayerEntity player) {
        return Optional.empty();
    }

    @Override
    public ItemStack insertStack(ItemStack stack) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getStack() {
        var x = this.gui.getSlot(this.getIndex());
        if (x == null) {
            return ItemStack.EMPTY;
        }
        return x.getItemStackForDisplay(this.gui).copy();
    }

    @Override
    public void setStackNoCallbacks(ItemStack stack) {

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

    @Override
    public void markDirty() {

    }
}
