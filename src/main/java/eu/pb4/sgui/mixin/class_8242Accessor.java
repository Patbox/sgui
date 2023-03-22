package eu.pb4.sgui.mixin;

import net.minecraft.class_8242;
import net.minecraft.util.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_8242.class)
public interface class_8242Accessor {
    @Accessor("field_43303")
    void setTextColorNoUpdate(DyeColor color);
}
