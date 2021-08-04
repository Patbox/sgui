package eu.pb4.sgui.api.gui.layered;

import com.google.common.collect.ImmutableList;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Layered Gui Implementation
 * <p>
 * This is wrapper around SimpleGui designed to simplify multi-layered/dynamic uis
 */
@SuppressWarnings({"unused"})
public class LayeredGui implements SlotGuiInterface {
    protected final int size;
    protected final int width;
    protected final int height;
    protected final BackendSimpleGui gui;
    protected final Layer backgroundLayer;
    protected final List<LayerView> layers;
    private boolean isDirty = false;

    /**
     * Constructs a new simple container gui for the supplied player.
     *
     * @param type                        the screen handler that the client should display
     * @param player                      the player to server this gui to
     * @param includePlayerInventorySlots if <code>true</code> the players inventory
     *                                    will be treated as slots of this gui
     */
    public LayeredGui(ScreenHandlerType<?> type, ServerPlayerEntity player, boolean includePlayerInventorySlots) {
        int width = GuiHelpers.getWidth(type);
        if (width != 9) {
            type = ScreenHandlerType.GENERIC_9X3;
        }

        this.height = GuiHelpers.getHeight(type) + (includePlayerInventorySlots ? 4 : 0);
        this.width = 9;

        this.gui = new BackendSimpleGui(type, player, includePlayerInventorySlots, this);
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
        SlotGuiInterface.super.onTick();

        if (this.isDirty) {
            this.draw();
        }
    }


    protected void draw() {
        this.isDirty = false;
        this.layers.sort(Comparator.comparingInt(a -> a.zIndex));

        for (int i = 0; i < this.size; i++) {
            GuiElementInterface element = this.backgroundLayer.elements[i];
            Slot slot = this.backgroundLayer.slots[i];

            for (LayerView view : this.layers) {
                GuiElementInterface viewElement = view.elements[i];
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
            } else if (this.gui.getSlot(i) != element && element != null) {
                this.gui.setSlot(i, element);
            } else if (this.gui.getSlotRedirect(i) != slot && slot != null) {
                this.gui.setSlotRedirect(i, slot);
            }
        }
    }



    /**
     * Returns the number of vertical slots in this gui.
     *
     * @return the height of this gui
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * Returns the number of horizontal slots in this gui.
     *
     * @return the width of this gui
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * Sets background slot with selected GuiElement.
     *
     * @param index   the slots index, from 0 to (max size - 1)
     * @param element any GuiElement
     * @throws IndexOutOfBoundsException if the slot is out of bounds
     * @see LayeredGui#addSlot(GuiElementInterface)
     */
    public void setSlot(int index, GuiElementInterface element) {
        this.backgroundLayer.setSlot(index, element);
    }

    /**
     * Allows to add own Slot instances, that can point to any inventory.
     * Do not add duplicates (including player inventory) as it can cause item duplication!
     *
     * @param index the slot index (in this gui)
     * @param slot  the slot to redirect to
     * @see LayeredGui#addSlotRedirect(Slot)
     */
    public void setSlotRedirect(int index, Slot slot) {
        this.backgroundLayer.setSlotRedirect(index, slot);
    }

    /**
     * Returns the first empty background slot inside the inventory.
     *
     * @return the index of the first empty slot or <code>-1</code> if full
     */
    public int getFirstEmptySlot() {
        return this.backgroundLayer.getFirstEmptySlot();
    }

    /**
     * Reverts background slot to it's original state.
     *
     * @param index slot index
     */
    public void clearSlot(int index) {
        this.backgroundLayer.clearSlot(index);
    }

    /**
     * Returns if the gui includes the player inventory slots.
     *
     * @return <code>true</code> if the player inventory slots.
     */
    public boolean isIncludingPlayer() {
        return this.gui.isIncludingPlayer();
    }

    /**
     * Returns the number of slots in the virtual inventory only.
     * Works the same as {@link LayeredGui#getSize()}, however excludes player gui slots if <code>includePlayer</code> is <code>true</code>.
     *
     * @return the size of the virtual inventory
     * @see LayeredGui#getSize()
     */
    public int getVirtualSize() {
        return this.gui.getVirtualSize();
    }

    /**
     * Returns the element in the referenced background slot.
     *
     * @param index the slot index
     * @return the element or <code>null</code> if empty
     * @see LayeredGui#getSlotRedirect(int)
     */
    public GuiElementInterface getSlot(int index) {
        return this.backgroundLayer.getSlot(index);
    }

    /**
     * Returns the external background slot the referenced slot is redirecting to.
     *
     * @param index the slot index
     * @return the element or <code>null</code> if no redirect
     * @see LayeredGui#getSlot(int)
     */
    public Slot getSlotRedirect(int index) {
        return this.backgroundLayer.getSlotRedirect(index);
    }

    /**
     * Returns if this gui has slot redirects.
     *
     * @return <code>true</code> if this gui has slot redirects
     * @see LayeredGui#getSlotRedirect(int)
     */
    public boolean isRedirectingSlots() {
        return this.gui.isRedirectingSlots();
    }

    /**
     * Executes when player clicks any slot.
     *
     * @param index  the slot index
     * @param type   the simplified type of click
     * @param action Minecraft's Slot Action Type
     * @return <code>true</code> if to allow manipulation of redirected slots, otherwise <code>false</code>
     */
    public boolean onAnyClick(int index, ClickType type, SlotActionType action) {
        return true;
    }

    @Deprecated
    @ApiStatus.Internal
    public boolean click(int index, ClickType type, SlotActionType action) {
        return false;
    }

    @Override
    public Text getTitle() {
        return this.gui.getTitle();
    }

    @Override
    public void setTitle(Text title) {
        this.gui.setTitle(title);
    }

    @Override
    public ScreenHandlerType<?> getType() {
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
    public void close(boolean screenHandlerIsClosed) {
        this.gui.close(screenHandlerIsClosed);
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
    public ServerPlayerEntity getPlayer() {
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
