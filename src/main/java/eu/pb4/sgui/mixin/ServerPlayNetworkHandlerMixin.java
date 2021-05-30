package eu.pb4.sgui.mixin;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import eu.pb4.sgui.api.gui.SignGui;
import eu.pb4.sgui.virtual.VirtualScreenHandlerInterface;
import eu.pb4.sgui.virtual.book.BookScreenHandler;
import eu.pb4.sgui.virtual.inventory.VirtualScreenHandler;
import eu.pb4.sgui.virtual.sign.SignScreenHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.filter.TextStream;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {

    private ScreenHandler previousScreen = null;

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
                int button = packet.getButton();
                ClickType type = ClickType.toClickType(packet.getActionType(), button, slot);
                boolean ignore = handler.getGui().onAnyClick(slot, type, packet.getActionType());
                if (ignore && !handler.getGui().getLockPlayerInventory() && (slot >= handler.getGui().getSize() || slot < 0 || handler.getGui().getSlotRedirect(slot) != null)) {
                    if (type == ClickType.MOUSE_DOUBLE_CLICK || (type.isDragging && type.value == 2)) {
                        this.sendPacket(new InventoryS2CPacket(handler.syncId, handler.getStacks()));
                        this.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-1, -1, this.player.currentScreenHandler.getCursorStack()));
                    }

                    return;
                }

                boolean allow = handler.getGui().click(slot, type, packet.getActionType());
                if (!allow) {
                    if (slot >= 0 && slot < handler.getGui().getSize()) {
                        this.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(handler.syncId, slot, handler.getSlot(slot).getStack()));
                    }
                    this.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-1, -1, this.player.currentScreenHandler.getCursorStack()));

                    if (type.numKey) {
                        this.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(handler.syncId, type.value + handler.slots.size() - 10, handler.getSlot(type.value + handler.slots.size() - 10).getStack()));
                    } else if (type == ClickType.MOUSE_DOUBLE_CLICK || type == ClickType.MOUSE_LEFT_SHIFT || type == ClickType.MOUSE_RIGHT_SHIFT || (type.isDragging && type.value == 2)) {
                        this.sendPacket(new InventoryS2CPacket(handler.syncId, handler.getStacks()));
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                ci.cancel();
            }

            ci.cancel();
        } else if (this.player.currentScreenHandler instanceof BookScreenHandler) {
            ci.cancel();
        }
    }

    @Inject(method = "onClickSlot", at = @At("TAIL"))
    private void resyncGui(ClickSlotC2SPacket packet, CallbackInfo ci) {
        if (this.player.currentScreenHandler instanceof VirtualScreenHandler) {
            try {
                VirtualScreenHandler handler = (VirtualScreenHandler) this.player.currentScreenHandler;

                int slot = packet.getSlot();
                int button = packet.getButton();
                ClickType type = ClickType.toClickType(packet.getActionType(), button, slot);

                if (type == ClickType.MOUSE_DOUBLE_CLICK || (type.isDragging && type.value == 2) || type.shift) {
                    this.sendPacket(new InventoryS2CPacket(handler.syncId, handler.getStacks()));
                    //this.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-1, -1, this.player.inventory.getCursorStack()));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Inject(method = "onCloseHandledScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;closeScreenHandler()V", shift = At.Shift.BEFORE))
    private void storeScreenHandler(CloseHandledScreenC2SPacket packet, CallbackInfo info) {
        if (this.player.currentScreenHandler instanceof VirtualScreenHandlerInterface) {
            this.previousScreen = this.player.currentScreenHandler;
        }
    }

    @Inject(method = "onCloseHandledScreen", at = @At("TAIL"))
    private void executeClosing(CloseHandledScreenC2SPacket packet, CallbackInfo info) {
        try {
            if (this.previousScreen != null) {
                if (this.previousScreen instanceof VirtualScreenHandlerInterface) {
                    ((VirtualScreenHandlerInterface) this.previousScreen).getGui().close(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.previousScreen = null;
    }


    @Inject(method = "onRenameItem", at = @At("TAIL"))
    private void catchRenamingWithCustomGui(RenameItemC2SPacket packet, CallbackInfo ci) {
        if (this.player.currentScreenHandler instanceof VirtualScreenHandler) {
            try {
                VirtualScreenHandler handler = (VirtualScreenHandler) this.player.currentScreenHandler;
                if (handler.getGui() instanceof AnvilInputGui) {
                    ((AnvilInputGui) handler.getGui()).input(packet.getName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Inject(method = "onCraftRequest", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;updateLastActionTime()V", shift = At.Shift.BEFORE), cancellable = true)
    private void catchRecipeRequests(CraftRequestC2SPacket packet, CallbackInfo ci) {
        if (this.player.currentScreenHandler instanceof VirtualScreenHandler) {
            try {
                VirtualScreenHandler handler = (VirtualScreenHandler) this.player.currentScreenHandler;
                handler.gui.onCraftRequest(packet.getRecipe(), packet.shouldCraftAll());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Inject(method = "onSignUpdate(Lnet/minecraft/network/packet/c2s/play/UpdateSignC2SPacket;)V", at = @At("HEAD"), cancellable = true)
    private void catchSignUpdate(UpdateSignC2SPacket packet, CallbackInfo ci) {
        try {
            if (this.player.currentScreenHandler instanceof SignScreenHandler) {
                SignGui gui = ((SignScreenHandler) this.player.currentScreenHandler).getGui();
                for (int i = 0; i < packet.getText().length; i++) {
                    gui.setLineInternal(i, new LiteralText(packet.getText()[i]));
                }
                gui.close(true);
                ci.cancel();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
