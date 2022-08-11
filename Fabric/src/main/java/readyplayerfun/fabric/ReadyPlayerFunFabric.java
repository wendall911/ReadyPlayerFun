package readyplayerfun.fabric;

import net.fabricmc.api.ModInitializer;

import readyplayerfun.ReadyPlayerFun;
import readyplayerfun.fabric.config.ConfigHandler;

public class ReadyPlayerFunFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ReadyPlayerFun.init();
        ConfigHandler.init();
    }

}
