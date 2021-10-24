package eu.pb4.sgui.virtual.inventory;

import eu.pb4.sgui.api.gui.SlotGuiInterface;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public record VirtualScreenHandlerFactory(SlotGuiInterface gui) implements NamedScreenHandlerFactory {

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
