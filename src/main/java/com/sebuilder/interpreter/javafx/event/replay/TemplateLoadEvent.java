package com.sebuilder.interpreter.javafx.event.replay;

import com.sebuilder.interpreter.Locator;

import java.util.List;

public class TemplateLoadEvent {
    private final Locator parentLocator;

    private final List<String> targetTag;

    private final boolean withDataSource;

    public TemplateLoadEvent(Locator parentLocator, List<String> targetTag, boolean withDataSource) {
        this.parentLocator = parentLocator;
        this.targetTag = targetTag;
        this.withDataSource = withDataSource;
    }

    public Locator getParentLocator() {
        return parentLocator;
    }

    public List<String> getTargetTag() {
        return targetTag;
    }

    public boolean isWithDataSource() {
        return withDataSource;
    }
}
