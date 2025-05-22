package eu.pb4.sgui.virtual.merchant;


import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.MerchantResultSlot;
import net.minecraft.world.item.trading.MerchantOffer;

public class VirtualTradeOutputSlot extends MerchantResultSlot {

    private final MerchantContainer merchantInventory;

    public VirtualTradeOutputSlot(Player player, VirtualMerchant merchant, MerchantContainer merchantInventory, int index, int x, int y) {
        super(player, merchant, merchantInventory, index, x, y);
        this.merchantInventory = merchantInventory;
    }

    @Override
    public boolean mayPickup(Player Player) {
        MerchantOffer tradeOffer = this.merchantInventory.getActiveOffer();
        VirtualMerchantScreenHandler handler = (VirtualMerchantScreenHandler) Player.containerMenu;
        return tradeOffer != null && handler.getGui().onTrade(tradeOffer);
    }

}
