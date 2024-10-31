package readyplayerfun.util;

import lombok.Getter;
import lombok.Setter;

public class WorldState {

    @Getter @Setter private long startPauseTime;
    @Getter @Setter private boolean paused = false;
    @Getter @Setter private long checkTime = System.currentTimeMillis();
    @Getter @Setter private long gameTime;
    @Getter @Setter private long dayTime;
    @Getter @Setter private boolean needsSave = true;
    @Getter @Setter private boolean loaded = false;

    public WorldState() {}
}
