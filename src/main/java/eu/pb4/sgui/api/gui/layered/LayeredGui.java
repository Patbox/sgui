package eu.pb4.sgui.api.gui.layered;

import com.google.common.collect.ImmutableList;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.SguiUtils;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.gui.SlotBasedGui;
import net.minecraft.world.inventory.ContainerInput;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;

/**
 * Layered Gui Implementation
 * <p>
 * This is wrapper around SimpleGui designed to simplify multi-layered/dynamic uis
 */
@SuppressWarnings({"unused"})
public class LayeredGui implements SlotBasedGui {
    protected final int size;
    protected final int width;
    protected final int height;
    protected final BackendSimpleGui gui;
    protected final Layer backgroundLayer;
    protected final List<LayerView> layers;
    private boolean isDirty = false;

    /**
     * Constructs a new layered container gui for the supplied player.
     *
     * @param type                  the screen handler that the client should display
     * @param player                the player to server this gui to
     * @param manipulatePlayerSlots if <code>true</code> the players inventory
     *                              will be treated as slots of this gui
     */
    public LayeredGui(MenuType<?> type, ServerPlayer player, boolean manipulatePlayerSlots) {
        int width = SguiUtils.getWidth(type);
        if (width != 9) {
            type = MenuType.GENERIC_9x3;
        }

        this.height = SguiUtils.getHeight(type) + (manipulatePlayerSlots ? 4 : 0);
        this.width = 9;

        this.gui = new BackendSimpleGui(type, player, manipulatePlayerSlots, this);
        this.size = this.width * this.height;
        this.backgroundLayer = new Layer(this.height, this.width);
        this.layers = new ArrayList<>();
    }

    public LayerView addLayer(Layer layer, int x, int y) {
        LayerView view = new LayerView(x, y, layer, this);
        this.layers.add(view);
        return view;
    }

    public void removeLayer(LayerView view) {
        this.layers.remove(view);
        view.remove();
        this.draw();
    }

    public ImmutableList<LayerView> getLayers() {
        return ImmutableList.copyOf(this.layers);
    }

    @Override
    public void onTick() {
        SlotBasedGui.super.onTick();

        if (this.isDirty) {
            this.draw();
        }
    }


    protected void draw() {
        this.isDirty = false;
        this.layers.sort(Comparator.comparingInt(a -> a.zIndex));

        for (int i = 0; i < this.size; i++) {
            GuiElement element = this.backgroundLayer.elements[i];
            Slot slot = this.backgroundLayer.slots[i];

            for (LayerView view : this.layers) {
                GuiElement viewElement = view.elements[i];
                Slot viewSlot = view.slots[i];

                if (viewElement != null) {
                    element = viewElement;
                    slot = null;
                } else if (viewSlot != null) {
                    element = null;
                    slot = viewSlot;
                }
            }

            if (slot == null && element == null) {
                this.gui.clearSlot(i);
            } else if (this.gui.getGuiElement(i) != element && element != null) {
                this.gui.setSlot(i, element);
            } else if (this.gui.getCustomSlot(i) != slot && slot != null) {
                this.gui.setSlot(i, slot);
            }
        }
    }


    public int getHeight() {
        return this.height;
    }


    public int getWidth() {
        return this.width;
    }


    public void setSlot(int index, GuiElement element) {
        this.backgroundLayer.setSlot(index, element);
    }


    public void setSlot(int index, Slot slot) {
        this.backgroundLayer.setSlot(index, slot);
    }


    public int getFirstEmptySlot() {
        return this.backgroundLayer.getFirstEmptySlot();
    }


    public void clearSlot(int index) {
        this.backgroundLayer.clearSlot(index);
    }


    public boolean isIncludingPlayer() {
        return this.gui.isIncludingPlayer();
    }


    public int getVirtualSize() {
        return this.gui.getVirtualSize();
    }


    public GuiElement getGuiElement(int index) {
        return this.backgroundLayer.getGuiElement(index);
    }


    public Slot getCustomSlot(int index) {
        return this.backgroundLayer.getCustomSlot(index);
    }


    public boolean isRedirectingSlots() {
        return this.gui.isRedirectingSlots();
    }


    public boolean onAnyClick(int index, ClickType type, ContainerInput action) {
        return true;
    }

    @Deprecated
    @ApiStatus.Internal
    public boolean click(int index, ClickType type, ContainerInput action) {
        return false;
    }

    @Override
    public Component getTitle() {
        return this.gui.getTitle();
    }

    @Override
    public void setTitle(Component title) {
        this.gui.setTitle(title);
    }

    @Override
    public MenuType<?> getType() {
        return this.gui.getType();
    }

    @Override
    public boolean isOpen() {
        return this.gui.isOpen();
    }

    @Override
    public boolean open() {
        if (this.isDirty) {
            this.draw();
        }
        return this.gui.open();
    }

    @Override
    public int getSize() {
        return this.size;
    }

    @Override
    public void close(boolean skipSync) {
        this.gui.close(skipSync);
    }

    @Override
    public boolean getLockPlayerInventory() {
        return this.gui.getLockPlayerInventory();
    }

    @Override
    public void setLockPlayerInventory(boolean value) {
        this.gui.setLockPlayerInventory(value);
    }

    @Override
    public boolean getAutoUpdate() {
        return this.gui.getAutoUpdate();
    }

    @Override
    public void setAutoUpdate(boolean value) {
        this.gui.setAutoUpdate(value);
    }

    @Override
    public ServerPlayer getPlayer() {
        return this.gui.getPlayer();
    }

    @Override
    public int getSyncId() {
        return this.gui.getSyncId();
    }

    public void markDirty() {
        this.isDirty = true;
    }
}
