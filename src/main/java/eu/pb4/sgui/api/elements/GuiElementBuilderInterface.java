package eu.pb4.sgui.api.elements;

/**
 * This is the interface used by all gui element builders.
 *
 * @see GuiElementBuilder
 * @see AnimatedGuiElementBuilder
 * @see BookElementBuilder
 */
public interface GuiElementBuilderInterface<T extends GuiElementBuilderInterface<T>> {
    /**
     * Set the callback to execute when this element
     * is clicked inside a gui.
     *
     * @param callback the callback
     * @return this element builder
     */
    T setCallback(GuiElementInterface.ClickCallback callback);

    /**
     * Set the callback to execute when this element
     * is clicked inside a gui.
     *
     * @param callback the callback
     * @return this element builder
     */
    default T setCallback(GuiElementInterface.ItemClickCallback callback) {
        return this.setCallback((GuiElementInterface.ClickCallback) callback);
    }

    /**
     * Constructs the GuiElement with the values
     * from the builder.
     *
     * @return the built element
     */
    GuiElementInterface build();


}
