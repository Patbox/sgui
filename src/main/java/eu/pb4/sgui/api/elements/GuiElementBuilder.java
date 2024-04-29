package eu.pb4.sgui.api.elements;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.component.DataComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.component.type.UnbreakableComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Gui Element Builder
 * <br>
 * The GuiElementBuilder is the best way of constructing gui elements.
 * It supplies all the methods needed to construct a standard {@link GuiElement}.
 *
 * @see GuiElementBuilderInterface
 */
@SuppressWarnings({"unused"})
public class GuiElementBuilder implements GuiElementBuilderInterface<GuiElementBuilder> {
    protected ItemStack itemStack = new ItemStack(Items.STONE);
    protected GuiElement.ClickCallback callback = GuiElementInterface.EMPTY_CALLBACK;

    /**
     * Constructs a GuiElementBuilder with the default options
     */
    public GuiElementBuilder() {
    }

    /**
     * Constructs a GuiElementBuilder with the specified Item.
     *
     * @param item the item to use
     */
    public GuiElementBuilder(Item item) {
        this.itemStack = new ItemStack(item);
    }

    /**
     * Constructs a GuiElementBuilder with the specified Item
     * and number of items.
     *
     * @param item  the item to use
     * @param count the number of items
     */
    public GuiElementBuilder(Item item, int count) {
        this.itemStack = new ItemStack(item, count);
    }

    /**
     * Constructs a GuiElementBuilder with the specified ItemStack
     *
     * @param stack  the item stack to use
     */
    public GuiElementBuilder(ItemStack stack) {
        this.itemStack = stack.copy();
    }

    /**
     * Constructs a GuiElementBuilder based on the supplied stack.
     *
     * @param stack the stack to base the builder of
     * @return the constructed builder
     */
    public static GuiElementBuilder from(ItemStack stack) {
        return new GuiElementBuilder(stack);
    }

    @Deprecated
    public static List<Text> getLore(ItemStack stack) {
        return stack.getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT).lines();
    }

    /**
     * Sets the type of Item of the element.
     *
     * @param item the item to use
     * @return this element builder
     */
    public GuiElementBuilder setItem(Item item) {
        this.itemStack = new ItemStack(item.getRegistryEntry(), this.itemStack.getCount(), this.itemStack.getComponentChanges());
        return this;
    }

    /**
     * Sets the name of the element.
     *
     * @param name the name to use
     * @return this element builder
     */
    public GuiElementBuilder setName(Text name) {
        this.itemStack.set(DataComponentTypes.ITEM_NAME, name.copy());
        return this;
    }

    /**
     * Sets the rarity of the element.
     *
     * @param rarity to use
     * @return this element builder
     */
    public GuiElementBuilder setRarity(Rarity rarity) {
        this.itemStack.set(DataComponentTypes.RARITY, rarity);
        return this;
    }

    /**
     * Sets the number of items in the element.
     *
     * @param count the number of items
     * @return this element builder
     */
    public GuiElementBuilder setCount(int count) {
        this.itemStack.setCount(count);
        return this;
    }


    /**
     * Sets the max number of items in the element.
     *
     * @param count the number of items
     * @return this element builder
     */
    public GuiElementBuilder setMaxCount(int count) {
        this.itemStack.set(DataComponentTypes.MAX_STACK_SIZE, count);
        return this;
    }

    /**
     * Sets the lore lines of the element.
     *
     * @param lore a list of all the lore lines
     * @return this element builder
     */
    public GuiElementBuilder setLore(List<Text> lore) {
        this.itemStack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        return this;
    }

    /**
     * Adds a line of lore to the element.
     *
     * @param lore the line to add
     * @return this element builder
     */
    public GuiElementBuilder addLoreLine(Text lore) {
        this.itemStack.apply(DataComponentTypes.LORE, LoreComponent.DEFAULT, lore, LoreComponent::with);
        return this;
    }

    /**
     * Set the damage of the element. This will only be
     * visible if the item supports has durability.
     *
     * @param damage the amount of durability the item is missing
     * @return this element builder
     */
    public GuiElementBuilder setDamage(int damage) {
        this.itemStack.set(DataComponentTypes.DAMAGE, damage);
        return this;
    }

    /**
     * Set the max damage of the element.
     *
     * @param damage the amount of durability the item is missing
     * @return this element builder
     */
    public GuiElementBuilder setMaxDamage(int damage) {
        this.itemStack.set(DataComponentTypes.MAX_DAMAGE, damage);
        return this;
    }

    /**
     * Disables all default components on an item.
     * @return this element builder
     */
    public GuiElementBuilder noDefaults() {
        for (var x : this.itemStack.getItem().getComponents()) {
            if (this.itemStack.get(x.type()) == x.value()) {
                this.itemStack.set(x.type(), null);
            }
        }
        return this;
    }

    @Nullable
    public <T> T getComponent(DataComponentType<T> type) {
        return this.itemStack.get(type);
    }

    public <T> GuiElementBuilder setComponent(DataComponentType<T> type, @Nullable T value) {
        this.itemStack.set(type, value);
        return this;
    }

    /**
     * Hides all component-item related tooltip added by item's or non name/lore components.
     *
     * @return this element builder
     */
    public GuiElementBuilder hideDefaultTooltip() {
        this.itemStack.apply(DataComponentTypes.TRIM, null, comp -> comp != null ? comp.withShowInTooltip(false) : null);
        this.itemStack.apply(DataComponentTypes.UNBREAKABLE, null, comp -> comp != null ? comp.withShowInTooltip(false) : null);
        this.itemStack.apply(DataComponentTypes.ENCHANTMENTS, null, comp -> comp != null ? comp.withShowInTooltip(false) : null);
        this.itemStack.apply(DataComponentTypes.STORED_ENCHANTMENTS, null, comp -> comp != null ? comp.withShowInTooltip(false) : null);
        this.itemStack.apply(DataComponentTypes.ATTRIBUTE_MODIFIERS, null, comp -> comp != null ? comp.withShowInTooltip(false) : null);
        this.itemStack.apply(DataComponentTypes.DYED_COLOR, null, comp -> comp != null ? comp.withShowInTooltip(false) : null);
        this.itemStack.apply(DataComponentTypes.CAN_BREAK, null, comp -> comp != null ? comp.withShowInTooltip(false) : null);
        this.itemStack.apply(DataComponentTypes.CAN_PLACE_ON, null, comp -> comp != null ? comp.withShowInTooltip(false) : null);
        this.itemStack.set(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        return this;
    }

    /**
     * Hides tooltip completely, making it never show
     * @return this element builder
     */
    public GuiElementBuilder hideTooltip() {
        this.itemStack.set(DataComponentTypes.HIDE_TOOLTIP, Unit.INSTANCE);
        return this;
    }

    /**
     * Give the element the specified enchantment.
     *
     * @param enchantment the enchantment to apply
     * @param level       the level of the specified enchantment
     * @return this element builder
     */
    public GuiElementBuilder enchant(Enchantment enchantment, int level) {
        this.itemStack.addEnchantment(enchantment, level);
        return this;
    }

    /**
     * Sets the element to have an enchantment glint.
     *
     * @return this element builder
     */
    public GuiElementBuilder glow() {
        this.itemStack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        return this;
    }

    /**
     * Sets the element to have an enchantment glint.
     *
     * @return this element builder
     */
    public GuiElementBuilder glow(boolean value) {
        this.itemStack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, value);
        return this;
    }

    /**
     * Sets the custom model data of the element.
     *
     * @param value the value used for custom model data
     * @return this element builder
     */
    public GuiElementBuilder setCustomModelData(int value) {
        this.itemStack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(value));
        return this;
    }

    /**
     * Sets the element to be unbreakable, also hides the durability bar.
     *
     * @return this element builder
     */
    public GuiElementBuilder unbreakable() {
        this.itemStack.set(DataComponentTypes.UNBREAKABLE, new UnbreakableComponent(true));
        return this;
    }

    /**
     * Sets the skull owner tag of a player head.
     * If the server parameter is not supplied it may lag the client while it loads the texture,
     * otherwise if the server is provided and the {@link GameProfile} contains a UUID then the
     * textures will be loaded by the server. This can take some time the first load,
     * however the skins are cached for later uses so its often less noticeable to let the
     * server load the textures.
     *
     * @param profile the {@link GameProfile} of the owner
     * @param server  the server instance, used to get the textures
     * @return this element builder
     */
    public GuiElementBuilder setSkullOwner(GameProfile profile, @Nullable MinecraftServer server) {
        if (profile.getId() != null && server != null) {
            if (server.getSessionService().getTextures(profile) == MinecraftProfileTextures.EMPTY) {
                var tmp = server.getSessionService().fetchProfile(profile.getId(), false);
                if (tmp != null) {
                    profile = tmp.profile();
                }
            }

        }
        this.itemStack.set(DataComponentTypes.PROFILE, new ProfileComponent(profile));
        return this;
    }

    /**
     * Sets the skull owner tag of a player head.
     * This method uses raw values required by client to display the skin
     * Ideal for textures generated with 3rd party websites like mineskin.org
     *
     * @param value     texture value used by client
     * @return this element builder
     */
    public GuiElementBuilder setSkullOwner(String value) {
        return this.setSkullOwner(value, null, null);
    }

    /**
     * Sets the skull owner tag of a player head.
     * This method uses raw values required by client to display the skin
     * Ideal for textures generated with 3rd party websites like mineskin.org
     *
     * @param value     texture value used by client
     * @param signature optional signature, will be ignored when set to null
     * @param uuid      UUID of skin owner, if null default will be used
     * @return this element builder
     */
    public GuiElementBuilder setSkullOwner(String value, @Nullable String signature, @Nullable UUID uuid) {
        PropertyMap map = new PropertyMap();
        map.put("textures", new Property("textures", value, signature));
        this.itemStack.set(DataComponentTypes.PROFILE, new ProfileComponent(Optional.empty(), Optional.ofNullable(uuid), map));
        return this;
    }

    @Override
    public GuiElementBuilder setCallback(GuiElementInterface.ClickCallback callback) {
        this.callback = callback;
        return this;
    }

    /**
     * Constructs an ItemStack using the current builder options.
     * Note that this ignores the callback as it is stored in
     * the {@link GuiElement}.
     *
     * @return this builder as a stack
     * @see GuiElementBuilder#build()
     */
    public ItemStack asStack() {
        return itemStack.copy();
    }

    @Override
    public GuiElement build() {
        return new GuiElement(this.itemStack, this.callback);
    }

    @Deprecated(forRemoval = true)
    public GuiElementBuilder hideFlags() {
        return this.hideDefaultTooltip();
    }
}
