package eu.pb4.sgui.api.containerwrappers;

import eu.pb4.sgui.api.gui.GuiLike;
import eu.pb4.sgui.api.gui.SlotBasedGui;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;

public record GuiLikeMenuProvider<T extends GuiLike>(T gui, MenuConstructor factory) implements MenuProvider {

    @Override
    public Component getDisplayName() {
        Component text = this.gui.getTitle();
        if (text == null) {
            text = Component.empty();
        }
        return text;
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return factory.createMenu(syncId, playerInventory, player);
    }

    public static <T extends SlotBasedGui> GuiLikeMenuProvider<T> ofDefault(T gui) {
        return new GuiLikeMenuProvider<>(gui, ((syncId, inv, player) -> new SlotBasedWrapperMenu(gui.getType(), syncId, gui, player)));
    }
}
