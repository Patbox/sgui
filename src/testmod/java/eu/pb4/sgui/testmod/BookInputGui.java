package eu.pb4.sgui.testmod;

import eu.pb4.sgui.api.gui.GuiInterface;
import eu.pb4.sgui.virtual.FakeScreenHandler;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.WritableBookItem;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.packet.s2c.play.OpenWrittenBookS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
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
    protected final ServerPlayerEntity player;
    protected boolean open = false;
    protected boolean reOpen = false;
    protected FakeScreenHandler screenHandler = null;

    protected int syncId = -1;
    private boolean autoUpdate;
    private String title;
    private List<String> pages;

    /**
     * Constructs a new book input gui for the supplied player.
     *
     * @param player the player to server this gui to
     */
    public BookInputGui(ServerPlayerEntity player) {
        this(player, null);
    }

    /**
     * Constructs a new book input gui for the supplied player based on provided book.
     *
     * @param player the player to server this gui to
     * @param book   book to base on
     */
    public BookInputGui(ServerPlayerEntity player, ItemStack book) {
        this.player = player;
        this.pages = new ArrayList<>();
        if (book != null && book.getItem() instanceof WritableBookItem) {
            if (book.hasTag() && book.getTag().contains("pages", NbtElement.LIST_TYPE)) {
                NbtList nbtList = book.getTag().getList("pages", NbtElement.STRING_TYPE);

                for (int i = 0; i < nbtList.size(); ++i) {
                    String string = nbtList.getString(i);
                    this.pages.add(string);
                }
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

        if (this.player.currentScreenHandler != this.player.playerScreenHandler && this.player.currentScreenHandler != this.screenHandler) {
            this.player.closeHandledScreen();
        }
        if (screenHandler == null) {
            this.screenHandler = new FakeScreenHandler(this);
        }
        this.player.currentScreenHandler = this.screenHandler;

        ItemStack stack = Items.WRITABLE_BOOK.getDefaultStack();
        NbtList list = new NbtList();
        for (String string : this.pages) {
            list.add(NbtString.of(string));
        }
        stack.getOrCreateTag().put("pages", list);

        this.player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, 0, PlayerInventory.OFF_HAND_SLOT, stack));
        this.player.networkHandler.sendPacket(new OpenWrittenBookS2CPacket(Hand.OFF_HAND));

        this.reOpen = false;
        this.open = true;

        return true;
    }

    @Override
    public ServerPlayerEntity getPlayer() {
        return this.player;
    }

    @Override
    public void close(boolean alreadyClosed) {
        if (this.open && !this.reOpen) {
            this.open = false;
            this.reOpen = false;

            this.player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, 0, PlayerInventory.OFF_HAND_SLOT, this.player.getOffHandStack()));

            if (alreadyClosed && this.player.currentScreenHandler == this.screenHandler) {
                this.player.closeScreenHandler();
            } else {
                this.player.closeHandledScreen();
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
    public Text getTitle() {
        return null;
    }

    @Deprecated
    @Override
    public void setTitle(Text title) {
    }

    @Deprecated
    @Override
    public ScreenHandlerType<?> getType() {
        return null;
    }

    @Deprecated
    @Override
    public int getSyncId() {
        return -1;
    }
}
