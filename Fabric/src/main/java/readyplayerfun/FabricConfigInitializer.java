package readyplayerfun;

import com.illusivesoulworks.spectrelib.config.SpectreLibInitializer;

public class FabricConfigInitializer implements SpectreLibInitializer {

    @Override
    public void onInitializeConfig() {
        ReadyPlayerFun.init();
    }

}
