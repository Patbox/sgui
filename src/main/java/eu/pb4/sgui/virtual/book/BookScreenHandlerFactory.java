package eu.pb4.sgui.virtual.book;

import eu.pb4.sgui.api.gui.BookGui;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public final class BookScreenHandlerFactory implements NamedScreenHandlerFactory {
    private final BookGui gui;

    public BookScreenHandlerFactory(BookGui gui) {
        this.gui = gui;
    }

    @Override
    public Text getDisplayName() {
        return new LiteralText("");
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new BookScreenHandler(syncId, this.gui, player);
    }
}
