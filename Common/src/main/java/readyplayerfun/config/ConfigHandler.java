package readyplayerfun.config;

import technology.roughness.whitenoise.config.WhiteNoiseConfigSpec;

import org.apache.commons.lang3.tuple.Pair;

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
        private final WhiteNoiseConfigSpec.IntValue pauseWhileEmptySeconds;

        public Common(WhiteNoiseConfigSpec.Builder builder) {
            builder.push("Server");

            enableWelcomeMessage = builder.comment("Show status message on first player login after server unpaused.")
                .define("enableWelcomeMessage", true);
            pauseWhileEmptySeconds = builder.comment("Number of seconds before server pauses. Backport of pause-when-empty-seconds from 1.21.2+")
                .defineInRange("pauseWhileEmptySeconds", 60, 1, 360);

            builder.pop();
        }

        public static boolean enableWelcomeMessage() {
            return COMMON.enableWelcomeMessage.get();
        }

        public static int pauseWhileEmptySeconds() {
            return COMMON.pauseWhileEmptySeconds.get();
        }

    }

}
