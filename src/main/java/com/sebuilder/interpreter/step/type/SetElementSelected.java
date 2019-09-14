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

package com.sebuilder.interpreter.step.type;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.export.Exportable;
import com.sebuilder.interpreter.step.LocatorHolder;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

public class SetElementSelected extends AbstractStepType implements ConditionalStep, Exportable, LocatorHolder {

    @Override
    public boolean doRun(TestRun ctx) {
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
    public void addElement(ExportResourceBuilder builder, RemoteWebDriver driver, WebElement element) {
        builder.stepOption("check", String.valueOf(element.isSelected()));
    }

    @Override
    public StepBuilder addDefaultParam(StepBuilder o) {
        if (!o.containsStringParam("check")) {
            o.put("check", "");
        }
        return o.apply(LocatorHolder.super::addDefaultParam);
    }

}
