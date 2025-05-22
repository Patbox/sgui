package eu.pb4.sgui.testmod;

import eu.pb4.sgui.api.elements.BookElementBuilder;
import eu.pb4.sgui.api.gui.BookGui;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;


public class TypewriterGui extends BookGui {
    private static final String[] KEYBOARD = {
            "QWERTYUIOP",
            "ASDFGHJKL",
            "ZXCVBNM,./"
    };

    private static final int MAX_LINE = 19;
    private static final int LINE = 15 - 6;
    private static final int MAX_LENGTH = LINE * MAX_LINE;

    private String word = "";

    public TypewriterGui(ServerPlayer player) {
        super(player);
        updateText();

        this.open();
    }

    private void updateText() {
        var text = Component.empty();

        int line = 0;
        for (int i = 0; i < this.word.length(); i += MAX_LINE) {
            text.append(this.word.substring(i, Math.min(i + MAX_LINE, this.word.length())) + '\n');
            line++;
        }
        text.append("\n".repeat(LINE - line));

        for (var kl : KEYBOARD) {
            for (int i = 0; i < kl.length(); i++) {
                var chr = kl.charAt(i);
                text.append(Component.literal("" + chr).setStyle(
                        Style.EMPTY.withClickEvent(new ClickEvent.ChangePage (1000 + chr))));
                text.append(" ");
                line++;
            }
            text.append("\n");
        }
        text.append("   ");
        text.append(Component.literal("[SPACE]").setStyle(
                Style.EMPTY.withClickEvent(new ClickEvent.ChangePage (1000 + ' '))));
        text.append("   ");
        text.append(Component.literal("[<---]").setStyle(
                Style.EMPTY.withClickEvent(new ClickEvent.ChangePage(100))));

        var book = new BookElementBuilder();
        book.addPage(text);
        this.setBook(book);
    }

    @Override
    public void onPageSelected(int page) {
        if (page == 100 && !this.word.isEmpty()) {
            this.word = this.word.substring(0, this.word.length() - 1);
        } else if (page >= 1000) {
            char chr = (char) (page - 1000);
            if (this.word.length() < MAX_LENGTH) {
                this.word += chr;
            }
        }
        this.updateText();
    }
}
