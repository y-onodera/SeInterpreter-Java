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
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

public class SetElementText implements StepType {

    public static final String HALF_WIDTH = "half-width";

    @Override
    public boolean run(TestRun ctx) {
        WebElement el = ctx.locator().find(ctx);
        el.click();
        el.clear();
        String input = ctx.text();
        if (isIncludeHalfWidhText(input)) {
            ctx.driver().executeScript("arguments[0].value = arguments[1]", el, input);
        } else {
            el.sendKeys(input);
            el.sendKeys(Keys.TAB);
        }
        return true;
    }

    private boolean isIncludeHalfWidhText(String input) {
        return input.chars().anyMatch(i -> isHalfWidth((char) i));
    }

    private boolean isHalfWidth(char c) {
        return '\u0000' <= c && c <= '\u00FF'
                || '\uFF61' <= c && c <= '\uFFDC'
                || '\uFFE8' <= c && c <= '\uFFEE';
    }
}
