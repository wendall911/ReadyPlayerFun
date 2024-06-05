package readyplayerfun.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedCommandNode;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.storage.PrimaryLevelData;

import org.apache.commons.lang3.time.DurationFormatUtils;

import readyplayerfun.config.ConfigHandler;
import readyplayerfun.ReadyPlayerFun;
import readyplayerfun.util.WorldState;

public class ServerEventHander {

    private static final Map<Long, WorldState> WORLD_STATE_MAP = new HashMap<>();

    public static void serverStopping(MinecraftServer server) {
        ServerLevel level = server.overworld();
        WorldState worldState = getWorldState(level);
        GameRules rules = level.getLevelData().getGameRules();

        worldState.setLoaded(false);

        rules.getRule(GameRules.RULE_DOFIRETICK).set(worldState.isDoFireTick(), null);
        rules.getRule(GameRules.RULE_RANDOMTICKING).set(worldState.getRandomTickSpeed(), null);
    }

    public static void onPlayerJoin(ServerPlayer sp) {
        PlayerList playerList = Objects.requireNonNull(sp.getServer()).getPlayerList();
        ServerLevel level = sp.serverLevel();
        WorldState worldState = getWorldState(level);

        if (playerList.getPlayerCount() >= 1 && worldState.isPaused()) {
            long duration = System.currentTimeMillis() - worldState.getStartPauseTime();
            String durationString = DurationFormatUtils.formatDuration(duration, "H:mm:ss", true);

            if (ConfigHandler.Common.enableWelcomeMessage()) {
                String msg = String.format("Welcome back! Server resumed after %s.", durationString);
                Component message = Component.translatable(msg);

                sp.displayClientMessage(message, true);
            }

            unpauseServer(String.format("onPlayerLogin, %s", durationString), level);
        }
    }

    public static void onPlayerLogout(ServerPlayer sp) {
        PlayerList playerList = Objects.requireNonNull(sp.getServer()).getPlayerList();
        ServerLevel level = sp.serverLevel();
        WorldState worldState = getWorldState(level);

        if (worldState.isLoaded() && playerList.getPlayerCount() <= 1) {
            pauseServer("onPlayerLogout", level);
        }
    }

    public static void levelPostTick(ServerLevel level) {
        long now = System.currentTimeMillis();
        WorldState worldState = getWorldState(level);

        if (!worldState.isLoaded()) {
            return;
        }
        else if (worldState.isPaused()) {
            if (level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
                level.getServer().getWorldData().overworldData().setGameTime(worldState.getGameTime());
                level.setDayTime(worldState.getDayTime());
            }

            if (worldState.isRaining()) {
                level.getServer().getWorldData().overworldData().setRaining(worldState.isRaining());
                level.getServer().getWorldData().overworldData().setRainTime(worldState.getRainTime());
                if (worldState.isThundering()) {
                    level.getServer().getWorldData().overworldData().setThundering(worldState.isThundering());
                    level.getServer().getWorldData().overworldData().setThunderTime(worldState.getThunderTime());
                }
            }
            else {
                level.getServer().getWorldData().overworldData().setClearWeatherTime(worldState.getWeatherTime());
            }
        }

        // Check pause state and fix if incorrect.
        if ((now - worldState.getCheckTime()) > 1000) {
            PlayerList playerList = level.getServer().getPlayerList();
            worldState.setCheckTime(now);

            if (worldState.isPaused ()&& playerList.getPlayerCount() >= 1) {
                unpauseServer("onWorldTick", level);
            }
            else if (!worldState.isPaused ()&& playerList.getPlayerCount() <= 0) {
                pauseServer("onWorldTick", level);
            }
        }
    }

    public static void levelLoad(ServerLevel level) {
        if (!(level.getLevelData() instanceof PrimaryLevelData info)) return;

        WorldState worldState = getWorldState(level);
        GameRules rules = info.getGameRules();
        int defaultRandomTickSpeed = 3;
        boolean defaultFireTick = true;

        if (ConfigHandler.Common.forceGameRules()) {
            worldState.setDoFireTick(ConfigHandler.Common.doFireTick());
            worldState.setRandomTickSpeed(ConfigHandler.Common.randomTickSpeed());
            rules.getRule(GameRules.RULE_DOFIRETICK).set(worldState.isDoFireTick(), null);
            rules.getRule(GameRules.RULE_RANDOMTICKING).set(worldState.getRandomTickSpeed(), null);
        }
        else {
            if (level.getGameTime() < 20) {
                worldState.setRandomTickSpeed(defaultRandomTickSpeed);
                worldState.setDoFireTick(defaultFireTick);
            }
            else {
                worldState.setRandomTickSpeed(rules.getInt(GameRules.RULE_RANDOMTICKING));
                worldState.setDoFireTick(rules.getBoolean(GameRules.RULE_DOFIRETICK));
            }
        }

        worldState.setLoaded(true);
    }

    private static WorldState getWorldState(ServerLevel level) {
        return WORLD_STATE_MAP.computeIfAbsent(level.getSeed(), k-> new WorldState());
    }

    private static void pauseServer(String ctx, ServerLevel level) {
        GameRules rules = level.getLevelData().getGameRules();
        WorldState worldState = getWorldState(level);

        worldState.setStartPauseTime(System.currentTimeMillis());
        worldState.setGameTime(level.getGameTime());
        worldState.setDayTime(level.getDayTime());

        worldState.setRaining(level.getServer().getWorldData().overworldData().isRaining());

        if (ConfigHandler.Common.forceGameRules()) {
            worldState.setRandomTickSpeed(rules.getInt(GameRules.RULE_RANDOMTICKING));
            worldState.setDoFireTick(rules.getBoolean(GameRules.RULE_DOFIRETICK));
        }

        ReadyPlayerFun.LOGGER.info(
            String.format("Pausing server %s at %d, %d", ctx, worldState.getGameTime(), worldState.getDayTime()));

        rules.getRule(GameRules.RULE_DOFIRETICK).set(false, null);
        rules.getRule(GameRules.RULE_RANDOMTICKING).set(0, null);

        if (worldState.isRaining()) {
            worldState.setThundering(level.getServer().getWorldData().overworldData().isThundering());
            worldState.setRainTime(level.getServer().getWorldData().overworldData().getRainTime());
            if (worldState.isThundering()) {
                worldState.setThunderTime(level.getServer().getWorldData().overworldData().getThunderTime());
            }
        }
        else {
            worldState.setWeatherTime(level.getServer().getWorldData().overworldData().getClearWeatherTime());
        }

        ReadyPlayerFun.LOGGER.debug(String.format("(pauseServer) Raining: %s rainTime: %d, thundering: %s, thunderTime %d, weatherTime: %d, randomTickSpeed: %d, doFireTick: %s",
            worldState.isRaining(), worldState.getRainTime(), worldState.isThundering(), worldState.getThunderTime(), worldState.getWeatherTime(), worldState.getRandomTickSpeed(), worldState.isDoFireTick()));

        worldState.setPaused(true);
    }

    private static void unpauseServer(String ctx, ServerLevel level) {
        WorldState worldState = getWorldState(level);

        level.getGameRules().getRule(GameRules.RULE_DOFIRETICK).set(worldState.isDoFireTick(), null);
        level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(worldState.getRandomTickSpeed(), null);

        ReadyPlayerFun.LOGGER.info(
            String.format("Unpausing server: %s at %d, %d", ctx, worldState.getGameTime(), worldState.getDayTime()));

        worldState.setPaused(false);
    }

    private static void cyclePause(ServerLevel level) {
        WorldState worldState = getWorldState(level);

        if (worldState.isPaused()) {
            unpauseServer("gamerule change", level);
            pauseServer("gamerule change", level);
        }
        else {
            pauseServer("gamerule change", level);
            unpauseServer("gamerule change", level);
        }
    }

    public static void onCommand(ParseResults<CommandSourceStack> results) {
        CommandContextBuilder<CommandSourceStack> ctx = results.getContext();
        List<ParsedCommandNode<CommandSourceStack>> nodes = ctx.getNodes();

        if (nodes.size() < 3) return;

        CommandSourceStack src = ctx.getSource();
        String commandName = nodes.get(0).getNode().getName();
        String argument = nodes.get(1).getRange().get(results.getReader());

        if ("gamerule".equals(commandName)) {
            ServerLevel level = src.getLevel();
            WorldState worldState = getWorldState(level);

            boolean cycle = false;

            switch (argument) {
                case "doFireTick" -> {
                    cycle = true;
                    worldState.setDoFireTick(nodes.get(2).getRange().get(results.getReader()).equals("true"));
                }
                case "randomTickSpeed" -> {
                    int tickSpeed = Integer.parseInt(nodes.get(2).getRange().get(results.getReader()));

                    if (tickSpeed >= 0) {
                        cycle = true;
                        worldState.setRandomTickSpeed(tickSpeed);
                    }
                }
            }

            if (cycle) {
                cyclePause(level);
            }
        }
    }

}
