package readyplayerfun.fabric;

import net.fabricmc.api.ModInitializer;

import readyplayerfun.ReadyPlayerFun;

public class ReadyPlayerFunFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ReadyPlayerFun.init();
    }

}
