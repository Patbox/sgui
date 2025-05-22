package eu.pb4.sgui.neoforge;

import eu.pb4.sgui.SGuiMod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import static eu.pb4.sgui.SGuiMod.LOGGER;

@Mod(SGuiMod.MOD_ID)
public class SGuiNeoForgeMod {

    public SGuiNeoForgeMod(IEventBus modEventBus, ModContainer modContainer) {
        SGuiMod.initialize();
        NeoForge.EVENT_BUS.register(this);

    }

    @SubscribeEvent
    public void onServerStart(ServerStartedEvent event) {
        LOGGER.info("Server GUI (SGui) started!");
    }
}