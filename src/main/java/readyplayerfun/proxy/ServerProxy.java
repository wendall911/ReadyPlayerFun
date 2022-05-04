package readyplayerfun.proxy;

import net.minecraftforge.common.MinecraftForge;

import readyplayerfun.config.ConfigHandler;
import readyplayerfun.event.ServerEventHandler;

public class ServerProxy {

    public ServerProxy() {
        MinecraftForge.EVENT_BUS.register(ServerEventHandler.INSTANCE);
        ConfigHandler.initServer();
    }

}
