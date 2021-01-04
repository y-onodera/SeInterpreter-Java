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

import com.google.common.base.Objects;
import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.TestRun;

/**
 * Generic Wait that wraps a getter.
 *
 * @author zarkonnen
 */
public class WaitFor extends AbstractStepType implements GetterUseStep {
    public final Getter getter;

    public WaitFor(Getter getter) {
        this.getter = getter;
    }

    @Override
    public Getter getGetter() {
        return getter;
    }

    @Override
    public boolean run(TestRun ctx) {
        int maxWaitMs = 60000; // qqDPS Eventually get this from somewhere.
        if (ctx.containsKey("maxWait")) {
            maxWaitMs = Integer.parseInt(ctx.string("maxWait"));
        }
        int intervalMs = 500; // qqDPS Eventually get this from somewhere.
        if (ctx.containsKey("interval")) {
            intervalMs = Integer.parseInt(ctx.string("interval"));
        }
        long stopBy = System.currentTimeMillis() + maxWaitMs;
        // NB: If the step is negated, a result of "true"  means that we haven't succeeded yet.
        //     If the step is normal,  a result of "false" means that we haven't succeeded yet.
        while (!this.test(ctx) && System.currentTimeMillis() < stopBy) {
            try {
                Thread.sleep(intervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return this.test(ctx);
    }

    @Override
    public StepBuilder addDefaultParam(StepBuilder o) {
        if (!o.containsStringParam("maxWait")) {
            o.put("maxWait", "60000");
        }
        if (!o.containsStringParam("interval")) {
            o.put("interval", "500");
        }
        return GetterUseStep.super.addDefaultParam(o);
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        WaitFor waitFor = (WaitFor) o;
        return Objects.equal(getGetter(), waitFor.getGetter());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), getGetter());
    }
}
