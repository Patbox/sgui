package eu.pb4.sgui.api.elements;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.gui.GuiLike;
import eu.pb4.sgui.api.gui.SlotBasedGui;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

/**
 * Gui Element Interface
 * <br>
 * This is the interface all GuiElements are based from. It contains
 * the bare basic methods for what is required to display and trigger
 * GuiElements.
 * <p>
 * Elements are typically not constructed directly, but rather through a GuiElementBuilder.
 *
 * @see GuiElementBuilderCreator
 * @see SimpleGuiElement
 * @see AnimatedGuiElement
 */
@SuppressWarnings({"unused"})
public interface GuiElement {
    ClickCallback EMPTY_CALLBACK = (x, y, z, a) -> {
    };

    /**
     * Returns the elements currently displayed stack
     *
     * @return the current stack
     */
    ItemStack getItemStack();

    /**
     * Returns the elements callback
     *
     * @return the callback
     */
    ClickCallback getGuiCallback();

    /**
     * Used for getting displayed item.
     * Can be used to create animations.
     */
    default ItemStack getItemStackForDisplay(GuiLike gui) {
        return this.getItemStack().copy();
    }

    /**
     * This method is called when this SimpleGuiElement is added
     * to a SlotGuiInstance
     *
     * @param gui A gui to which this SimpleGuiElement is added
     */
    default void onAdded(SlotBasedGui gui) {

    }

    /**
     * This method is called when this SimpleGuiElement is removed
     * from a SlotGuiInstance
     *
     * @param gui A gui to which this SimpleGuiElement is removed
     */
    default void onRemoved(SlotBasedGui gui) {

    }

    static ClickCallback callback(Runnable runnable) {
        return (index, type, action, gui) -> runnable.run();
    }

    static ClickCallback callback(Consumer<ClickType> clickTypeConsumer) {
        return (index, type, action, gui) -> clickTypeConsumer.accept(type);
    }

    default boolean onSetSelectedBundleItemIndex(SlotBasedGui slotBasedGui, int slotIndex, int selectedItemIndex) {
        return false;
    }

    /**
     * Gui-Aware Item Click Callback
     * <br>
     * The callback used to execute actions when an
     * element is clicked.
     */
    @FunctionalInterface
    interface ClickCallback {
        /**
         * Executed when a SimpleGuiElement is clicked.
         *
         * @param index  the slot index
         * @param type   the simplified type of click
         * @param action the Minecraft action type
         * @param gui    the gui being source of the click
         */
        void click(int index, ClickType type, ContainerInput action, SlotBasedGui gui);
    }
}
