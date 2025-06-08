package com.sebuilder.interpreter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class StepBuilder {
    private boolean negated;
    private String name;
    private final StepType stepType;
    private final Map<String, String> stringParams = new HashMap<>();
    private final Map<String, Locator> locatorParams = new HashMap<>();
    private final Map<String, BytesValueSource> headerParams = new HashMap<>();

    public StepBuilder(final StepType stepType) {
        this(null, stepType, false);
    }

    public StepBuilder(final String name, final StepType stepType, final boolean negated) {
        this.negated = negated;
        this.name = name;
        this.stepType = stepType;
    }

    public boolean isNegated() {
        return this.negated;
    }

    public String getName() {
        return this.name;
    }

    public StepType getStepType() {
        return this.stepType;
    }

    public Map<String, String> getStringParams() {
        return this.stringParams;
    }

    public Map<String, Locator> getLocatorParams() {
        return this.locatorParams;
    }

    public Map<String, BytesValueSource> getHeaderParams() {
        return headerParams;
    }

    public boolean containsStringParam(final String paramKey) {
        return this.stringParams.containsKey(paramKey);
    }

    public boolean containsLocatorParam(final String paramKey) {
        return this.locatorParams.containsKey(paramKey);
    }

    public StepBuilder apply(final Function<StepBuilder, StepBuilder> function) {
        return function.apply(this);
    }

    public StepBuilder locator(final Locator value) {
        this.put("locator", value);
        return this;
    }

    public StepBuilder name(final String name) {
        this.name = name;
        return this;
    }

    public StepBuilder negated(final boolean negated) {
        this.negated = negated;
        return this;
    }

    public StepBuilder put(final String key, final BytesValueSource value) {
        this.headerParams.put(key, value);
        return this;
    }

    public StepBuilder put(final String key, final Locator value) {
        this.locatorParams.put(key, value);
        return this;
    }

    public StepBuilder put(final String key, final String value) {
        this.stringParams.put(key, value);
        return this;
    }

    public StepBuilder stringParams(final Map<String, String> stringParams) {
        this.stringParams.putAll(stringParams);
        return this;
    }

    public StepBuilder locatorParams(final Map<String, Locator> locatorParams) {
        this.locatorParams.putAll(locatorParams);
        return this;
    }

    public StepBuilder headerParams(Map<String, BytesValueSource> headerParams) {
        this.headerParams.putAll(headerParams);
        return this;
    }

    public StepBuilder skip(final String aParam) {
        return this.put(Step.KEY_NAME_SKIP, aParam);
    }

    public Step build() {
        return new Step(this);
    }

}
