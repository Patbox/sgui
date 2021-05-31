package eu.pb4.sgui.mixin;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SignBlockEntity.class)
public interface SignBlockEntityAccessor {
    @Accessor("textColor")
    void setTextColorNoUpdate(DyeColor color);
}
