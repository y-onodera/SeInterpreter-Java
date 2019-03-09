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

import com.google.common.base.Objects;
import com.sebuilder.interpreter.Exportable;
import com.sebuilder.interpreter.LocatorHolder;
import com.sebuilder.interpreter.TestRun;

public class ClickElement implements ConditionalStep, Exportable, LocatorHolder {

    @Override
    public boolean doRun(TestRun ctx) {
        ctx.locator().find(ctx).click();
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        return this.getClass() == o.getClass();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.getClass().getSimpleName());
    }
}
