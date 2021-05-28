package eu.pb4.sgui.api.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.ScreenProperty;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilderInterface;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.virtual.VirtualScreenHandler;
import eu.pb4.sgui.virtual.VirtualScreenHandlerFactory;
import eu.pb4.sgui.virtual.VirtualSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerPropertyUpdateS2CPacket;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.OptionalInt;

/**
 * Simple gui implementation
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
    private Text title = null;
    protected boolean open = false;
    protected boolean autoUpdate = true;
    protected boolean reOpen = false;
    protected boolean lockPlayerInventory = false;
    protected VirtualScreenHandler screenHandler = null;

    protected int syncId = -1;

    protected boolean hasRedirects = false;

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

    public Text getTitle() {
        return this.title;
    }

    public void setTitle(Text title) {
        this.title = title;

        if (this.open) {
            this.reOpen = true;
            this.sendGui();
        }
    }

    public ScreenHandlerType<?> getType() {
        return this.type;
    }

    public boolean isOpen() {
        return this.open;
    }

    public boolean open() {
        if (this.player.isDisconnected() || this.open) {
            return false;
        } else {
            this.open = true;
            this.onUpdate(true);
            return this.sendGui();
        }
    }

    public int getSize() {
        return this.size;
    }

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }

    /**
     * Sets slot with selected GuiElement
     *
     * @param index Slots index, from 0 to (max size - 1)
     * @param element Any GuiElement
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
     * Sets slot with selected ItemStack
     *
     * @param index Slots index, from 0 to (max size - 1)
     * @param itemStack Stack of Items
     */
    public void setSlot(int index, ItemStack itemStack) {
        this.setSlot(index, new GuiElement(itemStack, (x, y, z) -> {}));
    }

    /**
     * Sets slot with selected GuiElement created from builder
     *
     * @param index Slots index, from 0 to (max size - 1)
     * @param element Any GuiElementBuilder
     */
    public void setSlot(int index, GuiElementBuilderInterface element) {
        this.setSlot(index, element.build());
    }

    /**
     * Sets slot with ItemStack and Callback
     *
     * @param index Slots index, from 0 to (max size - 1)
     * @param itemStack Stack of Items
     * @param callback Callback run when clicked
     */
    public void setSlot(int index, ItemStack itemStack, GuiElement.ItemClickCallback callback) {
        this.setSlot(index, new GuiElement(itemStack, callback));
    }

    /**
     * Allows to add own Slot instances, that can point to any inventory
     * Do not add duplicates (including player inventory)
     * as it can cause item duplication!
     *
     * @param index Slot index
     * @param slot Slot
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
     * Reverts slot to it's original state
     *
     * @param index Slot index
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
     * Closes gui
     */
    public void close() {
        this.close(false);
    }


    /**
     * Used internally!
     * Used for closing or activating stuff after gui is closed.
     *
     * @param screenHandlerIsClosed Is set to true, if gui's ScreenHandler is already closed
     */
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

    /**
     * Checks if gui includes slot of player inventory
     *
     * @return boolean
     */
    public boolean isIncludingPlayer() {
        return this.includePlayer;
    }

    public int getVirtualSize() {
        return this.sizeCont;
    }

    public GuiElementInterface getSlot(int index) {
        if (index >= 0 && index < this.size) {
            return this.elements[index];
        }
        return null;
    }

    public Slot getSlotRedirect(int index) {
        if (index >= 0 && index < this.size) {
            return this.slotRedirects[index];
        }
        return null;
    }

    public boolean click(int index, ClickType type, SlotActionType action) {
        GuiElementInterface element = this.getSlot(index);
        if (element != null) {
            element.getCallback().click(index, type, action);
        }
        return this.onClick(index, type, action, element);
    }

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

    public boolean getLockPlayerInventory() {
        return this.lockPlayerInventory || this.includePlayer;
    }

    public void setLockPlayerInventory(boolean value) {
        this.lockPlayerInventory = value;
    }

    public boolean getAutoUpdate() {
        return this.autoUpdate;
    }

    public void setAutoUpdate(boolean value) {
        this.autoUpdate = value;
    }

    public ServerPlayerEntity getPlayer() {
        return this.player;
    }

    @Override
    public int getSyncId() {
        return syncId;
    }

    public boolean isRedirectingSlots() {
        return this.hasRedirects;
    }

    /**
     * Allows to send some additional properties to guis
     *
     * See values at https://wiki.vg/Protocol#Window_Property as reference
     * @param property the property id
     * @param value the value of the property to send
     *              
     * @see GuiInterface#sendProperty(ScreenProperty, int) 
     */
    @Deprecated
    public void sendProperty(int property, int value) {
        this.player.networkHandler.sendPacket(new ScreenHandlerPropertyUpdateS2CPacket(this.syncId, property, value));
    }

    /**
     * Executed when player clicks GuiElement
     *
     * @param index slot index
     * @param type Simplified type of click
     * @param action Minecraft's Slot Action Type
     * @param element Clicked GuiElement
     * @return Returns false, for automatic handling and syncing or true, if you want to do it manually
     */
    public boolean onClick(int index, ClickType type, SlotActionType action, GuiElementInterface element) {
        return false;
    }

    /**
     * Executed when player clicks any slot
     *
     * @param index slot index
     * @param type Simplified type of click
     * @param action Minecraft's Slot Action Type
     * @return Returns true, if you want to allow manipulation of redirected slots. Otherwise false
     */
    public boolean onAnyClick(int index, ClickType type, SlotActionType action) {
        return true;
    }

    /**
     * Executed before gui is (re)send to player
     * @param firstUpdate
     */
    public void onUpdate(boolean firstUpdate) {}

    /**
     * Executes after closing gui
     */
    public void onClose() {}

    /**
     * Executes after opening
     */
    public void onOpen() {}

    /**
     * Executes on every gui tick
     */
    public void onTick() {}

    /**
     * Executes after player clicks any recipe from recipe book
     */
    public void onCraftRequest(Identifier recipe, boolean shift) {}
}
