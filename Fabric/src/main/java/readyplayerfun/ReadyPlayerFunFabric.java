package readyplayerfun;

import net.fabricmc.api.ModInitializer;

import readyplayerfun.event.ServerEventListener;

public class ReadyPlayerFunFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ServerEventListener.setup();
    }

}
