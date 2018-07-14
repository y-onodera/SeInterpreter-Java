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

import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.screenshot.LocatorInnerScrollElementHandler;
import com.sebuilder.interpreter.screenshot.Page;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class SaveScreenshot implements StepType {

    @Override
    public boolean run(TestRun ctx) {
        RemoteWebDriver wd = ctx.driver();
        wd.switchTo().defaultContent();
        wd.manage().window().maximize();
        Page target = new Page(ctx, 100, new LocatorInnerScrollElementHandler(wd));
        try {
            File file = new File(Context.getInstance().getScreenShotOutputDirectory(), ctx.suiteName() + "_" + ctx.scriptName() + "_" + ctx.string("file"));
            ImageIO.write(target.getFinalImage(), "PNG", file);
            return file.exists();
        } catch (IOException e) {
            ctx.log().error(e);
            return false;
        }
    }
}
