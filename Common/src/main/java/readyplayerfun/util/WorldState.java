package readyplayerfun.util;

import lombok.Getter;
import lombok.Setter;

public class WorldState {

    @Getter @Setter private long startPauseTime;
    @Getter @Setter private boolean paused = false;
    @Getter @Setter private long gameTime;
    @Getter @Setter private long dayTime;

    public WorldState() {}
}
