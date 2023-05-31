package eu.pb4.sgui.mixin;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SignBlockEntity.class)
public interface SignBlockEntityAccessor {

    @Accessor
    void setFrontText(SignText frontText);

    @Accessor
    void setBackText(SignText backText);

}
