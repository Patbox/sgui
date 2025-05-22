package eu.pb4.sgui.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerPlayer.class)
public interface ServerPlayerEntityAccessor {
    @Invoker
    void callInitMenu(AbstractContainerMenu screenHandler);
}
