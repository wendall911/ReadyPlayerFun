package readyplayerfun;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import readyplayerfun.config.ConfigHandler;
import readyplayerfun.proxy.CommonProxy;
import readyplayerfun.proxy.ServerProxy;

@Mod(ReadyPlayerFun.MODID)
public class ReadyPlayerFun {

    public static final String MODID = "readyplayerfun";

    public static final Logger logger = LogManager.getFormatterLogger(ReadyPlayerFun.MODID);

    public ReadyPlayerFun() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ConfigHandler.SERVER_SPECS);

        DistExecutor.runForDist(() -> CommonProxy::new, () -> ServerProxy::new);
    }

}

