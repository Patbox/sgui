package eu.pb4.sgui.api.gui;

import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.SlotHolder;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilderInterface;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * Simple Gui Builder
 * <p>
 * This class allow for quick building of gui layout, that will be used for multiple users.
 */
@SuppressWarnings({"unused"})
public final class SimpleGuiBuilder implements SlotHolder {
    private final int size;
    private final int width;
    private final int height;
    private final ScreenHandlerType<?> type;
    private final GuiElementInterface[] elements;
    private final Slot[] slotRedirects;
    private final boolean includePlayer;
    private final int sizeCont;
    private boolean lockPlayerInventory = false;
    private boolean hasRedirects = false;
    private Text title = null;

    /**
     * Constructs a new simple container gui for the supplied player.
     *
     * @param type                        the screen handler that the client should display
     * @param includePlayerInventorySlots if <code>true</code> the players inventory
     *                                    will be treated as slots of this gui
     */
    public SimpleGuiBuilder(ScreenHandlerType<?> type, boolean includePlayerInventorySlots) {
        this.height = GuiHelpers.getHeight(type);
        this.width = GuiHelpers.getWidth(type);

        this.type = type;

        int tmp = includePlayerInventorySlots ? 36 : 0;
        this.size = this.width * this.height + tmp;
        this.sizeCont = this.width * this.height;
        this.elements = new GuiElementInterface[this.size];
        this.slotRedirects = new Slot[this.size];

        this.includePlayer = includePlayerInventorySlots;
    }

    /**
     * Creates {@link SimpleGui} instance based on this builder.
     *
     * @param player Player
     * @return SimpleGui instance
     */
    public SimpleGui build(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(this.type, player, this.includePlayer);
        gui.setTitle(this.title);
        gui.setLockPlayerInventory(true);

        int pos = 0;

        for (GuiElementInterface element : this.elements) {
            if (element != null) {
                gui.setSlot(pos, element);
            }
            pos++;
        }

        pos = 0;

        for (Slot slot : this.slotRedirects) {
            if (slot != null) {
                gui.setSlotRedirect(pos, slot);
            }
            pos++;
        }

        return gui;
    }

    /**
     * Returns the number of vertical slots in this gui.
     *
     * @return the height of this gui
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * Returns the number of horizontal slots in this gui.
     *
     * @return the width of this gui
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * Sets slot with selected GuiElement.
     *
     * @param index   the slots index, from 0 to (max size - 1)
     * @param element any GuiElement
     * @throws IndexOutOfBoundsException if the slot is out of bounds
     * @see SimpleGuiBuilder#addSlot(GuiElementInterface)
     */
    public void setSlot(int index, GuiElementInterface element) {
        this.elements[index] = element;
    }

    /**
     * Sets the first open slot with selected GuiElement.
     *
     * @param element any GuiElement
     * @see SimpleGuiBuilder#setSlot(int, GuiElementInterface)
     */
    public void addSlot(GuiElementInterface element) {
        this.setSlot(this.getFirstEmptySlot(), element);
    }

    /**
     * Sets slot with selected ItemStack.
     *
     * @param index     the slots index, from 0 to (max size - 1)
     * @param itemStack a stack of items
     * @throws IndexOutOfBoundsException if the slot is out of bounds
     * @see SimpleGuiBuilder#addSlot(ItemStack)
     */
    public void setSlot(int index, ItemStack itemStack) {
        this.setSlot(index, new GuiElement(itemStack, GuiElementInterface.EMPTY_CALLBACK));
    }

    /**
     * Sets the first open slot with selected ItemStack.
     *
     * @param itemStack a stack of items
     * @see SimpleGuiBuilder#setSlot(int, ItemStack)
     */
    public void addSlot(ItemStack itemStack) {
        this.setSlot(this.getFirstEmptySlot(), itemStack);
    }

    /**
     * Sets slot with selected GuiElement created from a builder.
     *
     * @param index   the slots index, from 0 to (max size - 1)
     * @param element any GuiElementBuilder
     * @throws IndexOutOfBoundsException if the slot is out of bounds
     * @see SimpleGuiBuilder#addSlot(GuiElementBuilderInterface)
     */
    public void setSlot(int index, GuiElementBuilderInterface<?> element) {
        this.setSlot(index, element.build());
    }

    /**
     * Sets the first open slot with selected GuiElement created from a builder.
     *
     * @param element any GuiElementBuilder
     * @see SimpleGuiBuilder#setSlot(int, GuiElementBuilderInterface)
     */
    public void addSlot(GuiElementBuilderInterface<?> element) {
        this.setSlot(this.getFirstEmptySlot(), element.build());
    }

    /**
     * Sets slot with ItemStack and callback.
     *
     * @param index     the slots index, from 0 to (max size - 1)
     * @param itemStack a stack of items
     * @param callback  the callback to run when clicked
     * @throws IndexOutOfBoundsException if the slot is out of bounds
     * @see SimpleGuiBuilder#addSlot(ItemStack, GuiElement.ClickCallback)
     */
    public void setSlot(int index, ItemStack itemStack, GuiElement.ClickCallback callback) {
        this.setSlot(index, new GuiElement(itemStack, callback));
    }

    /**
     * Sets slot with ItemStack and callback.
     *
     * @param index     the slots index, from 0 to (max size - 1)
     * @param itemStack a stack of items
     * @param callback  the callback to run when clicked
     * @throws IndexOutOfBoundsException if the slot is out of bounds
     * @see SimpleGuiBuilder#addSlot(ItemStack, GuiElement.ClickCallback)
     */
    public void setSlot(int index, ItemStack itemStack, GuiElementInterface.ItemClickCallback callback) {
        this.setSlot(index, new GuiElement(itemStack, callback));
    }

    /**
     * Sets the first open slot with ItemStack and callback
     *
     * @param itemStack a stack of items
     * @param callback  the callback to run when clicked
     * @see SimpleGuiBuilder#setSlot(int, ItemStack, GuiElement.ClickCallback)
     */
    public void addSlot(ItemStack itemStack, GuiElement.ClickCallback callback) {
        this.setSlot(this.getFirstEmptySlot(), new GuiElement(itemStack, callback));
    }

    /**
     * Sets the first open slot with selected ItemStack.
     *
     * @param itemStack a stack of items
     * @see SimpleGuiBuilder#setSlot(int, ItemStack)
     */
    public void addSlot(ItemStack itemStack, GuiElementInterface.ItemClickCallback callback) {
        this.setSlot(this.getFirstEmptySlot(), new GuiElement(itemStack, callback));
    }

    /**
     * Allows to add own Slot instances, that can point to any inventory.
     * Do not add duplicates (including player inventory) as it can cause item duplication!
     *
     * @param index the slot index (in this gui)
     * @param slot  the slot to redirect to
     * @see SimpleGuiBuilder#addSlotRedirect(Slot)
     */
    public void setSlotRedirect(int index, Slot slot) {
        this.elements[index] = null;
        this.slotRedirects[index] = slot;
        this.hasRedirects = true;
    }

    /**
     * Sets the first open slot with selected Slot instance.
     * Works the same way as {@code setSlotRedirect}.
     * Do not add duplicates (including player inventory) as it can cause item duplication!
     *
     * @param slot the slot to redirect to
     * @see SimpleGuiBuilder#setSlotRedirect(int, Slot)
     */
    public void addSlotRedirect(Slot slot) {
        this.setSlotRedirect(this.getFirstEmptySlot(), slot);
    }

    /**
     * Returns the first empty slot inside the inventory.
     *
     * @return the index of the first empty slot or <code>-1</code> if full
     */
    public int getFirstEmptySlot() {
        for (int i = 0; i < this.elements.length; i++) {
            if (this.elements[i] == null && this.slotRedirects[i] == null) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Reverts slot to it's original state.
     *
     * @param index slot index
     */
    public void clearSlot(int index) {
        this.elements[index] = null;
        this.slotRedirects[index] = null;
    }

    /**
     * Returns if the gui includes the player inventory slots.
     *
     * @return <code>true</code> if the player inventory slots.
     */
    public boolean isIncludingPlayer() {
        return this.includePlayer;
    }

    /**
     * Returns the number of slots in the virtual inventory only.
     * Works the same as {@link SimpleGuiBuilder#getSize()}, however excludes player gui slots if <code>includePlayer</code> is <code>true</code>.
     *
     * @return the size of the virtual inventory
     * @see SimpleGuiBuilder#getSize()
     */
    public int getVirtualSize() {
        return this.sizeCont;
    }

    /**
     * Returns the element in the referenced slot.
     *
     * @param index the slot index
     * @return the element or <code>null</code> if empty
     * @see SimpleGuiBuilder#getSlotRedirect(int)
     */
    public GuiElementInterface getSlot(int index) {
        if (index >= 0 && index < this.size) {
            return this.elements[index];
        }
        return null;
    }

    /**
     * Returns the external slot the referenced slot is redirecting to.
     *
     * @param index the slot index
     * @return the element or <code>null</code> if no redirect
     * @see SimpleGuiBuilder#getSlot(int)
     */
    public Slot getSlotRedirect(int index) {
        if (index >= 0 && index < this.size) {
            return this.slotRedirects[index];
        }
        return null;
    }

    /**
     * Returns if this gui has slot redirects.
     *
     * @return <code>true</code> if this gui has slot redirects
     * @see SimpleGuiBuilder#getSlotRedirect(int)
     */
    public boolean isRedirectingSlots() {
        return this.hasRedirects;
    }

    public Text getTitle() {
        return this.title;
    }

    public void setTitle(Text title) {
        this.title = title;
    }

    public ScreenHandlerType<?> getType() {
        return this.type;
    }

    @Override
    public int getSize() {
        return this.size;
    }

    public boolean getLockPlayerInventory() {
        return this.lockPlayerInventory || this.includePlayer;
    }

    public void setLockPlayerInventory(boolean value) {
        this.lockPlayerInventory = value;
    }
}
