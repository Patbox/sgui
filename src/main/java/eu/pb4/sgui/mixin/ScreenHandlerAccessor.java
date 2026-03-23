package eu.pb4.sgui.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.RemoteSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerMenu.class)
public interface ScreenHandlerAccessor {
    @Accessor
    RemoteSlot getRemoteCarried();

    @Accessor
    NonNullList<RemoteSlot> getRemoteSlots();
}
