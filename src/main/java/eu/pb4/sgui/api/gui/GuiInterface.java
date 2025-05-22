package eu.pb4.sgui.api.gui;

import eu.pb4.sgui.api.ScreenProperty;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"unused"})
public interface GuiInterface {

    /**
     * Sets the title of the gui.
     *
     * @param title the new title
     */
    void setTitle(Component title);

    /**
     * Returns the title of the gui.
     *
     * @return the title of the gui or <code>null</code> if not set
     */
    @Nullable
    Component getTitle();

    /**
     * Returns the {@link net.minecraft.world.inventory.MenuType} type that will be sent to the client. <br>
     * The other GUI data should match what the client would expect for this handler (slot count, ect).
     *
     * @return the screen handler type
     */
    MenuType<?> getType();

    /**
     * Returns the player this gui was constructed for.
     *
     * @return the player
     */
    ServerPlayer getPlayer();

    /**
     * Returns the sync id used for communicating information about this screen between the server and client.
     *
     * @return the sync id or <code>-1</code> if the screen has not been opened
     */
    int getSyncId();

    /**
     * Returns <code>true</code> the screen is currently open on te players screen
     *
     * @return <code>true</code> the screen is open
     */
    boolean isOpen();

    /**
     * Opens the screen for the player.
     *
     * @return <code>true</code> if the screen successfully opened
     * @see GuiInterface#onOpen()
     */
    boolean open();

    boolean getAutoUpdate();

    void setAutoUpdate(boolean value);

    /**
     * Used internally for closing the gui.
     *
     * @param alreadyClosed Is set to true, if gui's ScreenHandler is already closed
     * @see GuiInterface#onClose()
     */
    @ApiStatus.Internal
    void close(boolean alreadyClosed);

    /**
     * Closes the current gui
     *
     * @see GuiInterface#onClose()
     */
    default void close() {
        this.close(false);
    }

    /**
     * Executes when the screen is opened
     */
    default void beforeOpen() {
    }

    /**
     * Executes when the screen is opened
     */
    default void afterOpen() {
    }

    /**
     * Executes when the screen is opened
     */
    default void onOpen() {
    }

    /**
     * Executes when the screen is closed
     */
    default void onClose() {
    }

    /**
     * Executes each tick while the screen is open
     */
    default void onTick() {
    }

    default boolean canPlayerClose() {
        return true;
    }

    default void handleException(Throwable throwable) {
        throwable.printStackTrace();
    }


    /**
     * Send additional properties to the GUI.
     *
     * @param property the property to adjust
     * @param value    the value of the property to send
     * @throws IllegalArgumentException if the property is not valid for this GUI
     * @since 0.4.0
     */
    default void sendProperty(ScreenProperty property, int value) {
        if (!property.validFor(this.getType())) {
            throw new IllegalArgumentException(String.format("The property '%s' is not valid for the handler '%s'", property.name(), BuiltInRegistries.MENU.getId(this.getType())));
        }
        if (this.isOpen()) {
            this.getPlayer().connection.send(new ClientboundContainerSetDataPacket(this.getSyncId(), property.id(), value));
        }
    }

    default void sendRawProperty(int id, int value) {
        if (this.isOpen()) {
            this.getPlayer().connection.send(new ClientboundContainerSetDataPacket(this.getSyncId(), id, value));
        }
    }

    default boolean resetMousePosition() {
        return false;
    }
}
