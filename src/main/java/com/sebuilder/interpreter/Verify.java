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

package com.sebuilder.interpreter;

import com.google.common.base.Objects;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Generic Verify that wraps a getter.
 * @author zarkonnen
 */
public class Verify implements StepType {
	public final Getter getter;

	public Verify(Getter getter) {
		this.getter = getter;
	}
	
	@Override
	public boolean run(TestRun ctx) {
		String got = getter.get(ctx);
		boolean result = getter.cmpParamName() == null
				? Boolean.parseBoolean(got)
				: ctx.string(getter.cmpParamName()).equals(got);
		return result != ctx.currentStep().isNegated();
	}

    @Override
    public void supplementSerialized(JSONObject o) throws JSONException {
        getter.supplementSerialized(o);
        if (getter.cmpParamName() != null) {
            if (!o.has(getter.cmpParamName())) {
                o.put(getter.cmpParamName(), "");
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Verify verify = (Verify) o;
        return Objects.equal(getter, verify.getter);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getter);
    }
}