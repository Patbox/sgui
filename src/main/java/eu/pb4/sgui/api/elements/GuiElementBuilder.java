package eu.pb4.sgui.api.elements;

import eu.pb4.sgui.mixin.BuilderAccessor;
import eu.pb4.sgui.mixin.DataComponentPatchAccessor;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

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
    private SimpleGuiElement.ClickCallback callback = GuiElement.EMPTY_CALLBACK;

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
        this.item = item;
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
        this.item = item;
        this.count = count;
    }

    /**
     * Constructs a GuiElementBuilder with the specified ItemStack
     *
     * @param stack the item stack to use
     */
    public GuiElementBuilder(ItemStack stack) {
        this.item = stack.getItem();
        this.count = stack.getCount();
        ((BuilderAccessor) this.components).getMap().putAll(((DataComponentPatchAccessor) (Object) stack.getComponentsPatch()).getMap());
    }

    /**
     * Constructs a GuiElementBuilder with the specified ItemStack
     *
     * @param stack the item stack to use
     */
    public GuiElementBuilder(ItemStackTemplate stack) {
        this.item = stack.item().value();
        this.count = stack.count();
        ((BuilderAccessor) this.components).getMap().putAll(((DataComponentPatchAccessor) (Object) stack.components()).getMap());
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
