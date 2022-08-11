package readyplayerfun;

import dev.architectury.injectables.annotations.ExpectPlatform;

public class ReadyPlayerFunConfig {

    public static final String WELCOME_MESSAGE = "Show status message on first player login after server unpaused.";
    public static final String FORCE_GAME_RULES = "Force game rules regardless of server setting for 'paused' rules.";

    @ExpectPlatform
    public static boolean enableWelcomeMessage() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean forceGameRules() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean doFireTick() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static int randomTickSpeed() {
        throw new AssertionError();
    }

}
