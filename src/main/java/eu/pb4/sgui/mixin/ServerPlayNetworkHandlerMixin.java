package eu.pb4.sgui.mixin;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import eu.pb4.sgui.api.gui.HotbarGui;
import eu.pb4.sgui.api.gui.SignGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.virtual.FakeScreenHandler;
import eu.pb4.sgui.virtual.VirtualScreenHandlerInterface;
import eu.pb4.sgui.virtual.book.BookScreenHandler;
import eu.pb4.sgui.virtual.hotbar.HotbarScreenHandler;
import eu.pb4.sgui.virtual.inventory.VirtualScreenHandler;
import eu.pb4.sgui.virtual.merchant.VirtualMerchantScreenHandler;
import io.netty.buffer.Unpooled;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.message.LastSeenMessageList;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.filter.FilteredMessage;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {

    @Unique
    private ScreenHandler sgui$previousScreen = null;

    @Shadow
    public ServerPlayerEntity player;

    @Shadow
    public abstract void sendPacket(Packet<?> packet);

    @Inject(method = "onClickSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;updateLastActionTime()V", shift = At.Shift.AFTER), cancellable = true)
    private void sgui$handleGuiClicks(ClickSlotC2SPacket packet, CallbackInfo ci) {
        if (this.player.currentScreenHandler instanceof VirtualScreenHandler handler) {
            try {
                var gui = handler.getGui();

                int slot = packet.getSlot();
                int button = packet.getButton();
                ClickType type = ClickType.toClickType(packet.getActionType(), button, slot);
                boolean ignore = gui.onAnyClick(slot, type, packet.getActionType());
                if (ignore && !handler.getGui().getLockPlayerInventory() && (slot >= handler.getGui().getSize() || slot < 0 || handler.getGui().getSlotRedirect(slot) != null)) {
                    if (type == ClickType.MOUSE_DOUBLE_CLICK || (type.isDragging && type.value == 2)) {
                        GuiHelpers.sendPlayerScreenHandler(this.player);
                    }

                    return;
                }

                boolean allow = gui.click(slot, type, packet.getActionType());
                if (handler.getGui().isOpen()) {
                    if (!allow) {
                        if (slot >= 0 && slot < handler.getGui().getSize()) {
                            this.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(handler.syncId, handler.nextRevision(), slot, handler.getSlot(slot).getStack()));
                        }
                        GuiHelpers.sendSlotUpdate(this.player, -1, -1, this.player.currentScreenHandler.getCursorStack(), handler.getRevision());

                        if (type.numKey) {
                            int x = type.value + handler.slots.size() - 10;
                            GuiHelpers.sendSlotUpdate(player, handler.syncId, x, handler.getSlot(x).getStack(), handler.nextRevision());
                        } else if (type == ClickType.MOUSE_DOUBLE_CLICK || type == ClickType.MOUSE_LEFT_SHIFT || type == ClickType.MOUSE_RIGHT_SHIFT || (type.isDragging && type.value == 2)) {
                            GuiHelpers.sendPlayerScreenHandler(this.player);
                        }
                    }
                }

            } catch (Throwable e) {
                handler.getGui().handleException(e);
                ci.cancel();
            }

            ci.cancel();
        } else if (this.player.currentScreenHandler instanceof BookScreenHandler) {
            ci.cancel();
        }
    }

    @Inject(method = "onClickSlot", at = @At("TAIL"))
    private void sgui$resyncGui(ClickSlotC2SPacket packet, CallbackInfo ci) {
        if (this.player.currentScreenHandler instanceof VirtualScreenHandler handler) {
            try {
                int slot = packet.getSlot();
                int button = packet.getButton();
                ClickType type = ClickType.toClickType(packet.getActionType(), button, slot);

                if (type == ClickType.MOUSE_DOUBLE_CLICK || (type.isDragging && type.value == 2) || type.shift) {
                    GuiHelpers.sendPlayerScreenHandler(this.player);
                }

            } catch (Throwable e) {
                handler.getGui().handleException(e);
            }
        }
    }

    @Inject(method = "onCloseHandledScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V", shift = At.Shift.AFTER), cancellable = true)
    private void sgui$storeScreenHandler(CloseHandledScreenC2SPacket packet, CallbackInfo info) {
        if (this.player.currentScreenHandler instanceof VirtualScreenHandlerInterface handler) {
            if (handler.getGui().canPlayerClose()) {
                this.sgui$previousScreen = this.player.currentScreenHandler;
            } else {
                var screenHandler = this.player.currentScreenHandler;
                if (screenHandler.getType() != null) {
                    try {
                        this.sendPacket(new OpenScreenS2CPacket(screenHandler.syncId, screenHandler.getType(), handler.getGui().getTitle()));
                        screenHandler.syncState();
                    } catch (Throwable e) {

                    }
                }
                info.cancel();
            }

        }
    }

    @Inject(method = "onCloseHandledScreen", at = @At("TAIL"))
    private void sgui$executeClosing(CloseHandledScreenC2SPacket packet, CallbackInfo info) {
        try {
            if (this.sgui$previousScreen != null) {
                if (this.sgui$previousScreen instanceof VirtualScreenHandlerInterface screenHandler) {
                    screenHandler.getGui().close(true);
                }
            }
        } catch (Throwable e) {
            if (this.sgui$previousScreen instanceof VirtualScreenHandlerInterface screenHandler) {
                screenHandler.getGui().handleException(e);
            } else {
                e.printStackTrace();
            }
        }
        this.sgui$previousScreen = null;
    }


    @Inject(method = "onRenameItem", at = @At("TAIL"))
    private void sgui$catchRenamingWithCustomGui(RenameItemC2SPacket packet, CallbackInfo ci) {
        if (this.player.currentScreenHandler instanceof VirtualScreenHandler handler) {
            try {
                if (handler.getGui() instanceof AnvilInputGui) {
                    ((AnvilInputGui) handler.getGui()).input(packet.getName());
                }
            } catch (Throwable e) {
                handler.getGui().handleException(e);
            }
        }
    }

    @Inject(method = "onCraftRequest", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;updateLastActionTime()V", shift = At.Shift.BEFORE), cancellable = true)
    private void sgui$catchRecipeRequests(CraftRequestC2SPacket packet, CallbackInfo ci) {
        if (this.player.currentScreenHandler instanceof VirtualScreenHandler handler && handler.getGui() instanceof SimpleGui gui) {
            try {
                gui.onCraftRequest(packet.getRecipe(), packet.shouldCraftAll());
            } catch (Throwable e) {
                handler.getGui().handleException(e);
            }
        }
    }

    @Inject(method = "onSignUpdate", at = @At("HEAD"), cancellable = true)
    private void sgui$catchSignUpdate(UpdateSignC2SPacket packet, List<FilteredMessage> signText, CallbackInfo ci) {
        try {
            if (this.player.currentScreenHandler instanceof FakeScreenHandler fake && fake.getGui() instanceof SignGui gui) {
                for (int i = 0; i < packet.getText().length; i++) {
                    gui.setLineInternal(i, Text.literal(packet.getText()[i]));
                }
                gui.close(true);
                ci.cancel();
            }
        } catch (Throwable e) {
            if (this.player.currentScreenHandler instanceof VirtualScreenHandlerInterface handler ) {
                handler.getGui().handleException(e);
            } else {
                e.printStackTrace();
            }
        }
    }

    @Inject(method = "onSelectMerchantTrade", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V"), cancellable = true)
    private void sgui$catchMerchantTradeSelect(SelectMerchantTradeC2SPacket packet, CallbackInfo ci) {
        if (this.player.currentScreenHandler instanceof VirtualMerchantScreenHandler merchantScreenHandler) {
            int id = packet.getTradeId();
            merchantScreenHandler.selectNewTrade(id);
            ci.cancel();
        }
    }

    @Inject(method = "onUpdateSelectedSlot", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V"), cancellable = true)
    private void sgui$catchUpdateSelectedSlot(UpdateSelectedSlotC2SPacket packet, CallbackInfo ci) {
        if (this.player.currentScreenHandler instanceof HotbarScreenHandler handler) {
            if (!handler.getGui().onSelectedSlotChange(packet.getSelectedSlot())) {
                this.sendPacket(new UpdateSelectedSlotS2CPacket(handler.getGui().getSelectedSlot()));
            }
            ci.cancel();
        }
    }

    @Inject(method = "onCreativeInventoryAction", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V"), cancellable = true)
    private void sgui$cancelCreativeAction(CreativeInventoryActionC2SPacket packet, CallbackInfo ci) {
        if (this.player.currentScreenHandler instanceof VirtualScreenHandlerInterface) {
            ci.cancel();
        }
    }

    @Inject(method = "onHandSwing", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V"), cancellable = true)
    private void sgui$clickHandSwing(HandSwingC2SPacket packet, CallbackInfo ci) {
        if (this.player.currentScreenHandler instanceof HotbarScreenHandler screenHandler) {
            var gui = screenHandler.getGui();
            if (!gui.onHandSwing()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "onPlayerInteractItem", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V"), cancellable = true)
    private void sgui$clickWithItem(PlayerInteractItemC2SPacket packet, CallbackInfo ci) {
        if (this.player.currentScreenHandler instanceof HotbarScreenHandler screenHandler) {
            var gui = screenHandler.getGui();
            if (screenHandler.slotsOld != null) {
                screenHandler.slotsOld.set(gui.getSelectedSlot() + 36, ItemStack.EMPTY);
                screenHandler.slotsOld.set(45, ItemStack.EMPTY);
            }
            gui.onClickItem();
            ci.cancel();
        }
    }

    @Inject(method = "onPlayerInteractBlock", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V"), cancellable = true)
    private void sgui$clickOnBlock(PlayerInteractBlockC2SPacket packet, CallbackInfo ci) {
        if (this.player.currentScreenHandler instanceof HotbarScreenHandler screenHandler) {
            var gui = screenHandler.getGui();

            if (!gui.onClickBlock(packet.getBlockHitResult())) {
                var pos = packet.getBlockHitResult().getBlockPos();
                if (screenHandler.slotsOld != null) {
                    screenHandler.slotsOld.set(gui.getSelectedSlot() + 36, ItemStack.EMPTY);
                    screenHandler.slotsOld.set(45, ItemStack.EMPTY);
                }

                this.sendPacket(new BlockUpdateS2CPacket(pos, this.player. getServerWorld().getBlockState(pos)));
                pos = pos.offset(packet.getBlockHitResult().getSide());
                this.sendPacket(new BlockUpdateS2CPacket(pos, this.player.getServerWorld().getBlockState(pos)));
                this.sendPacket(new PlayerActionResponseS2CPacket(packet.getSequence()));

                ci.cancel();
            }
        }
    }

    @Inject(method = "onPlayerAction", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V"), cancellable = true)
    private void sgui$onPlayerAction(PlayerActionC2SPacket packet, CallbackInfo ci) {
        if (this.player.currentScreenHandler instanceof HotbarScreenHandler screenHandler) {
            var gui = screenHandler.getGui();

            if (!gui.onPlayerAction(packet.getAction(), packet.getDirection())) {
                var pos = packet.getPos();
                if (screenHandler.slotsOld != null) {
                    screenHandler.slotsOld.set(gui.getSelectedSlot() + 36, ItemStack.EMPTY);
                    screenHandler.slotsOld.set(45, ItemStack.EMPTY);
                }
                this.sendPacket(new BlockUpdateS2CPacket(pos, this.player.getServerWorld().getBlockState(pos)));
                pos = pos.offset(packet.getDirection());
                this.sendPacket(new BlockUpdateS2CPacket(pos, this.player.getServerWorld().getBlockState(pos)));
                this.sendPacket(new PlayerActionResponseS2CPacket(packet.getSequence()));
                ci.cancel();
            }
        }
    }

    @Inject(method = "onPlayerInteractEntity", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V"), cancellable = true)
    private void sgui$clickOnEntity(PlayerInteractEntityC2SPacket packet, CallbackInfo ci) {
        if (this.player.currentScreenHandler instanceof HotbarScreenHandler screenHandler) {
            var gui = screenHandler.getGui();
            var buf = new PacketByteBuf(Unpooled.buffer());
            packet.write(buf);

            int entityId = buf.readVarInt();
            var type = buf.readEnumConstant(HotbarGui.EntityInteraction.class);

            Vec3d interactionPos = null;

            switch (type) {
                case INTERACT:
                    buf.readVarInt();
                    break;
                case INTERACT_AT:
                    interactionPos = new Vec3d(buf.readFloat(), buf.readFloat(), buf.readFloat());
                    buf.readVarInt();
            }

            var isSneaking = buf.readBoolean();

            if (!gui.onClickEntity(entityId, type, isSneaking, interactionPos)) {
                if (screenHandler.slotsOld != null) {
                    screenHandler.slotsOld.set(gui.getSelectedSlot() + 36, ItemStack.EMPTY);
                    screenHandler.slotsOld.set(45, ItemStack.EMPTY);
                }
                ci.cancel();
            }
        }
    }

    @Inject(method = "method_44900", at = @At("HEAD"), cancellable = true)
    private void sgui$onMessage(ChatMessageC2SPacket packet, Optional<LastSeenMessageList> optional, CallbackInfo ci) {
        if (this.player.currentScreenHandler instanceof BookScreenHandler handler) {
            try {
                if (handler.getGui().onCommand(packet.chatMessage())) {
                    ci.cancel();
                }
            } catch (Throwable e) {
                handler.getGui().handleException(e);
            }
        }
    }

    @Inject(method = "method_44356", at = @At("HEAD"), cancellable = true)
    private void sgui$onCommand(CommandExecutionC2SPacket packet, Optional<LastSeenMessageList> optional, CallbackInfo ci) {
        if (this.player.currentScreenHandler instanceof BookScreenHandler handler) {
            try {
                if (handler.getGui().onCommand("/" + packet.command())) {
                    ci.cancel();
                }
            } catch (Throwable e) {
                handler.getGui().handleException(e);
            }
        }
    }
}
