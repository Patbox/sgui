package eu.pb4.sgui.api.gui;

import eu.pb4.sgui.api.ScreenProperty;
import eu.pb4.sgui.api.elements.BookElementBuilder;
import eu.pb4.sgui.virtual.SguiScreenHandlerFactory;
import eu.pb4.sgui.virtual.book.BookScreenHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

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
    protected final ServerPlayer player;
    protected ItemStack book;
    protected int page = 0;
    @Deprecated(forRemoval = true)
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
    public BookGui(ServerPlayer player, ItemStack book) {
        this.player = player;
        this.book = book;
    }

    public BookGui(ServerPlayer player) {
        this(player, ItemStack.EMPTY);
    }

    /**
     * Constructs a new BookGui for the supplied player, based
     * on the provided book.
     *
     * @param player the player to serve this gui to
     * @param book   the book builder to display
     */
    public BookGui(ServerPlayer player, BookElementBuilder book) {
        this.player = player;
        this.book = book.asStack();
    }

    /**
     * Sets the selected page number
     *
     * @param page the page index, from 0
     */
    public void setPage(int page) {
        this.page = page;
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
     * Updates the book.
     */
    public void setBook(ItemStack book) {
        this.book = book;
    }

    /**
     * Updates the book.
     */
    public void setBook(BookElementBuilder book) {
        this.book = book.asStack();
    }

    /**
     * Activates when the 'Take Book' button is pressed
     */
    public void onTakeBookButton() {
    }

    /**
     * Activates when player selects a page (via click event).
     * The page passed is equal to one provided in click event!
     */
    public void onPageSelected(int page) {
        this.setPage(page - 1);
    }

    /**
     * Activates when player selects a page (via click event).
     */
    public void onNextPageButton() {
        this.setPage(this.getPage() + 1);
    }

    /**
     * Activates when player selects a page (via click event).
     */
    public void onPreviousPageButton() {
        this.setPage(this.getPage() - 1);
    }

    @Override
    public MenuType<?> getType() { return MenuType.LECTERN; }

    @Override
    public ServerPlayer getPlayer() {
        return this.player;
    }

    @Override
    public int getSyncId() {
        return this.syncId;
    }

    @Override
    public boolean isOpen() {
        return this.screenHandler == this.player.containerMenu;
    }

    @Override
    public boolean open() {
        var state = false;
        if (!this.player.hasDisconnected() && !this.isOpen()) {
            this.beforeOpen();
            state = this.setupScreenHandler();
            this.afterOpen();
        }
        return state;
    }

    protected boolean setupScreenHandler() {
        //noinspection removal
        this.open = true;
        this.onOpen();
        this.reOpen = true;
        OptionalInt temp = this.player.openMenu(new SguiScreenHandlerFactory<>(this, (syncId, inv, player) -> new BookScreenHandler(syncId, this, player)));
        this.reOpen = false;
        if (temp.isPresent()) {
            this.syncId = temp.getAsInt();
            if (this.player.containerMenu instanceof BookScreenHandler) {
                this.screenHandler = (BookScreenHandler) this.player.containerMenu;
                this.sendProperty(ScreenProperty.SELECTED, this.page);
                return true;
            }
        }
        return false;
    }

    /**
     * Called when player executes command via {@link net.minecraft.Component.ClickEvent.Action#RUN_COMMAND}
     *
     * @param command input command
     * @return Returns false, for continuing execution or true, if you want to cancel it
     */
    public boolean onCommand(String command) {
        return false;
    }

    @Override
    public void close(boolean screenHandlerIsClosed) {
        if (this.isOpen() && !this.reOpen) {
            //noinspection removal
            this.open = this.isOpen();
            this.reOpen = false;

            if (!screenHandlerIsClosed && this.player.containerMenu == this.screenHandler) {
                this.player.closeContainer();
            }

            this.onClose();
        } else {
            this.reOpen = false;
        }
    }

    @Deprecated
    @Override
    public void setTitle(Component title) {
    }

    @Deprecated
    @Override
    public Component getTitle() {
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
