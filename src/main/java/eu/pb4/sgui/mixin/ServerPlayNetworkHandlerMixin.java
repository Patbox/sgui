package eu.pb4.sgui.mixin;

import eu.pb4.sgui.ClickType;
import eu.pb4.sgui.virtual.VirtualScreenHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.ConfirmScreenActionS2CPacket;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Shadow
    public abstract void sendPacket(Packet<?> packet);

    @Inject(method = "onClickSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;updateLastActionTime()V", shift = At.Shift.AFTER), cancellable = true)
    private void handleGuiClicks(ClickSlotC2SPacket packet, CallbackInfo ci) {
        if (this.player.currentScreenHandler instanceof VirtualScreenHandler) {
            try {
                VirtualScreenHandler handler = (VirtualScreenHandler) this.player.currentScreenHandler;

                int slot = packet.getSlot();
                int button = packet.getClickData();
                ClickType type = ClickType.toClickType(packet.getActionType(), button, slot);

                if (!handler.getGui().getLockPlayerInventory() && (slot >= handler.getGui().getSize() || slot < 0) && !type.shift && !type.isDragging) {
                    return;
                }

                boolean allow = handler.getGui().click(slot, type, packet.getActionType());

                if (!allow) {
                    if (slot >= 0 && slot < handler.getGui().getSize()) {
                        this.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(handler.syncId, slot, handler.getSlot(slot).getStack()));
                    }
                    this.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-1, -1, this.player.inventory.getCursorStack()));

                    if (type.numKey) {
                        this.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(handler.syncId, slot, handler.getSlot(type.value + handler.getGui().getSize() - 10).getStack()));
                    } else if (type == ClickType.MOUSE_DOUBLE_CLICK || type == ClickType.MOUSE_LEFT_SHIFT || type == ClickType.MOUSE_RIGHT_SHIFT || (type.isDragging && type.value == 2)) {
                        this.sendPacket(new InventoryS2CPacket(handler.syncId, handler.getStacks()));
                    }

                    this.sendPacket(new ConfirmScreenActionS2CPacket(handler.syncId, packet.getActionId(), false));
                } else {
                    this.sendPacket(new ConfirmScreenActionS2CPacket(handler.syncId, packet.getActionId(), true));
                }

            } catch (Exception e) {
                e.printStackTrace();
                ci.cancel();
            }

            ci.cancel();
        }

    }
}
