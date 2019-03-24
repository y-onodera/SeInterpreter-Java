package com.sebuilder.interpreter.javafx;

public enum EventBus {
    SINGLETON;
    private final com.google.common.eventbus.EventBus eventBus;

    public static EventBus registSubscriber(Object aObject) {
        SINGLETON.eventBus.register(aObject);
        return SINGLETON;
    }

    public static EventBus publish(Object aObject) {
        SINGLETON.eventBus.post(aObject);
        return SINGLETON;
    }

    EventBus() {
        eventBus = new com.google.common.eventbus.EventBus();
    }

}
