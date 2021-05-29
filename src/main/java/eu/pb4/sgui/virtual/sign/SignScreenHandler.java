package eu.pb4.sgui.virtual.sign;

import eu.pb4.sgui.api.gui.SignGui;
import eu.pb4.sgui.virtual.VirtualScreenHandlerInterface;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;

public class SignScreenHandler extends ScreenHandler implements VirtualScreenHandlerInterface {

    private final SignGui gui;

    public SignScreenHandler(SignGui gui) {
        super(null, -1);
        this.gui = gui;
    }

    @Override
    public SignGui getGui() {
        return gui;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void sendContentUpdates() {
        this.gui.onTick();
        super.sendContentUpdates();
    }
}
