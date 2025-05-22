package eu.pb4.sgui.api.elements;

import eu.pb4.sgui.api.gui.GuiInterface;
import net.minecraft.world.item.ItemStack;

/**
 * Simple Gui Element
 * <br>
 * A simple, single frame, GuiElement.
 * <p>
 * Gui elements are typically constructed via their respective builder.
 *
 * @see GuiElementBuilder
 * @see GuiElementInterface
 */
public class GuiElement implements GuiElementInterface {
    public static final GuiElement EMPTY = new GuiElement(ItemStack.EMPTY, EMPTY_CALLBACK);

    protected final ClickCallback callback;
    protected ItemStack item;

    /**
     * Constructs a GuiElement with the supplied options.
     *
     * @param item     the stack to use for display
     * @param callback the callback to execute when the element is selected
     * @see GuiElementBuilder
     */
    public GuiElement(ItemStack item, ClickCallback callback) {
        this.item = item;
        this.callback = callback;
    }

    /**
     * Constructs a GuiElement with the supplied options.
     *
     * @param item     the stack to use for display
     * @param callback the callback to execute when the element is selected
     * @see GuiElementBuilder
     */
    public GuiElement(ItemStack item, ItemClickCallback callback) {
        this.item = item;
        this.callback = callback;
    }

    @Override
    public ItemStack getItemStack() {
        return this.item;
    }

    /**
     * Sets the display ItemStack
     *
     * @param itemStack the display item
     */
    public void setItemStack(ItemStack itemStack) {
        this.item = itemStack;
    }

    @Override
    public ClickCallback getGuiCallback() {
        return this.callback;
    }

    @Override
    public ItemStack getItemStackForDisplay(GuiInterface gui) {
        return this.item.copy();
    }
}
