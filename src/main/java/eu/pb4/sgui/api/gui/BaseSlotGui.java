package eu.pb4.sgui.api.gui;

import eu.pb4.sgui.api.SguiUtils;
import eu.pb4.sgui.api.ScreenProperty;
import eu.pb4.sgui.api.elements.GuiElement;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;

public abstract class BaseSlotGui implements SlotBasedGui {
    protected final ServerPlayer player;
    protected final GuiElement[] elements;
    protected final Slot[] slotRedirects;
    protected boolean autoUpdate = true;
    protected boolean reOpen = false;
    protected final int size;
    protected final IntList properties = new IntArrayList();


    public BaseSlotGui(ServerPlayer player, int size) {
        this.player = player;
        this.elements = new GuiElement[size];
        this.slotRedirects = new Slot[size];
        this.size = size;
    }

    @Override
    public void setSlot(int index, GuiElement element) {
        if (this.elements[index] != null) {
            this.elements[index].onRemoved(this);
        }
        this.elements[index] = element;
        this.slotRedirects[index] = null;
        element.onAdded(this);
    }

    @Override
    public void setSlot(int index, Slot slot) {
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
    public GuiElement getSlotElement(int index) {
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
        return SguiUtils.getCurrentGui(this.player) == this;
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

    @Override
    public ServerPlayer getPlayer() {
        return this.player;
    }

    @Override
    public void sendProperty(ScreenProperty property, int value) {
        SlotBasedGui.super.sendProperty(property, value);
        while (this.properties.size() <= property.id()) {
            this.properties.add(0);
        }
        this.properties.set(property.id(), value);
    }

    @Override
    public void sendRawProperty(int id, int value) {
        SlotBasedGui.super.sendRawProperty(id, value);
        while (this.properties.size() <= id) {
            this.properties.add(0);
        }
        this.properties.set(id, value);
    }
}
