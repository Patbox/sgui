package eu.pb4.sgui.api.elements;

import net.minecraft.item.ItemStack;

public class GuiElement implements GuiElementInterface {
    private final ItemClickCallback callback;
    private ItemStack item;

    public GuiElement(ItemStack item, ItemClickCallback callback) {
        this.item = item;
        this.callback = callback;
    }

    public ItemStack getItemStack() {
        return this.item;
    }

    public ItemStack getItemStackInternalUseOnly() {
        return this.item.copy();
    }

    public ItemClickCallback getCallback() {
        return this.callback;
    }

    public void setItemStack(ItemStack itemStack) {
        this.item = itemStack;
    }

}
