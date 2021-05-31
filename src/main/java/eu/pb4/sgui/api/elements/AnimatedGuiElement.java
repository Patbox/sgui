package eu.pb4.sgui.api.elements;

import net.minecraft.item.ItemStack;

public class AnimatedGuiElement implements GuiElementInterface {
    private final ItemClickCallback callback;
    private ItemStack[] items;
    private int frame = 0;
    private int tick = 0;
    private final int changeEvery;
    private final boolean random;

    public AnimatedGuiElement(ItemStack[] items, int interval, boolean random, ItemClickCallback callback) {
        this.items = items;
        this.callback = callback;
        this.changeEvery = interval;
        this.random = random;
    }

    public void setItemStacks(ItemStack[] itemStacks) {
        this.items = itemStacks;
    }

    @Override
    public ItemStack getItemStack() {
        return this.items[frame];
    }

    @Override
    public ItemClickCallback getCallback() {
        return this.callback;
    }

    @Override
    public ItemStack getItemStackInternalUseOnly() {
        int cFrame = this.frame;

        this.tick += 1;
        if (this.tick >= this.changeEvery) {
            this.tick = 0;
            this.frame += 1;
            if (this.frame >= this.items.length) {
                this.frame = 0;
            }

            if (this.random) {
                this.frame = (int) (Math.random() * this.items.length);
            }
        }


        return this.items[cFrame].copy();
    }

}
