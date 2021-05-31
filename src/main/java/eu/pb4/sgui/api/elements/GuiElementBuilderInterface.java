package eu.pb4.sgui.api.elements;

/**
 * This is the interface used by all gui element builders.
 *
 * @see GuiElementBuilder
 * @see AnimatedGuiElementBuilder
 * @see BookElementBuilder
 */
public interface GuiElementBuilderInterface {

    /**
     * Set the callback to execute when this element
     * is clicked inside a gui.
     *
     * @param callback the callback
     * @return this element builder
     */
    GuiElementBuilderInterface setCallback(GuiElementInterface.ItemClickCallback callback);

    /**
     * Constructs the GuiElement with the values
     * from the builder.
     *
     * @return the built element
     */
    GuiElementInterface build();
}
