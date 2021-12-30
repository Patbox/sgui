package eu.pb4.sgui.api.gui;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract class BaseSlotGui implements SlotGuiInterface {
    protected final UUID uuid;
    protected final MinecraftServer server;
    protected final GuiElementInterface[] elements;
    protected final Slot[] slotRedirects;
    protected boolean open = false;
    protected boolean autoUpdate = true;
    protected boolean reOpen = false;
    protected final int size;


    public BaseSlotGui(ServerPlayerEntity player, int size) {
        this.uuid = player.getUuid();
        this.server = player.getServer();
        this.elements = new GuiElementInterface[size];
        this.slotRedirects = new Slot[size];
        this.size = size;
    }

    @Override
    public void setSlot(int index, GuiElementInterface element) {
        if (this.elements[index] != null) {
            this.elements[index].onRemoved(this);
        }
        this.elements[index] = element;
        this.slotRedirects[index] = null;
        element.onAdded(this);
    }

    @Override
    public void setSlotRedirect(int index, Slot slot) {
        if (this.elements[index] != null) {
            this.elements[index].onRemoved(this);
            this.elements[index] = null;
        }
        this.slotRedirects[index] = slot;
    }

    @Override
    public int getFirstEmptySlot() {
        for (int i = 0; i < this.elements.length; i++) {
            if (this.elements[i] == null && this.slotRedirects[i] == null) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void clearSlot(int index) {
        if (this.elements[index] != null) {
            this.elements[index].onRemoved(this);
            this.elements[index] = null;
        }
        this.slotRedirects[index] = null;
    }

    @Override
    public GuiElementInterface getSlot(int index) {
        if (index >= 0 && index < this.size) {
            return this.elements[index];
        }
        return null;
    }

    @Override
    public Slot getSlotRedirect(int index) {
        if (index >= 0 && index < this.size) {
            return this.slotRedirects[index];
        }
        return null;
    }

    @Override
    public boolean isOpen() {
        return this.open;
    }

    @Override
    public boolean getAutoUpdate() {
        return this.autoUpdate;
    }

    @Override
    public void setAutoUpdate(boolean value) {
        this.autoUpdate = value;
    }

    @Override
    public int getSize() {
        return this.size;
    }

    @Override @Nullable
    public ServerPlayerEntity getPlayer() {
        return server.getPlayerManager().getPlayer(uuid);
    }
}
