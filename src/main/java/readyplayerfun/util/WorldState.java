package readyplayerfun.util;

import lombok.Getter;
import lombok.Setter;

public class WorldState {

    @Getter @Setter private long startPauseTime;
    @Getter @Setter private boolean paused = false;
    @Getter @Setter private long checkTime = System.currentTimeMillis();
    @Getter @Setter private long gameTime;
    @Getter @Setter private long dayTime;
    @Getter @Setter private boolean raining = false;
    @Getter @Setter private boolean thundering = false;
    @Getter @Setter private int weatherTime;
    @Getter @Setter private int rainTime;
    @Getter @Setter private int thunderTime;
    @Getter @Setter private boolean doFireTick;
    @Getter @Setter private int randomTickSpeed;
    @Getter @Setter private boolean loaded = false;

    public WorldState() {}
}
