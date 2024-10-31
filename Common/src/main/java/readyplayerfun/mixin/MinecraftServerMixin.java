package readyplayerfun.mixin;

import java.util.function.BooleanSupplier;

import net.minecraft.Util;
import net.minecraft.server.MinecraftServer;

import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;

import org.slf4j.Logger;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import readyplayerfun.config.ConfigHandler;
import readyplayerfun.event.ServerEventHander;
import readyplayerfun.util.WorldState;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Shadow @Final private static Logger LOGGER;
    private int emptyTicks;

    @Inject(method = "tickServer", at = @At("HEAD"), cancellable = true)
    private void rpf$onTickServer(BooleanSupplier pHasTimeLeft, CallbackInfo ci) {
        MinecraftServer server = (MinecraftServer) (Object) this;

        // Don't do anything on client side
        if (!server.isDedicatedServer()) return;

        WorldState worldState = readyplayerfun$getWorldState(server);
        ProfilerFiller profilerFiller = server.getProfiler();

        // Save on pause
        if (worldState.isPaused() && worldState.isNeedsSave()) {
            profilerFiller.push("saveOnPause");
            LOGGER.info("Server empty for {} seconds, pausing", ConfigHandler.Common.pauseWhileEmptySeconds());
            server.saveEverything(false, false, false);
            worldState.setNeedsSave(false);
            profilerFiller.pop();
        }

        // Tick essential things, don't tick world
        if (worldState.isPaused()) {
            long curNanos = Util.getNanos();

            // Tick connection
            profilerFiller.push("connection");
            server.getConnection().tick();

            profilerFiller.popPush("server gui");

            // Handle console inputs normally
            ((DedicatedServer) server).handleConsoleInputs();

            // Ensure player count is correct for remote players
            if (curNanos - ((MinecraftServerAccessor) server).getLastServerStatus() >= 5000000000L) {
                ((MinecraftServerAccessor) server).setLastServerStatus(curNanos);
                ((MinecraftServerAccessor) server).setStatus(((MinecraftServerAccessor) server).invokeBuildServerStatus());
            }

            ci.cancel();
        }
    }

    @Unique
    private WorldState readyplayerfun$getWorldState(MinecraftServer server) {
        ServerLevel level = server.overworld();
        int playerCount = server.getPlayerCount();
        WorldState worldState = ServerEventHander.getWorldState(level);
        int j = ConfigHandler.Common.pauseWhileEmptySeconds() * 20;

        if (playerCount == 0 && !server.tickRateManager().isSprinting()) {
            ++this.emptyTicks;
        } else {
            this.emptyTicks = 0;
        }

        // Check if we have even fired any pause events. If not, set server state to paused.
        if (this.emptyTicks >= j && !worldState.isPaused()) {
            ServerEventHander.pauseServer("tickServer", level);
            worldState = ServerEventHander.getWorldState(level);
        }

        return worldState;
    }

}
