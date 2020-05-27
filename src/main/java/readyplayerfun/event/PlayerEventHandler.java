package readyplayerfun.event;

import org.apache.commons.lang3.time.DurationFormatUtils;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.Loader;

import readyplayerfun.config.ConfigHandler;
import readyplayerfun.ReadyPlayerFun;

import sereneseasons.season.SeasonSavedData;
import sereneseasons.handler.season.SeasonHandler;

import weather2.config.ConfigMisc;
import weather2.ServerTickHandler;
import weather2.util.WeatherUtilConfig;

public class PlayerEventHandler {

    private long startPauseTime;
    private boolean paused = false;
    private long worldTime;
    private int seasonCycleTicks;
    private long checkTime = System.currentTimeMillis();
    private boolean raining = false;
    private boolean thundering = false;
    private int weatherTime;
    private int rainTime;
    private int thunderTime;
    private int weatherAutoSaveInterval;
    private boolean doFireTick;
    private int randomTickSpeed;

    @SubscribeEvent
    public void onPlayerLogin(final PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.player;
            PlayerList playerList = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();

            if (playerList.getCurrentPlayerCount() >= 1 && paused) {
                long duration = System.currentTimeMillis() - startPauseTime;
                String durationString = DurationFormatUtils.formatDuration(duration, "H:mm:ss", true);
                String msg = String.format("Welcome back! Server resumed after %s.", durationString);

                if (ConfigHandler.server.ENABLE_WELCOME_MESSAGE) {
                    player.sendMessage(new TextComponentString(msg));
                }

                unpauseServer(String.format("onPlayerLogin, %s", durationString), event.player.world);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(final PlayerEvent.PlayerLoggedOutEvent event) {
        PlayerList playerList = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();
        World world = FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld();

        if (playerList.getCurrentPlayerCount() <= 1) {
            pauseServer(world, "onPlayerLogout");
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        World world = event.world;
        long now = System.currentTimeMillis();

        if (world == null || world.provider == null || world.provider.getDimension() != 0) {
            return;
        }
        else if (paused && event.phase == TickEvent.Phase.START) {
            if (Loader.isModLoaded("sereneseasons")) {
                SeasonSavedData savedData = SeasonHandler.getSeasonSavedData(world);

                savedData.seasonCycleTicks--;
                savedData.markDirty();
            }

            if (world.getGameRules().getBoolean("doDaylightCycle")) {
                world.setWorldTime(worldTime);
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
                world.getWorldInfo().setCleanWeatherTime(weatherTime);
            }
        }

        // Check pause state and fix if incorrect.
        if ((now - checkTime) > 1000) {
            PlayerList playerList = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();
            checkTime = now;
            if (paused && playerList.getCurrentPlayerCount() >= 1) {
                unpauseServer("onWorldTick", world);
            }
            else if (!paused && playerList.getCurrentPlayerCount() <= 0) {
                pauseServer(world, "init");
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldLoad(WorldEvent.Load event) {
        World world = event.getWorld();
        randomTickSpeed = world.getGameRules().getInt("randomTickSpeed");
        doFireTick = world.getGameRules().getBoolean("doFireTick");
    }

    private void pauseServer(World world, String ctx) {
        ReadyPlayerFun.logger.info(String.format("Pausing server %s", ctx));
        startPauseTime = System.currentTimeMillis();
        worldTime = world.getWorldTime();
        raining = world.getWorldInfo().isRaining();
        randomTickSpeed = world.getGameRules().getInt("randomTickSpeed");
        doFireTick = world.getGameRules().getBoolean("doFireTick");

        world.getGameRules().setOrCreateGameRule("doFireTick", "false");
        world.getGameRules().setOrCreateGameRule("randomTickSpeed", "0");

        if (Loader.isModLoaded("weather2")) {
            weatherAutoSaveInterval = ConfigMisc.Misc_AutoDataSaveIntervalInTicks;
            ConfigMisc.Misc_AutoDataSaveIntervalInTicks = 0x7fffffff;
            ServerTickHandler.reset();
            WeatherUtilConfig.listDimensionsWeather.clear();
        }

        if (raining) {
            thundering = world.getWorldInfo().isThundering();
            rainTime = world.getWorldInfo().getRainTime();
            if (thundering) {
                thunderTime = world.getWorldInfo().getThunderTime();
            }
        }
        else {
            weatherTime = world.getWorldInfo().getCleanWeatherTime();
        }

        ReadyPlayerFun.logger.debug(String.format("(pauseServer) Raining: %s rainTime: %d, thundering: %s, thunderTime %d, weatherTime: %d, randomTickSpeed: %d, doFireTick: %s",
                    raining, rainTime, thundering, thunderTime, weatherTime, randomTickSpeed, doFireTick));

        paused = true;
    }

    private void unpauseServer(String ctx, World world) {
        if (Loader.isModLoaded("weather2")) {
            ConfigMisc.Misc_AutoDataSaveIntervalInTicks = weatherAutoSaveInterval;
            ServerTickHandler.initialize();
        }

        world.getGameRules().setOrCreateGameRule("doFireTick", String.valueOf(doFireTick));
        world.getGameRules().setOrCreateGameRule("randomTickSpeed", String.valueOf(randomTickSpeed));

        ReadyPlayerFun.logger.info(String.format("Unpausing server: %s", ctx));

        paused = false;
    }

}
