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
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.screenshot.LocatorInnerScrollElementHandler;
import com.sebuilder.interpreter.screenshot.Page;
import com.sebuilder.interpreter.screenshot.VerticalPrinter;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class SaveScreenshot implements StepType, LocatorHolder {

    @Override
    public boolean run(TestRun ctx) {
        RemoteWebDriver wd = ctx.driver();
        Dimension beforeSize = wd.manage().window().getSize();
        if (this.needMaximize(wd)) {
            wd.manage().window().maximize();
            this.waitForRepaint();
        }
        wd.switchTo().defaultContent();
        Page target = new Page(ctx, 100, new LocatorInnerScrollElementHandler(wd));
        try {
            File file = new File(ctx.getListener().getScreenShotOutputDirectory(), ctx.getTestRunName() + "_" + ctx.string("file"));
            ImageIO.write(target.printImage(new VerticalPrinter(), 0), "PNG", file);
            this.returnSize(wd, beforeSize);
            return file.exists();
        } catch (IOException e) {
            ctx.log().error(e);
            this.returnSize(wd, beforeSize);
            return false;
        }
    }

    @Override
    public void supplementSerialized(JSONObject o) throws JSONException {
        LocatorHolder.super.supplementSerialized(o);
        if (!o.has("file")) {
            o.put("file", "");
        }
    }

    private boolean needMaximize(RemoteWebDriver wd) {
        return wd instanceof InternetExplorerDriver || wd instanceof EdgeDriver;
    }

    private void returnSize(RemoteWebDriver wd, Dimension beforeSize) {
        if (this.needMaximize(wd)) {
            wd.manage().window().setSize(beforeSize);
            this.waitForRepaint();
        }
    }

    private void waitForRepaint() {
        try {
            Thread.sleep(600);
        } catch (InterruptedException var2) {
            throw new IllegalStateException("Exception while waiting for repaint", var2);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        return this.getClass() == o.getClass();
    }

    @Override
    public int hashCode() {
        return this.getClass().getSimpleName().hashCode();
    }
}
