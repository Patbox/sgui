package eu.pb4.sgui.api.containerwrappers.slot;

import eu.pb4.sgui.api.gui.SlotBasedGui;
import java.util.Optional;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class WrappingSlot extends Slot {

    private final SlotBasedGui gui;

    public WrappingSlot(SlotBasedGui gui, int index, int x, int y) {
        super(FakeContainer.INSTANCE, index, x, y);
        this.gui = gui;
    }

    @Override
    public ItemStack remove(int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean mayPickup(Player playerEntity) {
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
        var x = this.gui.getGuiElement(this.getContainerSlot());
        if (x == null) {
            return ItemStack.EMPTY;
        }
        return x.getItemStackForDisplay(this.gui).copy();
    }

    @Override
    public void set(ItemStack stack) {

    }

    @Override
    public void setByPlayer(ItemStack stack) {

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

    public record FakeContainer() implements Container {
        public static final FakeContainer INSTANCE = new FakeContainer();

        @Override
        public int getContainerSize() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public ItemStack getItem(int index) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItem(int index, int count) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItemNoUpdate(int index) {
            return ItemStack.EMPTY;
        }


        @Override
        public void setItem(int index, ItemStack stack) {
        }

        @Override
        public void setChanged() {
        }

        @Override
        public boolean stillValid(Player player) {
            return false;
        }

        @Override
        public void clearContent() {
        }
    }
}
