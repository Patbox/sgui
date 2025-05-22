package eu.pb4.sgui.api.elements;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.gui.GuiInterface;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import net.minecraft.world.item.ItemStack;

/**
 * Gui Element Interface
 * <br>
 * This is the interface all GuiElements are based from. It contains
 * the bare basic methods for what is required to display and trigger
 * GuiElements.
 *
 * Elements are typically not constructed directly, but rather through a GuiElementBuilder.
 * @see GuiElementBuilderInterface
 *
 * @see GuiElement
 * @see AnimatedGuiElement
 */
@SuppressWarnings({"unused"})
public interface GuiElementInterface {
    ClickCallback EMPTY_CALLBACK = (x,y,z,a) -> {};
    ItemClickCallback EMPTY_CALLBACK_OLD = (x,y,z) -> {};

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
    default ClickCallback getGuiCallback() {
        return this.getCallback();
    }

    /**
     * Used for getting displayed item.
     * Can be used to create animations.
     */
    default ItemStack getItemStackForDisplay(GuiInterface gui) {
        return this.getItemStack().copy();
    }

    /**
     * This method is called when this GuiElement is added
     * to a SlotGuiInstance
     *
     * @param gui A gui to which this GuiElement is added
     */
    default void onAdded(SlotGuiInterface gui) {

    }

    /**
     * This method is called when this GuiElement is removed
     * from a SlotGuiInstance
     *
     * @param gui A gui to which this GuiElement is removed
     */
    default void onRemoved(SlotGuiInterface gui) {

    }

    /**
     * Item Click Callback
     * <br>
     * The callback used to execute actions when an
     * element is clicked.
     */
    @FunctionalInterface
    interface ItemClickCallback extends ClickCallback {

        /**
         * Executed when a GuiElement is clicked.
         *
         * @param index  the slot index
         * @param type   the simplified type of click
         * @param action the Minecraft action type
         */
        void click(int index, ClickType type, net.minecraft.world.inventory.ClickType action);

        default void click(int index, ClickType type, net.minecraft.world.inventory.ClickType action, SlotGuiInterface gui) {
            this.click(index, type, action);
        }
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
         * Executed when a GuiElement is clicked.
         *  @param index  the slot index
         * @param type   the simplified type of click
         * @param action the Minecraft action type
         * @param gui    the gui being source of the click
         */
        void click(int index, ClickType type, net.minecraft.world.inventory.ClickType action, SlotGuiInterface gui);
    }

    @Deprecated
    default ItemClickCallback getCallback() {
        return GuiElement.EMPTY_CALLBACK_OLD;
    }
}
