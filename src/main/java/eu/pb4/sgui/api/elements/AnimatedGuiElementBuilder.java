package eu.pb4.sgui.api.elements;

import com.mojang.authlib.GameProfile;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class AnimatedGuiElementBuilder implements GuiElementBuilderInterface {
    private Item item = Items.STONE;
    private NbtCompound tag;
    private int count = 1;
    private Text name = null;
    private List<Text> lore = new ArrayList<>();
    private int damage = -1;
    private GuiElement.ItemClickCallback callback = (index, type, action) -> {};
    private byte hideFlags = 0;
    private final Map<Enchantment, Integer> enchantments = new HashMap<>();

    private final List<ItemStack> itemStacks = new ArrayList<>();
    private int interval = 1;
    private boolean random = false;

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
        this.itemStacks.add(asStack());

        this.item = Items.STONE;
        this.tag = null;
        this.count = 1;
        this.name = null;
        this.lore = new ArrayList<>();
        this.damage = -1;
        this.hideFlags = 0;
        this.enchantments.clear();

        return this;
    }

    /**
     * Sets the type of Item of the element.
     *
     * @param item the item to use
     * @return this element builder
     */
    public AnimatedGuiElementBuilder setItem(Item item) {
        this.item = item;
        return this;
    }

    /**
     * Sets the name of the element.
     *
     * @param name the name to use
     * @return this element builder
     */
    public AnimatedGuiElementBuilder setName(MutableText name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the number of items in the element.
     *
     * @param count the number of items
     * @return this element builder
     */
    public AnimatedGuiElementBuilder setCount(int count) {
        this.count = count;
        return this;
    }

    /**
     * Sets the lore lines of the element.
     *
     * @param lore a list of all the lore lines
     * @return this element builder
     */
    public AnimatedGuiElementBuilder setLore(List<Text> lore) {
        this.lore = lore;
        return this;
    }

    /**
     * Adds a line of lore to the element.
     *
     * @param lore the line to add
     * @return this element builder
     */
    public AnimatedGuiElementBuilder addLoreLine(Text lore) {
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
    public AnimatedGuiElementBuilder setDamage(int damage) {
        this.damage = damage;
        return this;
    }

    /**
     * Hide all {@link net.minecraft.item.ItemStack.TooltipSection}s from the element display
     *
     * @return this element builder
     */
    public AnimatedGuiElementBuilder hideFlags() {
        this.hideFlags = 127;
        return this;
    }

    /**
     * Hide a {@link net.minecraft.item.ItemStack.TooltipSection}
     * from the elements display.
     *
     * @param section the section to hide
     * @return this element builder
     */
    public AnimatedGuiElementBuilder hideFlag(ItemStack.TooltipSection section) {
        this.hideFlags = (byte) (this.hideFlags | section.getFlag());
        return this;
    }

    /**
     * Set the {@link net.minecraft.item.ItemStack.TooltipSection}s to
     * hide from the elements display, by the flags.
     *
     * @param value the flags to hide
     * @return this element builder
     * @see AnimatedGuiElementBuilder#hideFlag(ItemStack.TooltipSection)
     */
    public AnimatedGuiElementBuilder hideFlags(byte value) {
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
    public AnimatedGuiElementBuilder enchant(Enchantment enchantment, int level) {
        this.enchantments.put(enchantment, level);
        return this;
    }

    /**
     * Sets the element to have an enchantment glint.
     *
     * @return this element builder
     */
    public AnimatedGuiElementBuilder glow() {
        this.enchantments.put(Enchantments.LUCK_OF_THE_SEA, 1);
        return hideFlag(ItemStack.TooltipSection.ENCHANTMENTS);
    }

    /**
     * Sets the custom model data of the element.
     *
     * @param value the value used for custom model data
     * @return this element builder
     */
    public AnimatedGuiElementBuilder setCustomModelData(int value) {
        this.getOrCreateTag().putInt("CustomModelData", value);
        return this;
    }

    /**
     * Sets the element to be unbreakable, also hides the durability bar.
     *
     * @return this element builder
     */
    public AnimatedGuiElementBuilder unbreakable() {
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
    public AnimatedGuiElementBuilder setSkullOwner(GameProfile profile, @Nullable MinecraftServer server) {
        if (profile.getId() != null && server != null) {
            profile = server.getSessionService().fillProfileProperties(profile, false);
            this.getOrCreateTag().put("SkullOwner", NbtHelper.writeGameProfile(new NbtCompound(), profile));
        } else {
            this.getOrCreateTag().putString("SkullOwner", profile.getName());
        }
        return this;
    }

    @Override
    public AnimatedGuiElementBuilder setCallback(GuiElement.ItemClickCallback callback) {
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

    private NbtCompound getOrCreateTag() {
        if (this.tag == null) {
            this.tag = new NbtCompound();
        }
        return this.tag;
    }

    public AnimatedGuiElement build() {
        return new AnimatedGuiElement(this.itemStacks.toArray(new ItemStack[0]), this.interval, this.random, this.callback);
    }
}
