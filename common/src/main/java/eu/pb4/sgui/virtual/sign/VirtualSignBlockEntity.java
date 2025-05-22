package eu.pb4.sgui.virtual.sign;

import eu.pb4.sgui.mixin.SignBlockEntityAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;

/**
 * SignBlockEntity which doesn't invoke {@link SignBlockEntity#markUpdated()}
 */
public class VirtualSignBlockEntity extends SignBlockEntity {

    public VirtualSignBlockEntity(Level level, BlockPos pos, BlockState state) {
        super(pos, state);
        this.setLevel(level);
    }

    public boolean setText(SignText text, boolean front) {
        return front ? this.setFrontText(text) : this.setBackText(text);
    }

    private boolean setBackText(SignText backText) {
        if (backText != this.getBackText()) {
            ((SignBlockEntityAccessor) this).setBackText(backText);
            return true;
        } else {
            return false;
        }
    }

    private boolean setFrontText(SignText frontText) {
        if (frontText != this.getFrontText()) {
            ((SignBlockEntityAccessor) this).setFrontText(frontText);
            return true;
        } else {
            return false;
        }
    }

}
