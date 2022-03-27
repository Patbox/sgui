package eu.pb4.sgui.virtual.merchant;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.village.SimpleMerchant;

public class VirtualMerchant extends SimpleMerchant {

    private boolean isLeveled = false;
    private int level = 1;

    public VirtualMerchant(PlayerEntity playerEntity) {
        super(playerEntity);
    }

    public void setLeveled(boolean leveled) {
        this.isLeveled = leveled;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return this.level;
    }

    @Override
    public void onSellingItem(ItemStack stack) {
        ServerPlayerEntity player = (ServerPlayerEntity) this.getCustomer();
        assert player != null;
        if (player.currentScreenHandler instanceof VirtualMerchantScreenHandler current) {
            current.getGui().getSelectedTrade();
        }
        super.onSellingItem(stack);
    }

    @Override
    public boolean isLeveledMerchant() {
        return isLeveled;
    }

    @Override
    public void sendOffers(PlayerEntity player, Text test, int levelProgress) {
        if (player.currentScreenHandler instanceof VirtualMerchantScreenHandler) {
            ((VirtualMerchantScreenHandler) player.currentScreenHandler).getGui().sendUpdate();
        }
    }
}
