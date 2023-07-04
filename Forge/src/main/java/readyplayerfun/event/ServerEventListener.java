package readyplayerfun.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

public class ServerEventListener {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onCommand(CommandEvent event) {
        ServerEventHander.onCommand(event.getParseResults());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity() != null ? event.getEntity() : null;

        if (player != null && !player.getLevel().isClientSide()) {
            ServerPlayer sp = (ServerPlayer) player;

            ServerEventHander.onPlayerJoin(sp);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity() != null ? event.getEntity() : null;

        if (player != null && !player.getLevel().isClientSide()) {
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
    public void levelPostTick(TickEvent.LevelTickEvent event) {
        if (event.side == LogicalSide.SERVER && event.phase == TickEvent.Phase.END) {
            ServerEventHander.levelPostTick((ServerLevel) event.level);
        }
    }

}
