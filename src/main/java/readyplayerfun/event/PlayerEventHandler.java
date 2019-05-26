package readyplayerfun.event;

import org.apache.commons.lang3.time.DurationFormatUtils;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import readyplayerfun.config.ConfigHandler;
import readyplayerfun.ReadyPlayerFun;

import sereneseasons.season.SeasonSavedData;
import sereneseasons.handler.season.SeasonHandler;

public class PlayerEventHandler {

    private long startPauseTime;
    private boolean paused = false;
    private long worldTime;
    private int seasonCycleTicks;
    private long checkTime = System.currentTimeMillis();

    @SideOnly(Side.SERVER)
    @SubscribeEvent
    public void onPlayerLogin(final PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.player;
            PlayerList playerList = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();

            if (playerList.getCurrentPlayerCount() >= 1 && paused) {
                if (ConfigHandler.server.ENABLE_WELCOME_MESSAGE) {
                    long duration = System.currentTimeMillis() - startPauseTime;
                    String durationString = DurationFormatUtils.formatDuration(duration, "H:mm:ss", true);

                    TextComponentTranslation msg = new TextComponentTranslation("readyplayerfun.message.unpaused", durationString);
                    player.sendMessage(msg);
                }

                paused = false;
            }
        }
    }

    @SideOnly(Side.SERVER)
    @SubscribeEvent
    public void onPlayerLogout(final PlayerEvent.PlayerLoggedOutEvent event) {
        PlayerList playerList = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();
        World world = FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld();

        if (playerList.getCurrentPlayerCount() <= 1) {
            startPauseTime = System.currentTimeMillis();
            worldTime = world.getWorldTime();
            paused = true;
        }
    }

    @SideOnly(Side.SERVER)
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
        }
        // If pause got into a bad state, check every second to make sure we resume.
        if (paused && (now - checkTime) > 1000) {
            PlayerList playerList = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();
            checkTime = now;
            if (playerList.getCurrentPlayerCount() >= 1) {
                paused = false;
            }
        }
    }

    @SideOnly(Side.SERVER)
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldLoad(WorldEvent.Load event) {
        World world = event.getWorld();
        startPauseTime = System.currentTimeMillis();
        worldTime = world.getWorldTime();
        paused = true;
    }

}
