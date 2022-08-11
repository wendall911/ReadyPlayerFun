package readyplayerfun;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import readyplayerfun.event.ServerEventHander;

public class ReadyPlayerFun {

    public static final String MODID = "readyplayerfun";
    public static final Logger LOGGER = LogManager.getFormatterLogger(ReadyPlayerFun.MODID);
   
    public static void init() {
        ServerEventHander.init();
    }

}
