package eu.pb4.sgui.virtual.book;


import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BookSlot extends Slot {
    public BookSlot(Container inventory, int index, int x, int y) {
        super(inventory, index, x, y);
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
    public boolean hasItem() {
        return true;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    protected void onSwapCraft(int amount) {
    }

    @Override
    protected void checkTakeAchievements(ItemStack stack) {
    }

    @Override
    public ItemStack getItem() {
        return this.container.getItem(0);
    }

    @Override
    public void setByPlayer(ItemStack stack) {
    }

    @Override
    public void setChanged() {
    }
}
