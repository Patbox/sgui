package eu.pb4.sgui.mixin;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.util.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SignBlockEntity.class)
public interface SignBlockEntityAccessor {
    @Accessor("textColor")
    void setTextColorNoUpdate(DyeColor color);
}
