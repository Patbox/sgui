package eu.pb4.sgui.virtual.merchant;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.village.SimpleMerchant;
import net.minecraft.village.TradeOffer;

public class VirtualMerchant extends SimpleMerchant {

    private boolean isLeveled = false;
    private int level = 1;

    public VirtualMerchant(PlayerEntity playerEntity) {
        super(playerEntity);
    }

    public void setLeveled(boolean leveled) {
        isLeveled = leveled;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return this.level;
    }

    @Override
    public boolean isLeveledMerchant() {
        return isLeveled;
    }

    @Override
    public void trade(TradeOffer offer) {
        if (this.getCurrentCustomer() != null && this.getCurrentCustomer().currentScreenHandler instanceof VirtualMerchantScreenHandler) {
            if (!((VirtualMerchantScreenHandler) this.getCurrentCustomer().currentScreenHandler).getGui().onTrade(offer)) {
                return;
            }
        }
        super.trade(offer);
    }

    @Deprecated
    @Override
    public void sendOffers(PlayerEntity player, Text test, int levelProgress) {
        throw new UnsupportedOperationException("Use the other constructor");
    }
}
