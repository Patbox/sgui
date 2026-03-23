package eu.pb4.sgui.api.gui;

import eu.pb4.sgui.api.SguiUtils;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.mixin.ScreenHandlerAccessor;
import eu.pb4.sgui.api.containerwrappers.SlotBasedWrapperMenu;
import eu.pb4.sgui.api.containerwrappers.GuiLikeMenuProvider;
import eu.pb4.sgui.api.containerwrappers.slot.WrappingSlot;
import java.util.ArrayList;
import java.util.OptionalInt;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;

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
    protected final MenuType<?> type;
    private final boolean includePlayer;
    private final int sizeCont;
    protected boolean lockPlayerInventory = false;
    protected SlotBasedWrapperMenu wrappedMenu = null;
    protected int syncId = -1;
    protected boolean hasRedirects = false;
    private Component title = null;

    /**
     * Constructs a new simple container gui for the supplied player.
     *
     * @param type                  the screen handler that the client should display
     * @param player                the player to server this gui to
     * @param manipulatePlayerSlots if <code>true</code> the players inventory
     *                              will be treated as slots of this gui
     */
    public SimpleGui(MenuType<?> type, ServerPlayer player, boolean manipulatePlayerSlots) {
        super(player, SguiUtils.getHeight(type) * SguiUtils.getWidth(type) + (manipulatePlayerSlots ? 36 : 0));
        this.height = SguiUtils.getHeight(type);
        this.width = SguiUtils.getWidth(type);

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
    public void setSlot(int index, GuiElement element) {
        super.setSlot(index, element);
        if (this.wrappedMenu != null && this.autoUpdate) {
            this.wrappedMenu.setSlot(index, new WrappingSlot(this, index, 0, 0));
        }
    }

    @Override
    public void setSlot(int index, Slot slot) {
        super.setSlot(index, slot);
        if (this.wrappedMenu != null && this.autoUpdate) {
            this.wrappedMenu.setSlot(index, slot);
        }
    }

    @Override
    public void clearSlot(int index) {
        super.clearSlot(index);
        this.hasRedirects = true;
        if (this.wrappedMenu != null && this.autoUpdate) {
            this.wrappedMenu.setSlot(index, new WrappingSlot(this, index, 0, 0));
        }
    }

    @Override
    public boolean isOpen() {
        return this.wrappedMenu != null && this.wrappedMenu == this.player.containerMenu;
    }

    @Override
    public Component getTitle() {
        return this.title;
    }

    @Override
    public void setTitle(Component title) {
        this.title = title;

        if (this.isOpen() && this.autoUpdate) {
            this.forceUpdateAll();
        }
    }

    protected void forceUpdateAll() {
        var list = new ArrayList<Packet<? super ClientGamePacketListener>>();
        list.add(new ClientboundOpenScreenPacket(this.syncId, this.type, title));
        list.add(new ClientboundContainerSetContentPacket(this.syncId, this.wrappedMenu.getStateId(),
                this.wrappedMenu.getItems(), this.wrappedMenu.getCarried()));
        for (int i = 0; i < this.properties.size(); i++) {
            list.add(new ClientboundContainerSetDataPacket(this.syncId, i, this.properties.getInt(i)));
        }

        this.player.connection.send(new ClientboundBundlePacket(list));
        for (var i = 0; i < this.wrappedMenu.slots.size(); i++) {
            this.wrappedMenu.setRemoteSlot(i, this.wrappedMenu.slots.get(i).getItem().copy());
        }
        ((ScreenHandlerAccessor) this.wrappedMenu).getRemoteCarried().force(this.wrappedMenu.getCarried());
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
     * @see SimpleGui#getCustomSlot(int)
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
        OptionalInt temp = this.player.openMenu(GuiLikeMenuProvider.ofDefault(this));
        this.reOpen = false;
        if (temp.isPresent()) {
            this.syncId = temp.getAsInt();
            if (this.player.containerMenu instanceof SlotBasedWrapperMenu) {
                this.wrappedMenu = (SlotBasedWrapperMenu) this.player.containerMenu;
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
    public void onCraftRequest(RecipeDisplayId recipe, boolean shift) {
    }

    @Override
    public MenuType<?> getType() {
        return this.type;
    }

    @Override
    public boolean open() {
        if (this.player.hasDisconnected() || this.isOpen()) {
            return false;
        } else {
            this.beforeOpen();
            this.onOpen();
            this.sendGui();
            return this.isOpen();
        }
    }

    public AbstractContainerMenu openAsMenu(int syncId, Inventory playerInventory, Player player) {
        if (this.player.hasDisconnected() || player != this.player || this.isOpen()) {
            return null;
        } else {
            this.beforeOpen();
            this.onOpen();
            this.wrappedMenu = new SlotBasedWrapperMenu(this.getType(), syncId, this, player);
            return this.wrappedMenu;
        }
    }

    @Override
    public void close(boolean skipSync) {
        if ((this.isOpen() || skipSync) && !this.reOpen) {
            if (!skipSync && this.player.containerMenu == this.wrappedMenu) {
                this.player.closeContainer();
                this.wrappedMenu = null;
            }

            this.player.containerMenu.sendAllDataToRemote();

            this.onManualClose();
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
}
