package com.sebuilder.interpreter.javafx.event;

import com.sebuilder.interpreter.javafx.EventBus;

public class ReportErrorEvent {
    public static final ReportErrorEvent CLEAR = new ReportErrorEvent(null);
    private final Throwable source;

    public static void publishIfExecuteThrowsException(ThrowableAction action) {
        EventBus.publish(CLEAR);
        try {
            action.execute();
        } catch (Throwable th) {
            EventBus.publish(new ReportErrorEvent(th));
        }
    }

    public ReportErrorEvent(Throwable source) {
        this.source = source;
    }

    public Throwable getSource() {
        return source;
    }

    public interface ThrowableAction {
        void execute() throws Exception;
    }
}
