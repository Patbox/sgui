package eu.pb4.sgui.api.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

public interface GuiInterface {
    void setTitle(Text title);
    Text getTitle();
    ScreenHandlerType<?> getType();
    boolean isOpen();
    boolean open();
    int getSize();
    void close();
    void close(boolean alreadyClosed);
    boolean getLockPlayerInventory();
    void setLockPlayerInventory(boolean value);
    boolean getAutoUpdate();
    void setAutoUpdate(boolean value);
    void onOpen();
    boolean onClick(int index, ClickType type, SlotActionType action, GuiElementInterface element);
    void onUpdate(boolean firstUpdate);
    void onClose();
}
