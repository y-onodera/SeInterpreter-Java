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
public record Step(
        String name,
        StepType type,
        boolean negated,
        Map<String, String> stringParams,
        Map<String, Locator> locatorParams
) {
    public static final String KEY_NAME_SKIP = "skip";

    public Step(StepType type) {
        this("", type, false);
    }

    public Step(String name, StepType type, boolean isNegated) {
        this(name, type, isNegated, new HashMap<>(), new HashMap<>());
    }

    public Step(StepBuilder stepBuilder) {
        this(stepBuilder.getName()
                , stepBuilder.getStepType()
                , stepBuilder.isNegated()
                , new HashMap<>(stepBuilder.getStringParams())
                , new HashMap<>(stepBuilder.getLocatorParams())
        );
    }

    public Result execute(TestRun ctx) {
        try {
            boolean aspectSuccess = ctx.startTest();
            int execSteps = this.type.getExecSteps(ctx);
            if (this.run(ctx)) {
                return new Result(ctx.processTestSuccess(this.type.isAcceptEndAdvice()) && aspectSuccess, execSteps);
            }
            return new Result(ctx.processTestFailure(this.type.isAcceptEndAdvice()), execSteps);
        } catch (Throwable e) {
            throw ctx.processTestError(e);
        }
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

    public Locator getLocator(String key) {
        return this.locatorParams.get(key);
    }

    public boolean locatorContains(String key) {
        return this.locatorParams.containsKey(key);
    }

    public Step copy() {
        return this.builder().build();
    }

    public TestCase toTestCase() {
        return new TestCaseBuilder().addStep(this).build();
    }

    public StepBuilder builder() {
        return new StepBuilder(this.type)
                .name(this.name)
                .negated(this.negated)
                .stringParams(this.stringParams)
                .locatorParams(this.locatorParams)
                ;
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
        if (this.name != null) {
            sb.append(this.name).append(": ");
        }
        sb.append(this.type.getStepTypeName());
        if (this.type instanceof GetterUseStep) {
            sb.append(" negated=").append(this.negated);
        }
        this.stringParams.forEach((key, value) -> sb.append(" ").append(key).append("=").append(value));
        this.locatorParams.forEach((key, value) -> sb.append(" ").append(key).append("=").append(value.toPrettyString()));
        return sb.toString();
    }

    public Map<String, String> toMap() {
        Map<String, String> result = new HashMap<>();
        if (this.name != null) {
            result.put("name", this.name);
        }
        result.put("type", this.type.getStepTypeName());
        result.putAll(this.stringParams);
        this.locatorParams.forEach((key, value) -> {
            result.put(key + ".type", value.type());
            result.put(key + ".value", value.value());
        });
        return result;
    }

    private boolean isSkippable() {
        return this.stringParams.containsKey(KEY_NAME_SKIP);
    }

    public static record Result(boolean success, int execSteps) {
    }

}
