package readyplayerfun.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.ModLoadingContext;

import org.apache.commons.lang3.tuple.Pair;

import readyplayerfun.ReadyPlayerFun;

@Mod.EventBusSubscriber(modid = ReadyPlayerFun.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ConfigHandler {

    private ConfigHandler() {}

    public static void init() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Common.CONFIG_SPEC);
    }

    public static final class Common {

        public static final ForgeConfigSpec CONFIG_SPEC;

        private static final Common CONFIG;

        public static BooleanValue ENABLE_WELCOME_MESSAGE;

        static {
            Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);

            CONFIG_SPEC = specPair.getRight();
            CONFIG = specPair.getLeft();
        }

        Common(ForgeConfigSpec.Builder builder) {
            ENABLE_WELCOME_MESSAGE = builder
                .comment("Show status message on first player login after server unpaused.")
                .define("ENABLE_WELCOME_MESSAGE", true);
        }

    }

}
