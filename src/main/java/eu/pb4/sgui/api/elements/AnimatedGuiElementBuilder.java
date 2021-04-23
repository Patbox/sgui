package eu.pb4.sgui.api.elements;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnimatedGuiElementBuilder implements GuiElementBuilderInterface {
    private Item item = Items.STONE;
    private int count = 1;
    private Text name = null;
    private List<Text> lore = new ArrayList<>();
    private GuiElement.ItemClickCallback callback = (index, type, action) -> {};
    private byte hideFlags = 0;
    private Map<Enchantment, Integer> enchantments = new HashMap<>();
    private int customModelData = -1;
    private List<ItemStack> itemStacks = new ArrayList<>();
    private int interval = 1;
    private boolean random = false;

    public AnimatedGuiElementBuilder() {}

    public AnimatedGuiElementBuilder setInterval(int interval) {
        this.interval = interval;
        return this;
    }

    public AnimatedGuiElementBuilder setRandom(boolean value) {
        this.random = value;
        return this;
    }

    public AnimatedGuiElementBuilder setItem(Item item) {
        this.item = item;
        return this;
    }

    public AnimatedGuiElementBuilder setName(Text name) {
        this.name = name;
        return this;
    }

    public AnimatedGuiElementBuilder setCount(int count) {
        this.count = count;
        return this;
    }

    public AnimatedGuiElementBuilder setCustomModelData(int value) {
        this.customModelData = value;
        return this;
    }

    public AnimatedGuiElementBuilder setLore(List<Text> lore) {
        this.lore = lore;
        return this;
    }

    public AnimatedGuiElementBuilder addLoreLine(Text lore) {
        this.lore.add(lore);
        return this;
    }

    public AnimatedGuiElementBuilder setCallback(GuiElement.ItemClickCallback callback) {
        this.callback = callback;
        return this;
    }

    public AnimatedGuiElementBuilder hideFlags() {
        this.hideFlags = 64;
        return this;
    }

    public AnimatedGuiElementBuilder hideFlags(byte value) {
        this.hideFlags = value;
        return this;
    }

    public AnimatedGuiElementBuilder enchant(Enchantment enchantment, int level) {
        this.enchantments.put(enchantment, level);
        return this;
    }

    public AnimatedGuiElementBuilder glow() {
        this.enchantments.put(Enchantments.LUCK_OF_THE_SEA, 1);
        this.hideFlags = (byte) (this.hideFlags | 0x01);
        return this;
    }

    public AnimatedGuiElementBuilder saveItemStack() {
        ItemStack itemStack = new ItemStack(this.item, this.count);
        if (this.name != null) {
            itemStack.setCustomName(this.name);
        }

        for (Map.Entry<Enchantment, Integer> entry : this.enchantments.entrySet()) {
            itemStack.addEnchantment(entry.getKey(), entry.getValue());
        }

        if (this.lore.size() > 0) {
            CompoundTag display = itemStack.getOrCreateSubTag("display");
            ListTag loreItems = new ListTag();
            for (Text l : this.lore) {
                loreItems.add(StringTag.of(Text.Serializer.toJson(l)));
            }
            display.put("Lore", loreItems);
        }

        itemStack.getOrCreateTag().putByte("HideFlags", this.hideFlags);
        if (this.customModelData != -1) {
            itemStack.getOrCreateTag().putInt("CustomModelData", this.customModelData);
        }

        this.itemStacks.add(itemStack);

        this.item = Items.STONE;
        this.count = 1;
        this.name = null;
        this.lore = new ArrayList<>();
        this.hideFlags = 0;
        this.enchantments = new HashMap<>();
        this.customModelData = -1;
        return this;
    }

    public AnimatedGuiElement build() {
        return new AnimatedGuiElement(this.itemStacks.toArray(new ItemStack[0]), this.interval, this.random, this.callback);
    }
}
