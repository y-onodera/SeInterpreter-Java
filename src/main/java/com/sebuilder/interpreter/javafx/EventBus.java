package com.sebuilder.interpreter.javafx;

public class EventBus {
    private static EventBus ourInstance = new EventBus();

    private final com.google.common.eventbus.EventBus eventBus;

    public static EventBus getInstance() {
        return ourInstance;
    }

    public static EventBus registSubscriber(Object aObject) {
        EventBus result = getInstance();
        result.eventBus.register(aObject);
        return result;
    }

    public static EventBus publish(Object aObject) {
        EventBus result = getInstance();
        result.eventBus.post(aObject);
        return result;
    }

    private EventBus() {
        eventBus = new com.google.common.eventbus.EventBus();
    }

}
