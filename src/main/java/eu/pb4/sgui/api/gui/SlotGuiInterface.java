package eu.pb4.sgui.api.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.SlotHolder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.virtual.inventory.VirtualScreenHandler;
import eu.pb4.sgui.virtual.inventory.VirtualSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public interface SlotGuiInterface extends SlotHolder, GuiInterface {

    /**
     * Returns the number of slots in the inventory.
     *
     * @return the inventory size
     */
    int getSize();

    boolean getLockPlayerInventory();

    void setLockPlayerInventory(boolean value);

    /**
     * Used internally to receive clicks from the client.
     *
     * @see SlotGuiInterface#onClick(int, ClickType, net.minecraft.world.inventory.ClickType, GuiElementInterface)
     * @see SlotGuiInterface#onAnyClick(int, ClickType, net.minecraft.world.inventory.ClickType)
     */
    @ApiStatus.Internal
    default boolean click(int index, ClickType type, net.minecraft.world.inventory.ClickType action) {
        GuiElementInterface element = this.getSlot(index);
        if (element != null) {
            element.getGuiCallback().click(index, type, action, this);
        }
        return this.onClick(index, type, action, element);
    }

    /**
     * Executes when player clicks any slot.
     *
     * @param index  the slot index
     * @param type   the simplified type of click
     * @param action Minecraft's Slot Action Type
     * @return <code>true</code> if to allow manipulation of redirected slots, otherwise <code>false</code>
     */
    default boolean onAnyClick(int index, ClickType type, net.minecraft.world.inventory.ClickType action) {
        return true;
    }

    /**
     * Executed when player clicks a {@link GuiElementInterface}
     *
     * @param index   slot index
     * @param type    Simplified type of click
     * @param action  Minecraft's Slot Action Type
     * @param element Clicked GuiElement
     * @return Returns false, for automatic handling and syncing or true, if you want to do it manually
     */
    default boolean onClick(int index, ClickType type, net.minecraft.world.inventory.ClickType action, GuiElementInterface element) {
        return false;
    }

    /**
     * Whether spectators can click on slots.
     *
     * @return Returns true if spectators can use this gui.
     */
    default boolean canSpectatorsClick() {
        return true;
    }

    /**
     * Maps a hotbar index into a slot index.
     *
     * @param slots The number of slots in the screen handler.
     * @param index The hotbar index, this should be [0-8]
     * @return The mapped slot index
     */
    default int getHotbarSlotIndex(int slots, int index) {
        return slots + index - 9;
    }

    /**
     * Gets the offhand slot index
     *
     * @return The offhand slot index
     */
    default int getOffhandSlotIndex() {
        return -1;
    }

    @Nullable
    default Slot getSlotRedirectOrPlayer(int index) {
        if (index < this.getSize()) {
            return this.getSlotRedirect(index);
        }

        if (this.getPlayer().containerMenu instanceof VirtualScreenHandler virt && virt.getGui() == this && index < virt.slots.size()) {
            return virt.slots.get(index);
        }
        return null;
    }

    default ItemStack quickMoveStack(int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.getSlotRedirectOrPlayer(index);
        if (slot != null && slot.hasItem() && !(slot instanceof VirtualSlot)) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            if (index < this.getVirtualSize()) {
                if (!this.insertItem(itemStack2, this.getVirtualSize(), this.getVirtualSize() + 9 * 4, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(itemStack2, 0, this.getVirtualSize(), false)) {
                return ItemStack.EMPTY;
            }
            if (itemStack2.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        } else if (slot instanceof VirtualSlot) {
            return slot.getItem();
        }

        return itemStack;
    }

    default boolean insertItem(ItemStack stack, int startIndex, int endIndex, boolean fromLast) {
        boolean modified = false;
        int i = startIndex;
        if (fromLast) {
            i = endIndex - 1;
        }

        if (stack.isStackable()) {
            while (!stack.isEmpty() && (fromLast ? i >= startIndex : i < endIndex)) {
                var slot = this.getSlotRedirectOrPlayer(i);
                if (slot != null && slot.mayPlace(stack)) {
                    var stackInSlot = slot.getItem();
                    if (!stackInSlot.isEmpty() && ItemStack.isSameItemSameComponents(stack, stackInSlot)) {
                        var totalCount = stackInSlot.getCount() + stack.getCount();
                        var maxSize = slot.getMaxStackSize(stackInSlot);
                        if (totalCount <= maxSize) {
                            stack.setCount(0);
                            stackInSlot.setCount(totalCount);
                            slot.setChanged();
                            modified = true;
                        } else if (stackInSlot.getCount() < maxSize) {
                            stack.shrink(maxSize - stackInSlot.getCount());
                            stackInSlot.setCount(maxSize);
                            slot.setChanged();
                            modified = true;
                        }
                    }
                }

                if (fromLast) {
                    i--;
                } else {
                    i++;
                }
            }
        }

        if (!stack.isEmpty()) {
            if (fromLast) {
                i = endIndex - 1;
            } else {
                i = startIndex;
            }

            while (fromLast ? i >= startIndex : i < endIndex) {
                var slot = this.getSlotRedirectOrPlayer(i);
                if (slot != null && slot.mayPlace(stack)) {
                    var stackInSlot = slot.getItem();
                    if (stackInSlot.isEmpty() && slot.mayPlace(stack)) {
                        int maxSize = slot.getMaxStackSize(stack);
                        slot.set(stack.split(Math.min(stack.getCount(), maxSize)));
                        slot.setChanged();
                        modified = true;
                        break;
                    }
                }

                if (fromLast) {
                    i--;
                } else {
                    i++;
                }
            }
        }

        return modified;
    }
}
