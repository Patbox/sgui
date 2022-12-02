package eu.pb4.sgui.api.elements;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;

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

    /**
     * Adds a new page to the book. <br>
     * Note that only signed books support formatting
     *
     * @param lines an array of lines, they will also wrap automatically to fit to the screen
     * @return this book builder
     * @see BookElementBuilder#setPage(int, Text...)
     */
    public BookElementBuilder addPage(Text... lines) {
        var text = Text.literal("");
        for (Text line : lines) {
            text.append(line).append("\n");
        }
        this.getOrCreatePages().add(NbtString.of(Text.Serializer.toJson(text)));
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
        var text = Text.literal("");
        for (Text line : lines) {
            text.append(line).append("\n");
        }
        this.getOrCreatePages().set(index, NbtString.of(Text.Serializer.toJson(text)));
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
        this.getOrCreateNbt().put("author", NbtString.of(author));
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
        this.getOrCreateNbt().put("title", NbtString.of(title));
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

    protected NbtList getOrCreatePages() {
        if (!this.getOrCreateNbt().contains("pages")) {
            this.getOrCreateNbt().put("pages", new NbtList());
        }
        return this.getOrCreateNbt().getList("pages", NbtElement.STRING_TYPE);
    }

    @Override
    public GuiElementBuilder setItem(Item item) {
        if (!(item.getRegistryEntry().isIn(ItemTags.LECTERN_BOOKS))) {
            throw new IllegalArgumentException("Item must be a type of book");
        }

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
        if (this.item == Items.WRITTEN_BOOK) {
            if (!this.getOrCreateNbt().contains("author")) {
                this.getOrCreateNbt().put("author", NbtString.of(""));
            }
            if (!this.getOrCreateNbt().contains("title")) {
                this.getOrCreateNbt().put("title", NbtString.of(""));
            }
        } else if (this.item == Items.WRITABLE_BOOK){
            NbtList pages = this.getOrCreatePages();
            for (int i = 0; i < pages.size(); i++) {
                try {
                    pages.set(i, NbtString.of(Text.Serializer.fromLenientJson(pages.getString(i)).getString()));
                } catch (Exception e) {
                    pages.set(i, NbtString.of("Invalid page data!"));
                }
            }
            this.getOrCreateNbt().put("pages", pages);

            this.getOrCreateNbt().remove("author");
            this.getOrCreateNbt().remove("title");
        }

        return super.asStack();
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

        BookElementBuilder builder = new BookElementBuilder(book.getCount());

        if (book.getOrCreateNbt().contains("title")) {
            builder.setTitle(book.getOrCreateNbt().getString("title"));
        }

        if (book.getOrCreateNbt().contains("author")) {
            builder.setTitle(book.getOrCreateNbt().getString("author"));
        }

        if (book.getOrCreateNbt().contains("pages")) {
            NbtList pages = book.getOrCreateNbt().getList("pages", NbtElement.STRING_TYPE);
            for (NbtElement page : pages) {
                builder.addPage(Text.Serializer.fromLenientJson(page.asString()));
            }
        }

        return builder;
    }

    /**
     * Returns the contents of the specified page.
     *
     * @param book  the book to get the page from
     * @param index the page index, from 0
     * @return the contents of the page or empty if page does not exist
     * @throws IllegalArgumentException if the item is not a book
     */
    public static Text getPageContents(ItemStack book, int index) {
        if (!book.getItem().getRegistryEntry().isIn(ItemTags.LECTERN_BOOKS)) {
            throw new IllegalArgumentException("Item must be a type of book");
        }

        if (book.getOrCreateNbt().contains("pages")) {
            NbtList pages = book.getOrCreateNbt().getList("pages", NbtElement.STRING_TYPE);
            if(index < pages.size()) {
                return Text.Serializer.fromJson(pages.get(index).asString());
            }
        }

        return Text.literal("");
    }

    /**
     * Returns the contents of the specified page.
     *
     * @param book  the book element builder to get the page from
     * @param index the page index, from 0
     * @return the contents of the page or empty if page does not exist
     */
    public static Text getPageContents(BookElementBuilder book, int index) {
        NbtList pages = book.getOrCreatePages();
        if(index < pages.size()) {
            return Text.Serializer.fromJson(pages.get(index).asString());
        }
        return Text.literal("");
    }

}
