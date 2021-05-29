package eu.pb4.sgui.api.gui;

import eu.pb4.sgui.virtual.VirtualScreenHandler;
import eu.pb4.sgui.virtual.merchant.VirtualMerchant;
import eu.pb4.sgui.virtual.merchant.VirtualMerchantScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;

import java.util.OptionalInt;

public class MerchantGui extends SimpleGui {

    protected final VirtualMerchant merchant;

    public MerchantGui(ServerPlayerEntity player, boolean includePlayerInventorySlots) {
        super(ScreenHandlerType.MERCHANT, player, includePlayerInventorySlots);
        this.merchant = new VirtualMerchant(player);
    }

    public void addTrade(TradeOffer trade) {
        this.merchant.getOffers().add(trade);

        if (this.open && this.autoUpdate) {
            this.sendUpdate();
        }
    }

    public void setTrade(int index, TradeOffer trade) {
        this.merchant.getOffers().add(index, trade);

        if (this.open && this.autoUpdate) {
            this.sendUpdate();
        }
    }

    public void setIsLeveled(boolean isLeveled) {
        this.merchant.setLeveled(isLeveled);

        if (this.open && this.autoUpdate) {
            this.sendUpdate();
        }
    }

    public void setLevel(int level) {
        this.merchant.setLevel(level);

        if (this.open && this.autoUpdate) {
            this.sendUpdate();
        }
    }

    public void setExperience(int experience) {
        this.merchant.setExperienceFromServer(experience);

        if (this.open && this.autoUpdate) {
            this.sendUpdate();
        }
    }

    /**
     * Ran before the trade is completed
     *
     * @param offer the trade offer being done
     * @return if the trade should complete
     */
    public boolean onTrade(TradeOffer offer) {
        return true;
    }

    public void sendUpdate() {
        TradeOfferList tradeOfferList = this.merchant.getOffers();
        if (!tradeOfferList.isEmpty()) {
            player.sendTradeOffers(this.syncId, tradeOfferList, this.merchant.getLevel(), this.merchant.getExperience(), this.merchant.isLeveledMerchant(), this.merchant.canRefreshTrades());
        }
    }

    @Override
    protected boolean sendGui() {
        this.reOpen = true;
        OptionalInt opSyncId = player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, playerInventory, playerx) -> new VirtualMerchantScreenHandler(syncId, playerInventory, this.merchant, this), this.getTitle()));
        if (opSyncId.isPresent()) {
            this.syncId = opSyncId.getAsInt();
            this.screenHandler = (VirtualMerchantScreenHandler) this.player.currentScreenHandler;

            TradeOfferList tradeOfferList = this.merchant.getOffers();
            if (!tradeOfferList.isEmpty()) {
                player.sendTradeOffers(opSyncId.getAsInt(), tradeOfferList, this.merchant.getLevel(), this.merchant.getExperience(), this.merchant.isLeveledMerchant(), this.merchant.canRefreshTrades());
            }

            this.reOpen = false;
            return true;
        }

        this.reOpen = false;
        return false;
    }

}
