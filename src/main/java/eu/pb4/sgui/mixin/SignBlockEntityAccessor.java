package eu.pb4.sgui.mixin;

import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SignBlockEntity.class)
public interface SignBlockEntityAccessor {

    @Accessor
    void setFrontText(SignText frontText);

    @Accessor
    void setBackText(SignText backText);

}
