package eu.pb4.sgui.virtual;

import eu.pb4.sgui.SimpleGui;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public final class VirtualScreenHandlerFactory implements NamedScreenHandlerFactory {
    private final SimpleGui gui;

    public VirtualScreenHandlerFactory(SimpleGui gui) {
        this.gui = gui;
    }

    @Override
    public Text getDisplayName() {
        Text text = this.gui.getTitle();
        if (text == null) {
            text = new LiteralText("");
        }
        return text;
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new VirtualScreenHandler(this.gui.getType(), syncId, this.gui, player);
    }
}
