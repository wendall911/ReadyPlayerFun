package readyplayerfun.event;

import java.util.List;
import java.util.Objects;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedCommandNode;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.CommandPerformEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.storage.PrimaryLevelData;

import org.apache.commons.lang3.time.DurationFormatUtils;

import readyplayerfun.ReadyPlayerFun;

public class ServerEventHander {

    private static long startPauseTime;
    private static boolean paused = false;
    private static long checkTime = System.currentTimeMillis();
    private static long gameTime;
    private static long dayTime;
    private static boolean raining = false;
    private static boolean thundering = false;
    private static int weatherTime;
    private static int rainTime;
    private static int thunderTime;
    private static boolean doFireTick;
    private static int randomTickSpeed;
    private static boolean loaded = false;

    public static void init() {
        PlayerEvent.PLAYER_JOIN.register(ServerEventHander::onPlayerJoin);
        PlayerEvent.PLAYER_QUIT.register(ServerEventHander::onPlayerLogout);
        LifecycleEvent.SERVER_LEVEL_LOAD.register(ServerEventHander::levelLoad);
        LifecycleEvent.SERVER_STOPPING.register(ServerEventHander::serverStopping);
        TickEvent.SERVER_LEVEL_POST.register(ServerEventHander::levelPostTick);
        CommandPerformEvent.EVENT.register((command) -> {
            ServerEventHander.onCommand(command);

            return EventResult.pass();
        });
    }

    public static void serverStopping(MinecraftServer server) {
        ServerLevel level = server.overworld();

        GameRules rules = level.getLevelData().getGameRules();

        loaded = false;

        rules.getRule(GameRules.RULE_DOFIRETICK).set(doFireTick, null);
        rules.getRule(GameRules.RULE_RANDOMTICKING).set(randomTickSpeed, null);
    }

    public static void onPlayerJoin(ServerPlayer sp) {
        PlayerList playerList = Objects.requireNonNull(sp.getServer()).getPlayerList();
        ServerLevel world = sp.getLevel();

        if (playerList.getPlayerCount() >= 1 && paused) {
            long duration = System.currentTimeMillis() - startPauseTime;
            String durationString = DurationFormatUtils.formatDuration(duration, "H:mm:ss", true);

            //if (ConfigHandler.Common.ENABLE_WELCOME_MESSAGE.get()) {
            String msg = String.format("Welcome back! Server resumed after %s.", durationString);
            Component message = Component.translatable(msg);

            sp.displayClientMessage(message, true);
            //}

            unpauseServer(String.format("onPlayerLogin, %s", durationString), world);
        }
    }

    public static void onPlayerLogout(ServerPlayer sp) {
        PlayerList playerList = Objects.requireNonNull(sp.getServer()).getPlayerList();
        ServerLevel world = sp.getLevel();

        if (playerList.getPlayerCount() <= 1) {
            pauseServer("onPlayerLogout", world);
        }
    }

    public static void levelPostTick(ServerLevel level) {
        long now = System.currentTimeMillis();

        if (!loaded) {
            return;
        }
        else if (paused) {
            if (level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
                level.getServer().getWorldData().overworldData().setGameTime(gameTime);
                level.setDayTime(dayTime);
            }

            if (raining) {
                level.getServer().getWorldData().overworldData().setRaining(raining);
                level.getServer().getWorldData().overworldData().setRainTime(rainTime);
                if (thundering) {
                    level.getServer().getWorldData().overworldData().setThundering(thundering);
                    level.getServer().getWorldData().overworldData().setThunderTime(thunderTime);
                }
            }
            else {
                level.getServer().getWorldData().overworldData().setClearWeatherTime(weatherTime);
            }
        }

        // Check pause state and fix if incorrect.
        if ((now - checkTime) > 1000) {
            PlayerList playerList = level.getServer().getPlayerList();
            checkTime = now;

            if (paused && playerList.getPlayerCount() >= 1) {
                unpauseServer("onWorldTick", level);
            }
            else if (!paused && playerList.getPlayerCount() <= 0) {
                pauseServer("onWorldTick", level);
            }
        }
    }

    public static void levelLoad(ServerLevel level) {
        if (!(level.getLevelData() instanceof PrimaryLevelData info)) return;

        GameRules rules = info.getGameRules();
        int defaultRandomTickSpeed = 3;
        boolean defaultFireTick = true;

        /*
        if (ConfigHandler.Server.FORCE_GAME_RULES.get()) {
            doFireTick = ConfigHandler.Server.DO_FIRE_TICK.get();
            randomTickSpeed = ConfigHandler.Server.RANDOM_TICK_SPEED.get();
            rules.getRule(GameRules.RULE_DOFIRETICK).set(doFireTick, null);
            rules.getRule(GameRules.RULE_RANDOMTICKING).set(randomTickSpeed, null);
        }
        else {
         */
        if (level.getGameTime() < 20) {
            randomTickSpeed = defaultRandomTickSpeed;
            doFireTick = defaultFireTick;
        }
        else {
            randomTickSpeed = rules.getInt(GameRules.RULE_RANDOMTICKING);
            doFireTick = rules.getBoolean(GameRules.RULE_DOFIRETICK);
        }
        //}
        ReadyPlayerFun.LOGGER.warn("levelLoad: %s %s", rules.getRule(GameRules.RULE_DOFIRETICK), rules.getRule(GameRules.RULE_RANDOMTICKING));

        loaded = true;
    }

    public static void levelUnload(ServerLevel level) {

    }

    private static void pauseServer(String ctx, ServerLevel level) {
        GameRules rules = level.getLevelData().getGameRules();

        startPauseTime = System.currentTimeMillis();
        gameTime = level.getGameTime();
        dayTime = level.getDayTime();

        raining = level.getServer().getWorldData().overworldData().isRaining();

        //if (!ConfigHandler.Server.FORCE_GAME_RULES.get()) {
            randomTickSpeed = rules.getInt(GameRules.RULE_RANDOMTICKING);
            doFireTick = rules.getBoolean(GameRules.RULE_DOFIRETICK);
        //}

        ReadyPlayerFun.LOGGER.info(
                String.format("Pausing server %s at %d, %d", ctx, gameTime, dayTime));

        rules.getRule(GameRules.RULE_DOFIRETICK).set(false, null);
        rules.getRule(GameRules.RULE_RANDOMTICKING).set(0, null);

        if (raining) {
            thundering = level.getServer().getWorldData().overworldData().isThundering();
            rainTime = level.getServer().getWorldData().overworldData().getRainTime();
            if (thundering) {
                thunderTime = level.getServer().getWorldData().overworldData().getThunderTime();
            }
        }
        else {
            weatherTime = level.getServer().getWorldData().overworldData().getClearWeatherTime();
        }

        ReadyPlayerFun.LOGGER.debug(String.format("(pauseServer) Raining: %s rainTime: %d, thundering: %s, thunderTime %d, weatherTime: %d, randomTickSpeed: %d, doFireTick: %s",
                raining, rainTime, thundering, thunderTime, weatherTime, randomTickSpeed, doFireTick));

        paused = true;
    }

    private static void unpauseServer(String ctx, ServerLevel level) {
        level.getGameRules().getRule(GameRules.RULE_DOFIRETICK).set(doFireTick, null);
        level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(randomTickSpeed, null);

        ReadyPlayerFun.LOGGER.info(
                String.format("Unpausing server: %s at %d, %d", ctx, gameTime, dayTime));

        paused = false;
    }

    private static void cyclePause(ServerLevel level) {
        if (paused) {
            unpauseServer("gamerule change", level);
            pauseServer("gamerule change", level);
        }
        else {
            pauseServer("gamerule change", level);
            unpauseServer("gamerule change", level);
        }
    }

    private static void onCommand(CommandPerformEvent event) {
        ParseResults<CommandSourceStack> results = event.getResults();
        CommandContextBuilder<CommandSourceStack> ctx = results.getContext();
        List<ParsedCommandNode<CommandSourceStack>> nodes = ctx.getNodes();

        if (nodes.size() < 3) return;

        CommandSourceStack src = ctx.getSource();
        String commandName = nodes.get(0).getNode().getName();
        String argument = nodes.get(1).getRange().get(results.getReader());
        if ("gamerule".equals(commandName)) {
            ServerLevel level = src.getLevel();
            boolean cycle = false;

            switch (argument) {
                case "doFireTick" -> {
                    cycle = true;
                    doFireTick = nodes.get(2).getRange().get(results.getReader()).equals("true");
                }
                case "randomTickSpeed" -> {
                    int tickSpeed = Integer.parseInt(nodes.get(2).getRange().get(results.getReader()));

                    if (tickSpeed >= 0) {
                        cycle = true;
                        randomTickSpeed = tickSpeed;
                    }
                }
            }

            if (cycle) {
                cyclePause(level);
            }
        }
    }

}
