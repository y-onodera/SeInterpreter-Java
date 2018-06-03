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

import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

public class SetElementSelected extends ExportableStep {
    @Override
    public boolean run(TestRun ctx) {
        WebElement e = ctx.locator().find(ctx);
        if (ctx.containsKey("check") && !Boolean.parseBoolean(ctx.string("check"))) {
            if (e.isSelected()) {
                e.click();
            }
            return true;
        }
        if (!e.isSelected()) {
            e.click();
        }
        return true;
    }

    @Override
    protected void addElement(RemoteWebDriver driver, WebElement element, JSONObject step) throws JSONException {
        step.put("check", "true");
    }
}
