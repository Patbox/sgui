package eu.pb4.sgui.api.elements;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.tag.ItemTags;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

/**
 * Book Element Builder
 * <br>
 * This is an GuiElementBuilder specifically designed for manipulating books.
 * Along with general manipulation from the GuiElementBuilder, it also
 * supplies multiple methods for manipulating pages, author, title, ect.
 */
public class BookElementBuilder extends GuiElementBuilder {

    /**
     * Constructs a new BookElementBuilder with the default settings.
     */
    public BookElementBuilder() {
        super(Items.WRITABLE_BOOK);
    }

    /**
     * Constructs a new BookElementBuilder with the supplied number
     * of items.
     *
     * @param count the number of items in the element
     */
    public BookElementBuilder(int count) {
        super(Items.WRITABLE_BOOK, count);
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
        LiteralText text = new LiteralText("");
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
        LiteralText text = new LiteralText("");
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
        this.getOrCreateTag().put("author", NbtString.of(author));
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
        this.getOrCreateTag().put("title", NbtString.of(title));
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
        if (!this.getOrCreateTag().contains("pages")) {
            this.getOrCreateTag().put("pages", new NbtList());
        }
        return this.getOrCreateTag().getList("pages", NbtElement.STRING_TYPE);
    }

    @Override
    public GuiElementBuilder setItem(Item item) {
        if (!ItemTags.LECTERN_BOOKS.contains(item)) {
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
            if (!this.getOrCreateTag().contains("author")) {
                this.getOrCreateTag().put("author", NbtString.of(""));
            }
            if (!this.getOrCreateTag().contains("title")) {
                this.getOrCreateTag().put("title", NbtString.of(""));
            }
        } else if (this.item == Items.WRITABLE_BOOK){
            NbtList pages = this.getOrCreatePages();
            for (int i = 0; i < pages.size(); i++) {
                pages.set(i, NbtString.of(Text.Serializer.fromLenientJson(pages.getString(i)).getString()));
            }
            this.getOrCreateTag().put("pages", pages);

            this.getOrCreateTag().remove("author");
            this.getOrCreateTag().remove("title");
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
        if (!ItemTags.LECTERN_BOOKS.contains(book.getItem())) {
            throw new IllegalArgumentException("Item must be a type of book");
        }

        BookElementBuilder builder = new BookElementBuilder(book.getCount());

        if (book.getOrCreateTag().contains("title")) {
            builder.setTitle(book.getOrCreateTag().getString("title"));
        }

        if (book.getOrCreateTag().contains("author")) {
            builder.setTitle(book.getOrCreateTag().getString("author"));
        }

        if (book.getOrCreateTag().contains("pages")) {
            NbtList pages = book.getOrCreateTag().getList("pages", NbtElement.STRING_TYPE);
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
        if (!ItemTags.LECTERN_BOOKS.contains(book.getItem())) {
            throw new IllegalArgumentException("Item must be a type of book");
        }

        if (book.getOrCreateTag().contains("pages")) {
            NbtList pages = book.getOrCreateTag().getList("pages", NbtElement.STRING_TYPE);
            if(index < pages.size()) {
                return Text.Serializer.fromJson(pages.get(index).asString());
            }
        }

        return new LiteralText("");
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
        return new LiteralText("");
    }

}
