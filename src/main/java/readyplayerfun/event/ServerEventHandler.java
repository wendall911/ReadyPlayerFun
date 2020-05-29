package readyplayerfun.event;

import org.apache.commons.lang3.time.DurationFormatUtils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerWorld;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModList;

import readyplayerfun.config.ConfigHandler;
import readyplayerfun.ReadyPlayerFun;

import sereneseasons.season.SeasonSavedData;
import sereneseasons.handler.season.SeasonHandler;

@Mod.EventBusSubscriber(modid = ReadyPlayerFun.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.DEDICATED_SERVER)
public class ServerEventHandler {

    private long startPauseTime;
    private boolean paused = false;
    private long checkTime = System.currentTimeMillis();
    private long gameTime;
    private long dayTime;
    private boolean raining = false;
    private boolean thundering = false;
    private int weatherTime;
    private int rainTime;
    private int thunderTime;
    private boolean doFireTick;
    private int randomTickSpeed;

    @SubscribeEvent
    public void onPlayerLogin(final PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) event.getPlayer();
            PlayerList playerList = event.getPlayer().getServer().getPlayerList();
            ServerWorld world = (ServerWorld)event.getPlayer().getEntityWorld().getWorld();

            if (playerList.getCurrentPlayerCount() >= 1 && paused) {
                long duration = System.currentTimeMillis() - startPauseTime;
                String durationString = DurationFormatUtils.formatDuration(duration, "H:mm:ss", true);
                String msg = String.format("Welcome back! Server resumed after %s.", durationString);

                if (ConfigHandler.enableWelcomeMessage.get()) {
                    player.sendMessage(new StringTextComponent(msg));
                }

                unpauseServer(String.format("onPlayerLogin, %s", durationString), world);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(final PlayerEvent.PlayerLoggedOutEvent event) {
        PlayerList playerList = event.getPlayer().getServer().getPlayerList();
        ServerWorld world = (ServerWorld)event.getPlayer().getEntityWorld().getWorld();

        if (playerList.getCurrentPlayerCount() <= 1) {
            pauseServer(world, "onPlayerLogout");
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        ServerWorld world = (ServerWorld)event.world.getWorld();
        long now = System.currentTimeMillis();

        if (world == null) {
            return;
        }
        else if (paused && event.phase == TickEvent.Phase.START) {
            if (ModList.get().isLoaded("sereneseasons")) {
                SeasonSavedData savedData = SeasonHandler.getSeasonSavedData(world);

                savedData.seasonCycleTicks--;
                savedData.markDirty();
            }

            if (world.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)) {
                for(ServerWorld serverworld : event.world.getServer().getWorlds()) {
                    serverworld.setGameTime(gameTime);
                    serverworld.setDayTime(dayTime);
                }
            }

            if (raining) {
                world.getWorldInfo().setRaining(raining);
                world.getWorldInfo().setRainTime(rainTime);
                if (thundering) {
                    world.getWorldInfo().setThundering(thundering);
                    world.getWorldInfo().setThunderTime(thunderTime);
                }
            }
            else {
                world.getWorldInfo().setClearWeatherTime(weatherTime);
            }
        }

        // Check pause state and fix if incorrect.
        if ((now - checkTime) > 1000) {
            PlayerList playerList = world.getServer().getPlayerList();
            checkTime = now;
            if (paused && playerList.getCurrentPlayerCount() >= 1) {
                unpauseServer("onWorldTick", world);
            }
            else if (!paused && playerList.getCurrentPlayerCount() <= 0) {
                pauseServer(world, "onWorldTick");
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldLoad(WorldEvent.Load event) {
        ServerWorld world = (ServerWorld)event.getWorld();
        randomTickSpeed = world.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED);
        doFireTick = world.getGameRules().getBoolean(GameRules.DO_FIRE_TICK);
    }

    private void pauseServer(ServerWorld world, String ctx) {
        startPauseTime = System.currentTimeMillis();
        gameTime = world.getGameTime();
        dayTime = world.getDayTime();

        raining = world.getWorldInfo().isRaining();
        randomTickSpeed = world.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED);
        doFireTick = world.getGameRules().getBoolean(GameRules.DO_FIRE_TICK);

        ReadyPlayerFun.logger.info(
                String.format("Pausing server %s at %d, %d", ctx, gameTime, dayTime));

        world.getGameRules().write().putInt(GameRules.RANDOM_TICK_SPEED.getName(), 0);
        world.getGameRules().get(GameRules.DO_FIRE_TICK).set(false, null);

        if (raining) {
            thundering = world.getWorldInfo().isThundering();
            rainTime = world.getWorldInfo().getRainTime();
            if (thundering) {
                thunderTime = world.getWorldInfo().getThunderTime();
            }
        }
        else {
            weatherTime = world.getWorldInfo().getClearWeatherTime();
        }

        ReadyPlayerFun.logger.debug(String.format("(pauseServer) Raining: %s rainTime: %d, thundering: %s, thunderTime %d, weatherTime: %d, randomTickSpeed: %d, doFireTick: %s",
                    raining, rainTime, thundering, thunderTime, weatherTime, randomTickSpeed, doFireTick));

        paused = true;
    }

    private void unpauseServer(String ctx, ServerWorld world) {
        world.getGameRules().get(GameRules.DO_FIRE_TICK).set(doFireTick, null);
        world.getGameRules().write().putInt(GameRules.RANDOM_TICK_SPEED.getName(), randomTickSpeed);

        ReadyPlayerFun.logger.info(
                String.format("Unpausing server: %s at %d, %d", ctx, gameTime, dayTime));

        paused = false;
    }

}
