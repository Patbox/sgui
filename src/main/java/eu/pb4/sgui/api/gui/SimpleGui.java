package eu.pb4.sgui.api.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.elements.GuiElement;
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
@SuppressWarnings({"unused"})
public class SimpleGui implements SlotGuiInterface {
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
        this.player = player;

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

    @Override
    public void setSlot(int index, GuiElementInterface element) {
        if (this.elements[index] != null) {
            this.elements[index].onRemoved(this);
        }
        this.elements[index] = element;
        this.slotRedirects[index] = null;
        element.onAdded(this);
        if (this.open && this.autoUpdate && this.screenHandler != null) {
            this.screenHandler.setSlot(index, new VirtualSlot(this.screenHandler.inventory, index, 0, 0));
        }
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
        if (this.elements[index] != null) {
            this.elements[index].onRemoved(this);
            this.elements[index] = null;
        }
        this.slotRedirects[index] = slot;
        if (this.open && this.autoUpdate && this.screenHandler != null) {
            this.screenHandler.setSlot(index, slot);
        }
        this.hasRedirects = true;
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
            element.getGuiCallback().click(index, type, action, this);
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

            this.player.networkHandler.sendPacket(new InventoryS2CPacket(this.player.playerScreenHandler.syncId, this.player.playerScreenHandler.nextRevision(), this.player.playerScreenHandler.getStacks(), ItemStack.EMPTY));
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
     *
     * @param property the property id
     * @param value    the value of the property to send
     * @deprecated As of 0.4.0, replaced by {@link GuiInterface#sendProperty} as its much more readable
     */
    @Deprecated
    public void sendProperty(int property, int value) {
        this.player.networkHandler.sendPacket(new ScreenHandlerPropertyUpdateS2CPacket(this.syncId, property, value));
    }

    @Deprecated
    public void setSlot(int index, ItemStack itemStack, GuiElementInterface.ItemClickCallback callback) {
        this.setSlot(index, new GuiElement(itemStack, callback));
    }

    @Deprecated
    public void addSlot(ItemStack itemStack, GuiElementInterface.ItemClickCallback callback) {
        this.addSlot(new GuiElement(itemStack, callback));
    }
}
