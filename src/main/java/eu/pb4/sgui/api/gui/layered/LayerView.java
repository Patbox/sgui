package eu.pb4.sgui.api.gui.layered;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.Slot;

/**
 * This class is a view of Layer object of {@link Layer}
 */
@SuppressWarnings({"unused"})
public final class LayerView {
    private final Layer layer;
    private final LayeredGui gui;
    GuiElementInterface[] elements;
    Slot[] slots;
    private int height;
    private int width;
    int zIndex;
    private int x;
    private int y;
    private boolean removed = false;
    private final int offsetX = 0;
    private final int OffsetZ = 0;

    LayerView(int x, int y, Layer layer, LayeredGui gui) {
        this.x = x;
        this.y = y;
        this.layer = layer;
        this.height = this.layer.height;
        this.width = this.layer.width;
        layer.layerViews.add(this);
        this.gui = gui;
        this.elements = new GuiElementInterface[this.gui.size];
        this.slots = new Slot[this.gui.size];
        this.redraw();
    }

    void remove() {
        if (!this.removed) {
            layer.layerViews.remove(this);
            this.removed = true;
        }
    }

    void redraw() {
        if (!this.removed) {
            for (int i = 0; i < this.gui.size; i++) {
                int x = i % this.gui.width - this.x;
                int y = i / this.gui.width - this.y;

                if (x < 0 || x >= this.width || y < 0 || y >= this.height) {
                    this.elements[i] = null;
                    this.slots[i] = null;
                    continue;
                }

                int iL = x + y * this.layer.width;

                this.elements[i] = this.layer.elements[iL];
                this.slots[i] = this.layer.slots[iL];
            }

            this.gui.markDirty();
        }
    }

    public void setZIndex(int value) {
        this.zIndex = value;
        this.gui.markDirty();
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
        this.redraw();
    }

    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
        this.redraw();
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
        this.redraw();
    }

    public void setSize(int height, int width) {
        this.height = Mth.clamp(height, 0, this.layer.height);
        this.width = Mth.clamp(width, 0, this.layer.width);
        this.redraw();
    }

    public int getHeight() {
        return this.height;
    }

    public void setHeight(int height) {
        this.height = Mth.clamp(height, 0, this.layer.height);
        this.redraw();
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(int width) {
        this.width = Mth.clamp(width, 0, this.layer.width);
        this.redraw();
    }

    public Layer getLayer() {
        return this.layer;
    }
}
