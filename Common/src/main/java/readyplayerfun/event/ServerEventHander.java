package readyplayerfun.event;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.mojang.brigadier.ParseResults;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

import org.apache.commons.lang3.time.DurationFormatUtils;

import readyplayerfun.config.ConfigHandler;
import readyplayerfun.ReadyPlayerFun;
import readyplayerfun.util.WorldState;

public class ServerEventHander {

    private static final Map<Long, WorldState> WORLD_STATE_MAP = new HashMap<>();

    public static void serverStopping(MinecraftServer server) {
        // Nothing to do now in 1.21.3+ Keeping in case we have a request to do something ...
    }

    public static void onPlayerJoin(ServerPlayer sp) {
        PlayerList playerList = Objects.requireNonNull(sp.getServer()).getPlayerList();
        ServerLevel level = sp.getServer().overworld();
        WorldState worldState = getWorldState(level);

        if (playerList.getPlayerCount() >= 1 && worldState.isPaused()) {
            long duration = System.currentTimeMillis() - worldState.getStartPauseTime();
            String durationString = DurationFormatUtils.formatDuration(duration, "H:mm:ss", true);

            if (ConfigHandler.Common.enableWelcomeMessage()) {
                String msg = String.format(ConfigHandler.Common.welcomeMessage(), durationString);
                Component message = Component.translatable(msg);

                sp.displayClientMessage(message, true);
            }

            unpauseServer(String.format("onPlayerLogin, %s", durationString), level);
        }
    }

    public static void onPlayerLogout(ServerPlayer sp) {
        // Nothing to do now in 1.21.3+ Keeping in case we have a request to do something ...
    }

    public static void levelPostTick(ServerLevel level) {
        // Nothing to do now in 1.21.3+ Keeping in case we have a request to do something ...
    }

    public static void levelLoad(ServerLevel level) {
        // Nothing to do now in 1.21.3+ Keeping in case we have a request to do something ...
    }

    public static WorldState getWorldState(ServerLevel level) {
        return WORLD_STATE_MAP.computeIfAbsent(level.getSeed(), k-> new WorldState());
    }

    public static void pauseServer(String ctx, ServerLevel level) {
        WorldState worldState = getWorldState(level);

        worldState.setStartPauseTime(System.currentTimeMillis());
        worldState.setGameTime(level.getGameTime());
        worldState.setDayTime(level.getDayTime());

        ReadyPlayerFun.LOGGER.info(
            String.format("%s rule set, pausing server at %d, %d", ctx, worldState.getGameTime(), worldState.getDayTime()));

        worldState.setPaused(true);
    }

    private static void unpauseServer(String ctx, ServerLevel level) {
        WorldState worldState = getWorldState(level);

        ReadyPlayerFun.LOGGER.info(
            String.format("Unpausing server: %s at %d, %d", ctx, level.getGameTime(), level.getDayTime()));

        worldState.setPaused(false);
    }

    private static void cyclePause(ServerLevel level) {
        // Not needed, as we don't actually set anything
    }

    public static void onCommand(ParseResults<CommandSourceStack> results) {
        // Nothing to do now in 1.21.3+ Keeping in case we have a request to do something ...
    }

}
