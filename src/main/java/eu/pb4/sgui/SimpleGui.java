package eu.pb4.sgui;

import eu.pb4.sgui.virtual.VirtualScreenHandlerFactory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.OptionalInt;

public class SimpleGui implements GuiInterface {
    protected final ServerPlayerEntity player;
    protected final int size;
    protected final int width;
    protected final int height;
    protected final ScreenHandlerType<?> type;
    protected final GuiElement[] elements;
    protected final Slot[] slotRedirects;
    private final boolean includePlayer;
    private final int sizeCont;
    private Text title = null;
    private boolean open = false;
    private boolean autoUpdate = true;
    protected boolean reOpen = false;
    private boolean lockPlayerInventory = false;

    private int syncId = -1;

    public SimpleGui(ScreenHandlerType<?> type, ServerPlayerEntity player, boolean includePlayer) {
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
        int tmp = includePlayer ? 36 : 0;
        this.size = this.width * this.height + tmp;
        this.sizeCont = this.width * this.height;
        this.elements = new GuiElement[this.size];
        this.slotRedirects = new Slot[this.size];


        this.includePlayer = includePlayer;
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
            this.reOpen = false;
            return this.sendGui();
        }
    }

    public int getSize() {
        return this.size;
    }

    public int getHeight() {
        return this.height;
    }

    public int getWitdh() {
        return this.width;
    }

    public void setSlot(int index, GuiElement element) {
        this.elements[index] = element;
        if (this.open && this.autoUpdate) {
            this.player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(this.syncId, index, element.getItem()));
        }
    }

    public void setSlot(int index, ItemStack itemStack) {
        this.setSlot(index, new GuiElement(itemStack, (x, y, z) -> {}));
    }

    public void setSlot(int index, GuiElementBuilder element) {
        this.setSlot(index, element.build());
    }

    public void setSlot(int index, ItemStack itemStack, GuiElement.ItemClickCallback callback) {
        this.setSlot(index, new GuiElement(itemStack, callback));
    }

    public void setSlotRedirect(int index, Slot slot) {
        this.elements[index] = null;
        this.slotRedirects[index] = slot;
        if (this.open && this.autoUpdate) {
            this.player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(this.syncId, index, slot.getStack()));
        }
    }

    public void clearSlot(int index) {
        this.elements[index] = null;
        this.slotRedirects[index] = null;
    }

    public void updateSlot(int index, ItemStack itemStack) {
        GuiElement element = this.elements[index];

        if (element != null) {
            element.setItem(itemStack);
        }

        if (this.open && this.autoUpdate) {
            this.player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(this.syncId, index, itemStack));
        }
    }

    public void close() {
        this.close(true);
    }


    public void close(boolean screenIsClosed) {
        if (this.open && !this.reOpen) {
            if (!screenIsClosed && this.player.currentScreenHandler.syncId == this.syncId) {
                this.player.closeHandledScreen();
            }

            this.player.networkHandler.sendPacket(new InventoryS2CPacket(this.player.playerScreenHandler.syncId, this.player.playerScreenHandler.getStacks()));
            this.open = false;
            this.onClose();
        } else {
            this.reOpen = false;
        }
    }

    public boolean isIncludingPlayer() {
        return this.includePlayer;
    }

    public int getVirtualSize() {
        return this.sizeCont;
    }

    public GuiElement getSlot(int index) {
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
        GuiElement element = this.getSlot(index);
        if (element != null) {
            element.getCallback().click(index, type, action);
        }
        return this.onClick(index, type, action, element);
    }

    protected boolean sendGui() {
        OptionalInt temp = this.player.openHandledScreen(new VirtualScreenHandlerFactory(this));
        if (temp.isPresent()) {
            this.syncId = temp.getAsInt();
            return true;
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

    public void onOpen() {
    }

    public boolean onClick(int index, ClickType type, SlotActionType action, GuiElement element) {
        return false;
    }

    public boolean onAnyClick(int index, ClickType type, SlotActionType action) {
        return true;
    }

    public void onUpdate(boolean firstUpdate) {
    }

    public void onClose() {
    }
}
