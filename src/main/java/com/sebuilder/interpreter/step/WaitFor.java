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
import com.sebuilder.interpreter.TestRun;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Generic Wait that wraps a getter.
 *
 * @author zarkonnen
 */
public class WaitFor implements GetterUseStep {
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
        boolean result;
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
    public void supplementSerialized(JSONObject o) throws JSONException {
        GetterUseStep.super.supplementSerialized(o);
        if (!o.has("maxWait")) {
            o.put("maxWait", "60000");
        }
        if (!o.has("interval")) {
            o.put("interval", "500");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WaitFor waitFor = (WaitFor) o;
        return Objects.equal(getter, waitFor.getter);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getter);
    }
}
