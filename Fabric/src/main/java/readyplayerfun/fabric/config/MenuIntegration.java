package readyplayerfun.fabric.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import me.shedaniel.autoconfig.AutoConfig;
import readyplayerfun.fabric.ReadyPlayerFunConfigImpl;

public class MenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return factory -> AutoConfig.getConfigScreen(ReadyPlayerFunConfigImpl.class, factory).get();
    }

}
