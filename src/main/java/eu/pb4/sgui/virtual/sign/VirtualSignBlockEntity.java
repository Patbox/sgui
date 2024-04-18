package eu.pb4.sgui.virtual.sign;

import eu.pb4.sgui.mixin.SignBlockEntityAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * SignBlockEntity which doesn't invoke {@link SignBlockEntity#updateListeners()}
 */
public class VirtualSignBlockEntity extends SignBlockEntity {

    public VirtualSignBlockEntity(World world, BlockPos pos, BlockState state) {
        super(pos, state);
        this.setWorld(world);
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
