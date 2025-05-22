package eu.pb4.sgui.testmod;

import eu.pb4.sgui.api.gui.GuiInterface;
import eu.pb4.sgui.virtual.FakeScreenHandler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WritableBookItem;
import net.minecraft.world.item.component.WritableBookContent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * This implementation doesn't work, because Mojang is handling opening of
 * editing gui on client for some reason (but opening signed one on server)
 * I'm not removing it for now, as hopefully it will be fixed that one day.
 *
 * Book Input Gui Implementation
 * <p>
 * BookInputGui is used to display (modifiable) book pages to the player.
 * You can provide (optional) book ItemStack used as a base of sended book.
 * One of the limitations of this ui compared to standard BookGui is lack
 * of support for custom formatting
 * <p>
 * BookInputGui has lots of deprecated methods which have no function, this is
 * mainly due to the lack of item slots in the book interface.
 */
@Deprecated
public class BookInputGui implements GuiInterface {
    protected final ServerPlayer player;
    protected boolean open = false;
    protected boolean reOpen = false;
    protected FakeScreenHandler screenHandler = null;

    protected int syncId = -1;
    private boolean autoUpdate;
    private String title;
    private final List<String> pages;

    /**
     * Constructs a new book input gui for the supplied player.
     *
     * @param player the player to server this gui to
     */
    public BookInputGui(ServerPlayer player) {
        this(player, null);
    }

    /**
     * Constructs a new book input gui for the supplied player based on provided book.
     *
     * @param player the player to server this gui to
     * @param book   book to base on
     */
    public BookInputGui(ServerPlayer player, ItemStack book) {
        this.player = player;
        this.pages = new ArrayList<>();
        if (book != null && book.getItem() instanceof WritableBookItem) {
            for (Filterable<String> page : book.get(DataComponents.WRITABLE_BOOK_CONTENT).pages()) {
                this.pages.add(page.raw());
            }
        }
    }

    @Override
    public boolean isOpen() {
        return this.open;
    }

    @Override
    public boolean open() {
        this.reOpen = true;

        if (this.player.containerMenu != this.player.inventoryMenu && this.player.containerMenu != this.screenHandler) {
            this.player.closeContainer();
        }
        if (screenHandler == null) {
            this.screenHandler = new FakeScreenHandler(this);
        }
        this.player.containerMenu = this.screenHandler;

        ItemStack stack = Items.WRITABLE_BOOK.getDefaultInstance();
        stack.set(DataComponents.WRITABLE_BOOK_CONTENT, new WritableBookContent(pages.stream().map(Filterable::passThrough).toList()));

        this.player.connection.send(new ClientboundContainerSetSlotPacket(-2, 0, Inventory.SLOT_OFFHAND, stack));
        this.player.connection.send(new ClientboundOpenBookPacket(InteractionHand.OFF_HAND));

        this.reOpen = false;
        this.open = true;

        return true;
    }

    @Override
    public ServerPlayer getPlayer() {
        return this.player;
    }

    @Override
    public void close(boolean alreadyClosed) {
        if (this.open && !this.reOpen) {
            this.open = false;
            this.reOpen = false;

            this.player.connection.send(new ClientboundContainerSetSlotPacket(-2, 0, Inventory.SLOT_OFFHAND, this.player.getOffhandItem()));

            if (alreadyClosed && this.player.containerMenu == this.screenHandler) {
                this.player.doCloseContainer();
            } else {
                this.player.closeContainer();
            }

            this.onClose();
        } else {
            this.reOpen = false;
            this.open();
        }
    }


    @ApiStatus.Internal
    public void writeBook(String title, List<String> pages, boolean signed) {
        if (title != null) {
            this.title = title;
        }

        int x = 0;
        for (String page : pages) {
            this.setPageInternal(x++, page);
        }
        this.onBookWritten(title, pages, signed);
        this.close(true);
    }

    /**
     * This method is when player closes the book with or without signing it
     *
     * @param title  Optional title
     * @param pages  List of pages (including predefined ones)
     * @param signed true if book was signed by player
     */
    public void onBookWritten(@Nullable String title, List<String> pages, boolean signed) {
    }

    /**
     * This method allows to get title of book
     *
     * @return Title of Book (or null)
     */
    @Nullable
    public String getBookTitle() {
        return this.title;
    }

    /**
     * This methods returns list of pages
     *
     * @return List of String
     */
    public List<String> getBookPages() {
        return this.pages;
    }

    /**
     * Adds new page
     *
     * @param page Page content
     */
    public void addPage(String page) {
        this.pages.add(page);
        if (this.open && this.autoUpdate) {
            this.open();
        }
    }
    /**
     * Adds new page
     *
     * @param pos Position
     * @param page Page content
     */
    public void addPage(int pos, String page) {
        this.pages.add(pos, page);
        if (this.open && this.autoUpdate) {
            this.open();
        }
    }

    /**
     * Sets page content
     *
     * @param pos Position
     * @param page Page content
     */
    public void setPage(int pos, String page) {
        this.setPageInternal(pos, page);
        if (this.open && this.autoUpdate) {
            this.open();
        }
    }

    protected void setPageInternal(int pos, String page) {
        if (this.pages.size() > pos) {
            this.pages.set(pos, page);
        } else {
            for (int x = this.pages.size(); x < pos - 1; x++) {
                this.pages.add("");
            }
            this.pages.add(page);
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

    @Deprecated
    @Override
    public Component getTitle() {
        return null;
    }

    @Deprecated
    @Override
    public void setTitle(Component title) {
    }

    @Deprecated
    @Override
    public MenuType<?> getType() {
        return null;
    }

    @Deprecated
    @Override
    public int getSyncId() {
        return -1;
    }
}
