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

import com.github.romankh3.image.comparison.ImageComparison;
import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.github.romankh3.image.comparison.model.ImageComparisonState;
import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.screenshot.LocatorInnerScrollElementHandler;
import com.sebuilder.interpreter.screenshot.Page;
import com.sebuilder.interpreter.screenshot.VerticalPrinter;
import com.sebuilder.interpreter.step.AbstractStepType;
import com.sebuilder.interpreter.step.LocatorHolder;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.remote.RemoteWebDriver;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

public class SaveScreenshot extends AbstractStepType implements LocatorHolder {

    @Override
    public boolean isContinueAtFailure() {
        return true;
    }

    @Override
    public boolean run(TestRun ctx) {
        RemoteWebDriver wd = ctx.driver();
        wd.switchTo().defaultContent();
        try {
            final String fileName = ctx.getTestRunName() + "_" + ctx.string("file");
            File file = ctx.getListener().addScreenshot(fileName);
            BufferedImage actual;
            if (ctx.getBoolean("default")) {
                try (ByteArrayInputStream imageArrayStream = new ByteArrayInputStream(wd.getScreenshotAs(OutputType.BYTES))) {
                    actual = ImageIO.read(imageArrayStream);
                } catch (IOException var9) {
                    throw new RuntimeException("Can not load screenshot data", var9);
                }
            } else {
                Page target = new Page(ctx, new LocatorInnerScrollElementHandler());
                actual = target.printImage(new VerticalPrinter(), 0);
            }
            if (ctx.getBoolean("verify")) {
                BufferedImage expect = ImageIO.read(new File(Context.getExpectScreenShotDirectory(), fileName));
                boolean compareResult = this.compare(file, actual, expect);
                if (!compareResult) {
                    File expectFile = ctx.getListener().saveExpectScreenshot();
                    ImageIO.write(expect, "PNG", expectFile);
                }
                return compareResult;
            }
            ImageIO.write(actual, "PNG", file);
            return file.exists();
        } catch (IOException e) {
            ctx.log().error(e);
            return false;
        }
    }

    @Override
    public StepBuilder addDefaultParam(StepBuilder o) {
        if (!o.containsStringParam("file")) {
            o.put("file", "");
        }
        if (!o.containsStringParam("verify")) {
            o.put("verify", "false");
        }
        if (!o.containsStringParam("default")) {
            o.put("default", "false");
        }
        if (!o.containsStringParam("locatorHeader")) {
            LocatorHolder.super.addDefaultParam("locatorHeader", o);
        }
        return o.apply(LocatorHolder.super::addDefaultParam);
    }

    protected boolean compare(File file, BufferedImage actual, BufferedImage expect) throws IOException {
        if (this.isSizeMissMatch(actual, expect)) {
            BufferedImage resizeActual = this.toSameSize(actual, expect.getWidth(), expect.getHeight());
            ImageIO.write(this.getComparisonResult(file, resizeActual, expect).getResult(), "PNG", file);
            return false;
        }
        ImageComparisonResult result = this.getComparisonResult(file, actual, expect);
        ImageIO.write(result.getResult(), "PNG", file);
        return result.getImageComparisonState() == ImageComparisonState.MATCH;
    }

    protected BufferedImage toSameSize(BufferedImage actual, int expectWidth, int expectHeight) {
        BufferedImage finalImage = new BufferedImage(expectWidth, expectHeight, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D graphics = finalImage.createGraphics();
        graphics.drawImage(actual, 0, 0, null);
        graphics.dispose();
        return finalImage;
    }

    protected ImageComparisonResult getComparisonResult(File file, BufferedImage actual, BufferedImage expect) {
        return new ImageComparison(expect, actual, file)
                .setDrawExcludedRectangles(true)
                .compareImages();
    }

    protected boolean isSizeMissMatch(BufferedImage actual, BufferedImage expect) {
        return actual.getHeight() != expect.getHeight() || actual.getWidth() != expect.getWidth();
    }

}
