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
 * Generic Store that wraps a getter.
 *
 * @author zarkonnen
 */
public class Store implements GetterUseStep {
    public final Getter getter;

    public Store(Getter getter) {
        this.getter = getter;
    }

    @Override
    public Getter getGetter() {
        return getter;
    }

    @Override
    public boolean run(TestRun ctx) {
        String value = getter.get(ctx);
        if (ctx.currentStep().containsParam("regex")) {
            value = value.replaceAll(ctx.string("regex"), ctx.string("replacement"));
        }
        if (ctx.currentStep().isNegated()) {
            value = String.valueOf(!Boolean.valueOf(value));
        }
        ctx.putVars(ctx.string("variable"), value);
        return true;
    }

    @Override
    public StepBuilder addDefaultParam(StepBuilder o) {
        if (!o.containsStringParam("regex")) {
            o.put("regex", "");
        }
        if (!o.containsStringParam("replacement")) {
            o.put("replacement", "");
        }
        if (!o.containsStringParam("variable")) {
            o.put("variable", "");
        }
        return GetterUseStep.super.addDefaultParam(o);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Store store = (Store) o;
        return Objects.equal(getter, store.getter);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getter);
    }
}