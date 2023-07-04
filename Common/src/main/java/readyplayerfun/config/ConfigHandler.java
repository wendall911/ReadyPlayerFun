package readyplayerfun.config;

import com.illusivesoulworks.spectrelib.config.SpectreConfigSpec;

import org.apache.commons.lang3.tuple.Pair;

public class ConfigHandler {

    public static final SpectreConfigSpec COMMON_SPEC;

    private static final Common COMMON;

    static {
        final Pair<Common, SpectreConfigSpec> specPairCommon = new SpectreConfigSpec.Builder().configure(Common::new);

        COMMON_SPEC = specPairCommon.getRight();
        COMMON = specPairCommon.getLeft();
    }

    public static void init() {}

    public static class Common {

        private final SpectreConfigSpec.BooleanValue enableWelcomeMessage;
        private final SpectreConfigSpec.BooleanValue forceGameRules;
        private final SpectreConfigSpec.BooleanValue doFireTick;
        private final SpectreConfigSpec.IntValue randomTickSpeed;

        public Common(SpectreConfigSpec.Builder builder) {
            builder.push("Server");

            enableWelcomeMessage = builder.comment("Show status message on first player login after server unpaused.")
                .define("enableWelcomeMessage", true);

            forceGameRules = builder.comment("Force game rules regardless of server setting for 'paused' rules.")
                .define("forceGameRules", false);

            doFireTick = builder.comment("doFireTick default value. Used if forceGameRules is set to true.")
                .define("doFireTick", true);

            randomTickSpeed = builder.comment("randomTickSpeed default value. Used if forceGameRules is set to true.")
                .defineInRange("randomTickSpeed", 3, 0, 50);

            builder.pop();
        }

        public static boolean enableWelcomeMessage() {
            return COMMON.enableWelcomeMessage.get();
        }

        public static boolean forceGameRules() {
            return COMMON.forceGameRules.get();
        }

        public static boolean doFireTick() {
            return COMMON.doFireTick.get();
        }

        public static int randomTickSpeed() {
            return COMMON.randomTickSpeed.get();
        }

    }

}
