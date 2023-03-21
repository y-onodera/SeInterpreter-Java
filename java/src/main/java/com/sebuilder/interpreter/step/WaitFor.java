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

import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.TestRun;

import java.util.Objects;

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
        int maxWaitMs = 60000; // qqDPS Eventually get this from somewhere.
        if (ctx.containsKey("maxWait")) {
            maxWaitMs = Integer.parseInt(ctx.string("maxWait"));
        } else if (Context.getEnvironmentProperties().containsKey("maxWait")) {
            maxWaitMs = Integer.parseInt(Context.bindEnvironmentProperties("${env.maxWait}"));
        }
        int intervalMs = 500; // qqDPS Eventually get this from somewhere.
        if (ctx.containsKey("interval")) {
            intervalMs = Integer.parseInt(ctx.string("interval"));
        } else if (Context.getEnvironmentProperties().containsKey("interval")) {
            intervalMs = Integer.parseInt(Context.bindEnvironmentProperties("${env.interval}"));
        }
        final long stopBy = System.currentTimeMillis() + maxWaitMs;
        // NB: If the step is negated, a result of "true"  means that we haven't succeeded yet.
        //     If the step is normal,  a result of "false" means that we haven't succeeded yet.
        while (!this.test(ctx) && System.currentTimeMillis() < stopBy) {
            try {
                Thread.sleep(intervalMs);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return this.test(ctx);
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
