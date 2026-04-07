package readyplayerfun.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;

public class ServerEventListener {

    public static void setup() {
        ServerLevelEvents.LOAD.register((server, world) -> ServerEventHander.levelLoad(world));
        ServerLifecycleEvents.SERVER_STOPPING.register(ServerEventHander::serverStopping);
        ServerTickEvents.END_LEVEL_TICK.register(ServerEventHander::levelPostTick);
    }

}
