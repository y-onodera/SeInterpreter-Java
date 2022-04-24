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
import com.github.romankh3.image.comparison.model.Rectangle;
import com.sebuilder.interpreter.*;
import com.sebuilder.interpreter.screenshot.LocatorInnerScrollElementHandler;
import com.sebuilder.interpreter.screenshot.Page;
import com.sebuilder.interpreter.screenshot.VerticalPrinter;
import com.sebuilder.interpreter.step.AbstractStepType;
import com.sebuilder.interpreter.step.LocatorHolder;
import org.openqa.selenium.remote.RemoteWebDriver;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
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
                actual = ctx.getScreenshot();
            } else {
                Page target = new Page(ctx, new LocatorInnerScrollElementHandler());
                actual = target.printImage(new VerticalPrinter(), 0);
            }
            if (ctx.getBoolean("verify")) {
                BufferedImage expect = ImageIO.read(new File(Context.getExpectScreenShotDirectory(), fileName));
                boolean compareResult = this.compare(file, actual, expect, ctx);
                if (!compareResult) {
                    File expectFile = ctx.getListener().saveExpectScreenshot(file);
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
        if (!o.containsLocatorParam("locatorHeader")) {
            LocatorHolder.super.addDefaultParam("locatorHeader", o);
        }
        if (!o.containsImageAreaParam("imageAreaExclude")) {
            o.put("imageAreaExclude", new ImageArea(""));
        }
        return o.apply(LocatorHolder.super::addDefaultParam);
    }

    protected boolean compare(File file, BufferedImage actual, BufferedImage expect, TestRun ctx) throws IOException {
        BufferedImage actualResize = actual;
        if (this.isSizeMissMatch(actual, expect)) {
            actualResize = this.toSameSize(actual, expect);
        }
        ImageArea exclude = ctx.getImageArea("imageAreaExclude");
        ImageComparisonResult result = this.getComparisonResult(file, actualResize, expect, exclude.getRectangles());
        result.getRectangles()
                .stream()
                .forEach(it -> ctx.log().info("diff rectangle: {},{},{},{}"
                        , it.getMinPoint().getX(), it.getMinPoint().getY()
                        , it.getMaxPoint().getX(), it.getMaxPoint().getY()));
        ImageIO.write(result.getResult(), "PNG", file);
        return result.getImageComparisonState() == ImageComparisonState.MATCH;
    }

    protected BufferedImage toSameSize(BufferedImage actual, BufferedImage expect) {
        BufferedImage finalImage = new BufferedImage(expect.getWidth(), expect.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D graphics = finalImage.createGraphics();
        graphics.drawImage(actual, 0, 0, null);
        graphics.dispose();
        return finalImage;
    }

    protected ImageComparisonResult getComparisonResult(File file, BufferedImage actual, BufferedImage expect, java.util.List<Rectangle> rectangles) {
        return new ImageComparison(expect, actual, file)
                .setExcludedAreas(rectangles)
                .setDrawExcludedRectangles(true)
                .compareImages();
    }

    protected boolean isSizeMissMatch(BufferedImage actual, BufferedImage expect) {
        return actual.getHeight() != expect.getHeight() || actual.getWidth() != expect.getWidth();
    }

}
