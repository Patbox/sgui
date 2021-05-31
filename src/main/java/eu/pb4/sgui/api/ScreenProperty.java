package eu.pb4.sgui.api;

import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Screen Properties
 * <br>
 * Screen properties are values sent to client {@link net.minecraft.screen.ScreenHandler}s which
 * update visual or logical elements of the screen. <br>
 * Screen properties are specific to the {@link ScreenHandlerType} that they modify.
 * 
 * @see eu.pb4.sgui.api.gui.GuiInterface#sendProperty(ScreenProperty, int)
 */
@SuppressWarnings("unused")
public enum ScreenProperty {
    /**
     * {@link ScreenHandlerType#FURNACE}, {@link ScreenHandlerType#BLAST_FURNACE}, {@link ScreenHandlerType#SMOKER}
     * <p>
     * The level of the fire icon in the furnace
     * <ul>
     *     <li>Empty = 0</li>
     *     <li>Full = Value of MAX_FUEL_BURN_TIME</li>
     * </ul>
     */
    FIRE_LEVEL(0, ScreenHandlerType.FURNACE, ScreenHandlerType.BLAST_FURNACE, ScreenHandlerType.SMOKER),
    /**
     * The maximum burn time of the furnace fuel
     */
    MAX_FUEL_BURN_TIME(1, ScreenHandlerType.FURNACE, ScreenHandlerType.BLAST_FURNACE, ScreenHandlerType.SMOKER),
    /**
     * The current progress ticks of the arrow
     * <ul>
     *     <li>No Progress = 0</li>
     *     <li>Complete = Value of MAX_PROGRESS</li>
     * </ul>
     */
    CURRENT_PROGRESS(2, ScreenHandlerType.FURNACE, ScreenHandlerType.BLAST_FURNACE, ScreenHandlerType.SMOKER),
    /**
     * The ticks required for the burn to complete (200 on a vanilla server)
     */
    MAX_PROGRESS(3, ScreenHandlerType.FURNACE, ScreenHandlerType.BLAST_FURNACE, ScreenHandlerType.SMOKER),

    /**
     * {@link ScreenHandlerType#ENCHANTMENT}
     * <p>
     * The level requirement of the respective enchantment.
     */
    TOP_LEVEL_REQ(0, ScreenHandlerType.ENCHANTMENT),
    MIDDLE_LEVEL_REQ(1, ScreenHandlerType.ENCHANTMENT),
    BOTTOM_LEVEL_REQ(2, ScreenHandlerType.ENCHANTMENT),
    /**
     * Used for drawing the enchantment names (in SGA) clientside.
     * <p>
     * The same seed is used to calculate enchantments, but some of the data isn't sent to the client to prevent easily guessing the entire list (the seed value here is the regular seed bitwise and 0xFFFFFFF0).
     */
    ENCHANT_SEED(3, ScreenHandlerType.ENCHANTMENT),
    /**
     * The enchantment id of the respective enchantment (set to -1 to hide).
     * <p>
     * To get the id use {@link Registry#getRawId(Object)} for {@link Registry#ENCHANTMENT}.
     */
    TOP_ENCHANTMENT_ID(4, ScreenHandlerType.ENCHANTMENT),
    MIDDLE_ENCHANTMENT_ID(5, ScreenHandlerType.ENCHANTMENT),
    BOTTOM_ENCHANTMENT_ID(6, ScreenHandlerType.ENCHANTMENT),
    /**
     * The enchantment level of the respective enchantment
     * <ul>
     *     <li>-1 = No Enchantment</li>
     *     <li>1 = I</li>
     *     <li>2 = II</li>
     *     <li>...</li>
     *     <li>6 = VI</li>
     * </ul>
     */
    TOP_ENCHANTMENT_LEVEL(7, ScreenHandlerType.ENCHANTMENT),
    MIDDLE_ENCHANTMENT_LEVEL(8, ScreenHandlerType.ENCHANTMENT),
    BOTTOM_ENCHANTMENT_LEVEL(9, ScreenHandlerType.ENCHANTMENT),

    /**
     * {@link ScreenHandlerType#BEACON}
     * <p>
     * Controls what effect buttons are enabled, equivalent to the number of layers.
     * <ul>
     *     <li>No Layers = 0</li>
     *     <li>Full Beacon = 4</li>
     * </ul>
     */
    POWER_LEVEL(0, ScreenHandlerType.BEACON),
    /**
     * The effect id for the respective effect
     * To get the id use {@link Registry#getRawId(Object)} for {@link Registry#POTION}
     */
    FIRST_EFFECT(1, ScreenHandlerType.BEACON),
    SECOND_EFFECT(2, ScreenHandlerType.BEACON),

    /**
     * {@link ScreenHandlerType#ANVIL}
     * <p>
     * The level cost of the operation. Anything >30 will display as 'Too Expensive!'
     */
    LEVEL_COST(0, ScreenHandlerType.ANVIL),

    /**
     * {@link ScreenHandlerType#BREWING_STAND}
     * <p>
     * The ticks remaining until the operation completes
     * <ul>
     *     <li>Empty Arrow = 400</li>
     *     <li>Full Arrow = 0</li>
     * </ul>
     */
    BREW_TIME(0, ScreenHandlerType.BREWING_STAND),
    /**
     * The ticks remaining in the fuel display
     * <ul>
     *     <li>Empty Bubbles = 0</li>
     *     <li>Full Bubbles = 20</li>
     * </ul>
     */
    POWDER_FUEL_TIME(1, ScreenHandlerType.BREWING_STAND),

    /**
     * {@link ScreenHandlerType#STONECUTTER}, {@link ScreenHandlerType#LOOM}, {@link ScreenHandlerType#LECTERN}
     * <p>
     * The index of the selected element (Cut, Pattern, Page, ect.)
     * <ul>
     *     <li>No Element Selected = -1</li>
     *     <li>First Element = 0</li>
     *     <li>Second Element = 1</li>
     *     <li><code>n</code> Element = <code>n</code></li>
     * </ul>
     */
    SELECTED(0, ScreenHandlerType.STONECUTTER, ScreenHandlerType.LOOM, ScreenHandlerType.LECTERN);

    private final int id;
    private final ScreenHandlerType<?>[] types;

    ScreenProperty(int id, ScreenHandlerType<?>... types) {
        this.id = id;
        this.types = types;
    }

    public int id() {
        return id;
    }

    public boolean validFor(ScreenHandlerType<?> type) {
        return ArrayUtils.contains(types, type);
    }
}
