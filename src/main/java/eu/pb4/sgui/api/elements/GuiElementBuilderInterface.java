package eu.pb4.sgui.api.elements;


public interface GuiElementBuilderInterface {
    GuiElementBuilderInterface setCallback(GuiElementInterface.ItemClickCallback callback);
    GuiElementInterface build();
}
