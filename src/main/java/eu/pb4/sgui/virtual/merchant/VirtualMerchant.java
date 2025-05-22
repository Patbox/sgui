package eu.pb4.sgui.virtual.merchant;


import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.ClientSideMerchant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class VirtualMerchant extends ClientSideMerchant {

    private boolean isLeveled = false;
    private int level = 1;

    public VirtualMerchant(Player Player) {
        super(Player);
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
    public void notifyTradeUpdated(ItemStack stack) {
        ServerPlayer player = (ServerPlayer) this.getTradingPlayer();
        assert player != null;
        if (player.containerMenu instanceof VirtualMerchantScreenHandler current) {
            current.getGui().getSelectedTrade();
        }
        super.notifyTradeUpdated(stack);
    }

    @Override
    public boolean showProgressBar() {
        return isLeveled;
    }

    @Override
    public void openTradingScreen(Player player, Component test, int levelProgress) {
        if (player.containerMenu instanceof VirtualMerchantScreenHandler) {
            ((VirtualMerchantScreenHandler) player.containerMenu).getGui().sendUpdate();
        }
    }
}
