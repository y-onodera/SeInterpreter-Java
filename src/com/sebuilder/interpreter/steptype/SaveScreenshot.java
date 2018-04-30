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

import java.io.File;
import java.io.IOException;

import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;
import ru.yandex.qatools.ashot.shooting.ShootingStrategy;

import javax.imageio.ImageIO;

public class SaveScreenshot implements StepType {
    @Override
    public boolean run(TestRun ctx) {
        Screenshot screenshot = new AShot()
                .shootingStrategy(getStrategy(ctx))
                .takeScreenshot(ctx.driver());

        File file = new File(ctx.string("file"));
        try {
            ImageIO.write(screenshot.getImage(), "PNG", file);
            return file.exists();
        } catch (IOException e) {
            ctx.log().error(e);
            return false;
        }
    }

    private ShootingStrategy getStrategy(TestRun ctx) {
        if (ctx.currentStep().locatorParams.containsKey("locator")) {
            if (ctx.containsKey("onlySelected")) {
                return new OnlySelectedViewportPastingDecorator(ShootingStrategies.simple(), ctx).withScrollTimeout(100);
            }
            return new SelectedViewportPastingDecorator(ShootingStrategies.simple(), ctx).withScrollTimeout(100);
        }
        return ShootingStrategies.viewportPasting(100);
    }
}
