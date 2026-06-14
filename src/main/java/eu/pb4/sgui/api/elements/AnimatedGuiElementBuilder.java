package eu.pb4.sgui.api.elements;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Animated Gui Element Builder
 * <br>
 * The {@link AnimatedGuiElementBuilder} is the best way of constructing
 * an {@link AnimatedGuiElement}.
 * It supplies all the methods needed to construct each frame and mesh
 * them together to create the full animation.
 *
 * @see GuiElementBuilderCreator
 */
@SuppressWarnings({"unused"})
public final class AnimatedGuiElementBuilder extends BaseItemStackBuilder<AnimatedGuiElementBuilder> implements GuiElementBuilderCreator<AnimatedGuiElementBuilder> {
    protected final List<ItemStack> itemStacks = new ArrayList<>();
    protected SimpleGuiElement.ClickCallback callback = SimpleGuiElement.EMPTY_CALLBACK;
    protected int interval = 1;
    protected boolean random = false;

    /**
     * Constructs a AnimatedGuiElementBuilder with the default options
     */
    public AnimatedGuiElementBuilder() {
    }

    /**
     * Constructs a AnimatedGuiElementBuilder with the supplied interval
     *
     * @param interval the time between frame changes
     * @return this element builder
     */
    public AnimatedGuiElementBuilder setInterval(int interval) {
        this.interval = interval;
        return this;
    }

    /**
     * Sets if the frames should be randomly chosen or more in order
     * of addition.
     *
     * @param value <code>true</code> to select random frames
     * @return this element builder
     */
    public AnimatedGuiElementBuilder setRandom(boolean value) {
        this.random = value;
        return this;
    }

    /**
     * Saves the current stack that is being created.
     * This will add it to the animation and reset the
     * settings awaiting another creation.
     *
     * @return this element builder
     */
    public AnimatedGuiElementBuilder saveItemStack() {
        this.itemStacks.add(this.asStack());
        this.item = Items.TRIAL_KEY;
        this.components = DataComponentPatch.builder();
        this.count = 1;
        return this;
    }

    @Override
    public AnimatedGuiElementBuilder setCallback(SimpleGuiElement.ClickCallback callback) {
        this.callback = callback;
        return this;
    }

    public AnimatedGuiElement build() {
        return new AnimatedGuiElement(this.itemStacks.toArray(new ItemStack[0]), this.interval, this.random, this.callback);
    }
}
