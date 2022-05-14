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
 * Interface for classes that define how to finish a step of a particular type. Implementing classes
 * should be located in com.sebuilder.interpreter.steptype.
 *
 * @author zarkonnen
 */
public interface StepType extends StepElement {

    /**
     * Perform the action this step consists of.
     *
     * @param ctx Current test finish.
     * @return Whether the step succeeded. This should be true except for failed verify steps, which
     * should return false. Other failures should throw a RuntimeException.
     */
    boolean run(TestRun ctx);

    default String getStepTypeName() {
        return this.getClass().getSimpleName();
    }

    default StepBuilder toStep() {
        return new StepBuilder(this);
    }

    default boolean isContinueAtFailure() {
        return false;
    }

    default boolean isAcceptEndAdvice() {
        return true;
    }

    default  int getExecSteps(TestRun ctx) {
        return 1;
    }

}
