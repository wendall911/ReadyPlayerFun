package readyplayerfun.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.time.DurationFormatUtils;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedCommandNode;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.storage.PrimaryLevelData;

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
            ServerLevel level = sp.getLevel();
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
        Player player = event.getPlayer() != null ? event.getPlayer() : null;

        if (player != null && !player.level.isClientSide) {
            ServerPlayer sp = (ServerPlayer) player;
            PlayerList playerList = Objects.requireNonNull(sp.getServer()).getPlayerList();
            ServerLevel level = sp.getLevel();
            WorldState worldState = getWorldState(level);

            if (worldState.isLoaded() && playerList.getPlayerCount() <= 1) {
                pauseServer("onPlayerLogout", level);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        long now = System.currentTimeMillis();
        ServerLevel level = event.world instanceof ServerLevel ? (ServerLevel)event.world : null;

        if (level == null) return;

        WorldState worldState = getWorldState(level);

        if (!worldState.isLoaded() || event.side != LogicalSide.SERVER) {
            return;
        }
        else if (worldState.isPaused() && event.phase == TickEvent.Phase.END) {
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

            if (worldState.isPaused() && playerList.getPlayerCount() >= 1) {
                unpauseServer("onWorldTick", level);
            }
            else if (!worldState.isPaused() && playerList.getPlayerCount() <= 0) {
                pauseServer("onWorldTick", level);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onWorldLoad(WorldEvent.Load event) {
        ServerLevel world = event.getWorld() instanceof ServerLevel ? (ServerLevel)event.getWorld() : null;
        int defaultRandomTickSpeed = 3;
        boolean defaultFireTick = true;

        if (world == null) return;
        if (world.isClientSide()) return;
        if (!(world.getLevelData() instanceof PrimaryLevelData info)) return;

        GameRules rules = info.getGameRules();
        WorldState worldState = getWorldState(world);

        if (ConfigHandler.Server.FORCE_GAME_RULES.get()) {
            worldState.setDoFireTick(ConfigHandler.Server.DO_FIRE_TICK.get());
            worldState.setRandomTickSpeed(ConfigHandler.Server.RANDOM_TICK_SPEED.get());
            rules.getRule(GameRules.RULE_DOFIRETICK).set(worldState.isDoFireTick(), null);
            rules.getRule(GameRules.RULE_RANDOMTICKING).set(worldState.getRandomTickSpeed(), null);
        }
        else {
            if (world.getGameTime() < 20) {
                worldState.setDoFireTick(defaultFireTick);
                worldState.setRandomTickSpeed(defaultRandomTickSpeed);
            }
            else {
                worldState.setDoFireTick(rules.getBoolean(GameRules.RULE_DOFIRETICK));
                worldState.setRandomTickSpeed(rules.getInt(GameRules.RULE_RANDOMTICKING));
            }
        }

        worldState.setLoaded(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onServerStopping(ServerStoppingEvent server) {
        ServerLevel level = server.getServer().overworld();
        WorldState worldState = getWorldState(level);

        worldState.setLoaded(false);

        if (!level.isClientSide()) {
            GameRules rules = level.getLevelData().getGameRules();

            rules.getRule(GameRules.RULE_DOFIRETICK).set(worldState.isDoFireTick(), null);
            rules.getRule(GameRules.RULE_RANDOMTICKING).set(worldState.getRandomTickSpeed(), null);
        }
    }

    @SubscribeEvent
    public static void onCommand(CommandEvent event) {
        ParseResults<CommandSourceStack> results = event.getParseResults();
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

        if (!ConfigHandler.Server.FORCE_GAME_RULES.get()) {
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

}
