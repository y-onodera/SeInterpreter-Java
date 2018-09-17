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

package com.sebuilder.interpreter.steptype;

import com.sebuilder.interpreter.LocatorHolder;
import com.sebuilder.interpreter.TestRun;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.interactions.Actions;

public class DragAndDropToElement implements ConditionalStep, LocatorHolder {
    @Override
    public boolean doRun(TestRun ctx) {
        new Actions(ctx.driver()).dragAndDrop(
                ctx.locator().find(ctx),
                ctx.locator("locator2").find(ctx)).build().perform();
        return true;
    }

    @Override
    public void supplementSerialized(JSONObject o) throws JSONException {
        LocatorHolder.super.supplementSerialized(o);
        if (!o.has("locator2")) {
            JSONObject locator2 = new JSONObject();
            locator2.put("type", "");
            locator2.put("value", "");
            o.put("locator2", locator2);
        }
    }
}
