package com.hdweiss.morgand.synchronizer;

public class SynchronizerEvent {
    public enum State {
        Done, Intermediate, Progress, SecondaryProgress
    }

    public State state;
    public int progress = 0;
    public String filename = "";

    public SynchronizerEvent(State state) {
        this(state, 0);
    }

    public SynchronizerEvent(State state, int progress) {
        this(state, progress, "");
    }

    public SynchronizerEvent(State state, int progress, String filename) {
        this.state = state;
        this.progress = progress;
        this.filename = filename;
    }
}
