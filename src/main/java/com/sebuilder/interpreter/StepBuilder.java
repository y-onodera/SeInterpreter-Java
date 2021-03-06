package com.sebuilder.interpreter;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.function.Function;

public class StepBuilder {
    private boolean negated;
    private String name;
    private StepType stepType;
    private Map<String, String> stringParams = Maps.newHashMap();
    private Map<String, Locator> locatorParams = Maps.newHashMap();

    public StepBuilder(StepType stepType) {
        this(null, stepType, false);
    }

    public StepBuilder(String name, StepType stepType, boolean negated) {
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

    public boolean containsStringParam(String paramKey) {
        return this.stringParams.containsKey(paramKey);
    }

    public boolean containsLocatorParam(String paramKey) {
        return this.locatorParams.containsKey(paramKey);
    }

    public StepBuilder apply(Function<StepBuilder, StepBuilder> function) {
        return function.apply(this);
    }

    public StepBuilder locator(Locator value) {
        this.put("locator", value);
        return this;
    }

    public StepBuilder name(String name) {
        this.name = name;
        return this;
    }

    public StepBuilder negated(boolean negated) {
        this.negated = negated;
        return this;
    }

    public StepBuilder put(String key, Locator value) {
        this.locatorParams.put(key, value);
        return this;
    }

    public StepBuilder put(String key, String value) {
        this.stringParams.put(key, value);
        return this;
    }

    public StepBuilder stringParams(Map<String, String> stringParams) {
        this.stringParams.putAll(stringParams);
        return this;
    }

    public StepBuilder locatorParams(Map<String, Locator> locatorParams) {
        this.locatorParams.putAll(locatorParams);
        return this;
    }

    public StepBuilder skip(String aParam) {
        return this.put(Step.KEY_NAME_SKIP,aParam);
    }

    public Step build() {
        return new Step(this);
    }

}
