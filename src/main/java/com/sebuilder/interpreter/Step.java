/*
 * Copyright 2012 Sauce Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sebuilder.interpreter;

import com.sebuilder.interpreter.step.GetterUseStep;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A Selenium 2 step.
 *
 * @author jkowalczyk
 */
public class Step {
    public static final String KEY_NAME_SKIP = "skip";
    private boolean negated;
    private String name;
    private final StepType type;
    private final HashMap<String, String> stringParams = new HashMap<>();
    private final HashMap<String, Locator> locatorParams = new HashMap<>();

    public Step(StepType type) {
        this.type = type;
    }

    public Step(String name, StepType type, boolean isNegated) {
        this.name = name;
        this.type = type;
        this.negated = isNegated;
    }

    public Step(StepBuilder stepBuilder) {
        this.name = stepBuilder.getName();
        this.type = stepBuilder.getStepType();
        this.negated = stepBuilder.isNegated();
        this.stringParams.putAll(stepBuilder.getStringParams());
        this.locatorParams.putAll(stepBuilder.getLocatorParams());
    }

    public String getName() {
        return name;
    }

    public StepType getType() {
        return this.type;
    }

    public boolean isNegated() {
        return negated;
    }

    public boolean run(TestRun testRun) {
        if (this.isSkip(testRun.vars())) {
            return true;
        }
        return this.type.run(testRun);
    }

    public boolean isSkip(InputData vars) {
        if (this.isSkippable()) {
            return vars.evaluate(this.getParam(KEY_NAME_SKIP));
        }
        return false;
    }

    public Collection<String> paramKeys() {
        return this.stringParams.keySet();
    }

    public String getParam(String paramName) {
        return this.stringParams.get(paramName);
    }

    public boolean containsParam(String paramKey) {
        return this.stringParams.containsKey(paramKey);
    }

    public Collection<String> locatorKeys() {
        return this.locatorParams.keySet();
    }

    public Locator getLocator(String locatorName) {
        return this.locatorParams.get(locatorName);
    }

    public boolean locatorContains(String locatorName) {
        return this.locatorParams.containsKey(locatorName);
    }

    public Step copy() {
        return this.builder().build();
    }

    public TestCase toTestCase() {
        return new TestCaseBuilder().addStep(this).build();
    }

    public StepBuilder builder() {
        return new StepBuilder(this.getType())
                .name(this.name)
                .negated(this.negated)
                .stringParams(this.stringParams)
                .locatorParams(this.locatorParams);
    }

    public Step withAllParam() {
        StepBuilder o = this.builder();
        this.type.addDefaultParam(o);
        return o.build();
    }

    @Override
    public String toString() {
        return this.toPrettyString();
    }

    public String toPrettyString() {
        StringBuilder sb = new StringBuilder();
        if (name != null) {
            sb.append(name).append(": ");
        }
        sb.append(type.getStepTypeName());

        if (type instanceof GetterUseStep) {
            sb.append(" negated=" + this.negated);
        }

        for (Map.Entry<String, String> pe : stringParams.entrySet()) {
            sb.append(" ").append(pe.getKey()).append("=").append(pe.getValue());
        }
        for (Map.Entry<String, Locator> le : locatorParams.entrySet()) {
            sb.append(" ").append(le.getKey()).append("=").append(le.getValue().toPrettyString());
        }
        return sb.toString();
    }

    private boolean isSkippable() {
        return this.stringParams.containsKey(KEY_NAME_SKIP);
    }

}
