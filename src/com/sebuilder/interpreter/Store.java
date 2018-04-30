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

/**
 * Generic Store that wraps a getter.
 *
 * @author zarkonnen
 */
public class Store implements StepType {
    public final Getter getter;

    public Store(Getter getter) {
        this.getter = getter;
    }

    @Override
    public boolean run(TestRun ctx) {
        String value = getter.get(ctx);
        if (ctx.currentStep().stringParams.containsKey("regex")) {
            value = value.replaceAll(ctx.string("regex"), ctx.string("replacement"));
        }
        ctx.vars().put(ctx.string("variable"), value);
        return true;
    }
}