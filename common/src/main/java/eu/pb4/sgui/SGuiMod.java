package eu.pb4.sgui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SGuiMod {
    public static final String MOD_ID = "sgui";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);


    public static void initialize() {
        LOGGER.info("Server GUI (SGui) loaded!");
    }
}