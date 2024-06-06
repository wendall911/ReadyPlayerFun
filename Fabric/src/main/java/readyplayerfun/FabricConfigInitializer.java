package readyplayerfun;

import com.illusivesoulworks.spectrelib.config.SpectreConfigInitializer;

public class FabricConfigInitializer implements SpectreConfigInitializer {

    @Override
    public void onInitializeConfig() {
        ReadyPlayerFun.init();
    }

}
