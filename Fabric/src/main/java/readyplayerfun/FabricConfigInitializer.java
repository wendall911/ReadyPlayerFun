package readyplayerfun;

import technology.roughness.whitenoise.config.WhiteNoiseInitializer;

public class FabricConfigInitializer implements WhiteNoiseInitializer {

    @Override
    public void onInitializeConfig() {
        ReadyPlayerFun.init();
    }

}
