package readyplayerfun;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import readyplayerfun.proxy.CommonProxy;

@Mod(modid = ReadyPlayerFun.MODID,
     version = ReadyPlayerFun.MOD_VERSION,
     name = ReadyPlayerFun.MOD_NAME,
     certificateFingerprint = "@FINGERPRINT@",
     dependencies = "required-after:forge@[@FORGE_VERSION@,);",
     acceptedMinecraftVersions = "[@MC_VERSION@,)"
)

public class ReadyPlayerFun {

    public static final String MODID = "@MODID@";
    public static final String MOD_VERSION = "@MOD_VERSION@";
    public static final String MOD_NAME = "@MOD_NAME@";

    @SidedProxy(clientSide = "readyplayerfun.proxy.ClientProxy", serverSide = "readyplayerfun.proxy.ServerProxy")
    public static CommonProxy proxy;

    @Mod.Instance
    public static ReadyPlayerFun instance;

    public static Logger logger = LogManager.getFormatterLogger(ReadyPlayerFun.MODID);

    @Mod.EventHandler
    public void onFingerprintViolation(FMLFingerprintViolationEvent event) {
        logger.warn("Invalid fingerprint detected!");
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger.info("Pre-init started");
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        logger.info("Init started");
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        logger.info("Post-init started");
        proxy.postInit(event);
        logger.info("Finished Loading");
    }

}

