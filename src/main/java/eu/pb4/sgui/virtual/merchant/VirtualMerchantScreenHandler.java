package eu.pb4.sgui.virtual.merchant;

import eu.pb4.sgui.api.gui.MerchantGui;
import eu.pb4.sgui.virtual.inventory.VirtualScreenHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;

public class VirtualMerchantScreenHandler extends VirtualScreenHandler {

    private final VirtualMerchant merchant;
    private final MerchantContainer merchantInventory;

    public VirtualMerchantScreenHandler(int syncId, ServerPlayer player, VirtualMerchant merchant, MerchantGui gui, MerchantContainer merchantInventory) {
        super(MenuType.MERCHANT, syncId, gui, player);
        this.merchant = merchant;
        this.merchantInventory = merchantInventory;
    }

    @Override
    public void slotsChanged(Container inventory) {
        try {
            this.merchantInventory.updateSellItem();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.slotsChanged(inventory);
    }

    @Override
    public boolean canDragTo(Slot slot) {
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack newCursorStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack clickedStack = slot.getItem();
            newCursorStack = clickedStack.copy();
            if (index == 2) {
                if (!this.moveItemStackTo(clickedStack, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(clickedStack, newCursorStack);
            } else if (index != 0 && index != 1) {
                if (index >= 3 && index < 30) {
                    if (!this.moveItemStackTo(clickedStack, 30, 39, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= 30 && index < 39 && !this.moveItemStackTo(clickedStack, 3, 30, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(clickedStack, 3, 39, false)) {
                return ItemStack.EMPTY;
            }

            if (clickedStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (clickedStack.getCount() == newCursorStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, clickedStack);
        }

        return newCursorStack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.merchant.setTradingPlayer(null);
        if (!player.level().isClientSide()) {
            if (!player.isAlive() || player instanceof ServerPlayer && ((ServerPlayer)player).hasDisconnected()) {
                ItemStack itemStack = this.merchantInventory.removeItemNoUpdate(0);
                if (!itemStack.isEmpty()) {
                    player.drop(itemStack, false);
                }

                itemStack = this.merchantInventory.removeItemNoUpdate(1);
                if (!itemStack.isEmpty()) {
                    player.drop(itemStack, false);
                }
            } else if (player instanceof ServerPlayer) {
                player.getInventory().placeItemBackInInventory(this.merchantInventory.removeItemNoUpdate(0));
                player.getInventory().placeItemBackInInventory(this.merchantInventory.removeItemNoUpdate(1));
            }

        }
    }

    public void selectNewTrade(int tradeIndex) {
        this.merchantInventory.setSelectionHint(tradeIndex);
        this.getGui().onSelectTrade(this.merchant.getOffers().get(tradeIndex));

        if (this.merchant.getOffers().size() > tradeIndex) {
            ItemStack itemStack = this.merchantInventory.getItem(0);
            if (!itemStack.isEmpty()) {
                if (!this.moveItemStackTo(itemStack, 3, 39, true)) {
                    return;
                }

                this.merchantInventory.setItem(0, itemStack);
            }

            ItemStack itemStack2 = this.merchantInventory.getItem(1);
            if (!itemStack2.isEmpty()) {
                if (!this.moveItemStackTo(itemStack2, 3, 39, true)) {
                    return;
                }

                this.merchantInventory.setItem(1, itemStack2);
            }

            if (this.merchantInventory.getItem(0).isEmpty() && this.merchantInventory.getItem(1).isEmpty()) {
                ItemStack itemStack3 = this.merchant.getOffers().get(tradeIndex).getCostA();
                this.autofill(0, itemStack3);
                ItemStack itemStack4 = this.merchant.getOffers().get(tradeIndex).getItemCostB().map(ItemCost::itemStack).orElse(ItemStack.EMPTY);
                this.autofill(1, itemStack4);
            }

        }
    }

    private void autofill(int slot, ItemStack stack) {
        if (!stack.isEmpty()) {
            for(int i = 3; i < 39; ++i) {
                ItemStack itemStack = this.slots.get(i).getItem();
                if (!itemStack.isEmpty() && ItemStack.isSameItemSameComponents(stack, itemStack)) {
                    ItemStack itemStack2 = this.merchantInventory.getItem(slot);
                    int j = itemStack2.isEmpty() ? 0 : itemStack2.getCount();
                    int k = Math.min(stack.getMaxStackSize() - j, itemStack.getCount());
                    ItemStack itemStack3 = itemStack.copy();
                    int l = j + k;
                    itemStack.shrink(k);
                    itemStack3.setCount(l);
                    this.merchantInventory.setItem(slot, itemStack3);
                    if (l >= stack.getMaxStackSize()) {
                        break;
                    }
                }
            }
        }
    }

    @Override
    public MerchantGui getGui() {
        return (MerchantGui) super.getGui();
    }
}
