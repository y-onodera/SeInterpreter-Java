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

package com.sebuilder.interpreter.step;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.TestRun;
import org.openqa.selenium.StaleElementReferenceException;

import java.util.Objects;
import java.util.Optional;

/**
 * Generic Wait that wraps a getter.
 *
 * @author zarkonnen
 */
public class WaitFor extends AbstractStepType implements GetterUseStep {
    public final Getter getter;

    public WaitFor(final Getter getter) {
        this.getter = getter;
    }

    @Override
    public Getter getGetter() {
        return this.getter;
    }

    @Override
    public boolean run(final TestRun ctx) {
        final boolean negated = ctx.currentStep().negated();
        // A stale element means the page is still re-rendering it. That is neither a pass nor a
        // fail (whether negated or not), so keep polling; the poll also replaces the getter's own
        // retry, which is why testOnce() is used.
        return Polling.from(ctx)
                .retryOn(StaleElementReferenceException.class)
                .until(() -> this.getGetter().testOnce(ctx) != negated ? Optional.of(true) : Optional.empty());
    }

    @Override
    public StepBuilder addDefaultParam(final StepBuilder o) {
        if (!o.containsStringParam("maxWait")) {
            o.put("maxWait", "60000");
        }
        if (!o.containsStringParam("interval")) {
            o.put("interval", "500");
        }
        return GetterUseStep.super.addDefaultParam(o);
    }

    @Override
    public boolean equals(final Object o) {
        if (!super.equals(o)) {
            return false;
        }
        final WaitFor waitFor = (WaitFor) o;
        return Objects.equals(this.getGetter(), waitFor.getGetter());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.getGetter());
    }
}
