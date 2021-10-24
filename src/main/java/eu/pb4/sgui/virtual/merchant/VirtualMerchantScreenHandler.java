package eu.pb4.sgui.virtual.merchant;

import eu.pb4.sgui.api.gui.MerchantGui;
import eu.pb4.sgui.virtual.inventory.VirtualScreenHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.village.MerchantInventory;

public class VirtualMerchantScreenHandler extends VirtualScreenHandler {

    private final VirtualMerchant merchant;
    private final MerchantInventory merchantInventory;

    public VirtualMerchantScreenHandler(int syncId, ServerPlayerEntity player, VirtualMerchant merchant, MerchantGui gui, MerchantInventory merchantInventory) {
        super(ScreenHandlerType.MERCHANT, syncId, gui, player);
        this.merchant = merchant;
        this.merchantInventory = merchantInventory;
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        try {
            this.merchantInventory.updateOffers();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onContentChanged(inventory);
    }

    @Override
    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
        return false;
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        ItemStack newCursorStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasStack()) {
            ItemStack clickedStack = slot.getStack();
            newCursorStack = clickedStack.copy();
            if (index == 2) {
                if (!this.insertItem(clickedStack, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickTransfer(clickedStack, newCursorStack);
            } else if (index != 0 && index != 1) {
                if (index >= 3 && index < 30) {
                    if (!this.insertItem(clickedStack, 30, 39, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= 30 && index < 39 && !this.insertItem(clickedStack, 3, 30, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(clickedStack, 3, 39, false)) {
                return ItemStack.EMPTY;
            }

            if (clickedStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }

            if (clickedStack.getCount() == newCursorStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTakeItem(player, clickedStack);
        }

        return newCursorStack;
    }

    @Override
    public void close(PlayerEntity playerEntity) {
        super.close(playerEntity);
        this.merchant.setCurrentCustomer(null);
        if (!this.merchant.getMerchantWorld().isClient) {
            if (!playerEntity.isAlive() || playerEntity instanceof ServerPlayerEntity && ((ServerPlayerEntity)playerEntity).isDisconnected()) {
                ItemStack itemStack = this.merchantInventory.removeStack(0);
                if (!itemStack.isEmpty()) {
                    playerEntity.dropItem(itemStack, false);
                }

                itemStack = this.merchantInventory.removeStack(1);
                if (!itemStack.isEmpty()) {
                    playerEntity.dropItem(itemStack, false);
                }
            } else if (playerEntity instanceof ServerPlayerEntity) {
                playerEntity.getInventory().offerOrDrop(this.merchantInventory.removeStack(0));
                playerEntity.getInventory().offerOrDrop(this.merchantInventory.removeStack(1));
            }

        }
    }

    public void selectNewTrade(int tradeIndex) {
        this.merchantInventory.setOfferIndex(tradeIndex);
        this.getGui().onSelectTrade(this.merchant.getOffers().get(tradeIndex));

        if (this.merchant.getOffers().size() > tradeIndex) {
            ItemStack itemStack = this.merchantInventory.getStack(0);
            if (!itemStack.isEmpty()) {
                if (!this.insertItem(itemStack, 3, 39, true)) {
                    return;
                }

                this.merchantInventory.setStack(0, itemStack);
            }

            ItemStack itemStack2 = this.merchantInventory.getStack(1);
            if (!itemStack2.isEmpty()) {
                if (!this.insertItem(itemStack2, 3, 39, true)) {
                    return;
                }

                this.merchantInventory.setStack(1, itemStack2);
            }

            if (this.merchantInventory.getStack(0).isEmpty() && this.merchantInventory.getStack(1).isEmpty()) {
                ItemStack itemStack3 = this.merchant.getOffers().get(tradeIndex).getAdjustedFirstBuyItem();
                this.autofill(0, itemStack3);
                ItemStack itemStack4 = this.merchant.getOffers().get(tradeIndex).getSecondBuyItem();
                this.autofill(1, itemStack4);
            }

        }
    }

    private void autofill(int slot, ItemStack stack) {
        if (!stack.isEmpty()) {
            for(int i = 3; i < 39; ++i) {
                ItemStack itemStack = this.slots.get(i).getStack();
                if (!itemStack.isEmpty() && ItemStack.canCombine(stack, itemStack)) {
                    ItemStack itemStack2 = this.merchantInventory.getStack(slot);
                    int j = itemStack2.isEmpty() ? 0 : itemStack2.getCount();
                    int k = Math.min(stack.getMaxCount() - j, itemStack.getCount());
                    ItemStack itemStack3 = itemStack.copy();
                    int l = j + k;
                    itemStack.decrement(k);
                    itemStack3.setCount(l);
                    this.merchantInventory.setStack(slot, itemStack3);
                    if (l >= stack.getMaxCount()) {
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
