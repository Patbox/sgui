package eu.pb4.sgui.virtual.book;

import eu.pb4.sgui.api.gui.BookGui;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public record BookScreenHandlerFactory(BookGui gui) implements NamedScreenHandlerFactory {

    @Override
    public Text getDisplayName() {
        return LiteralText.EMPTY;
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new BookScreenHandler(syncId, this.gui, player);
    }
}
