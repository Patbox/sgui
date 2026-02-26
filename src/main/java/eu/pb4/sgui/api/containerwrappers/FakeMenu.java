package eu.pb4.sgui.api.containerwrappers;

import eu.pb4.sgui.api.gui.GuiLike;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Some guis don't use screen handlers (Sign or book input)
 * This is mostly utility class to simplify implementation
 */
public class FakeMenu extends AbstractWrapperMenu {
    public FakeMenu(GuiLike gui) {
        super(null, -1, gui);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
