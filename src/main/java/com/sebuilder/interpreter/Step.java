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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * A Selenium 2 step.
 *
 * @author jkowalczyk
 */
public class Step {
    private boolean negated;
    private String name;
    private StepType type;
    private HashMap<String, String> stringParams = new HashMap<>();
    private HashMap<String, Locator> locatorParams = new HashMap<>();

    public Step(StepType type) {
        this.type = type;
    }

    public Step(String name, StepType type, boolean isNegated) {
        this.name = name;
        this.type = type;
        this.negated = isNegated;
    }

    public String getName() {
        return name;
    }

    public StepType getType() {
        return this.type;
    }

    public boolean isNegated() {
        return negated;
    }

    public boolean run(TestRun testRun) {
        if (this.isSkip()) {
            return true;
        }
        return this.type.run(testRun);
    }

    public boolean isSkip() {
        if (this.isSkippable()) {
            return Boolean.parseBoolean(TestRuns.replaceVariable(this.getParam("skip"), this.stringParams));
        }
        return false;
    }

    public void put(String key, String value) {
        this.stringParams.put(key, value);
    }

    public String getParam(String paramName) {
        return this.stringParams.get(paramName);
    }

    public boolean containsParam(String paramKey) {
        return this.stringParams.containsKey(paramKey);
    }

    public void put(String key, Locator value) {
        this.locatorParams.put(key, value);
    }

    public Locator getLocator(String locatorName) {
        return this.locatorParams.get(locatorName);
    }

    public boolean locatorContains(String locatorName) {
        return this.locatorParams.containsKey(locatorName);
    }

    public Step copy() {
        Step newStep = new Step(this.name, this.type, this.negated);
        this.stringParams.entrySet().forEach(it -> newStep.stringParams.put(it.getKey(), it.getValue()));
        this.locatorParams.entrySet().forEach(it -> newStep.locatorParams.put(it.getKey(), it.getValue().copy()));
        return newStep;
    }

    @Override
    public String toString() {
        try {
            return toJSON().toString();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public String toPrettyString() {
        boolean negateEnable = false;
        StringBuilder sb = new StringBuilder();
        if (name != null) {
            sb.append(name).append(": ");
        }
        if (type instanceof Assert) {
            sb.append("assert").append(((Assert) type).getter.getClass().getSimpleName());
            negateEnable = true;
        } else if (type instanceof Verify) {
            sb.append("verify").append(((Verify) type).getter.getClass().getSimpleName());
            negateEnable = true;
        } else if (type instanceof WaitFor) {
            sb.append("waitFor").append(((WaitFor) type).getter.getClass().getSimpleName());
            negateEnable = true;
        } else if (type instanceof Store) {
            sb.append("store").append(((Store) type).getter.getClass().getSimpleName());
            negateEnable = true;
        } else if (this.type instanceof Print) {
            sb.append("print").append(((Print) type).getter.getClass().getSimpleName());
            negateEnable = true;
        } else {
            sb.append(type.getClass().getSimpleName());
        }

        if (negateEnable) {
            sb.append(" negated=" + this.negated);
        }

        for (Map.Entry<String, String> pe : stringParams.entrySet()) {
            sb.append(" ").append(pe.getKey()).append("=").append(pe.getValue());
        }
        for (Map.Entry<String, Locator> le : locatorParams.entrySet()) {
            sb.append(" ").append(le.getKey()).append("=").append(le.getValue().toPrettyString());
        }
        return sb.toString();
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject o = new JSONObject();
        if (this.name != null) {
            o.put("step_name", this.name);
        }
        if (this.type instanceof Assert) {
            o.put("type", "assert" + ((Assert) this.type).getter.getClass().getSimpleName());
        } else if (this.type instanceof Verify) {
            o.put("type", "verify" + ((Verify) this.type).getter.getClass().getSimpleName());
        } else if (this.type instanceof WaitFor) {
            o.put("type", "waitFor" + ((WaitFor) this.type).getter.getClass().getSimpleName());
        } else if (this.type instanceof Store) {
            o.put("type", "store" + ((Store) this.type).getter.getClass().getSimpleName());
        } else if (this.type instanceof Print) {
            o.put("type", "print" + ((Print) this.type).getter.getClass().getSimpleName());
        } else {
            o.put("type", this.type.getClass().getSimpleName());
        }
        o.put("negated", this.negated);
        for (Map.Entry<String, String> pe : this.stringParams.entrySet()) {
            o.put(pe.getKey(), pe.getValue());
        }
        for (Map.Entry<String, Locator> le : this.locatorParams.entrySet()) {
            o.put(le.getKey(), le.getValue().toJSON());
        }
        if (!isSkippable()) {
            o.put("skip", "false");
        }
        return o;
    }

    public JSONObject toFullJSON() throws JSONException {
        JSONObject o = this.toJSON();
        this.type.supplementSerialized(o);
        return o;
    }

    private boolean isSkippable() {
        return this.stringParams.containsKey("skip");
    }

}
