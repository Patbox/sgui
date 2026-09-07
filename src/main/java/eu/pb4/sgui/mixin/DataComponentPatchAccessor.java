package eu.pb4.sgui.mixin;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import net.minecraft.core.component.DataComponentType;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;

@org.spongepowered.asm.mixin.Mixin(net.minecraft.core.component.DataComponentPatch.class)
public interface DataComponentPatchAccessor {
    @Accessor
    Reference2ObjectMap<DataComponentType<?>, Object> getMap();
}
