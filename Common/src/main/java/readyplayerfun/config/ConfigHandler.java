package readyplayerfun.config;

import java.util.function.Predicate;

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
        private final SpectreConfigSpec.ConfigValue<String> welcomeMessage;

        private static final Predicate<Object> messageValidator = s -> s instanceof String
            && ((String) s).matches(".*%s.*");

        public Common(SpectreConfigSpec.Builder builder) {
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
