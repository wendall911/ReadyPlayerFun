package readyplayerfun.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;

public class ServerEventListener {

    public static void setup() {
        ServerWorldEvents.LOAD.register((server, world) -> ServerEventHander.levelLoad(world));
        ServerLifecycleEvents.SERVER_STOPPING.register(ServerEventHander::serverStopping);
        ServerTickEvents.END_WORLD_TICK.register(ServerEventHander::levelPostTick);
    }

}
