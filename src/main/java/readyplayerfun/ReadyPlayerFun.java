package readyplayerfun;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.DistExecutor;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import readyplayerfun.proxy.CommonProxy;
import readyplayerfun.proxy.ServerProxy;

@Mod(ReadyPlayerFun.MODID)
public class ReadyPlayerFun {

    public static final String MODID = "readyplayerfun";
    public static final Logger LOGGER = LogManager.getFormatterLogger(ReadyPlayerFun.MODID);

    public ReadyPlayerFun() {
        DistExecutor.safeRunForDist(() -> CommonProxy::new, () -> ServerProxy::new);
    }

}

