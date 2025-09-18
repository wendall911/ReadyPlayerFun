package readyplayerfun.config;

import java.util.function.Predicate;

import org.apache.commons.lang3.tuple.Pair;

import technology.roughness.whitenoise.config.WhiteNoiseConfigSpec;

public class ConfigHandler {

    public static final WhiteNoiseConfigSpec COMMON_SPEC;

    private static final Common COMMON;

    static {
        final Pair<Common, WhiteNoiseConfigSpec> specPairCommon = new WhiteNoiseConfigSpec.Builder().configure(Common::new);

        COMMON_SPEC = specPairCommon.getRight();
        COMMON = specPairCommon.getLeft();
    }

    public static void init() {}

    public static class Common {

        private final WhiteNoiseConfigSpec.BooleanValue enableWelcomeMessage;
        private final WhiteNoiseConfigSpec.ConfigValue<String> welcomeMessage;

        private static final Predicate<Object> messageValidator = s -> s instanceof String
            && ((String) s).matches(".*%s.*");

        public Common(WhiteNoiseConfigSpec.Builder builder) {
            builder.push("Server");

            enableWelcomeMessage = builder.comment("Show status message on first player login after server unpaused.")
                .define("enableWelcomeMessage", true);
            welcomeMessage = builder.comment("Welcome message users see when joining. '%' is variable for the time elapsed.")
                .define("welcomeMessage", "Welcome back! Server resumed after %s.", messageValidator);

            builder.pop();
        }

        public static boolean enableWelcomeMessage() {
            return COMMON.enableWelcomeMessage.get();
        }

        public static String welcomeMessage() {
            return COMMON.welcomeMessage.get();
        }

    }

}
