package readyplayerfun;

import com.illusivesoulworks.spectrelib.config.SpectreConfig;
import com.illusivesoulworks.spectrelib.config.SpectreConfigLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import readyplayerfun.config.ConfigHandler;

public class ReadyPlayerFun {

    public static final String MODID = "readyplayerfun";
    public static final String MOD_NAME = "Ready Player Fun";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
   
    public static void init() {
        SpectreConfig commonConfig = SpectreConfigLoader.add(SpectreConfig.Type.COMMON, ConfigHandler.COMMON_SPEC, MODID);
        commonConfig.addLoadListener(config -> ConfigHandler.init());
    }

}
