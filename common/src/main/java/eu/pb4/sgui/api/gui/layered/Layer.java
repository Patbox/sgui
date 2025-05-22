package eu.pb4.sgui.api.gui.layered;

import eu.pb4.sgui.api.SlotHolder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.world.inventory.Slot;

import java.util.HashSet;
import java.util.Set;

/**
 * This is implementation of layer, which can be used with {@link LayeredGui}
 */
public class Layer implements SlotHolder {
    protected final int height;
    protected final int width;
    protected final int size;
    protected final GuiElementInterface[] elements;
    protected final Slot[] slots;
    final Set<LayerView> layerViews = new HashSet<>();

    public Layer(int height, int width) {
        this.height = height;
        this.width = width;
        this.size = height * width;
        this.elements = new GuiElementInterface[this.size];
        this.slots = new Slot[this.size];
    }

    protected void markDirty() {
        for (LayerView layerView : this.layerViews) {
            layerView.redraw();
        }
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public void setSlot(int index, GuiElementInterface element) {
        this.elements[index] = element;
        this.slots[index] = null;
        this.markDirty();
    }

    @Override
    public void setSlotRedirect(int index, Slot slot) {
        this.elements[index] = null;
        this.slots[index] = slot;
        this.markDirty();
    }

    @Override
    public int getFirstEmptySlot() {
        for (int i = 0; i < this.elements.length; i++) {
            if (this.elements[i] == null && this.slots[i] == null) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void clearSlot(int index) {
        this.elements[index] = null;
        this.slots[index] = null;
        this.markDirty();
    }

    public void clearSlots() {
        for (int x = 0; x < this.elements.length; x++) {
            this.elements[x] = null;
            this.slots[x] = null;
        }
        this.markDirty();
    }

    @Override
    public int getSize() {
        return this.size;
    }

    @Override
    public GuiElementInterface getSlot(int index) {
        return this.elements[index];
    }

    @Override
    public Slot getSlotRedirect(int index) {
        return this.slots[index];
    }

    @Override
    @Deprecated
    public boolean isRedirectingSlots() {
        return false;
    }

    @Override
    @Deprecated
    public boolean isIncludingPlayer() {
        return false;
    }

    @Override
    @Deprecated
    public int getVirtualSize() {
        return this.size;
    }
}
