package eu.pb4.sgui.api.elements;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import eu.pb4.sgui.api.GuiHelpers;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.component.type.UnbreakableComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Animated Gui Element Builder
 * <br>
 * The {@link AnimatedGuiElementBuilder} is the best way of constructing
 * an {@link AnimatedGuiElement}.
 * It supplies all the methods needed to construct each frame and mesh
 * them together to create the full animation.
 *
 * @see GuiElementBuilderInterface
 */
@SuppressWarnings({"unused"})
public class AnimatedGuiElementBuilder implements GuiElementBuilderInterface<AnimatedGuiElementBuilder> {
    protected final List<ItemStack> itemStacks = new ArrayList<>();
    protected ItemStack itemStack = new ItemStack(Items.STONE);
    protected GuiElement.ClickCallback callback = GuiElement.EMPTY_CALLBACK;
    protected int interval = 1;
    protected boolean random = false;

    /**
     * Constructs a AnimatedGuiElementBuilder with the default options
     */
    public AnimatedGuiElementBuilder() {
    }

    /**
     * Constructs a AnimatedGuiElementBuilder with the supplied interval
     *
     * @param interval the time between frame changes
     * @return this element builder
     */
    public AnimatedGuiElementBuilder setInterval(int interval) {
        this.interval = interval;
        return this;
    }

    /**
     * Sets if the frames should be randomly chosen or more in order
     * of addition.
     *
     * @param value <code>true</code> to select random frames
     * @return this element builder
     */
    public AnimatedGuiElementBuilder setRandom(boolean value) {
        this.random = value;
        return this;
    }

    /**
     * Saves the current stack that is being created.
     * This will add it to the animation and reset the
     * settings awaiting another creation.
     *
     * @return this element builder
     */
    public AnimatedGuiElementBuilder saveItemStack() {
        this.itemStacks.add(this.itemStack.copy());
        this.itemStack = new ItemStack(Items.STONE);
        return this;
    }

    /**
     * Sets the type of Item of the element.
     *
     * @param item the item to use
     * @return this element builder
     */
    public AnimatedGuiElementBuilder setItem(Item item) {
        this.itemStack = new ItemStack(item.getRegistryEntry(), this.itemStack.getCount(), this.itemStack.getComponentChanges());
        return this;
    }

    /**
     * Sets the name of the element.
     *
     * @param name the name to use
     * @return this element builder
     */
    public AnimatedGuiElementBuilder setName(Text name) {
        this.itemStack.set(DataComponentTypes.CUSTOM_NAME, name.copy().styled(GuiHelpers.STYLE_CLEARER));
        return this;
    }

    /**
     * Sets the item name of the element.
     *
     * @param name the name to use
     * @return this element builder
     */
    public AnimatedGuiElementBuilder setItemName(Text name) {
        this.itemStack.set(DataComponentTypes.ITEM_NAME, name.copy());
        return this;
    }

    /**
     * Sets the rarity of the element.
     *
     * @param rarity to use
     * @return this element builder
     */
    public AnimatedGuiElementBuilder setRarity(Rarity rarity) {
        this.itemStack.set(DataComponentTypes.RARITY, rarity);
        return this;
    }

    /**
     * Sets the number of items in the element.
     *
     * @param count the number of items
     * @return this element builder
     */
    public AnimatedGuiElementBuilder setCount(int count) {
        this.itemStack.setCount(count);
        return this;
    }

    /**
     * Sets the max number of items in the element.
     *
     * @param count the number of items
     * @return this element builder
     */
    public AnimatedGuiElementBuilder setMaxCount(int count) {
        this.itemStack.set(DataComponentTypes.MAX_STACK_SIZE, count);
        return this;
    }

    /**
     * Sets the lore lines of the element.
     *
     * @param lore a list of all the lore lines
     * @return this element builder
     */
    public AnimatedGuiElementBuilder setLore(List<Text> lore) {
        var l = new ArrayList<Text>(lore.size());
        for (var t : lore) {
            l.add(t.copy().styled(GuiHelpers.STYLE_CLEARER));
        }

        this.itemStack.set(DataComponentTypes.LORE, new LoreComponent(l));
        return this;
    }

    /**
     * Sets the lore lines of the element, without clearing out formatting.
     *
     * @param lore a list of all the lore lines
     * @return this element builder
     */
    public AnimatedGuiElementBuilder setLoreRaw(List<Text> lore) {
        this.itemStack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        return this;
    }

    /**
     * Adds a line of lore to the element.
     *
     * @param lore the line to add
     * @return this element builder
     */
    public AnimatedGuiElementBuilder addLoreLine(Text lore) {
        this.itemStack.apply(DataComponentTypes.LORE, LoreComponent.DEFAULT, lore.copy().styled(GuiHelpers.STYLE_CLEARER), LoreComponent::with);
        return this;
    }

    /**
     * Adds a line of lore to the element, without clearing out formatting.
     *
     * @param lore the line to add
     * @return this element builder
     */
    public AnimatedGuiElementBuilder addLoreLineRaw(Text lore) {
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
    public AnimatedGuiElementBuilder setDamage(int damage) {
        this.itemStack.set(DataComponentTypes.DAMAGE, damage);
        return this;
    }

    /**
     * Set the max damage of the element.
     *
     * @param damage the amount of durability the item is missing
     * @return this element builder
     */
    public AnimatedGuiElementBuilder setMaxDamage(int damage) {
        this.itemStack.set(DataComponentTypes.MAX_DAMAGE, damage);
        return this;
    }

    /**
     * Disables all default components on an item.
     * @return this element builder
     */
    public AnimatedGuiElementBuilder noDefaults() {
        for (var x : this.itemStack.getItem().getComponents()) {
            if (this.itemStack.get(x.type()) == x.value()) {
                this.itemStack.set(x.type(), null);
            }
        }
        return this;
    }

    @Nullable
    public <T> T getComponent(ComponentType<T> type) {
        return this.itemStack.get(type);
    }

    public <T> AnimatedGuiElementBuilder setComponent(ComponentType<T> type, @Nullable T value) {
        this.itemStack.set(type, value);
        return this;
    }

    /**
     * Hides all component-item related tooltip added by item's or non name/lore components.
     *
     * @return this element builder
     */
    public AnimatedGuiElementBuilder hideDefaultTooltip() {
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
     * Give the element the specified enchantment.
     *
     * @param enchantment the enchantment to apply
     * @param level       the level of the specified enchantment
     * @return this element builder
     */
    public AnimatedGuiElementBuilder enchant(RegistryEntry<Enchantment> enchantment, int level) {
        this.itemStack.addEnchantment(enchantment, level);
        return this;
    }

    /**
     * Give the element the specified enchantment.
     *
     * @param server MinecraftServer
     * @param enchantment the enchantment to apply
     * @param level       the level of the specified enchantment
     * @return this element builder
     */
    public AnimatedGuiElementBuilder enchant(MinecraftServer server, RegistryKey<Enchantment> enchantment, int level) {
        return enchant(server.getRegistryManager(), enchantment, level);
    }

    /**
     * Give the element the specified enchantment.
     *
     * @param lookup WrapperLookup
     * @param enchantment the enchantment to apply
     * @param level       the level of the specified enchantment
     * @return this element builder
     */
    public AnimatedGuiElementBuilder enchant(RegistryWrapper.WrapperLookup lookup, RegistryKey<Enchantment> enchantment, int level) {
        return enchant(lookup.getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(enchantment), level);
    }

    /**
     * Sets the element to have an enchantment glint.
     *
     * @return this element builder
     */
    public AnimatedGuiElementBuilder glow() {
        this.itemStack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        return this;
    }

    /**
     * Sets the element to have an enchantment glint.
     *
     * @return this element builder
     */
    public AnimatedGuiElementBuilder glow(boolean value) {
        this.itemStack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, value);
        return this;
    }

    /**
     * Sets the custom model data of the element.
     *
     * @param value the value used for custom model data
     * @return this element builder
     */
    public AnimatedGuiElementBuilder setCustomModelData(int value) {
        this.itemStack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(value));
        return this;
    }

    /**
     * Sets the element to be unbreakable, also hides the durability bar.
     *
     * @return this element builder
     */
    public AnimatedGuiElementBuilder unbreakable() {
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
    public AnimatedGuiElementBuilder setSkullOwner(GameProfile profile, @Nullable MinecraftServer server) {
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
    public AnimatedGuiElementBuilder setSkullOwner(String value) {
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
    public AnimatedGuiElementBuilder setSkullOwner(String value, @Nullable String signature, @Nullable UUID uuid) {
        PropertyMap map = new PropertyMap();
        map.put("textures", new Property("textures", value, signature));
        this.itemStack.set(DataComponentTypes.PROFILE, new ProfileComponent(Optional.empty(), Optional.ofNullable(uuid), map));
        return this;
    }

    /**
     * Sets the model of the element.
     *
     * @param model model to display item as
     * @return this element builder
     */
    public AnimatedGuiElementBuilder model(Identifier model) {
        this.itemStack.set(DataComponentTypes.ITEM_MODEL, model);
        return this;
    }

    public AnimatedGuiElementBuilder model(Item model) {
        this.itemStack.set(DataComponentTypes.ITEM_MODEL, model.getComponents().get(DataComponentTypes.ITEM_MODEL));
        return this;
    }


    @Override
    public AnimatedGuiElementBuilder setCallback(GuiElement.ClickCallback callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public AnimatedGuiElementBuilder setCallback(GuiElementInterface.ItemClickCallback callback) {
        this.callback = callback;
        return this;
    }

    /**
     * Constructs an ItemStack from the current builder options.
     * Note that this ignores the callback as it is stored in
     * the {@link GuiElement}.
     *
     * @return this builder as a stack
     * @see AnimatedGuiElementBuilder#build()
     */
    public ItemStack asStack() {
        return this.itemStack.copy();
    }

    public AnimatedGuiElement build() {
        return new AnimatedGuiElement(this.itemStacks.toArray(new ItemStack[0]), this.interval, this.random, this.callback);
    }
}
