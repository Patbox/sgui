package eu.pb4.sgui;

import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

public interface GuiInterface {
    void setTitle(Text title);
    Text getTitle();
    ScreenHandlerType getType();
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
    boolean onClick(int index, ClickType type, SlotActionType action, GuiElement element);
    void onUpdate(boolean firstUpdate);
    void onClose();
}
