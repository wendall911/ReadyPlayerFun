package readyplayerfun.mixin;

import java.util.List;

import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.MinecraftServer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MinecraftServer.class)
public interface MinecraftServerAccessor {

    @Accessor
    List<Runnable> getTickables();

    @Accessor
    long getLastServerStatus();

    @Accessor
    void setLastServerStatus(long l);

    @Accessor
    void setStatus(ServerStatus status);

    @Invoker
    ServerStatus invokeBuildServerStatus();

}
