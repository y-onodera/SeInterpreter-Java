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
import com.sebuilder.interpreter.step.AbstractStepType;
import com.sebuilder.interpreter.step.LocatorHolder;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

public class SetElementText extends AbstractStepType implements ConditionalStep, Exportable, LocatorHolder {

    @Override
    public boolean doRun(TestRun ctx) {
        WebElement el = ctx.locator().find(ctx);
        el.click();
        el.clear();
        String input = ctx.text();
        if (isIncludeHalfWidthText(input)) {
            ctx.driver().executeScript("arguments[0].value = arguments[1]", el, input);
            el.sendKeys(Keys.TAB);
        } else {
            el.sendKeys(input);
            el.sendKeys(Keys.TAB);
        }
        return true;
    }

    @Override
    public void addElement(ExportResourceBuilder builder, RemoteWebDriver driver, WebElement element) {
        String text = element.getText();
        if (text.isEmpty()) {
            text = element.getAttribute("value");
        }
        builder.stepOption("text", text);
    }

    @Override
    public StepBuilder addDefaultParam(StepBuilder o) {
        if (!o.containsStringParam("text")) {
            o.put("text", "");
        }
        return o.apply(LocatorHolder.super::addDefaultParam);
    }

    private boolean isIncludeHalfWidthText(String input) {
        return input.chars().anyMatch(i -> isHalfWidth((char) i));
    }

    private boolean isHalfWidth(char c) {
        return '\uFF65' <= c && c <= '\uFF9F';
    }

}
