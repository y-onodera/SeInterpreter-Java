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

import java.util.ArrayList;

public class SwitchToWindowByIndex extends AbstractStepType {
    @Override
    public boolean run(final TestRun ctx) {
        // Converting the set into a List is hopefully OK because it's a
        // LinkedHashSet, and so the order should be the same as from the
        // server.
        final ArrayList<String> handles = new ArrayList<String>(ctx.driver().getWindowHandles());
        final int index = Integer.parseInt(ctx.string("index"));
        if (index >= handles.size()) {
            throw new ArrayIndexOutOfBoundsException("Cannot switch to window index " + index + ". There are only " + handles.size() + " window handles available.");
        }
        ctx.driver().switchTo().window(handles.get(index));
        return true;
    }

    @Override
    public StepBuilder addDefaultParam(final StepBuilder o) {
        if (!o.containsStringParam("index")) {
            o.put("index", "");
        }
        return o;
    }

}
