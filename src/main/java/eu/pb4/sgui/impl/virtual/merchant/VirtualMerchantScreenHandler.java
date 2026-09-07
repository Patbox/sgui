package eu.pb4.sgui.impl.virtual.merchant;

import eu.pb4.sgui.api.gui.MerchantGui;
import eu.pb4.sgui.api.containerwrappers.SlotBasedWrapperMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import org.jspecify.annotations.NonNull;

public class VirtualMerchantScreenHandler extends SlotBasedWrapperMenu {

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
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return false;
    }

    @Override
    public @NonNull ItemStack quickMoveStack(Player player, int index) {
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
                slot.setByPlayer(ItemStack.EMPTY);
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
    public void removed(Player playerEntity) {
        super.removed(playerEntity);
        this.merchant.setTradingPlayer(null);
        if (!playerEntity.level().isClientSide()) {
            if (!playerEntity.isAlive() || playerEntity instanceof ServerPlayer && ((ServerPlayer)playerEntity).hasDisconnected()) {
                ItemStack itemStack = this.merchantInventory.removeItemNoUpdate(0);
                if (!itemStack.isEmpty()) {
                    playerEntity.drop(itemStack, false, Prediction.SERVER_ONLY);
                }

                itemStack = this.merchantInventory.removeItemNoUpdate(1);
                if (!itemStack.isEmpty()) {
                    playerEntity.drop(itemStack, false, Prediction.SERVER_ONLY);
                }
            } else if (playerEntity instanceof ServerPlayer) {
                playerEntity.getInventory().placeItemBackInInventory(this.merchantInventory.removeItemNoUpdate(0), Prediction.SERVER_ONLY);
                playerEntity.getInventory().placeItemBackInInventory(this.merchantInventory.removeItemNoUpdate(1), Prediction.SERVER_ONLY);
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

    public MerchantGui getGui() {
        return (MerchantGui) super.getBackingGui();
    }
}
