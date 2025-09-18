package readyplayerfun;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import technology.roughness.whitenoise.config.WhiteNoiseConfig;
import technology.roughness.whitenoise.config.WhiteNoiseConfigLoader;

import readyplayerfun.config.ConfigHandler;

public class ReadyPlayerFun {

    public static final String MODID = "readyplayerfun";
    public static final String MOD_NAME = "Ready Player Fun";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
   
    public static void initConfig() {
        WhiteNoiseConfig commonConfig = WhiteNoiseConfigLoader.add(WhiteNoiseConfig.Type.COMMON, ConfigHandler.COMMON_SPEC, MODID);
        commonConfig.addLoadListener((config, flag) -> ConfigHandler.init());
    }

}
