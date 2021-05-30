package eu.pb4.sgui.virtual.merchant;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.TradeOutputSlot;
import net.minecraft.village.MerchantInventory;
import net.minecraft.village.TradeOffer;

public class VirtualTradeOutputSlot extends TradeOutputSlot {

    private final MerchantInventory merchantInventory;

    public VirtualTradeOutputSlot(PlayerEntity player, VirtualMerchant merchant, MerchantInventory merchantInventory, int index, int x, int y) {
        super(player, merchant, merchantInventory, index, x, y);
        this.merchantInventory = merchantInventory;
    }

    @Override
    public boolean canTakeItems(PlayerEntity playerEntity) {
        TradeOffer tradeOffer = this.merchantInventory.getTradeOffer();
        VirtualMerchantScreenHandler handler = (VirtualMerchantScreenHandler) playerEntity.currentScreenHandler;
        return tradeOffer != null && handler.getGui().onTrade(tradeOffer);
    }

}
