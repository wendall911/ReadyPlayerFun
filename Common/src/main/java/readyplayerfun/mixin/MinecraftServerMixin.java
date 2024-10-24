package readyplayerfun.mixin;

import java.util.function.BooleanSupplier;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import org.slf4j.Logger;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import readyplayerfun.event.ServerEventHander;
import readyplayerfun.util.WorldState;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Shadow @Final private static Logger LOGGER;

    @Inject(method = "tickServer",
            at = @At(
                    value = "FIELD",
                    target="Lnet/minecraft/server/MinecraftServer;LOGGER:Lorg/slf4j/Logger;",
                    ordinal = 0,
                    shift = At.Shift.AFTER
            ),
            require = 1
    )
    private void rpf$onTickServer(BooleanSupplier pHasTimeLeft, CallbackInfo ci) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        ServerLevel level = server.overworld();

        // Don't do anything on client side
        if (!server.isDedicatedServer()) return;

        ServerEventHander.pauseServer("pause-when-empty-seconds", level);

        WorldState worldState = ServerEventHander.getWorldState(level);

        if (worldState.isPaused()) {

        }
    }

}
