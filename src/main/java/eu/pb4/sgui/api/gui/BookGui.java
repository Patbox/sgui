package eu.pb4.sgui.api.gui;

import eu.pb4.sgui.api.ScreenProperty;
import eu.pb4.sgui.api.elements.BookElementBuilder;
import eu.pb4.sgui.virtual.book.BookScreenHandler;
import eu.pb4.sgui.virtual.book.BookScreenHandlerFactory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.OptionalInt;

/**
 * Book Gui Implementation
 * <p>
 * BookGui is used to display book pages to the player. A pre-existing book needs
 * to be passed into the constructor, this is what will be displayed.
 * <p>
 * BookGui has lots of deprecated methods which have no function, this is
 * mainly due to the lack of item slots in the book interface.
 */
@SuppressWarnings("unused")
public class BookGui implements GuiInterface {
    protected final ServerPlayerEntity player;
    protected final ItemStack book;
    protected int page = 0;
    protected boolean open = false;
    protected boolean reOpen = false;
    protected BookScreenHandler screenHandler = null;

    protected int syncId = -1;

    /**
     * Constructs a new BookGui for the supplied player, based
     * on the provided book.
     *
     * @param player the player to serve this gui to
     * @param book   the book stack to display
     * @throws IllegalArgumentException if the provided item is not a book
     */
    public BookGui(ServerPlayerEntity player, ItemStack book) {
        this.player = player;

        if (ItemTags.LECTERN_BOOKS.contains(book.getItem())) {
            throw new IllegalArgumentException("Item must be a book");
        }
        this.book = book;
    }

    /**
     * Constructs a new BookGui for the supplied player, based
     * on the provided book.
     *
     * @param player the player to serve this gui to
     * @param book   the book builder to display
     */
    public BookGui(ServerPlayerEntity player, BookElementBuilder book) {
        this.player = player;
        this.book = book.asStack();
    }

    /**
     * Sets the selected page number
     *
     * @param page the page index, from 0
     */
    public void setPage(int page) {
        this.page = MathHelper.clamp(page, 0, WrittenBookItem.getPageCount(this.getBook()));
        this.sendProperty(ScreenProperty.SELECTED, this.page);
    }

    /**
     * Returns the current selected page
     *
     * @return the page index, from 0
     */
    public int getPage() {
        return page;
    }

    /**
     * Returns the book item used to store the data.
     *
     * @return the book stack
     */
    public ItemStack getBook() {
        return this.book;
    }

    /**
     * Activates when the 'Take Book' button is pressed
     */
    public void onTakeBookButton() {
    }

    @Override
    public ScreenHandlerType<?> getType() { return ScreenHandlerType.LECTERN; }

    @Override
    public ServerPlayerEntity getPlayer() {
        return this.player;
    }

    @Override
    public int getSyncId() {
        return this.syncId;
    }

    @Override
    public boolean isOpen() {
        return this.open;
    }

    @Override
    public boolean open() {
        if (!this.player.isDisconnected() && !this.open) {
            this.open = true;
            this.onOpen();
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

    @Deprecated
    @Override
    public void setTitle(Text title) {
    }

    @Deprecated
    @Override
    public Text getTitle() {
        return null;
    }

    @Deprecated
    @Override
    public boolean getAutoUpdate() {
        return false;
    }

    @Deprecated
    @Override
    public void setAutoUpdate(boolean value) {
    }
}
