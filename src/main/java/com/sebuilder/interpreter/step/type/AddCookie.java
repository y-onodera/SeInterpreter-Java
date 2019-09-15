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
import org.openqa.selenium.Cookie;

import java.util.Date;

public class AddCookie extends AbstractStepType {
    @Override
    public boolean run(TestRun ctx) {
        Cookie.Builder cb = new Cookie.Builder(ctx.string("name"), ctx.string("value"));
        for (String opt : ctx.string("options").split(",")) {
            String[] kv = opt.split("=", 2);
            if (kv.length == 1) {
                continue;
            }
            if (kv[0].trim().equals("path")) {
                cb.path(kv[1].trim());
            }
            if (kv[0].trim().equals("max_age")) {
                cb.expiresOn(new Date(new Date().getTime() + Long.parseLong(kv[1].trim()) * 1000l));
            }
            ctx.driver().manage().addCookie(cb.build());
        }
        ctx.driver().navigate().refresh();
        return true;
    }

    @Override
    public StepBuilder addDefaultParam(StepBuilder o) {
        if (!o.containsStringParam("name")) {
            o.put("name", "");
        }
        if (!o.containsStringParam("value")) {
            o.put("value", "");
        }
        if (!o.containsStringParam("options")) {
            o.put("options", "");
        }
        return o;
    }

}
