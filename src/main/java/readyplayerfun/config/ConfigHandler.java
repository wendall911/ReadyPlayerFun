package readyplayerfun.config;

import net.minecraftforge.common.config.Config;

import readyplayerfun.ReadyPlayerFun;

@Config(modid=ReadyPlayerFun.MODID)
public class ConfigHandler {

    public static Server server;
    public static class Server {

        @Config.Comment({"Show status message on first player login after server unpaused."})
        public static boolean ENABLE_WELCOME_MESSAGE = true;

    }

}
