package eu.pb4.sgui.api.elements;

import eu.pb4.sgui.api.gui.GuiInterface;
import net.minecraft.world.item.ItemStack;

import java.util.WeakHashMap;

/**
 * Animated Gui Element
 * <br>
 * Animated gui elements are a GuiElement constructed of
 * multiple different {@link ItemStack} frames, which cycle
 * (optionally randomly) on a set cycle time.
 *
 * Gui elements are typically constructed via their respective builder.
 * @see AnimatedGuiElementBuilder
 *
 * @see GuiElementInterface
 */
public class AnimatedGuiElement implements GuiElementInterface {
    protected final ClickCallback callback;
    protected ItemStack[] items;
    protected int frame = 0;
    protected int tick = 0;
    protected final int changeEvery;
    protected final boolean random;
    protected WeakHashMap<GuiInterface, TickAndFrame> ticks = new WeakHashMap<>();

    /**
     * Constructs an AnimatedGuiElement using the supplied options.
     *
     * @param items    an array of ItemStack frames
     * @param interval the interval each frame should remain active for
     * @param random   <code>true</code> is the frames should be randomly chosen
     * @param callback the callback to execute when the element is selected
     * @see AnimatedGuiElementBuilder
     */
    public AnimatedGuiElement(ItemStack[] items, int interval, boolean random, ClickCallback callback) {
        this.items = items;
        this.callback = callback;
        this.changeEvery = interval;
        this.random = random;
    }

    /**
     * Constructs an AnimatedGuiElement using the supplied options.
     *
     * @param items    an array of ItemStack frames
     * @param interval the interval each frame should remain active for
     * @param random   <code>true</code> is the frames should be randomly chosen
     * @param callback the callback to execute when the element is selected
     * @see AnimatedGuiElementBuilder
     */
    public AnimatedGuiElement(ItemStack[] items, int interval, boolean random, ItemClickCallback callback) {
        this.items = items;
        this.callback = callback;
        this.changeEvery = interval;
        this.random = random;
    }

    /**
     * Sets the elements animation frames.
     *
     * @param itemStacks the new animation frames
     */
    public void setItemStacks(ItemStack[] itemStacks) {
        this.items = itemStacks;
    }

    @Override
    public ItemStack getItemStack() {
        return this.items[frame];
    }

    @Override
    public ClickCallback getGuiCallback() {
        return this.callback;
    }

    @Override
    public ItemStack getItemStackForDisplay(GuiInterface gui) {
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


    protected static class TickAndFrame {
        public int tick;
        public int frame;
    }
}
