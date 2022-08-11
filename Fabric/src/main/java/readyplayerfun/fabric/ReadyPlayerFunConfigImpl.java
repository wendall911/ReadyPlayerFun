package readyplayerfun.fabric;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

import readyplayerfun.ReadyPlayerFunConfig;
import readyplayerfun.ReadyPlayerFun;
import readyplayerfun.fabric.config.ConfigHandler;

@Config(name = ReadyPlayerFun.MODID)
public class ReadyPlayerFunConfigImpl implements ConfigData {

    @Comment(ReadyPlayerFunConfig.WELCOME_MESSAGE)
    boolean enableWelcomeMessage = true;

    @Comment(ReadyPlayerFunConfig.FORCE_GAME_RULES)
    boolean forceGameRules = false;

    @Comment("doFireTick")
    boolean doFireTick = true;

    @Comment("randomTickSpeed")
    int randomTickSpeed = 3;

    public static boolean enableWelcomeMessage() {
        return ConfigHandler.config.enableWelcomeMessage;
    }

    public static boolean forceGameRules() {
        return ConfigHandler.config.forceGameRules;
    }

    public static boolean doFireTick() {
        return ConfigHandler.config.doFireTick;
    }

    public static int randomTickSpeed() {
        return ConfigHandler.config.randomTickSpeed;
    }

}
