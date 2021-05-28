package eu.pb4.sgui.api.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.ScreenProperty;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.virtual.BookScreenHandler;
import eu.pb4.sgui.virtual.BookScreenHandlerFactory;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.OptionalInt;

public class BookGui implements GuiInterface {
    protected final ServerPlayerEntity player;
    protected final ItemStack book;
    protected int page = 0;
    protected boolean open = false;
    protected boolean reOpen = false;
    protected BookScreenHandler screenHandler = null;

    protected int syncId = -1;

    public BookGui(ServerPlayerEntity player, ItemStack book) {
        this.player = player;
        this.book = book;
    }

    /**
     * Sets the selected page number
     *
     * @param page the page index, from 0
     */
    public void setPage(int page) {
        this.page = MathHelper.clamp(page, 0, WrittenBookItem.getPageCount(this.getBook()));
        this.sendProperty(ScreenProperty.PAGE_NUMBER, this.page);
    }

    /**
     * Gets the current selected page
     *
     * @return the page index, from 0
     */
    public int getPage() {
        return page;
    }

    @Override
    public void setTitle(Text title) {}

    @Override
    public Text getTitle() {return null;}

    @Override
    public ScreenHandlerType<?> getType() { return ScreenHandlerType.LECTERN; }

    @Override
    public ServerPlayerEntity getPlayer() {
        return player;
    }

    @Override
    public int getSyncId() {
        return syncId;
    }

    @Override
    public boolean isOpen() {
        return this.open;
    }

    @Override
    public boolean open() {
        if (!this.player.isDisconnected() && !this.open) {
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
        }
        return false;
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

    /**
     * Activates when the 'Take Book' button is pressed
     */
    public boolean onTakeBookButton() {
        return false;
    }
}
