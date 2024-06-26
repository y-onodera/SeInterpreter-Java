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
import com.sebuilder.interpreter.StepElement;
import com.sebuilder.interpreter.TestRun;

/**
 * Interface to plug into the generic Assert/Verify/Store steps that does the work of actually
 * getting the relevant variable.
 *
 * @author zarkonnen
 */
public interface Getter extends StepElement {

    /**
     * @param ctx Current test run.
     * @return The value this getter gets, eg the page title.
     */
    String get(TestRun ctx);

    default boolean test(final TestRun ctx) {
        final String got = this.get(ctx);
        final boolean result;
        if (this.cmpParamName() == null) {
            result = Boolean.parseBoolean(got);
        } else {
            ctx.getListener().info("actual:" + got);
            final String expect = ctx.string(this.cmpParamName());
            ctx.getListener().info("expect:" + expect);
            result = expect.equals(got);
        }
        return result;
    }

    /**
     * @return The name of the parameter to compare the result of the get to, or null if the get
     * returns a boolean "true"/"false".
     */
    String cmpParamName();

    default WaitFor toWaitFor() {
        return new WaitFor(this);
    }

    default Store toStore() {
        return new Store(this);
    }

    default Print toPrint() {
        return new Print(this);
    }

    default Verify toVerify() {
        return new Verify(this);
    }

    default Assert toAssert() {
        return new Assert(this);
    }

    default If toIf() {
        return new If(this);
    }

    default Retry toRetry() {
        return new Retry(this);
    }

    @Override
    default StepBuilder addDefaultParam(final StepBuilder o) {
        if (this.cmpParamName() != null) {
            if (!o.containsStringParam(this.cmpParamName())) {
                o.put(this.cmpParamName(), "");
            }
        }
        return o;
    }

}
