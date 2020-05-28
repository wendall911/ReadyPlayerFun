package readyplayerfun;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.ModLoadingContext;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import readyplayerfun.config.ConfigHandler;
import readyplayerfun.event.PlayerEventHandler;

@Mod(ReadyPlayerFun.MODID)
public class ReadyPlayerFun {

    public static final String MODID = "readyplayerfun";

    public static final Logger logger = LogManager.getFormatterLogger(ReadyPlayerFun.MODID);

    public ReadyPlayerFun() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ConfigHandler.SERVER_SPECS);

        MinecraftForge.EVENT_BUS.register(new PlayerEventHandler());
    }

}

