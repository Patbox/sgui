package eu.pb4.sgui.api.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.ScreenProperty;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.network.packet.s2c.play.ScreenHandlerPropertyUpdateS2CPacket;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.registry.Registry;

public interface GuiInterface {
    void setTitle(Text title);
    Text getTitle();
    ScreenHandlerType<?> getType();
    ServerPlayerEntity getPlayer();
    int getSyncId();
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
    void onTick();

    /**
     * Send additional properties to the Gui.
     *
     * @param property the property to adjust
     * @param value    the value of the property to send
     * @see ScreenProperty
     */
    default void sendProperty(ScreenProperty property, int value) {
        if (!property.validFor(this.getType())) {
            throw new IllegalStateException(String.format("The property '%s' is not valid for the handler '%s'", property.name(), Registry.SCREEN_HANDLER.getId(this.getType())));
        }
        if (this.isOpen()) {
            this.getPlayer().networkHandler.sendPacket(new ScreenHandlerPropertyUpdateS2CPacket(this.getSyncId(), property.id(), value));
        }
    }
}
