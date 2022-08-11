package readyplayerfun.event;

import java.util.List;
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

@Mod.EventBusSubscriber(modid = ReadyPlayerFun.MODID)
public class ServerEventHandler {

    public static final ServerEventHandler INSTANCE = new ServerEventHandler();

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

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getPlayer() != null ? event.getPlayer() : null;

        if (player != null && !player.level.isClientSide) {
            ServerPlayer sp = (ServerPlayer) player;
            PlayerList playerList = Objects.requireNonNull(sp.getServer()).getPlayerList();
            ServerLevel world = sp.getLevel();

            if (playerList.getPlayerCount() >= 1 && paused) {
                long duration = System.currentTimeMillis() - startPauseTime;
                String durationString = DurationFormatUtils.formatDuration(duration, "H:mm:ss", true);

                if (ConfigHandler.Common.ENABLE_WELCOME_MESSAGE.get()) {
                    String msg = String.format("Welcome back! Server resumed after %s.", durationString);

                    sp.displayClientMessage(new TextComponent(msg), true);
                }

                unpauseServer(String.format("onPlayerLogin, %s", durationString), world);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getPlayer() != null ? event.getPlayer() : null;

        if (loaded && player != null && !player.level.isClientSide) {
            ServerPlayer sp = (ServerPlayer) player;
            PlayerList playerList = Objects.requireNonNull(sp.getServer()).getPlayerList();
            ServerLevel world = sp.getLevel();

            if (playerList.getPlayerCount() <= 1) {
                pauseServer("onPlayerLogout", world);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        long now = System.currentTimeMillis();
        ServerLevel world = event.world instanceof ServerLevel ? (ServerLevel)event.world : null;

        if (!loaded || event.side != LogicalSide.SERVER || world == null) {
            return;
        }
        else if (paused && event.phase == TickEvent.Phase.END) {
            if (world.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
                world.getServer().getWorldData().overworldData().setGameTime(gameTime);
                world.setDayTime(dayTime);
            }

            if (raining) {
                world.getServer().getWorldData().overworldData().setRaining(raining);
                world.getServer().getWorldData().overworldData().setRainTime(rainTime);
                if (thundering) {
                    world.getServer().getWorldData().overworldData().setThundering(thundering);
                    world.getServer().getWorldData().overworldData().setThunderTime(thunderTime);
                }
            }
            else {
                world.getServer().getWorldData().overworldData().setClearWeatherTime(weatherTime);
            }
        }

        // Check pause state and fix if incorrect.
        if ((now - checkTime) > 1000) {
            PlayerList playerList = world.getServer().getPlayerList();
            checkTime = now;

            if (paused && playerList.getPlayerCount() >= 1) {
                unpauseServer("onWorldTick", world);
            }
            else if (!paused && playerList.getPlayerCount() <= 0) {
                pauseServer("onWorldTick", world);
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

        if (ConfigHandler.Server.FORCE_GAME_RULES.get()) {
            doFireTick = ConfigHandler.Server.DO_FIRE_TICK.get();
            randomTickSpeed = ConfigHandler.Server.RANDOM_TICK_SPEED.get();
            rules.getRule(GameRules.RULE_DOFIRETICK).set(doFireTick, null);
            rules.getRule(GameRules.RULE_RANDOMTICKING).set(randomTickSpeed, null);
        }
        else {
            if (world.getGameTime() < 20) {
                randomTickSpeed = defaultRandomTickSpeed;
                doFireTick = defaultFireTick;
            }
            else {
                randomTickSpeed = rules.getInt(GameRules.RULE_RANDOMTICKING);
                doFireTick = rules.getBoolean(GameRules.RULE_DOFIRETICK);
            }
        }

        loaded = true;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onServerStopping(ServerStoppingEvent server) {
        ServerLevel level = server.getServer().overworld();

        loaded = false;

        if (!level.isClientSide()) {
            GameRules rules = level.getLevelData().getGameRules();

            rules.getRule(GameRules.RULE_DOFIRETICK).set(doFireTick, null);
            rules.getRule(GameRules.RULE_RANDOMTICKING).set(randomTickSpeed, null);
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

    private static void pauseServer(String ctx, ServerLevel world) {
        GameRules rules = world.getLevelData().getGameRules();

        startPauseTime = System.currentTimeMillis();
        gameTime = world.getGameTime();
        dayTime = world.getDayTime();

        raining = world.getServer().getWorldData().overworldData().isRaining();

        if (!ConfigHandler.Server.FORCE_GAME_RULES.get()) {
            randomTickSpeed = rules.getInt(GameRules.RULE_RANDOMTICKING);
            doFireTick = rules.getBoolean(GameRules.RULE_DOFIRETICK);
        }

        ReadyPlayerFun.LOGGER.info(
                String.format("Pausing server %s at %d, %d", ctx, gameTime, dayTime));

        rules.getRule(GameRules.RULE_DOFIRETICK).set(false, null);
        rules.getRule(GameRules.RULE_RANDOMTICKING).set(0, null);

        if (raining) {
            thundering = world.getServer().getWorldData().overworldData().isThundering();
            rainTime = world.getServer().getWorldData().overworldData().getRainTime();
            if (thundering) {
                thunderTime = world.getServer().getWorldData().overworldData().getThunderTime();
            }
        }
        else {
            weatherTime = world.getServer().getWorldData().overworldData().getClearWeatherTime();
        }

        ReadyPlayerFun.LOGGER.debug(String.format("(pauseServer) Raining: %s rainTime: %d, thundering: %s, thunderTime %d, weatherTime: %d, randomTickSpeed: %d, doFireTick: %s",
                    raining, rainTime, thundering, thunderTime, weatherTime, randomTickSpeed, doFireTick));

        paused = true;
    }

    private static void unpauseServer(String ctx, ServerLevel world) {
        world.getGameRules().getRule(GameRules.RULE_DOFIRETICK).set(doFireTick, null);
        world.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(randomTickSpeed, null);

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

}
