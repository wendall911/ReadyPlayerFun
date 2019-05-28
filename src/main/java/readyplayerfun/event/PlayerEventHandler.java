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

public class PlayerEventHandler {

    private boolean worldLoaded = false;
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

    @SubscribeEvent
    public void onPlayerLogin(final PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.player;
            PlayerList playerList = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();

            if (playerList.getCurrentPlayerCount() >= 1 && paused) {
                long duration = System.currentTimeMillis() - startPauseTime;
                String durationString = DurationFormatUtils.formatDuration(duration, "H:mm:ss", true);
                String msg = String.format("Welcome back! Server resumed after %s.", durationString);

                ReadyPlayerFun.logger.info(msg);

                if (ConfigHandler.server.ENABLE_WELCOME_MESSAGE) {
                    player.sendMessage(new TextComponentString(msg));
                }

                paused = false;
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(final PlayerEvent.PlayerLoggedOutEvent event) {
        PlayerList playerList = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();
        World world = FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld();

        if (playerList.getCurrentPlayerCount() <= 1) {
            pauseServer(world, "onPlayerLogout", 0);
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
                ReadyPlayerFun.logger.info(String.format("Unpausing server %s", "onWorldTick"));
                paused = false;
            }
            else if (!paused && playerList.getCurrentPlayerCount() <= 0) {
                pauseServer(world, "onWorldTick", 0);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldLoad(WorldEvent.Load event) {
        if (!worldLoaded) {
            pauseServer(event.getWorld(), "onWorldLoad", 1);
        }
        worldLoaded = true;
    }

    private void pauseServer(World world, String ctx, int pad) {
        ReadyPlayerFun.logger.info(String.format("Pausing server %s", ctx));
        startPauseTime = System.currentTimeMillis();
        worldTime = world.getWorldTime() + pad;
        raining = world.getWorldInfo().isRaining();

        if (raining) {
            thundering = world.getWorldInfo().isThundering();
            rainTime = world.getWorldInfo().getRainTime() + pad;
            if (thundering) {
                thunderTime = world.getWorldInfo().getThunderTime() + pad;
            }
        }
        else {
            weatherTime = world.getWorldInfo().getCleanWeatherTime() + pad;
        }
        ReadyPlayerFun.logger.debug(String.format("Raining: %s rainTime: %d, thundering: %s, thunderTime %d, weatherTime: %d",
                    raining, rainTime, thundering, thunderTime, weatherTime));

        paused = true;
    }

}
