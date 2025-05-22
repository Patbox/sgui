package eu.pb4.sgui.virtual.inventory;

import eu.pb4.sgui.api.gui.SlotGuiInterface;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class VirtualSlot extends Slot {

    private final SlotGuiInterface gui;

    public VirtualSlot(SlotGuiInterface gui, int index, int x, int y) {
        super(VirtualInventory.INSTANCE, index, x, y);
        this.gui = gui;
    }

    @Override
    public ItemStack remove(int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean mayPickup(Player Player) {
        return false;
    }

    @Override
    public boolean allowModification(Player player) {
        return false;
    }

    @Override
    public ItemStack safeInsert(ItemStack stack, int count) {
        return stack;
    }

    @Override
    public Optional<ItemStack> tryRemove(int min, int max, Player player) {
        return Optional.empty();
    }

    @Override
    public ItemStack safeInsert(ItemStack stack) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getItem() {
        var x = this.gui.getSlot(this.getContainerSlot());
        if (x == null) {
            return ItemStack.EMPTY;
        }
        return x.getItemStackForDisplay(this.gui).copy();
    }

    @Override
    public void setByPlayer(ItemStack stack) {

    }

    @Override
    public void set(ItemStack stack) {

    }

    @Override
    public boolean hasItem() {
        return true;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public void setChanged() {

    }
}
