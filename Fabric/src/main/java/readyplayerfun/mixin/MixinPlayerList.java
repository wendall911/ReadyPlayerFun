package readyplayerfun.mixin;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import readyplayerfun.event.ServerEventHander;

@Mixin(PlayerList.class)
public class MixinPlayerList {

    @Inject(method = "placeNewPlayer", at = @At("RETURN"))
    private void injectPlaceNewPlayer(Connection connection, ServerPlayer sp, CommonListenerCookie cookie, CallbackInfo ci) {
        ServerEventHander.onPlayerJoin(sp);
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void injectRemove(ServerPlayer sp, CallbackInfo ci) {
        ServerEventHander.onPlayerLogout(sp);
    }

}
