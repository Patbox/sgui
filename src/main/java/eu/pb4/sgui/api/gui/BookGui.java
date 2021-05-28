package eu.pb4.sgui.api.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.virtual.BookScreenHandler;
import eu.pb4.sgui.virtual.BookScreenHandlerFactory;
import net.minecraft.item.ItemStack;
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
    protected BookScreenHandler screenHandler = null;
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
                if (this.player.currentScreenHandler instanceof BookScreenHandler) {
                    this.screenHandler = (BookScreenHandler) this.player.currentScreenHandler;
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

    public void close() {
        this.close(false);
    }

    @Deprecated
    public void close(boolean screenHandlerIsClosed) {
        if (this.open && !this.reOpen) {
            this.open = false;
            this.reOpen = false;

            if (!screenHandlerIsClosed && this.player.currentScreenHandler == this.screenHandler) {
                this.player.closeHandledScreen();
            }

            this.onClose();
        } else {
            this.reOpen = false;
        }
    }

    public boolean getLockPlayerInventory() {
        return false;
    }

    public void setLockPlayerInventory(boolean value) {}

    public boolean getAutoUpdate() {
        return false;
    }

    public void setAutoUpdate(boolean value) {}

    public void onOpen() {}

    public boolean onClick(int index, ClickType type, SlotActionType action, GuiElementInterface element) {
        return false;
    }

    public void onUpdate(boolean firstUpdate) {}

    public void onClose() {}

    public void onTick() {}

    public ItemStack getBook() {
        return this.book;
    }
}
