package readyplayerfun.config;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.List;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
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

    public static void initServer() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Server.CONFIG_SPEC);
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

    public static final class Server {

        public static final ForgeConfigSpec CONFIG_SPEC;

        private static final Server CONFIG;

        public static BooleanValue FORCE_GAME_RULES;
        public static BooleanValue DO_FIRE_TICK;
        public static IntValue RANDOM_TICK_SPEED;

        static {
            Pair<Server, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Server::new);

            CONFIG_SPEC = specPair.getRight();
            CONFIG = specPair.getLeft();
        }

        Server(ForgeConfigSpec.Builder builder) {
            FORCE_GAME_RULES = builder
                .comment("Force game rules regardless of server setting for 'paused' rules.")
                .define("FORCE_GAME_RULES", true);
            DO_FIRE_TICK = builder
                .comment("doFireTick")
                .define("DO_FIRE_TICK", true);
            RANDOM_TICK_SPEED = builder
                .comment("randomTickSpeed")
                .defineInRange("RANDOM_TICK_SPEED", 3, 0, 20);
        }
    }

}
