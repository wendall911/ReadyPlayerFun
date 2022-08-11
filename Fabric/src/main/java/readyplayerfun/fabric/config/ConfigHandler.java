package readyplayerfun.fabric.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;

import readyplayerfun.fabric.ReadyPlayerFunConfigImpl;

public class ConfigHandler {

    public static ReadyPlayerFunConfigImpl config;

    public static void init() {
        AutoConfig.register(ReadyPlayerFunConfigImpl.class, JanksonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(ReadyPlayerFunConfigImpl.class).getConfig();
    }

}
