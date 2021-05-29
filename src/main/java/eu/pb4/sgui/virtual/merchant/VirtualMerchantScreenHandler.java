package eu.pb4.sgui.virtual.merchant;

import eu.pb4.sgui.api.gui.MerchantGui;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.MerchantScreenHandler;

public class VirtualMerchantScreenHandler extends MerchantScreenHandler {

    protected final VirtualMerchant merchant;
    protected final MerchantGui gui;

    public VirtualMerchantScreenHandler(int syncId, PlayerInventory playerInventory, VirtualMerchant merchant, MerchantGui gui) {
        super(syncId, playerInventory, merchant);
        this.merchant = merchant;
        this.gui = gui;
    }

    public VirtualMerchant getMerchant() {
        return merchant;
    }

    public MerchantGui getGui() {
        return gui;
    }
}
