package eu.pb4.sgui.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.SguiUtils;
import eu.pb4.sgui.api.containerwrappers.AbstractWrapperMenu;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import eu.pb4.sgui.api.gui.SignGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.containerwrappers.FakeMenu;
import eu.pb4.sgui.api.containerwrappers.WrapperBookGuiContainerMenu;
import eu.pb4.sgui.impl.virtual.hotbar.WrapperHotbarContainerMenu;
import eu.pb4.sgui.api.containerwrappers.SlotBasedWrapperMenu;
import eu.pb4.sgui.impl.virtual.merchant.VirtualMerchantScreenHandler;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin extends ServerCommonPacketListenerImpl {

    @Shadow
    public ServerPlayer player;
    @Unique
    private boolean sgui$bookIgnoreClose = false;

    public ServerGamePacketListenerImplMixin(MinecraftServer server, Connection connection, CommonListenerCookie clientData) {
        super(server, connection, clientData);
    }

    @Inject(method = "handleContainerClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;resetLastActionTime()V", shift = At.Shift.AFTER), cancellable = true)
    private void sgui$handleGuiClicks(ServerboundContainerClickPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof SlotBasedWrapperMenu handler) {
            try {
                var gui = handler.getBackingGui();
                if (this.player.isSpectator() && !gui.canSpectatorsClick()) {
                    return;
                }

                int slot = packet.slotNum();
                int button = packet.buttonNum();

                ClickType type = ClickType.toClickType(packet.containerInput(), button, slot);
                boolean ignore = gui.onAnyClick(slot, type, packet.containerInput());
                if (ignore && !handler.getBackingGui().getLockPlayerInventory() && (slot >= handler.getBackingGui().getSize() || slot < 0 || handler.getBackingGui().getCustomSlot(slot) != null)) {
                    return;
                }

                this.player.containerMenu.suppressRemoteUpdates();
                boolean bl = packet.stateId() != this.player.containerMenu.getStateId();

                for (var entry : Int2ObjectMaps.fastIterable(packet.changedSlots())) {
                    this.player.containerMenu.setRemoteSlotUnsafe(entry.getIntKey(), entry.getValue());
                }

                this.player.containerMenu.setRemoteCarried(packet.carriedItem());

                boolean allow = gui.click(slot, type, packet.containerInput());

                this.player.containerMenu.resumeRemoteUpdates();
                if (allow) {
                    if (bl) {
                        this.player.containerMenu.broadcastFullState();
                    } else {
                        this.player.containerMenu.broadcastChanges();
                    }
                }
            } catch (Throwable e) {
                handler.getBackingGui().handleException(e);
            }

            ci.cancel();
        } else if (this.player.containerMenu instanceof WrapperBookGuiContainerMenu) {
            ci.cancel();
        }
    }

    @ModifyExpressionValue(method = "handleContainerClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;isSpectator()Z"))
    private boolean sgui$canSpectatorClickSlot(boolean isSpectator) {
        return isSpectator && !(this.player.containerMenu instanceof SlotBasedWrapperMenu handler && handler.getBackingGui().canSpectatorsClick());
    }

    @Inject(method = "handleContainerClick", at = @At("TAIL"))
    private void sgui$resyncGui(ServerboundContainerClickPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof SlotBasedWrapperMenu handler) {
            try {
                int slot = packet.slotNum();
                int button = packet.buttonNum();
                ClickType type = ClickType.toClickType(packet.containerInput(), button, slot);

                if (type == ClickType.MOUSE_DOUBLE_CLICK || (type.isDragging && type.value == 2) || type.shift) {
                    SguiUtils.sendCurrentMenu(this.player);
                }

            } catch (Throwable e) {
                handler.getBackingGui().handleException(e);
            }
        }
    }

    @Inject(method = "handleContainerClose", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V", shift = At.Shift.AFTER), cancellable = true)
    private void sgui$storeScreenHandler(ServerboundContainerClosePacket packet, CallbackInfo info) {
        if (this.player.containerMenu instanceof AbstractWrapperMenu handler) {
            if (this.sgui$bookIgnoreClose && this.player.containerMenu instanceof WrapperBookGuiContainerMenu) {
                this.sgui$bookIgnoreClose = false;
                info.cancel();
                return;
            }

            if (handler.getBackingGui().canPlayerClose()) {
                handler.getBackingGui().onPlayerClose(true);
            } else {
                handler.getBackingGui().onPlayerClose(false);
                var screenHandler = this.player.containerMenu;
                try {
                    if (handler.getType() != null) {
                        this.send(new ClientboundOpenScreenPacket(screenHandler.containerId, screenHandler.getType(), handler.getBackingGui().getTitle()));
                        screenHandler.sendAllDataToRemote();
                    }
                } catch (Throwable ignored) {

                }
                info.cancel();
            }

        }
    }

    @Inject(method = "handleRenameItem", at = @At("TAIL"))
    private void sgui$catchRenamingWithCustomGui(ServerboundRenameItemPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof SlotBasedWrapperMenu handler) {
            try {
                if (handler.getBackingGui() instanceof AnvilInputGui) {
                    ((AnvilInputGui) handler.getBackingGui()).receiveInput(packet.getName());
                }
            } catch (Throwable e) {
                handler.getBackingGui().handleException(e);
            }
        }
    }

    @Inject(method = "handlePlaceRecipe", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;resetLastActionTime()V", shift = At.Shift.BEFORE))
    private void sgui$catchRecipeRequests(ServerboundPlaceRecipePacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof SlotBasedWrapperMenu handler && handler.getBackingGui() instanceof SimpleGui gui) {
            try {
                gui.onCraftRequest(packet.recipe(), packet.useMaxItems());
            } catch (Throwable e) {
                handler.getBackingGui().handleException(e);
            }
        }
    }

    @Inject(method = "updateSignText", at = @At("HEAD"), cancellable = true)
    private void sgui$catchSignUpdate(ServerboundSignUpdatePacket packet, List<FilteredText> signText, CallbackInfo ci) {
        try {
            if (this.player.containerMenu instanceof FakeMenu fake && fake.getBackingGui() instanceof SignGui gui) {
                for (int i = 0; i < packet.getLines().length; i++) {
                    gui.setLineInternal(i, Component.literal(packet.getLines()[i]));
                }
                gui.close(true);
                ci.cancel();
            }
        } catch (Throwable e) {
            if (this.player.containerMenu instanceof AbstractWrapperMenu handler) {
                handler.getBackingGui().handleException(e);
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
        if (this.player.containerMenu instanceof WrapperHotbarContainerMenu handler) {
            if (!handler.getBackingGui().onSelectedSlotChange(packet.getSlot())) {
                this.send(new ClientboundSetHeldSlotPacket(handler.getBackingGui().getSelectedSlot()));
            }
            ci.cancel();
        }
    }

    @Inject(method = "handleSetCreativeModeSlot", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V"), cancellable = true)
    private void sgui$cancelCreativeAction(ServerboundSetCreativeModeSlotPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof AbstractWrapperMenu) {
            ci.cancel();
        }
    }

    @Inject(method = "handlePickItemFromBlock", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V"), cancellable = true)
    private void sgui$pickBlockHandler(ServerboundPickItemFromBlockPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof WrapperHotbarContainerMenu screenHandler) {
            var gui = screenHandler.getBackingGui();
            if (!gui.onPickItemFromBlock(packet.pos(), packet.includeData())) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "handlePickItemFromEntity", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V"), cancellable = true)
    private void sgui$pickEntityHandler(ServerboundPickItemFromEntityPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof WrapperHotbarContainerMenu screenHandler) {
            var gui = screenHandler.getBackingGui();
            if (!gui.onPickItemFromEntity(packet.id(), packet.includeData())) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "handleAnimate", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V"), cancellable = true)
    private void sgui$clickHandSwing(ServerboundSwingPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof WrapperHotbarContainerMenu screenHandler) {
            var gui = screenHandler.getBackingGui();
            if (!gui.onHandSwing()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "handleUseItem", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V"), cancellable = true)
    private void sgui$clickWithItem(ServerboundUseItemPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof WrapperHotbarContainerMenu handler) {
            var gui = handler.getBackingGui();
            gui.onClickItem();
            handler.syncSelectedSlot();
            ci.cancel();
        }
    }

    @Inject(method = "handleUseItemOn", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V"), cancellable = true)
    private void sgui$clickOnBlock(ServerboundUseItemOnPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof WrapperHotbarContainerMenu handler) {
            var gui = handler.getBackingGui();

            if (!gui.onClickBlock(packet.getHitResult())) {
                var pos = packet.getHitResult().getBlockPos();
                handler.syncSelectedSlot();

                this.send(new ClientboundBlockUpdatePacket(pos, this.player.level().getBlockState(pos)));
                pos = pos.relative(packet.getHitResult().getDirection());
                this.send(new ClientboundBlockUpdatePacket(pos, this.player.level().getBlockState(pos)));
                this.send(new ClientboundBlockChangedAckPacket(packet.getSequence()));

                ci.cancel();
            }
        }
    }

    @Inject(method = "handlePlayerAction", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V"), cancellable = true)
    private void sgui$onPlayerAction(ServerboundPlayerActionPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof WrapperHotbarContainerMenu handler) {
            var gui = handler.getBackingGui();

            if (!gui.onPlayerAction(packet.getAction(), packet.getDirection())) {
                var pos = packet.getPos();
                handler.syncSelectedSlot();
                if (packet.getAction() == ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND) {
                    handler.syncOffhandSlot();
                }

                this.send(new ClientboundBlockUpdatePacket(pos, this.player.level().getBlockState(pos)));
                pos = pos.relative(packet.getDirection());
                this.send(new ClientboundBlockUpdatePacket(pos, this.player.level().getBlockState(pos)));
                this.send(new ClientboundBlockChangedAckPacket(packet.getSequence()));
                ci.cancel();
            }
        }
    }

    @Inject(method = "handleAttack", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V"), cancellable = true)
    private void sgui$handleAttack(ServerboundAttackPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof WrapperHotbarContainerMenu handler) {
            var gui = handler.getBackingGui();

            int entityId = packet.entityId();

            if (!gui.onEntityAttacked(entityId)) {
                handler.syncSelectedSlot();
                ci.cancel();
            }
        }
    }


    @Inject(method = "handleInteract", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V"), cancellable = true)
    private void sgui$clickOnEntity(ServerboundInteractPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof WrapperHotbarContainerMenu handler) {
            var gui = handler.getBackingGui();

            int entityId = packet.entityId();

            if (!gui.onEntityInteracted(entityId, packet.hand(), packet.usingSecondaryAction(), packet.location())) {
                handler.syncSelectedSlot();
                ci.cancel();
            }
        }
    }

    @Inject(method = "lambda$handleChat$0", at = @At("HEAD"), cancellable = true)
    private void sgui$onMessage(ServerboundChatPacket packet, Optional<LastSeenMessages> optional, CallbackInfo ci) {
        if (this.player.containerMenu instanceof WrapperBookGuiContainerMenu handler) {
            try {
                if (handler.getBackingGui().onCommand(packet.message())) {
                    ci.cancel();
                }
            } catch (Throwable e) {
                handler.getBackingGui().handleException(e);
            }
        }
    }

    @Inject(method = "lambda$handleChatCommand$0", at = @At("HEAD"), cancellable = true)
    private void sgui$onCommand(ServerboundChatCommandPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof WrapperBookGuiContainerMenu handler) {
            try {
                this.sgui$bookIgnoreClose = true;
                if (handler.getBackingGui().onCommand("/" + packet.command())) {
                    ci.cancel();
                }
            } catch (Throwable e) {
                handler.getBackingGui().handleException(e);
            }
        }
    }
}
