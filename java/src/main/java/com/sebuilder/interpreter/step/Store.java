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

import java.util.Objects;

/**
 * Generic Store that wraps a getter.
 *
 * @author zarkonnen
 */
public class Store extends AbstractStepType implements GetterUseStep {
    public final Getter getter;

    public Store(final Getter getter) {
        this.getter = getter;
    }

    @Override
    public Getter getGetter() {
        return this.getter;
    }

    @Override
    public boolean run(final TestRun ctx) {
        String value = this.getter.get(ctx);
        if (ctx.currentStep().containsParam("regex")) {
            value = value.replaceAll(ctx.string("regex"), ctx.string("replacement"));
        }
        if (ctx.currentStep().negated()) {
            value = String.valueOf(!Boolean.parseBoolean(value));
        }
        ctx.putVars(ctx.string("variable"), value);
        return true;
    }

    @Override
    public StepBuilder addDefaultParam(final StepBuilder o) {
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
    public boolean equals(final Object o) {
        if (!super.equals(o)) {
            return false;
        }
        final Store store = (Store) o;
        return Objects.equals(this.getGetter(), store.getGetter());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.getGetter());
    }
}