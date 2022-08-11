package readyplayerfun.forge;

import java.nio.file.Path;

import net.minecraftforge.fml.loading.FMLPaths;

public class ReadyPlayerFunExpectPlatformImpl {

    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

}
