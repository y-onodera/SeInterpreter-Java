package com.sebuilder.interpreter.javafx.event.script;

public class StepMoveEvent {
    private final int from;
    private final int to;

    public StepMoveEvent(int from, int to) {
        this.from = from;
        this.to = to;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }
}
