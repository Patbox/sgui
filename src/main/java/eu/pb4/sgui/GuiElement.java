package eu.pb4.sgui;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public class GuiElement {
    private final ItemClickCallback callback;
    private ItemStack item;

    public GuiElement(ItemStack item, ItemClickCallback callback) {
        this.item = item;
        this.callback = callback;
    }

    public ItemStack getItem() {
        return this.item.copy();
    }
    public ItemClickCallback getCallback() {
        return this.callback;
    }

    public void setItem(ItemStack itemStack) {
        this.item = itemStack;
    }


    @FunctionalInterface
    public interface ItemClickCallback {
        void click(int index, ClickType type, SlotActionType action);
    }
}
