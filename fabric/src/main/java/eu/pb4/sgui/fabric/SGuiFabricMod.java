package eu.pb4.sgui.fabric;

import eu.pb4.sgui.SGuiMod;
import net.fabricmc.api.ModInitializer;

public class SGuiFabricMod implements ModInitializer {

    @Override
    public void onInitialize(){
        SGuiMod.initialize();
    }
}