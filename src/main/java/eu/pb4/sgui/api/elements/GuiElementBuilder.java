package eu.pb4.sgui.api.elements;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Gui Element Builder
 * <br>
 * The GuiElementBuilder is the best way of constructing gui elements.
 * It supplies all the methods needed to construct a standard {@link SimpleGuiElement}.
 * <p>
 * For method definition, see {@link BaseItemStackBuilder}
 *
 * @see GuiElementBuilderCreator
 */
@SuppressWarnings({"unused"})
public final class GuiElementBuilder extends BaseItemStackBuilder<GuiElementBuilder>
        implements GuiElementBuilderCreator<GuiElementBuilder> {
    protected SimpleGuiElement.ClickCallback callback = GuiElement.EMPTY_CALLBACK;

    /**
     * Constructs a GuiElementBuilder with the default options
     */
    public GuiElementBuilder() {
    }

    /**
     * Constructs a GuiElementBuilder with the specified Item.
     *
     * @param item the item to use
     */
    public GuiElementBuilder(Item item) {
        this.itemStack = new ItemStack(item);
    }

    /**
     * Constructs a GuiElementBuilder with the specified item model.
     *
     * @param model Item model to use. Same as calling model(...).
     */
    public GuiElementBuilder(Identifier model) {
        this.model(model);
    }

    /**
     * Constructs a GuiElementBuilder with the specified Item
     * and number of items.
     *
     * @param item  the item to use
     * @param count the number of items
     */
    public GuiElementBuilder(Item item, int count) {
        this.itemStack = new ItemStack(item, count);
    }

    /**
     * Constructs a GuiElementBuilder with the specified ItemStack
     *
     * @param stack the item stack to use
     */
    public GuiElementBuilder(ItemStack stack) {
        this.itemStack = stack.copy();
    }

    /**
     * Constructs a GuiElementBuilder based on the supplied stack.
     *
     * @param stack the stack to base the builder of
     * @return the constructed builder
     */
    public static GuiElementBuilder from(ItemStack stack) {
        return new GuiElementBuilder(stack);
    }

    @Override
    public GuiElementBuilder setCallback(GuiElement.ClickCallback callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public SimpleGuiElement build() {
        return new SimpleGuiElement(this.asStack(), this.callback);
    }
}
