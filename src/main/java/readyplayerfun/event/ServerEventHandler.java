package readyplayerfun.event;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.time.DurationFormatUtils;

import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;

import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.LogicalSide;

import readyplayerfun.config.ConfigHandler;
import readyplayerfun.ReadyPlayerFun;
import readyplayerfun.util.WorldState;

@Mod.EventBusSubscriber(modid = ReadyPlayerFun.MODID)
public class ServerEventHandler {

    public static final ServerEventHandler INSTANCE = new ServerEventHandler();
    public static Map<Long, WorldState> WORLD_STATE_MAP = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getPlayer() != null ? event.getPlayer() : null;

        if (player != null && !player.level.isClientSide) {
            ServerPlayer sp = (ServerPlayer) player;
            PlayerList playerList = Objects.requireNonNull(sp.getServer()).getPlayerList();
            ServerLevel level = sp.getServer().overworld();
            WorldState worldState = getWorldState(level);

            if (playerList.getPlayerCount() >= 1 && worldState.isPaused()) {
                long duration = System.currentTimeMillis() - worldState.getStartPauseTime();
                String durationString = DurationFormatUtils.formatDuration(duration, "H:mm:ss", true);

                if (ConfigHandler.Common.ENABLE_WELCOME_MESSAGE.get()) {
                    String msg = String.format("Welcome back! Server resumed after %s.", durationString);

                    sp.displayClientMessage(new TextComponent(msg), true);
                }

                unpauseServer(String.format("onPlayerLogin, %s", durationString), level);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onWorldLoad(WorldEvent.Load event) {
        ServerLevel world = event.getWorld() instanceof ServerLevel ? (ServerLevel)event.getWorld() : null;

        if (world != null) {
            WorldState worldState = getWorldState(world);

            worldState.setLoaded(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onServerStopping(ServerStoppingEvent server) {
    }

    @SubscribeEvent
    public static void onCommand(CommandEvent event) {}

    public static WorldState getWorldState(ServerLevel level) {
        return WORLD_STATE_MAP.computeIfAbsent(level.getSeed(), k-> new WorldState());
    }

    public static void pauseServer(String ctx, ServerLevel level) {
        WorldState worldState = getWorldState(level);

        worldState.setStartPauseTime(System.currentTimeMillis());
        worldState.setGameTime(level.getGameTime());
        worldState.setDayTime(level.getDayTime());

        ReadyPlayerFun.LOGGER.info(
            String.format("Pausing server %s at %d, %d", ctx, worldState.getGameTime(), worldState.getDayTime()));

        worldState.setPaused(true);
    }

    private static void unpauseServer(String ctx, ServerLevel level) {
        WorldState worldState = getWorldState(level);

        ReadyPlayerFun.LOGGER.info(
            String.format("Unpausing server: %s at %d, %d", ctx, worldState.getGameTime(), worldState.getDayTime()));

        worldState.setPaused(false);
        worldState.setNeedsSave(true);
    }

}
