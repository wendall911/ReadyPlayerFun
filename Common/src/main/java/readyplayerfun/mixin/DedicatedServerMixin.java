package readyplayerfun.mixin;

import net.minecraft.server.dedicated.DedicatedServer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import readyplayerfun.config.ConfigHandler;

@Mixin(DedicatedServer.class)
public abstract class DedicatedServerMixin {

    @Inject(method = "pauseWhileEmptySeconds", at = @At("HEAD"), cancellable = true)
    private void rpf$onSetPauseWhileEmptySeconds(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(ConfigHandler.Common.pauseWhileEmptySeconds());
    }

}
