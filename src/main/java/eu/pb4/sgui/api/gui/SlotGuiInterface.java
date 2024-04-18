package eu.pb4.sgui.api.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.SlotHolder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.virtual.inventory.VirtualScreenHandler;
import eu.pb4.sgui.virtual.inventory.VirtualSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
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
     * @see SlotGuiInterface#onClick(int, ClickType, SlotActionType, GuiElementInterface)
     * @see SlotGuiInterface#onAnyClick(int, ClickType, SlotActionType)
     */
    @ApiStatus.Internal
    default boolean click(int index, ClickType type, SlotActionType action) {
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
    default boolean onAnyClick(int index, ClickType type, SlotActionType action) {
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
    default boolean onClick(int index, ClickType type, SlotActionType action, GuiElementInterface element) {
        return false;
    }

    @Nullable
    default Slot getSlotRedirectOrPlayer(int index) {
        if (index < this.getSize()) {
            return this.getSlotRedirect(index);
        }

        if (this.getPlayer().currentScreenHandler instanceof VirtualScreenHandler virt && virt.getGui() == this && index < virt.slots.size()) {
            return virt.slots.get(index);
        }
        return null;
    }

    default ItemStack quickMove(int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.getSlotRedirectOrPlayer(index);
        if (slot != null && slot.hasStack() && !(slot instanceof VirtualSlot)) {
            ItemStack itemStack2 = slot.getStack();
            itemStack = itemStack2.copy();
            if (index < this.getVirtualSize()) {
                if (!this.insertItem(itemStack2, this.getVirtualSize(), this.getVirtualSize() + 9 * 4, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(itemStack2, 0, this.getVirtualSize(), false)) {
                return ItemStack.EMPTY;
            }
            if (itemStack2.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        } else if (slot instanceof VirtualSlot) {
            return slot.getStack();
        }

        return itemStack;
    }

    default boolean insertItem(ItemStack stack, int startIndex, int endIndex, boolean fromLast) {
        boolean bl = false;
        int i = startIndex;
        if (fromLast) {
            i = endIndex - 1;
        }

        Slot slot;
        ItemStack itemStack;
        if (stack.isStackable()) {
            while(!stack.isEmpty()) {
                if (fromLast) {
                    if (i < startIndex) {
                        break;
                    }
                } else if (i >= endIndex) {
                    break;
                }

                slot = this.getSlotRedirectOrPlayer(i);
                if (slot != null && slot.canInsert(stack)) {
                    itemStack = slot.getStack();
                    if (!itemStack.isEmpty() && ItemStack.areItemsAndComponentsEqual(stack, itemStack)) {
                        int j = itemStack.getCount() + stack.getCount();
                        if (j <= stack.getMaxCount()) {
                            stack.setCount(0);
                            itemStack.setCount(j);
                            slot.markDirty();
                            bl = true;
                        } else if (itemStack.getCount() < stack.getMaxCount()) {
                            stack.decrement(stack.getMaxCount() - itemStack.getCount());
                            itemStack.setCount(stack.getMaxCount());
                            slot.markDirty();
                            bl = true;
                        }
                    }
                }

                if (fromLast) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        if (!stack.isEmpty()) {
            if (fromLast) {
                i = endIndex - 1;
            } else {
                i = startIndex;
            }

            while(true) {
                if (fromLast) {
                    if (i < startIndex) {
                        break;
                    }
                } else if (i >= endIndex) {
                    break;
                }

                slot = this.getSlotRedirectOrPlayer(i);
                if (slot != null) {
                    itemStack = slot.getStack();
                    if (itemStack.isEmpty() && slot.canInsert(stack)) {
                        if (stack.getCount() > slot.getMaxItemCount()) {
                            slot.setStack(stack.split(slot.getMaxItemCount()));
                        } else {
                            slot.setStack(stack.split(stack.getCount()));
                        }

                        slot.markDirty();
                        bl = true;
                        break;
                    }
                }

                if (fromLast) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        return bl;
    };
}
