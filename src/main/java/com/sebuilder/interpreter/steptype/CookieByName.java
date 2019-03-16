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

import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.step.Getter;
import org.json.JSONException;
import org.json.JSONObject;

public class CookieByName implements Getter {
    @Override
    public String get(TestRun ctx) {
        return ctx.driver().manage().getCookieNamed(ctx.string("name")).getValue();
    }

    @Override
    public String cmpParamName() {
        return "value";
    }

    @Override
    public void supplementSerialized(JSONObject o) throws JSONException {
        if (!o.has("name")) {
            o.put("name", "");
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
