package eu.pb4.sgui.api.elements;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Text;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Book Element Builder
 * <br>
 * This is an GuiElementBuilder specifically designed for manipulating books.
 * Along with general manipulation from the GuiElementBuilder, it also
 * supplies multiple methods for manipulating pages, author, title, ect.
 *
 * @see GuiElementBuilderInterface
 */
@SuppressWarnings({"unused"})
public class BookElementBuilder extends GuiElementBuilder {

    private static final WrittenBookContentComponent DEFAULT_WRITTEN_COMPONENT = new WrittenBookContentComponent(RawFilteredPair.of(""), "", 0, Collections.emptyList(), false);

    /**
     * Constructs a new BookElementBuilder with the default settings.
     */
    public BookElementBuilder() {
        super(Items.WRITTEN_BOOK);
    }

    /**
     * Constructs a new BookElementBuilder with the supplied number
     * of items.
     *
     * @param count the number of items in the element
     */
    public BookElementBuilder(int count) {
        super(Items.WRITTEN_BOOK, count);
    }

    private BookElementBuilder(ItemStack stack) {
        super(stack);
    }

    /**
     * Adds a new page to the book. <br>
     * Note that only signed books support formatting
     *
     * @param lines an array of lines, they will also wrap automatically to fit to the screen
     * @return this book builder
     * @see BookElementBuilder#setPage(int, Text...)
     */
    public BookElementBuilder addPage(Text... lines) {
        this.itemStack.apply(DataComponentTypes.WRITTEN_BOOK_CONTENT, DEFAULT_WRITTEN_COMPONENT, original -> {
            List<RawFilteredPair<Text>> updatedPages = new LinkedList<>(original.pages());
            var text = Text.empty();
            for (Text line : lines) {
                text.append(line).append("\n");
            }
            updatedPages.add(RawFilteredPair.of(text));
            return new WrittenBookContentComponent(original.title(), original.author(), original.generation(), updatedPages, original.resolved());
        });
        return this;
    }

    public BookElementBuilder addPage(Text text) {
        this.itemStack.apply(DataComponentTypes.WRITTEN_BOOK_CONTENT, DEFAULT_WRITTEN_COMPONENT, original -> {
            List<RawFilteredPair<Text>> updatedPages = new LinkedList<>(original.pages());
            updatedPages.add(RawFilteredPair.of(text));
            return new WrittenBookContentComponent(original.title(), original.author(), original.generation(), updatedPages, original.resolved());
        });
        return this;
    }

    /**
     * Sets a page of the book. <br>
     * Note that only signed books support formatting
     *
     * @param index the page index, from 0
     * @param lines an array of lines, they will also wrap automatically to fit to the screen
     * @return this book builder
     * @throws IndexOutOfBoundsException if the page has not been created
     * @see BookElementBuilder#addPage(Text...)
     */
    public BookElementBuilder setPage(int index, Text... lines) {
        this.itemStack.apply(DataComponentTypes.WRITTEN_BOOK_CONTENT, DEFAULT_WRITTEN_COMPONENT, original -> {
            List<RawFilteredPair<Text>> updatedPages = new LinkedList<>(original.pages());
            var text = Text.empty();
            for (Text line : lines) {
                text.append(line).append("\n");
            }
            updatedPages.set(index, RawFilteredPair.of(text));
            return new WrittenBookContentComponent(original.title(), original.author(), original.generation(), updatedPages, original.resolved());
        });
        return this;
    }

    public BookElementBuilder setPage(int index, Text text) {
        this.itemStack.apply(DataComponentTypes.WRITTEN_BOOK_CONTENT, DEFAULT_WRITTEN_COMPONENT, original -> {
            List<RawFilteredPair<Text>> updatedPages = new LinkedList<>(original.pages());
            updatedPages.set(index, RawFilteredPair.of(text));
            return new WrittenBookContentComponent(original.title(), original.author(), original.generation(), updatedPages, original.resolved());
        });
        return this;
    }

    /**
     * Sets the author of the book, also marks
     * the book as signed.
     *
     * @param author the authors name
     * @return this book builder
     */
    public BookElementBuilder setAuthor(String author) {
        this.itemStack.apply(DataComponentTypes.WRITTEN_BOOK_CONTENT, DEFAULT_WRITTEN_COMPONENT, original -> {
            return new WrittenBookContentComponent(original.title(), author, original.generation(), original.pages(), original.resolved());
        });
        this.signed();
        return this;
    }

    /**
     * Sets the title of the book, also marks
     * the book as signed.
     *
     * @param title the book title
     * @return this book builder
     */
    public BookElementBuilder setTitle(String title) {
        this.itemStack.apply(DataComponentTypes.WRITTEN_BOOK_CONTENT, DEFAULT_WRITTEN_COMPONENT, original -> {
            return new WrittenBookContentComponent(RawFilteredPair.of(title), original.author(), original.generation(), original.pages(), original.resolved());
        });
        this.signed();
        return this;
    }

    /**
     * Sets the book to be signed, not necessary
     * if already using setTitle or setAuthor.
     *
     * @return this book builder
     * @see BookElementBuilder#unSigned()
     */
    public BookElementBuilder signed() {
        this.setItem(Items.WRITTEN_BOOK);
        return this;
    }

    /**
     * Sets the book to not be signed, this will
     * also remove the title and author on
     * stack creation.
     *
     * @return this book builder
     * @see BookElementBuilder#signed() 
     */
    public BookElementBuilder unSigned() {
        this.setItem(Items.WRITABLE_BOOK);
        return this;
    }

    @Override
    public GuiElementBuilder setItem(Item item) {
        return super.setItem(item);
    }

    /**
     * Only written books may have formatting, thus if the book is not marked as signed,
     * we must strip the formatting. To sign a book use the {@link BookElementBuilder#setTitle(String)}
     * or {@link BookElementBuilder#setAuthor(String)} methods.
     *
     * @return the book as a stack
     */
    @Override
    public ItemStack asStack() {
        if (!itemStack.contains(DataComponentTypes.WRITTEN_BOOK_CONTENT)) {
            itemStack.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, DEFAULT_WRITTEN_COMPONENT);
        }
        return this.itemStack.copy();
    }

    /**
     * Constructs BookElementBuilder based on the supplied book.
     * Useful for making changes to existing books.
     * <br>
     * The method will check for the existence of a 'title'
     * and 'author' tag, if either is found it will assume
     * the book has been signed. This can be undone
     * with the {@link BookElementBuilder#unSigned()}.
     *
     *
     * @param book the target book stack
     * @return the builder
     * @throws IllegalArgumentException if the stack is not a book
     */
    public static BookElementBuilder from(ItemStack book) {
        if (!book.getItem().getRegistryEntry().isIn(ItemTags.LECTERN_BOOKS)) {
            throw new IllegalArgumentException("Item must be a type of book");
        }
        return new BookElementBuilder(book);
    }

    /**
     * Returns the contents of the specified page.
     *
     * @param book  the book to get the page from
     * @param index the page index, from 0
     * @return the contents of the page or empty if page does not exist
     * @throws IllegalArgumentException if the item is not a book
     */
    @Deprecated
    public static Text getPageContents(ItemStack book, int index) {
        WrittenBookContentComponent component = book.getOrDefault(DataComponentTypes.WRITTEN_BOOK_CONTENT, DEFAULT_WRITTEN_COMPONENT);
        if (index < component.pages().size()) {
            return component.pages().get(index).raw();
        }
        return Text.empty();
    }

    /**
     * Returns the contents of the specified page.
     *
     * @param book  the book element builder to get the page from
     * @param index the page index, from 0
     * @return the contents of the page or empty if page does not exist
     */
    public static Text getPageContents(BookElementBuilder book, int index) {
        return getPageContents(book.itemStack, index);
    }

}
