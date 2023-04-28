package eu.pb4.sgui.mixin;

import net.minecraft.block.entity.SignText;
import net.minecraft.util.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SignText.class)
public interface SignTextAccessor {
    @Accessor("color")
    void setTextColorNoUpdate(DyeColor color);
}
