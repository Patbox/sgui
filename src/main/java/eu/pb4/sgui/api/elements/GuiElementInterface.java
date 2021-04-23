package eu.pb4.sgui.api.elements;

import eu.pb4.sgui.api.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import org.jetbrains.annotations.ApiStatus;

public interface GuiElementInterface {
    ItemStack getItemStack();

    @ApiStatus.Internal
    ItemStack getItemStackInternalUseOnly();

    GuiElementInterface.ItemClickCallback getCallback();

    @FunctionalInterface
    interface ItemClickCallback {
        void click(int index, ClickType type, SlotActionType action);
    }
}
