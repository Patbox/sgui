package eu.pb4.sgui.api.gui;

import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.virtual.inventory.VirtualScreenHandler;
import eu.pb4.sgui.virtual.SguiScreenHandlerFactory;
import eu.pb4.sgui.virtual.inventory.VirtualSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerPropertyUpdateS2CPacket;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.OptionalInt;

/**
 * Simple Gui Implementation
 * <p>
 * This is the implementation for all {@link Slot} based screens. It contains methods for
 * interacting, redirecting and modifying slots and items.
 */
@SuppressWarnings({"unused"})
public class SimpleGui extends BaseSlotGui {
    protected final int width;
    protected final int height;
    protected final ScreenHandlerType<?> type;
    private final boolean includePlayer;
    private final int sizeCont;
    protected boolean lockPlayerInventory = false;
    protected VirtualScreenHandler screenHandler = null;
    protected int syncId = -1;
    protected boolean hasRedirects = false;
    private Text title = null;

    /**
     * Constructs a new simple container gui for the supplied player.
     *
     * @param type                  the screen handler that the client should display
     * @param player                the player to server this gui to
     * @param manipulatePlayerSlots if <code>true</code> the players inventory
     *                              will be treated as slots of this gui
     */
    public SimpleGui(ScreenHandlerType<?> type, ServerPlayerEntity player, boolean manipulatePlayerSlots) {
        super(player, GuiHelpers.getHeight(type) * GuiHelpers.getWidth(type) + (manipulatePlayerSlots ? 36 : 0));
        this.height = GuiHelpers.getHeight(type);
        this.width = GuiHelpers.getWidth(type);

        this.type = type;
        this.sizeCont = this.width * this.height;
        this.includePlayer = manipulatePlayerSlots;
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
        super.setSlot(index, element);
        if (this.isOpen() && this.autoUpdate) {
            this.screenHandler.setSlot(index, new VirtualSlot(this.screenHandler.inventory, index, 0, 0));
        }
    }

    @Override
    public void setSlotRedirect(int index, Slot slot) {
        super.setSlotRedirect(index, slot);
        if (this.isOpen() && this.autoUpdate) {
            this.screenHandler.setSlot(index, slot);
        }
    }

    @Override
    public void clearSlot(int index) {
        super.clearSlot(index);
        this.hasRedirects = true;
        if (this.isOpen() && this.autoUpdate) {
            this.screenHandler.setSlot(index, new VirtualSlot(this.screenHandler.inventory, index, 0, 0));
        }
    }

    @Override
    public boolean isOpen() {
        return this.screenHandler != null && this.screenHandler == this.player.currentScreenHandler;
    }

    @Override
    public Text getTitle() {
        return this.title;
    }

    @Override
    public void setTitle(Text title) {
        this.title = title;

        if (this.isOpen()) {
            this.player.networkHandler.sendPacket(new OpenScreenS2CPacket(this.syncId, this.type, title));
            this.screenHandler.syncState();
        }
    }

    @Override
    public boolean getAutoUpdate() {
        return this.autoUpdate;
    }

    @Override
    public void setAutoUpdate(boolean value) {
        this.autoUpdate = value;
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
        OptionalInt temp = this.player.openHandledScreen(SguiScreenHandlerFactory.ofDefault(this));
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
     * Executes after player clicks any recipe from recipe book.
     *
     * @param recipe the selected recipe identifier
     * @param shift  is shift was held
     */
    public void onCraftRequest(Identifier recipe, boolean shift) {
    }

    @Override
    public ScreenHandlerType<?> getType() {
        return this.type;
    }

    @Override
    public boolean open() {
        if (this.player.isDisconnected() || this.isOpen()) {
            return false;
        } else {
            this.beforeOpen();
            this.onOpen();
            this.sendGui();
            //noinspection removal
            this.open = this.isOpen();
            this.afterOpen();
            return this.isOpen();
        }
    }

    @Override
    public void close(boolean screenHandlerIsClosed) {
        if ((this.isOpen() || screenHandlerIsClosed) && !this.reOpen) {
            this.reOpen = false;

            if (!screenHandlerIsClosed && this.player.currentScreenHandler == this.screenHandler) {
                this.player.closeHandledScreen();
                this.screenHandler = null;
            }

            this.player.currentScreenHandler.syncState();

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
