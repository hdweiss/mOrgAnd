package com.hdweiss.morgand.synchronizer;

public class SyncEvent {
    public enum State {
        Done, Intermediate, Progress, SecondaryProgress
    }

    public State state;
    public int progress = 0;
    public String filename = "";

    public SyncEvent(State state) {
        this(state, 0);
    }

    public SyncEvent(State state, int progress) {
        this(state, progress, "");
    }

    public SyncEvent(State state, int progress, String filename) {
        this.state = state;
        this.progress = progress;
        this.filename = filename;
    }
}
