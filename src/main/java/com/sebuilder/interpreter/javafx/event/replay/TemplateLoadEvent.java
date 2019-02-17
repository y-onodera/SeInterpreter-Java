package com.sebuilder.interpreter.javafx.event.replay;

import com.sebuilder.interpreter.Locator;

import java.util.List;

public class TemplateLoadEvent {
    private final Locator parentLocator;

    private final List<String> targetTag;

    public TemplateLoadEvent(Locator parentLocator, List<String> targetTag) {
        this.parentLocator = parentLocator;
        this.targetTag = targetTag;
    }

    public Locator getParentLocator() {
        return parentLocator;
    }

    public List<String> getTargetTag() {
        return targetTag;
    }
}
