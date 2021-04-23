package eu.pb4.sgui.api.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.virtual.BookScreenHandlerFactory;
import eu.pb4.sgui.virtual.VirtualScreenHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.OptionalInt;

public class BookGui implements GuiInterface {
    private final ServerPlayerEntity player;
    private final ItemStack book;

    protected boolean open = false;
    protected boolean reOpen = false;
    protected VirtualScreenHandler screenHandler = null;
    private int syncId;

    public BookGui(ServerPlayerEntity player, ItemStack book) {
        this.player = player;
        this.book = book;
    }

    @Override
    public void setTitle(Text title) {}

    @Override
    public Text getTitle() {return null;}

    @Override
    public ScreenHandlerType<?> getType() { return ScreenHandlerType.LECTERN; }

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
            this.reOpen = true;
            OptionalInt temp = this.player.openHandledScreen(new BookScreenHandlerFactory(this));
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
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public void close() {
        this.close(false);
    }

    @Override
    public void close(boolean alreadyClosed) {
        if (this.open) {
            if (!alreadyClosed) {
                this.player.closeHandledScreen();
            }
            this.open = false;
            this.onClose();
        } else {
            this.reOpen = false;
        }
    }

    @Override
    public boolean getLockPlayerInventory() {
        return false;
    }

    @Override
    public void setLockPlayerInventory(boolean value) {

    }

    @Override
    public boolean getAutoUpdate() {
        return false;
    }

    @Override
    public void setAutoUpdate(boolean value) {

    }

    @Override
    public void onOpen() {

    }

    @Override
    public boolean onClick(int index, ClickType type, SlotActionType action, GuiElementInterface element) {
        return false;
    }

    @Override
    public void onUpdate(boolean firstUpdate) {

    }

    @Override
    public void onClose() {

    }

    public ItemStack getBook() {
        return this.book;
    }
}
