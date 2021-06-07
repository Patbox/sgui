package eu.pb4.sgui.api.elements;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Gui Element Builder
 * <br>
 * The GuiElementBuilder is the best way of constructing gui elements.
 * It supplies all the methods needed to construct a standard {@link GuiElement}.
 *
 * @see GuiElementBuilderInterface
 */
public class GuiElementBuilder implements GuiElementBuilderInterface {
    protected final Map<Enchantment, Integer> enchantments = new HashMap<>();
    protected Item item = Items.STONE;
    protected NbtCompound tag;
    protected int count = 1;
    protected Text name = null;
    protected List<Text> lore = new ArrayList<>();
    protected int damage = -1;
    protected GuiElement.ItemClickCallback callback = (index, type, action) -> {
    };
    protected byte hideFlags = 0;

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
        this.item = item;
    }

    /**
     * Constructs a GuiElementBuilder with the specified Item
     * and number of items.
     *
     * @param item  the item to use
     * @param count the number of items
     */
    public GuiElementBuilder(Item item, int count) {
        this.item = item;
        this.count = count;
    }

    /**
     * Constructs a GuiElementBuilder based on the supplied stack.
     *
     * @param stack the stack to base the builder of
     * @return the constructed builder
     */
    public static GuiElementBuilder from(ItemStack stack) {
        GuiElementBuilder builder = new GuiElementBuilder(stack.getItem(), stack.getCount());
        NbtCompound tag = stack.getOrCreateTag();

        if (stack.hasCustomName()) {
            builder.setName((MutableText) stack.getName());
            tag.getCompound("display").remove("Name");
        }

        if (tag.contains("display") && tag.getCompound("display").contains("Lore")) {
            builder.setLore(GuiElementBuilder.getLore(stack));
            tag.getCompound("display").remove("Lore");
        }

        if (stack.isDamaged()) {
            builder.setDamage(stack.getDamage());
            tag.remove("Damage");
        }

        if (stack.hasEnchantments()) {
            for (NbtElement enc : stack.getEnchantments()) {
                Registry.ENCHANTMENT.getOrEmpty(Identifier.tryParse(((NbtCompound) enc).getString("id"))).ifPresent(enchantment -> {
                    builder.enchant(enchantment, ((NbtCompound) enc).getInt("lvl"));
                });
            }
            tag.remove("Enchantments");
        }

        if (stack.getOrCreateTag().contains("HideFlags")) {
            builder.hideFlags(stack.getOrCreateTag().getByte("HideFlags"));
            tag.remove("HideFlags");
        }

        builder.tag = tag;

        return builder;
    }

    public static List<Text> getLore(ItemStack stack) {
        return stack.getOrCreateSubTag("display").getList("Lore", NbtType.STRING).stream().map(tag -> Text.Serializer.fromJson(tag.asString())).collect(Collectors.toList());
    }

    /**
     * Sets the type of Item of the element.
     *
     * @param item the item to use
     * @return this element builder
     */
    public GuiElementBuilder setItem(Item item) {
        this.item = item;
        return this;
    }

    /**
     * Sets the name of the element.
     *
     * @param name the name to use
     * @return this element builder
     */
    public GuiElementBuilder setName(MutableText name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the number of items in the element.
     *
     * @param count the number of items
     * @return this element builder
     */
    public GuiElementBuilder setCount(int count) {
        this.count = count;
        return this;
    }

    /**
     * Sets the lore lines of the element.
     *
     * @param lore a list of all the lore lines
     * @return this element builder
     */
    public GuiElementBuilder setLore(List<Text> lore) {
        this.lore = lore;
        return this;
    }

    /**
     * Adds a line of lore to the element.
     *
     * @param lore the line to add
     * @return this element builder
     */
    public GuiElementBuilder addLoreLine(Text lore) {
        this.lore.add(lore);
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
        this.damage = damage;
        return this;
    }

    /**
     * Hides all {@link net.minecraft.item.ItemStack.TooltipSection}s from the element display
     *
     * @return this element builder
     */
    public GuiElementBuilder hideFlags() {
        this.hideFlags = 127;
        return this;
    }

    /**
     * Hides a {@link net.minecraft.item.ItemStack.TooltipSection}
     * from the elements display.
     *
     * @param section the section to hide
     * @return this element builder
     */
    public GuiElementBuilder hideFlag(ItemStack.TooltipSection section) {
        this.hideFlags = (byte) (this.hideFlags | section.getFlag());
        return this;
    }

    /**
     * Set the {@link net.minecraft.item.ItemStack.TooltipSection}s to
     * hide from the elements display, by the flags.
     *
     * @param value the flags to hide
     * @return this element builder
     * @see GuiElementBuilder#hideFlag(ItemStack.TooltipSection)
     */
    public GuiElementBuilder hideFlags(byte value) {
        this.hideFlags = value;
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
        this.enchantments.put(enchantment, level);
        return this;
    }

    /**
     * Sets the element to have an enchantment glint.
     *
     * @return this element builder
     */
    public GuiElementBuilder glow() {
        this.enchantments.put(Enchantments.LUCK_OF_THE_SEA, 1);
        return hideFlag(ItemStack.TooltipSection.ENCHANTMENTS);
    }

    /**
     * Sets the custom model data of the element.
     *
     * @param value the value used for custom model data
     * @return this element builder
     */
    public GuiElementBuilder setCustomModelData(int value) {
        this.getOrCreateTag().putInt("CustomModelData", value);
        return this;
    }

    /**
     * Sets the element to be unbreakable, also hides the durability bar.
     *
     * @return this element builder
     */
    public GuiElementBuilder unbreakable() {
        this.getOrCreateTag().putBoolean("Unbreakable", true);
        return hideFlag(ItemStack.TooltipSection.UNBREAKABLE);
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
            profile = server.getSessionService().fillProfileProperties(profile, false);
            this.getOrCreateTag().put("SkullOwner", NbtHelper.writeGameProfile(new NbtCompound(), profile));
        } else {
            this.getOrCreateTag().putString("SkullOwner", profile.getName());
        }
        return this;
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
        NbtCompound skullOwner = new NbtCompound();
        NbtCompound properties = new NbtCompound();
        NbtCompound valueData = new NbtCompound();
        NbtList textures = new NbtList();

        valueData.putString("Value", value);
        if (signature != null) {
            valueData.putString("Signature", signature);
        }

        textures.add(valueData);
        properties.put("textures", textures);

        skullOwner.put("Id", NbtHelper.fromUuid(uuid != null ? uuid : Util.NIL_UUID));
        skullOwner.put("Properties", properties);
        this.getOrCreateTag().put("SkullOwner", skullOwner);

        return this;
    }

    @Override
    public GuiElementBuilder setCallback(GuiElement.ItemClickCallback callback) {
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
        ItemStack itemStack = new ItemStack(this.item, this.count);

        if (this.tag != null) {
            itemStack.getOrCreateTag().copyFrom(this.tag);
        }

        if (this.name != null) {
            if (this.name instanceof MutableText) {
                ((MutableText) this.name).styled(style -> style.withItalic(style.isItalic()));
            }
            itemStack.setCustomName(this.name);
        }

        if (this.item.isDamageable() && this.damage != -1) {
            itemStack.setDamage(damage);
        }

        for (Map.Entry<Enchantment, Integer> entry : this.enchantments.entrySet()) {
            itemStack.addEnchantment(entry.getKey(), entry.getValue());
        }

        if (this.lore.size() > 0) {
            NbtCompound display = itemStack.getOrCreateSubTag("display");
            NbtList loreItems = new NbtList();
            for (Text l : this.lore) {
                if (l instanceof MutableText) {
                    ((MutableText) l).styled(style -> style.withItalic(style.isItalic()));
                }
                loreItems.add(NbtString.of(Text.Serializer.toJson(l)));
            }
            display.put("Lore", loreItems);
        }

        if (this.hideFlags != 0) {
            itemStack.getOrCreateTag().putByte("HideFlags", this.hideFlags);
        }

        return itemStack;
    }

    protected NbtCompound getOrCreateTag() {
        if (this.tag == null) {
            this.tag = new NbtCompound();
        }
        return this.tag;
    }

    @Override
    public GuiElement build() {
        return new GuiElement(asStack(), this.callback);
    }
}
