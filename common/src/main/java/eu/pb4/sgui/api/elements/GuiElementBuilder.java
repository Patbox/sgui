package eu.pb4.sgui.api.elements;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import eu.pb4.sgui.api.GuiHelpers;
import it.unimi.dsi.fastutil.objects.ReferenceSortedSets;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.*;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    protected ItemStack itemStack = new ItemStack(Items.WHITE_DYE);
    protected GuiElement.ClickCallback callback = GuiElementInterface.EMPTY_CALLBACK;
    private boolean hideComponentTooltips;
    private boolean noTooltips;

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
     * Constructs a GuiElementBuilder with the specified item model.
     *
     * @param model Item model to use. Same as calling model(...).
     */
    public GuiElementBuilder(ResourceLocation model) {
        this.model(model);
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
    public static List<Component> getLore(ItemStack stack) {
        return stack.getOrDefault(DataComponents.LORE, ItemLore.EMPTY).lines();
    }

    /**
     * Sets the type of Item of the element.
     *
     * @param item the item to use
     * @return this element builder
     */
    public GuiElementBuilder setItem(Item item) {
        this.itemStack = new ItemStack(item.builtInRegistryHolder(), this.itemStack.getCount(), this.itemStack.getComponentsPatch());
        return this;
    }

    /**
     * Sets the name of the element.
     *
     * @param name the name to use
     * @return this element builder
     */
    public GuiElementBuilder setName(Component name) {
        this.itemStack.set(DataComponents.CUSTOM_NAME, name.copy().withStyle(GuiHelpers.STYLE_CLEARER));
        return this;
    }

    /**
     * Sets the item name of the element.
     *
     * @param name the name to use
     * @return this element builder
     */
    public GuiElementBuilder setItemName(Component name) {
        this.itemStack.set(DataComponents.ITEM_NAME, name.copy());
        return this;
    }

    /**
     * Sets the rarity of the element.
     *
     * @param rarity to use
     * @return this element builder
     */
    public GuiElementBuilder setRarity(Rarity rarity) {
        this.itemStack.set(DataComponents.RARITY, rarity);
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
        this.itemStack.set(DataComponents.MAX_STACK_SIZE, count);
        return this;
    }

    /**
     * Sets the lore lines of the element.
     *
     * @param lore a list of all the lore lines
     * @return this element builder
     */
    public GuiElementBuilder setLore(List<Component> lore) {
        var l = new ArrayList<Component>(lore.size());
        for (var t : lore) {
            l.add(t.copy().withStyle(GuiHelpers.STYLE_CLEARER));
        }

        this.itemStack.set(DataComponents.LORE, new ItemLore(l));
        return this;
    }

    /**
     * Sets the lore lines of the element, without clearing out formatting.
     *
     * @param lore a list of all the lore lines
     * @return this element builder
     */
    public GuiElementBuilder setLoreRaw(List<Component> lore) {
        this.itemStack.set(DataComponents.LORE, new ItemLore(lore));
        return this;
    }

    /**
     * Adds a line of lore to the element.
     *
     * @param lore the line to add
     * @return this element builder
     */
    public GuiElementBuilder addLoreLine(Component lore) {
        this.itemStack.update(DataComponents.LORE, ItemLore.EMPTY, lore.copy().withStyle(GuiHelpers.STYLE_CLEARER), ItemLore::withLineAdded);
        return this;
    }

    /**
     * Adds a line of lore to the element, without clearing out formatting.
     *
     * @param lore the line to add
     * @return this element builder
     */
    public GuiElementBuilder addLoreLineRaw(Component lore) {
        this.itemStack.update(DataComponents.LORE, ItemLore.EMPTY, lore, ItemLore::withLineAdded);
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
        this.itemStack.set(DataComponents.DAMAGE, damage);
        return this;
    }

    /**
     * Set the max damage of the element.
     *
     * @param damage the amount of durability the item is missing
     * @return this element builder
     */
    public GuiElementBuilder setMaxDamage(int damage) {
        this.itemStack.set(DataComponents.MAX_DAMAGE, damage);
        return this;
    }

    /**
     * Disables all default components on an item.
     * @return this element builder
     */
    public GuiElementBuilder noDefaults() {
        for (var x : this.itemStack.getItem().components()) {
            if (x.type() == DataComponents.ITEM_MODEL) {
                continue;
            }
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
        this.hideComponentTooltips = true;
        return this;
    }

    /**
     * Hides tooltip completely, making it never show
     * @return this element builder
     */
    public GuiElementBuilder hideTooltip() {
        this.noTooltips = true;
        return this;
    }

    /**
     * Give the element the specified enchantment.
     *
     * @param enchantment the enchantment to apply
     * @param level       the level of the specified enchantment
     * @return this element builder
     */
    public GuiElementBuilder enchant(Holder<Enchantment> enchantment, int level) {
        this.itemStack.enchant(enchantment, level);
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
    public GuiElementBuilder enchant(MinecraftServer server, ResourceKey<Enchantment> enchantment, int level) {
        return enchant(server.registryAccess(), enchantment, level);
    }

    /**
     * Give the element the specified enchantment.
     *
     * @param lookup WrapperLookup
     * @param enchantment the enchantment to apply
     * @param level       the level of the specified enchantment
     * @return this element builder
     */
    public GuiElementBuilder enchant(HolderLookup.Provider lookup, ResourceKey<Enchantment> enchantment, int level) {
        return enchant(lookup.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(enchantment), level);
    }

    /**
     * Sets the element to have an enchantment glint.
     *
     * @return this element builder
     */
    public GuiElementBuilder glow() {
        this.itemStack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        return this;
    }

    /**
     * Sets the element to have an enchantment glint.
     *
     * @return this element builder
     */
    public GuiElementBuilder glow(boolean value) {
        this.itemStack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, value);
        return this;
    }

    /**
     * Sets the custom model data of the element.
     *
     * @return this element builder
     */
    public GuiElementBuilder setCustomModelData(List<Float> floats, List<Boolean> flags, List<String> strings, List<Integer> colors) {
        this.itemStack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(floats, flags, strings, colors));
        return this;
    }

    /**
     * Sets the model of the element.
     *
     * @param model model to display item as
     * @return this element builder
     */
    public GuiElementBuilder model(ResourceLocation model) {
        this.itemStack.set(DataComponents.ITEM_MODEL, model);
        return this;
    }

    public GuiElementBuilder model(Item model) {
        this.itemStack.set(DataComponents.ITEM_MODEL, model.components().get(DataComponents.ITEM_MODEL));
        return this;
    }

    /**
     * Sets the element to be unbreakable, also hides the durability bar.
     *
     * @return this element builder
     */
    public GuiElementBuilder unbreakable() {
        this.itemStack.set(DataComponents.UNBREAKABLE, Unit.INSTANCE);
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
        this.itemStack.set(DataComponents.PROFILE, new ResolvableProfile(profile));
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
        this.itemStack.set(DataComponents.PROFILE, new ResolvableProfile(Optional.empty(), Optional.ofNullable(uuid), map));
        return this;
    }
    
    @Override
    public GuiElementBuilder setCallback(GuiElement.ClickCallback callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public GuiElementBuilder setCallback(GuiElementInterface.ItemClickCallback callback) {
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
        var copy = itemStack.copy();
        if (this.noTooltips) {
            copy.set(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(true, ReferenceSortedSets.emptySet()));
        } else {
            var comp = TooltipDisplay.DEFAULT;
            for (var entry : this.itemStack.getComponents()) {
                if (entry.value() instanceof TooltipProvider && entry.type() != DataComponents.LORE) {
                    comp = comp.withHidden(entry.type(), true);
                }
            }
            copy.set(DataComponents.TOOLTIP_DISPLAY, comp);
        }

        return copy;
    }

    @Override
    public GuiElement build() {
        return new GuiElement(this.asStack(), this.callback);
    }

    @Deprecated(forRemoval = true)
    public GuiElementBuilder hideFlags() {
        return this.hideDefaultTooltip();
    }
}
