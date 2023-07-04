package readyplayerfun;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import readyplayerfun.event.ServerEventListener;

@Mod(ReadyPlayerFun.MODID)
public class ReadyPlayerFunForge {

    public ReadyPlayerFunForge() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(this::setup);
        ReadyPlayerFun.init();
    }

    private void setup(final FMLCommonSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new ServerEventListener());
    }

}
