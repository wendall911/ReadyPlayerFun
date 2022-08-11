package readyplayerfun.fabric;

import java.nio.file.Path;

import net.fabricmc.loader.api.FabricLoader;

public class ReadyPlayerFunExpectPlatformImpl {

    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
