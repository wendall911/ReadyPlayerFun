package readyplayerfun.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class ServerEventListener {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onCommand(CommandEvent event) {
        ServerEventHander.onCommand(event.getParseResults());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();

        if (!player.level().isClientSide()) {
            ServerPlayer sp = (ServerPlayer) player;

            ServerEventHander.onPlayerJoin(sp);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();

        if (!player.level().isClientSide()) {
            ServerPlayer sp = (ServerPlayer) player;

            ServerEventHander.onPlayerLogout(sp);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void levelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel) {
            ServerLevel world = (ServerLevel) event.getLevel();
            
            ServerEventHander.levelLoad(world);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void serverStopping(ServerStoppingEvent event) {
        ServerEventHander.serverStopping(event.getServer());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void levelPostTick(ServerTickEvent.Post event) {
        event.getServer().getAllLevels().forEach(ServerEventHander::levelPostTick);
    }

}
