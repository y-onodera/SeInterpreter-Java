/*
 * Copyright 2016 Sauce Labs
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

package com.sebuilder.interpreter.step.type;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.step.AbstractStepType;

public class SwitchToWindowByTitle extends AbstractStepType {
    @Override
    public boolean run(final TestRun ctx) {
        final String title = ctx.string("title");
        for (final String h : ctx.driver().getWindowHandles()) {
            ctx.driver().switchTo().window(h);
            if (ctx.driver().getTitle().equals(title)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public StepBuilder addDefaultParam(final StepBuilder o) {
        if (!o.containsStringParam("title")) {
            o.put("title", "");
        }
        return o;
    }

}
