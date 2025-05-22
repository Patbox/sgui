package eu.pb4.sgui.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
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
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerPlayNetworkHandlerMixin extends ServerCommonPacketListenerImpl {

    @Shadow
    public ServerPlayer player;
    @Unique
    private boolean sgui$bookIgnoreClose = false;
    @Unique
    private AbstractContainerMenu sgui$previousScreen = null;

    public ServerPlayNetworkHandlerMixin(MinecraftServer server, Connection connection, CommonListenerCookie clientData) {
        super(server, connection, clientData);
    }

    @Inject(method = "handleContainerClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;resetLastActionTime()V", shift = At.Shift.AFTER), cancellable = true)
    private void sgui$handleGuiClicks(ServerboundContainerClickPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof VirtualScreenHandler handler) {
            try {
                var gui = handler.getGui();
                if (this.player.isSpectator() && !gui.canSpectatorsClick()) {
                    return;
                }

                int slot = packet.slotNum();
                int button = packet.buttonNum();

                ClickType type = ClickType.toClickType(packet.clickType(), button, slot);
                boolean ignore = gui.onAnyClick(slot, type, packet.clickType());
                if (ignore && !handler.getGui().getLockPlayerInventory() && (slot >= handler.getGui().getSize() || slot < 0 || handler.getGui().getSlotRedirect(slot) != null)) {
                    return;
                }

                this.player.containerMenu.suppressRemoteUpdates();
                boolean bl = packet.stateId() != this.player.containerMenu.getStateId();

                for (var entry : Int2ObjectMaps.fastIterable(packet.changedSlots())) {
                    this.player.containerMenu.setRemoteSlotUnsafe(entry.getIntKey(), entry.getValue());
                }

                this.player.containerMenu.setRemoteCarried(packet.carriedItem());

                boolean allow = gui.click(slot, type, packet.clickType());

                this.player.containerMenu.resumeRemoteUpdates();
                if (allow) {
                    if (bl) {
                        this.player.containerMenu.broadcastFullState();
                    } else {
                        this.player.containerMenu.broadcastChanges();
                    }
                }
            } catch (Throwable e) {
                handler.getGui().handleException(e);
            }

            ci.cancel();
        } else if (this.player.containerMenu instanceof BookScreenHandler) {
            ci.cancel();
        }
    }

    @ModifyExpressionValue(method = "handleContainerClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;isSpectator()Z"))
    private boolean sgui$canSpectatorClickSlot(boolean isSpectator) {
        return isSpectator && !(this.player.containerMenu instanceof VirtualScreenHandler handler && handler.getGui().canSpectatorsClick());
    }

    @Inject(method = "handleContainerClick", at = @At("TAIL"))
    private void sgui$resyncGui(ServerboundContainerClickPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof VirtualScreenHandler handler) {
            try {
                int slot = packet.slotNum();
                int button = packet.buttonNum();
                ClickType type = ClickType.toClickType(packet.clickType(), button, slot);

                if (type == ClickType.MOUSE_DOUBLE_CLICK || (type.isDragging && type.value == 2) || type.shift) {
                    GuiHelpers.sendPlayerScreenHandler(this.player);
                }

            } catch (Throwable e) {
                handler.getGui().handleException(e);
            }
        }
    }

    @Inject(method = "handleContainerClose", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V", shift = At.Shift.AFTER), cancellable = true)
    private void sgui$storeScreenHandler(ServerboundContainerClosePacket packet, CallbackInfo info) {
        if (this.player.containerMenu instanceof VirtualScreenHandlerInterface handler) {
            if (this.sgui$bookIgnoreClose && this.player.containerMenu instanceof BookScreenHandler) {
                this.sgui$bookIgnoreClose = false;
                info.cancel();
                return;
            }

            if (handler.getGui().canPlayerClose()) {
                this.sgui$previousScreen = this.player.containerMenu;
            } else {
                var screenHandler = this.player.containerMenu;
                try {
                    if (screenHandler.getType() != null) {
                        this.send(new ClientboundOpenScreenPacket(screenHandler.containerId, screenHandler.getType(), handler.getGui().getTitle()));
                        screenHandler.sendAllDataToRemote();
                    }
                } catch (Throwable ignored) {

                }
                info.cancel();
            }

        }
    }

    @Inject(method = "handleContainerClose", at = @At("TAIL"))
    private void sgui$executeClosing(ServerboundContainerClosePacket packet, CallbackInfo info) {
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


    @Inject(method = "handleRenameItem", at = @At("TAIL"))
    private void sgui$catchRenamingWithCustomGui(ServerboundRenameItemPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof VirtualScreenHandler handler) {
            try {
                if (handler.getGui() instanceof AnvilInputGui) {
                    ((AnvilInputGui) handler.getGui()).input(packet.getName());
                }
            } catch (Throwable e) {
                handler.getGui().handleException(e);
            }
        }
    }

    @Inject(method = "handlePlaceRecipe", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;resetLastActionTime()V", shift = At.Shift.BEFORE))
    private void sgui$catchRecipeRequests(ServerboundPlaceRecipePacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof VirtualScreenHandler handler && handler.getGui() instanceof SimpleGui gui) {
            try {
                gui.onCraftRequest(packet.recipe(), packet.useMaxItems());
            } catch (Throwable e) {
                handler.getGui().handleException(e);
            }
        }
    }

    @Inject(method = "updateSignText", at = @At("HEAD"), cancellable = true)
    private void sgui$catchSignUpdate(ServerboundSignUpdatePacket packet, List<FilteredText> signComponent, CallbackInfo ci) {
        try {
            if (this.player.containerMenu instanceof FakeScreenHandler fake && fake.getGui() instanceof SignGui gui) {
                for (int i = 0; i < packet.getLines().length; i++) {
                    gui.setLineInternal(i, Component.literal(packet.getLines()[i]));
                }
                gui.close(true);
                ci.cancel();
            }
        } catch (Throwable e) {
            if (this.player.containerMenu instanceof VirtualScreenHandlerInterface handler) {
                handler.getGui().handleException(e);
            } else {
                e.printStackTrace();
            }
        }
    }

    @Inject(method = "handleSelectTrade", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V"), cancellable = true)
    private void sgui$catchMerchantTradeSelect(ServerboundSelectTradePacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof VirtualMerchantScreenHandler merchantScreenHandler) {
            int id = packet.getItem();
            merchantScreenHandler.selectNewTrade(id);
            ci.cancel();
        }
    }

    @Inject(method = "handleSetCarriedItem", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V"), cancellable = true)
    private void sgui$catchUpdateSelectedSlot(ServerboundSetCarriedItemPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof HotbarScreenHandler handler) {
            if (!handler.getGui().onSelectedSlotChange(packet.getSlot())) {
                this.send(new ServerboundSetCarriedItemPacket(handler.getGui().getSelectedSlot()));
            }
            ci.cancel();
        }
    }

    @Inject(method = "handleSetCreativeModeSlot", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V"), cancellable = true)
    private void sgui$cancelCreativeAction(ServerboundSetCreativeModeSlotPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof VirtualScreenHandlerInterface) {
            ci.cancel();
        }
    }

    @Inject(method = "handleAnimate", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V"), cancellable = true)
    private void sgui$clickHandSwing(ServerboundSwingPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof HotbarScreenHandler screenHandler) {
            var gui = screenHandler.getGui();
            if (!gui.onHandSwing()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "handleUseItem", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V"), cancellable = true)
    private void sgui$clickWithItem(ServerboundUseItemPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof HotbarScreenHandler handler) {
            var gui = handler.getGui();
            gui.onClickItem();
            handler.syncSelectedSlot();
            ci.cancel();
        }
    }

    @Inject(method = "handleUseItemOn", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V"), cancellable = true)
    private void sgui$clickOnBlock(ServerboundUseItemOnPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof HotbarScreenHandler handler) {
            var gui = handler.getGui();

            if (!gui.onClickBlock(packet.getHitResult())) {
                var pos = packet.getHitResult().getBlockPos();
                handler.syncSelectedSlot();

                this.send(new ClientboundBlockUpdatePacket(pos, this.player.serverLevel().getBlockState(pos)));
                pos = pos.relative(packet.getHitResult().getDirection());
                this.send(new ClientboundBlockUpdatePacket(pos, this.player.serverLevel().getBlockState(pos)));
                this.send(new ClientboundBlockChangedAckPacket(packet.getSequence()));

                ci.cancel();
            }
        }
    }

    @Inject(method = "handlePlayerAction", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V"), cancellable = true)
    private void sgui$onPlayerAction(ServerboundPlayerActionPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof HotbarScreenHandler handler) {
            var gui = handler.getGui();

            if (!gui.onPlayerAction(packet.getAction(), packet.getDirection())) {
                var pos = packet.getPos();
                handler.syncSelectedSlot();
                if (packet.getAction() == ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND) {
                    handler.syncOffhandSlot();
                }

                this.send(new ClientboundBlockUpdatePacket(pos, this.player.serverLevel().getBlockState(pos)));
                pos = pos.relative(packet.getDirection());
                this.send(new ClientboundBlockUpdatePacket(pos, this.player.serverLevel().getBlockState(pos)));
                this.send(new ClientboundBlockChangedAckPacket(packet.getSequence()));
                ci.cancel();
            }
        }
    }

    @Inject(method = "handleInteract", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V"), cancellable = true)
    private void sgui$clickOnEntity(ServerboundInteractPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof HotbarScreenHandler handler) {
            var gui = handler.getGui();
            var buf = new FriendlyByteBuf(Unpooled.buffer());
            ((PlayerInteractEntityC2SPacketAccessor) packet).invokeWrite(buf);

            int entityId = buf.readVarInt();
            var type = buf.readEnum(HotbarGui.EntityInteraction.class);

            Vec3 interactionPos = null;

            switch (type) {
                case INTERACT:
                    buf.readVarInt();
                    break;
                case INTERACT_AT:
                    interactionPos = new Vec3(buf.readFloat(), buf.readFloat(), buf.readFloat());
                    buf.readVarInt();
            }

            var isSneaking = buf.readBoolean();

            if (!gui.onClickEntity(entityId, type, isSneaking, interactionPos)) {
                handler.syncSelectedSlot();
                ci.cancel();
            }
        }
    }

    @Inject(method = "handleChat", at = @At("HEAD"), cancellable = true)
    private void sgui$onMessage(ServerboundChatPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof BookScreenHandler handler) {
            try {
                if (handler.getGui().onCommand(packet.message())) {
                    ci.cancel();
                }
            } catch (Throwable e) {
                handler.getGui().handleException(e);
            }
        }
    }

    @Inject(method = "handleChatCommand", at = @At("HEAD"), cancellable = true)
    private void sgui$onCommand(ServerboundChatCommandPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof BookScreenHandler handler) {
            try {
                this.sgui$bookIgnoreClose = true;
                if (handler.getGui().onCommand("/" + packet.command())) {
                    ci.cancel();
                }
            } catch (Throwable e) {
                handler.getGui().handleException(e);
            }
        }
    }
}
