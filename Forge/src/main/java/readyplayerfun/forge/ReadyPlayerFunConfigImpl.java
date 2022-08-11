package readyplayerfun.forge;

import readyplayerfun.forge.config.ConfigHandler;

public class ReadyPlayerFunConfigImpl {

    public static boolean enableWelcomeMessage() {
        return ConfigHandler.Common.ENABLE_WELCOME_MESSAGE.get();
    }

    public static boolean forceGameRules() {
        return ConfigHandler.Server.FORCE_GAME_RULES.get();
    }

    public static boolean doFireTick() {
        return ConfigHandler.Server.DO_FIRE_TICK.get();
    }

    public static int randomTickSpeed() {
        return ConfigHandler.Server.RANDOM_TICK_SPEED.get();
    }

}
