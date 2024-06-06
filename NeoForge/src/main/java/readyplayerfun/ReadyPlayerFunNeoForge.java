package readyplayerfun;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

import readyplayerfun.event.ServerEventListener;

@Mod(ReadyPlayerFun.MODID)
public class ReadyPlayerFunNeoForge {

    public ReadyPlayerFunNeoForge(IEventBus eventBus) {
        eventBus.addListener(this::setup);
        ReadyPlayerFun.init();
    }

    private void setup(final FMLCommonSetupEvent event) {
        NeoForge.EVENT_BUS.register(new ServerEventListener());
    }

}
