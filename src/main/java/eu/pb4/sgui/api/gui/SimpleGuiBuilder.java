package eu.pb4.sgui.api.gui;

import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.SlotHolder;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilderInterface;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * Simple Gui Builder
 * <p>
 * This class allow for quick building of gui layout, that will be used for multiple users.
 */
@SuppressWarnings({"unused"})
public final class SimpleGuiBuilder implements SlotHolder {
    private final int size;
    private final int width;
    private final int height;
    private final ScreenHandlerType<?> type;
    private final GuiElementInterface[] elements;
    private final Slot[] slotRedirects;
    private final boolean includePlayer;
    private final int sizeCont;
    private boolean lockPlayerInventory = false;
    private boolean hasRedirects = false;
    private Text title = null;

    /**
     * Constructs a new simple container gui for the supplied player.
     *
     * @param type                        the screen handler that the client should display
     * @param manipulatePlayerSlots if <code>true</code> the players inventory
     *                                    will be treated as slots of this gui
     */
    public SimpleGuiBuilder(ScreenHandlerType<?> type, boolean manipulatePlayerSlots) {
        this.height = GuiHelpers.getHeight(type);
        this.width = GuiHelpers.getWidth(type);

        this.type = type;

        int tmp = manipulatePlayerSlots ? 36 : 0;
        this.size = this.width * this.height + tmp;
        this.sizeCont = this.width * this.height;
        this.elements = new GuiElementInterface[this.size];
        this.slotRedirects = new Slot[this.size];

        this.includePlayer = manipulatePlayerSlots;
    }

    /**
     * Creates {@link SimpleGui} instance based on this builder.
     *
     * @param player Player
     * @return SimpleGui instance
     */
    public SimpleGui build(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(this.type, player, this.includePlayer);
        gui.setTitle(this.title);
        gui.setLockPlayerInventory(true);

        int pos = 0;

        for (GuiElementInterface element : this.elements) {
            if (element != null) {
                gui.setSlot(pos, element);
            }
            pos++;
        }

        pos = 0;

        for (Slot slot : this.slotRedirects) {
            if (slot != null) {
                gui.setSlotRedirect(pos, slot);
            }
            pos++;
        }

        return gui;
    }


    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }

    public void setSlot(int index, GuiElementInterface element) {
        this.elements[index] = element;
    }

    public void addSlot(GuiElementInterface element) {
        this.setSlot(this.getFirstEmptySlot(), element);
    }

    public void setSlot(int index, ItemStack itemStack) {
        this.setSlot(index, new GuiElement(itemStack, GuiElementInterface.EMPTY_CALLBACK));
    }

    public void addSlot(ItemStack itemStack) {
        this.setSlot(this.getFirstEmptySlot(), itemStack);
    }

    public void setSlot(int index, GuiElementBuilderInterface<?> element) {
        this.setSlot(index, element.build());
    }

    public void addSlot(GuiElementBuilderInterface<?> element) {
        this.setSlot(this.getFirstEmptySlot(), element.build());
    }

    public void setSlot(int index, ItemStack itemStack, GuiElement.ClickCallback callback) {
        this.setSlot(index, new GuiElement(itemStack, callback));
    }

    public void setSlot(int index, ItemStack itemStack, GuiElementInterface.ItemClickCallback callback) {
        this.setSlot(index, new GuiElement(itemStack, callback));
    }

    public void addSlot(ItemStack itemStack, GuiElement.ClickCallback callback) {
        this.setSlot(this.getFirstEmptySlot(), new GuiElement(itemStack, callback));
    }

    public void addSlot(ItemStack itemStack, GuiElementInterface.ItemClickCallback callback) {
        this.setSlot(this.getFirstEmptySlot(), new GuiElement(itemStack, callback));
    }

    public void setSlotRedirect(int index, Slot slot) {
        this.elements[index] = null;
        this.slotRedirects[index] = slot;
        this.hasRedirects = true;
    }

    public void addSlotRedirect(Slot slot) {
        this.setSlotRedirect(this.getFirstEmptySlot(), slot);
    }

    public int getFirstEmptySlot() {
        for (int i = 0; i < this.elements.length; i++) {
            if (this.elements[i] == null && this.slotRedirects[i] == null) {
                return i;
            }
        }
        return -1;
    }

    public void clearSlot(int index) {
        this.elements[index] = null;
        this.slotRedirects[index] = null;
    }

    public boolean isIncludingPlayer() {
        return this.includePlayer;
    }

    public int getVirtualSize() {
        return this.sizeCont;
    }

    public GuiElementInterface getSlot(int index) {
        if (index >= 0 && index < this.size) {
            return this.elements[index];
        }
        return null;
    }

    public Slot getSlotRedirect(int index) {
        if (index >= 0 && index < this.size) {
            return this.slotRedirects[index];
        }
        return null;
    }

    public boolean isRedirectingSlots() {
        return this.hasRedirects;
    }

    public Text getTitle() {
        return this.title;
    }

    public void setTitle(Text title) {
        this.title = title;
    }

    public ScreenHandlerType<?> getType() {
        return this.type;
    }

    @Override
    public int getSize() {
        return this.size;
    }

    public boolean getLockPlayerInventory() {
        return this.lockPlayerInventory || this.includePlayer;
    }

    public void setLockPlayerInventory(boolean value) {
        this.lockPlayerInventory = value;
    }
}
