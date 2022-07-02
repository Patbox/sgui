package eu.pb4.sgui.virtual;

import eu.pb4.sgui.api.gui.GuiInterface;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import eu.pb4.sgui.virtual.inventory.VirtualScreenHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerFactory;
import net.minecraft.text.Text;

public record SguiScreenHandlerFactory<T extends GuiInterface>(T gui, ScreenHandlerFactory factory) implements NamedScreenHandlerFactory {

    @Override
    public Text getDisplayName() {
        Text text = this.gui.getTitle();
        if (text == null) {
            text = Text.empty();
        }
        return text;
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return factory.createMenu(syncId, playerInventory, player);
    }

    public static <T extends SlotGuiInterface> SguiScreenHandlerFactory<T> ofDefault(T gui) {
        return new SguiScreenHandlerFactory<>(gui, ((syncId, inv, player) -> new VirtualScreenHandler(gui.getType(), syncId, gui, player)));
    }
}
