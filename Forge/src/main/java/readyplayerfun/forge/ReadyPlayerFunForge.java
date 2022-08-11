package readyplayerfun.forge;

import dev.architectury.platform.forge.EventBuses;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import readyplayerfun.forge.proxy.CommonProxy;
import readyplayerfun.forge.proxy.ServerProxy;

import readyplayerfun.ReadyPlayerFun;

@Mod(ReadyPlayerFun.MODID)
public class ReadyPlayerFunForge {

    public ReadyPlayerFunForge() {
        EventBuses.registerModEventBus(ReadyPlayerFun.MODID, FMLJavaModLoadingContext.get().getModEventBus());
        DistExecutor.safeRunForDist(() -> CommonProxy::new, () -> ServerProxy::new);
        ReadyPlayerFun.init();
    }

}
