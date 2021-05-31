package eu.pb4.sgui.api.elements;

import eu.pb4.sgui.api.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import org.jetbrains.annotations.ApiStatus;

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
public interface GuiElementInterface {

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
    GuiElementInterface.ItemClickCallback getCallback();

    /**
     * Used internally for retrieving the stack
     * and updating the element.
     */
    @ApiStatus.Internal
    ItemStack getItemStackInternalUseOnly();

    /**
     * Item Click Callback
     * <br>
     * The callback used to execute actions when an
     * element is clicked.
     */
    @FunctionalInterface
    interface ItemClickCallback {

        /**
         * Executed when a GuiElement is clicked.
         *
         * @param index  the slot index
         * @param type   the simplified type of click
         * @param action the Minecraft action type
         */
        void click(int index, ClickType type, SlotActionType action);
    }
}
