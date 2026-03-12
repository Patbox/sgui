package eu.pb4.sgui.api;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilderCreator;
import eu.pb4.sgui.api.elements.SimpleGuiElement;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@SuppressWarnings({"unused"})
public interface SlotHolder {
    /**
     * Returns the number of vertical slots in this gui.
     *
     * @return the height of this gui
     */
    int getHeight();

    /**
     * Returns the number of horizontal slots in this gui.
     *
     * @return the width of this gui
     */
    int getWidth();

    /**
     * Sets slot with selected SimpleGuiElement.
     *
     * @param index   the slots index, from 0 to (max size - 1)
     * @param element any SimpleGuiElement
     * @throws IndexOutOfBoundsException if the slot is out of bounds
     * @see SlotHolder#addSlot(GuiElement)
     */
    void setSlot(int index, GuiElement element);

    /**
     * Sets the first open slot with selected SimpleGuiElement.
     *
     * @param element any SimpleGuiElement
     * @see SlotHolder#setSlot(int, GuiElement)
     */
    default void addSlot(GuiElement element) {
        this.setSlot(this.getFirstEmptySlot(), element);
    }

    /**
     * Sets slot with selected ItemStack.
     *
     * @param index     the slots index, from 0 to (max size - 1)
     * @param itemStack a stack of items
     * @throws IndexOutOfBoundsException if the slot is out of bounds
     * @see SlotHolder#addSlot(ItemStack)
     */
    default void setSlot(int index, ItemStack itemStack) {
        this.setSlot(index, new SimpleGuiElement(itemStack, GuiElement.EMPTY_CALLBACK));
    }

    /**
     * Sets the first open slot with selected ItemStack.
     *
     * @param itemStack a stack of items
     * @see SlotHolder#setSlot(int, ItemStack)
     */
    default void addSlot(ItemStack itemStack) {
        this.setSlot(this.getFirstEmptySlot(), itemStack);
    }

    /**
     * Sets slot with selected SimpleGuiElement created from a builder.
     *
     * @param index   the slots index, from 0 to (max size - 1)
     * @param element any GuiElementBuilder
     * @throws IndexOutOfBoundsException if the slot is out of bounds
     * @see SlotHolder#addSlot(GuiElementBuilderCreator)
     */
    default void setSlot(int index, GuiElementBuilderCreator<?> element) {
        this.setSlot(index, element.build());
    }

    /**
     * Sets the first open slot with selected SimpleGuiElement created from a builder.
     *
     * @param element any GuiElementBuilder
     * @see SlotHolder#setSlot(int, GuiElementBuilderCreator)
     */
    default void addSlot(GuiElementBuilderCreator<?> element) {
        this.setSlot(this.getFirstEmptySlot(), element.build());
    }

    /**
     * Sets slot with ItemStack and callback.
     *
     * @param index     the slots index, from 0 to (max size - 1)
     * @param itemStack a stack of items
     * @param callback  the callback to run when clicked
     * @throws IndexOutOfBoundsException if the slot is out of bounds
     * @see SlotHolder#addSlot(ItemStack, SimpleGuiElement.ClickCallback)
     */
    default void setSlot(int index, ItemStack itemStack, SimpleGuiElement.ClickCallback callback) {
        this.setSlot(index, new SimpleGuiElement(itemStack, callback));
    }

    /**
     * Sets slot with ItemStack and callback.
     *
     * @param index     the slots index, from 0 to (max size - 1)
     * @param itemStack a stack of items
     * @param callback  the callback to run when clicked
     * @throws IndexOutOfBoundsException if the slot is out of bounds
     * @see SlotHolder#addSlot(ItemStack, SimpleGuiElement.ClickCallback)
     */
    default void setSlot(int index, ItemStack itemStack, Runnable callback) {
        this.setSlot(index, new SimpleGuiElement(itemStack, GuiElement.callback(callback)));
    }

    /**
     * Sets slot with ItemStack and callback.
     *
     * @param index     the slots index, from 0 to (max size - 1)
     * @param itemStack a stack of items
     * @param callback  the callback to run when clicked
     * @throws IndexOutOfBoundsException if the slot is out of bounds
     * @see SlotHolder#addSlot(ItemStack, SimpleGuiElement.ClickCallback)
     */
    default void setSlot(int index, ItemStack itemStack, Consumer<ClickType> callback) {
        this.setSlot(index, new SimpleGuiElement(itemStack, GuiElement.callback(callback)));
    }

    /**
     * Sets the first open slot with ItemStack and callback
     *
     * @param itemStack a stack of items
     * @param callback  the callback to run when clicked
     * @see SlotHolder#setSlot(int, ItemStack, SimpleGuiElement.ClickCallback)
     */
    default void addSlot(ItemStack itemStack, SimpleGuiElement.ClickCallback callback) {
        this.setSlot(this.getFirstEmptySlot(), new SimpleGuiElement(itemStack, callback));
    }

    /**
     * Sets the first open slot with ItemStack and callback
     *
     * @param itemStack a stack of items
     * @param callback  the callback to run when clicked
     * @throws IndexOutOfBoundsException if the slot is out of bounds
     * @see SlotHolder#addSlot(ItemStack, SimpleGuiElement.ClickCallback)
     */
    default void addSlot(ItemStack itemStack, Runnable callback) {
        this.addSlot(new SimpleGuiElement(itemStack, GuiElement.callback(callback)));
    }

    /**
     * Sets the first open slot with ItemStack and callback
     *
     * @param itemStack a stack of items
     * @param callback  the callback to run when clicked
     * @throws IndexOutOfBoundsException if the slot is out of bounds
     * @see SlotHolder#addSlot(ItemStack, SimpleGuiElement.ClickCallback)
     */
    default void addSlot(ItemStack itemStack, Consumer<ClickType> callback) {
        this.addSlot(new SimpleGuiElement(itemStack, GuiElement.callback(callback)));
    }

    /**
     * Allows to add own Slot instances, that can point to any inventory.
     * Do not add duplicates (including player inventory) as it can cause item duplication!
     *
     * @param index the slot index (in this gui)
     * @param slot  the slot to redirect to
     * @see SlotHolder#addSlot(Slot)
     */
    void setSlot(int index, Slot slot);

    /**
     * Sets the first open slot with selected Slot instance.
     * Works the same way as {@code setSlotRedirect}.
     * Do not add duplicates (including player inventory) as it can cause item duplication!
     *
     * @param slot the slot to redirect to
     * @see SlotHolder#setSlot(int, Slot)
     */
    default void addSlot(Slot slot) {
        this.setSlot(this.getFirstEmptySlot(), slot);
    }

    /**
     * Returns the first empty slot inside the inventory.
     *
     * @return the index of the first empty slot or <code>-1</code> if full
     */
    int getFirstEmptySlot();

    /**
     * Reverts slot to it's original state.
     *
     * @param index slot index
     */
    void clearSlot(int index);

    /**
     * Returns if the gui includes the player inventory slots.
     *
     * @return <code>true</code> if the player inventory slots.
     */
    boolean isIncludingPlayer();

    /**
     * Returns the number of slots in the virtual inventory only.
     * Works the same as {@link SlotHolder#getSize()}, however excludes player gui slots if <code>includePlayer</code> is <code>true</code>.
     *
     * @return the size of the virtual inventory
     * @see SlotHolder#getSize()
     */
    int getVirtualSize();

    /**
     * Returns the number of slots in the inventory.
     *
     * @return the inventory size
     */
    int getSize();

    /**
     * Returns the element in the referenced slot.
     *
     * @param index the slot index
     * @return the element or <code>null</code> if empty
     * @see SlotHolder#getSlotRedirect(int)
     */
    @Nullable
    GuiElement getSlotElement(int index);

    /**
     * Returns the external slot the referenced slot is redirecting to.
     *
     * @param index the slot index
     * @return the element or <code>null</code> if no redirect
     * @see SlotHolder#getSlotElement(int)
     */
    @Nullable
    Slot getSlotRedirect(int index);

    /**
     * Returns if this gui has slot redirects.
     *
     * @return <code>true</code> if this gui has slot redirects
     * @see SlotHolder#getSlotRedirect(int)
     */
    boolean isRedirectingSlots();
}
