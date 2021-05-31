package eu.pb4.sgui.api.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilderInterface;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.virtual.inventory.VirtualScreenHandler;
import eu.pb4.sgui.virtual.inventory.VirtualScreenHandlerFactory;
import eu.pb4.sgui.virtual.inventory.VirtualSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerPropertyUpdateS2CPacket;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.OptionalInt;

/**
 * Simple Gui Implementation
 * <p>
 * This is the implementation for all {@link Slot} based screens. It contains methods for
 * interacting, redirecting and modifying slots and items.
 */
public class SimpleGui implements GuiInterface {
    protected final ServerPlayerEntity player;
    protected final int size;
    protected final int width;
    protected final int height;
    protected final ScreenHandlerType<?> type;
    protected final GuiElementInterface[] elements;
    protected final Slot[] slotRedirects;
    private final boolean includePlayer;
    private final int sizeCont;
    protected boolean open = false;
    protected boolean autoUpdate = true;
    protected boolean reOpen = false;
    protected boolean lockPlayerInventory = false;
    protected VirtualScreenHandler screenHandler = null;
    protected int syncId = -1;
    protected boolean hasRedirects = false;
    private Text title = null;

    /**
     * Constructs a new simple container gui for the supplied player.
     *
     * @param type                        the screen handler that the client should display
     * @param player                      the player to server this gui to
     * @param includePlayerInventorySlots if <code>true</code> the players inventory
     *                                    will be treated as slots of this gui
     */
    public SimpleGui(ScreenHandlerType<?> type, ServerPlayerEntity player, boolean includePlayerInventorySlots) {
        int width1;
        this.player = player;

        width1 = 9;

        if (ScreenHandlerType.GENERIC_9X6.equals(type)) {
            this.height = 6;
        } else if (ScreenHandlerType.GENERIC_9X5.equals(type)) {
            this.height = 5;
        } else if (ScreenHandlerType.GENERIC_9X4.equals(type)) {
            this.height = 4;
        } else if (ScreenHandlerType.GENERIC_9X3.equals(type) || ScreenHandlerType.SHULKER_BOX.equals(type)) {
            this.height = 3;
        } else if (ScreenHandlerType.GENERIC_9X2.equals(type)) {
            this.height = 2;
        } else if (ScreenHandlerType.CRAFTING.equals(type)) {
            this.height = 5;
            width1 = 2;
        } else if (ScreenHandlerType.GENERIC_3X3.equals(type)) {
            this.height = 3;
            width1 = 3;
        } else if (ScreenHandlerType.GENERIC_9X1.equals(type)) {
            this.height = 1;
        } else if (ScreenHandlerType.HOPPER.equals(type) || ScreenHandlerType.BREWING_STAND.equals(type)) {
            this.height = 1;
            width1 = 5;
        } else if (ScreenHandlerType.BLAST_FURNACE.equals(type) || ScreenHandlerType.FURNACE.equals(type) || ScreenHandlerType.SMOKER.equals(type) || ScreenHandlerType.ANVIL.equals(type) || ScreenHandlerType.SMITHING.equals(type) || ScreenHandlerType.GRINDSTONE.equals(type) || ScreenHandlerType.MERCHANT.equals(type) || ScreenHandlerType.CARTOGRAPHY_TABLE.equals(type) || ScreenHandlerType.LOOM.equals(type)) {
            this.height = 3;
            width1 = 1;
        } else if (ScreenHandlerType.ENCHANTMENT.equals(type) || ScreenHandlerType.STONECUTTER.equals(type)) {
            this.height = 2;
            width1 = 1;
        } else if (ScreenHandlerType.BEACON.equals(type)) {
            this.height = 1;
            width1 = 1;
        } else {
            this.height = 3;
            type = ScreenHandlerType.GENERIC_9X3;
        }

        this.type = type;

        this.width = width1;
        int tmp = includePlayerInventorySlots ? 36 : 0;
        this.size = this.width * this.height + tmp;
        this.sizeCont = this.width * this.height;
        this.elements = new GuiElementInterface[this.size];
        this.slotRedirects = new Slot[this.size];


        this.includePlayer = includePlayerInventorySlots;
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
     * @see SimpleGui#addSlot(GuiElementInterface)
     */
    public void setSlot(int index, GuiElementInterface element) {
        this.elements[index] = element;
        if (this.open && this.autoUpdate) {
            if (this.screenHandler != null) {
                this.screenHandler.setSlot(index, new VirtualSlot(this.screenHandler.inventory, index, 0, 0));
            }
        }
    }

    /**
     * Sets the first open slot with selected GuiElement.
     *
     * @param element any GuiElement
     * @see SimpleGui#setSlot(int, GuiElementInterface)
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
     * @see SimpleGui#addSlot(ItemStack) 
     */
    public void setSlot(int index, ItemStack itemStack) {
        this.setSlot(index, new GuiElement(itemStack, (x, y, z) -> {
        }));
    }

    /**
     * Sets the first open slot with selected ItemStack.
     *
     * @param itemStack a stack of items
     * @see SimpleGui#setSlot(int, ItemStack)
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
     * @see SimpleGui#addSlot(GuiElementBuilderInterface) 
     */
    public void setSlot(int index, GuiElementBuilderInterface element) {
        this.setSlot(index, element.build());
    }

    /**
     * Sets the first open slot with selected GuiElement created from a builder.
     *
     * @param element any GuiElementBuilder
     * @see SimpleGui#setSlot(int, GuiElementBuilderInterface)
     */
    public void addSlot(GuiElementBuilderInterface element) {
        this.setSlot(this.getFirstEmptySlot(), element.build());
    }

    /**
     * Sets slot with ItemStack and callback.
     *
     * @param index     the slots index, from 0 to (max size - 1)
     * @param itemStack a stack of items
     * @param callback  the callback to run when clicked
     * @throws IndexOutOfBoundsException if the slot is out of bounds
     * @see SimpleGui#addSlot(ItemStack, GuiElement.ItemClickCallback) 
     */
    public void setSlot(int index, ItemStack itemStack, GuiElement.ItemClickCallback callback) {
        this.setSlot(index, new GuiElement(itemStack, callback));
    }

    /**
     * Sets the first open slot with ItemStack and callback
     *
     * @param itemStack a stack of items
     * @param callback  the callback to run when clicked
     * @see SimpleGui#setSlot(int, ItemStack, GuiElement.ItemClickCallback)
     */
    public void addSlot(ItemStack itemStack, GuiElement.ItemClickCallback callback) {
        this.setSlot(this.getFirstEmptySlot(), new GuiElement(itemStack, callback));
    }

    /**
     * Allows to add own Slot instances, that can point to any inventory.
     * Do not add duplicates (including player inventory) as it can cause item duplication!
     *
     * @param index the slot index (in this gui)
     * @param slot  the slot to redirect to
     * @see SimpleGui#addSlotRedirect(Slot)
     */
    public void setSlotRedirect(int index, Slot slot) {
        this.elements[index] = null;
        this.slotRedirects[index] = slot;
        if (this.open && this.autoUpdate) {
            if (this.screenHandler != null) {
                this.screenHandler.setSlot(index, slot);
            }
        }
        this.hasRedirects = true;
    }

    /**
     * Sets the first open slot with selected Slot instance.
     * Works the same way as {@code setSlotRedirect}.
     * Do not add duplicates (including player inventory) as it can cause item duplication!
     *
     * @param slot the slot to redirect to
     * @see SimpleGui#setSlotRedirect(int, Slot)
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

        if (this.open && this.autoUpdate) {
            if (this.screenHandler != null) {
                this.screenHandler.setSlot(index, new VirtualSlot(this.screenHandler.inventory, index, 0, 0));
            }
        }
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
     * Works the same as {@link SimpleGui#getSize()}, however excludes player gui slots if <code>includePlayer</code> is <code>true</code>.
     *
     * @return the size of the virtual inventory
     * @see SimpleGui#getSize()
     */
    public int getVirtualSize() {
        return this.sizeCont;
    }

    /**
     * Returns the element in the referenced slot.
     *
     * @param index the slot index
     * @return the element or <code>null</code> if empty
     * @see SimpleGui#getSlotRedirect(int)
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
     * @see SimpleGui#getSlot(int)
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
     * @see SimpleGui#getSlotRedirect(int)
     */
    public boolean isRedirectingSlots() {
        return this.hasRedirects;
    }

    /**
     * Sends the gui to the player
     *
     * @return <code>true</code> if successful
     */
    protected boolean sendGui() {
        this.reOpen = true;
        OptionalInt temp = this.player.openHandledScreen(new VirtualScreenHandlerFactory(this));
        this.reOpen = false;
        if (temp.isPresent()) {
            this.syncId = temp.getAsInt();
            if (this.player.currentScreenHandler instanceof VirtualScreenHandler) {
                this.screenHandler = (VirtualScreenHandler) this.player.currentScreenHandler;
                return true;
            }
        }
        return false;
    }

    /**
     * Executes when player clicks any slot.
     *
     * @param index  the slot index
     * @param type   the simplified type of click
     * @param action Minecraft's Slot Action Type
     * @return <code>true</code> if to allow manipulation of redirected slots, otherwise <code>false</code>
     */
    public boolean onAnyClick(int index, ClickType type, SlotActionType action) {
        return true;
    }

    /**
     * Executes after player clicks any recipe from recipe book.
     *
     * @param recipe the selected recipe identifier
     * @param shift  is shift was held
     */
    public void onCraftRequest(Identifier recipe, boolean shift) {
    }

    /**
     * Used internally to receive clicks from the client.
     *
     * @see SimpleGui#onClick(int, ClickType, SlotActionType, GuiElementInterface)
     * @see SimpleGui#onAnyClick(int, ClickType, SlotActionType)
     */
    @ApiStatus.Internal
    public boolean click(int index, ClickType type, SlotActionType action) {
        GuiElementInterface element = this.getSlot(index);
        if (element != null) {
            element.getCallback().click(index, type, action);
        }
        return this.onClick(index, type, action, element);
    }

    @Override
    public Text getTitle() {
        return this.title;
    }

    @Override
    public void setTitle(Text title) {
        this.title = title;

        if (this.open) {
            this.reOpen = true;
            this.sendGui();
        }
    }

    @Override
    public ScreenHandlerType<?> getType() {
        return this.type;
    }

    @Override
    public boolean isOpen() {
        return this.open;
    }

    @Override
    public boolean open() {
        if (this.player.isDisconnected() || this.open) {
            return false;
        } else {
            this.open = true;
            this.onUpdate(true);
            return this.sendGui();
        }
    }

    @Override
    public int getSize() {
        return this.size;
    }

    @Override
    public void close(boolean screenHandlerIsClosed) {
        if (this.open && !this.reOpen) {
            this.open = false;
            this.reOpen = false;

            if (!screenHandlerIsClosed && this.player.currentScreenHandler == this.screenHandler) {
                this.player.closeHandledScreen();
            }

            this.player.networkHandler.sendPacket(new InventoryS2CPacket(this.player.playerScreenHandler.syncId, this.player.playerScreenHandler.getStacks()));
            this.onClose();
        } else {
            this.reOpen = false;
        }
    }

    @Override
    public boolean getLockPlayerInventory() {
        return this.lockPlayerInventory || this.includePlayer;
    }

    @Override
    public void setLockPlayerInventory(boolean value) {
        this.lockPlayerInventory = value;
    }

    @Override
    public boolean getAutoUpdate() {
        return this.autoUpdate;
    }

    @Override
    public void setAutoUpdate(boolean value) {
        this.autoUpdate = value;
    }

    @Override
    public ServerPlayerEntity getPlayer() {
        return this.player;
    }

    @Override
    public int getSyncId() {
        return syncId;
    }

    /**
     * Allows to send some additional properties to guis
     * <p>
     * See values at https://wiki.vg/Protocol#Window_Property as reference
     * @param property the property id
     * @param value    the value of the property to send
     *
     * @deprecated As of 0.4.0, replaced by {@link GuiInterface#sendProperty} as its much more readable
     */
    @Deprecated
    public void sendProperty(int property, int value) {
        this.player.networkHandler.sendPacket(new ScreenHandlerPropertyUpdateS2CPacket(this.syncId, property, value));
    }
}
