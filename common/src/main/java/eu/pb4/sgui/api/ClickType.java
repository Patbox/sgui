package eu.pb4.sgui.api;

/**
 * Simplified Click Type
 * <br>
 * The API supplies onClick methods with these more accurate click types.
 * Use the fields in this enum to get a more general idea of the click.
 */
public enum ClickType {
    MOUSE_LEFT(true, false, false, false, -1, false, false),
    MOUSE_RIGHT(false, true, false, false, -1, false, false),
    MOUSE_LEFT_SHIFT(true, false, false, true, -1, false, false),
    MOUSE_RIGHT_SHIFT(false, true, false, true, -1, false, false),
    NUM_KEY_1(false, false, false, false, 1, true, false),
    NUM_KEY_2(false, false, false, false, 2, true, false),
    NUM_KEY_3(false, false, false, false, 3, true, false),
    NUM_KEY_4(false, false, false, false, 4, true, false),
    NUM_KEY_5(false, false, false, false, 5, true, false),
    NUM_KEY_6(false, false, false, false, 6, true, false),
    NUM_KEY_7(false, false, false, false, 7, true, false),
    NUM_KEY_8(false, false, false, false, 8, true, false),
    NUM_KEY_9(false, false, false, false, 9, true, false),
    MOUSE_MIDDLE(false, false, true, false, -1, false, false),
    DROP(false, false, false, false, -1, false, false),
    CTRL_DROP(false, false, false, false, -1, false, false),
    MOUSE_LEFT_OUTSIDE(true, false, false, false, -1, false, false),
    MOUSE_RIGHT_OUTSIDE(false, true, false, false, -1, false, false),
    MOUSE_LEFT_DRAG_START(true, false, false, false, 0, false, true),
    MOUSE_RIGHT_DRAG_START(false, true, false, false, 0, false, true),
    MOUSE_MIDDLE_DRAG_START(false, false, true, false, 0, false, true),
    MOUSE_LEFT_DRAG_ADD(true, false, false, false, 1, false, true),
    MOUSE_RIGHT_DRAG_ADD(false, true, false, false, 1, false, true),
    MOUSE_MIDDLE_DRAG_ADD(false, false, true, false, 1, false, true),
    MOUSE_LEFT_DRAG_END(true, false, false, false, 2, false, true),
    MOUSE_RIGHT_DRAG_END(false, true, false, false, 2, false, true),
    MOUSE_MIDDLE_DRAG_END(false, false, true, false, 2, false, true),
    MOUSE_DOUBLE_CLICK(false, false, true, false, -1, false, false),
    UNKNOWN(false, false, false, false, -1, false, false),
    OFFHAND_SWAP(false, false, false, false, 40, false, false);

    public final boolean isLeft;
    public final boolean isRight;
    public final boolean isMiddle;
    public final boolean shift;
    public final int value;
    public final boolean numKey;
    public final boolean isDragging;

    ClickType(boolean isLeft, boolean isRight, boolean isMiddle, boolean shift, int value, boolean numKey, boolean isDragging) {
        this.isLeft = isLeft;
        this.isRight = isRight;
        this.isMiddle = isMiddle;
        this.shift = shift;
        this.value = value;
        this.numKey = numKey;
        this.isDragging = isDragging;
    }

    public static ClickType toClickType(net.minecraft.world.inventory.ClickType action, int button, int slot) {
        switch (action) {
            case PICKUP:
                return button == 0 ? MOUSE_LEFT : MOUSE_RIGHT;
            case QUICK_MOVE:
                return button == 0 ? MOUSE_LEFT_SHIFT : MOUSE_RIGHT_SHIFT;
            case SWAP:
                if (button >= 0 && button < 9) {
                    return ClickType.values()[button + 4];
                } else if (button == 40) {
                    return ClickType.OFFHAND_SWAP;
                }
                break;
            case CLONE:
                return MOUSE_MIDDLE;
            case THROW:
                return slot == -999 ? (button == 0 ? MOUSE_LEFT_OUTSIDE : MOUSE_RIGHT_OUTSIDE) : (button == 0 ? DROP : CTRL_DROP);
            case QUICK_CRAFT:
                switch (button) {
                    case 0:
                        return MOUSE_LEFT_DRAG_START;
                    case 1:
                        return MOUSE_LEFT_DRAG_ADD;
                    case 2:
                        return MOUSE_LEFT_DRAG_END;
                    case 4:
                        return MOUSE_RIGHT_DRAG_START;
                    case 5:
                        return MOUSE_RIGHT_DRAG_ADD;
                    case 6:
                        return MOUSE_RIGHT_DRAG_END;
                    case 8:
                        return MOUSE_MIDDLE_DRAG_START;
                    case 9:
                        return MOUSE_MIDDLE_DRAG_ADD;
                    case 10:
                        return MOUSE_MIDDLE_DRAG_END;
                }
            case PICKUP_ALL:
                return MOUSE_DOUBLE_CLICK;
        }

        return UNKNOWN;
    }
}
