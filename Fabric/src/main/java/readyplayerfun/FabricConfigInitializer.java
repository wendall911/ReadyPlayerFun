package readyplayerfun;

import technology.roughness.whitenoise.config.WhiteNoiseConfigInitializer;

public class FabricConfigInitializer implements WhiteNoiseConfigInitializer {

    @Override
    public void onInitializeConfig() {
        ReadyPlayerFun.initConfig();
    }

}
