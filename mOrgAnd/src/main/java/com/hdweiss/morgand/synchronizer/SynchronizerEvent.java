package com.hdweiss.morgand.synchronizer;

public class SynchronizerEvent {
    public enum State {
        Done, Intermediate, Progress, SecondaryProgress
    }

    public State state;
    public int progress = 0;

    public SynchronizerEvent(State state) {
        this.state = state;
    }

    public SynchronizerEvent(State state, int progress) {
        this.state = state;
        this.progress = progress;
    }
}
