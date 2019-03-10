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
package com.sebuilder.interpreter.factory;

import com.sebuilder.interpreter.Getter;
import com.sebuilder.interpreter.Locator;
import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Factory to create a StepType object from the step's name. Each step can be
 * loaded from a settable primary package or a secondary package. Thanks to this
 * mechanism, steps can be easily overridden when needed.
 *
 * @author jkowalczyk
 */
public class StepTypeFactory {
    public static final String DEFAULT_PACKAGE = "com.sebuilder.interpreter.steptype";

    /**
     * Primary package used to load stepType instances
     */
    private String primaryPackage = DEFAULT_PACKAGE;
    /**
     * Secondary package used to load stepType instances when first package is
     * not found
     */
    private String secondaryPackage = DEFAULT_PACKAGE;

    public void setPrimaryPackage(String primaryPackage) {
        this.primaryPackage = primaryPackage;
    }

    public void setSecondaryPackage(String secondaryPackage) {
        this.secondaryPackage = secondaryPackage;
    }

    /**
     * Mapping of the names of step types to their implementing classes, lazily
     * loaded through reflection. StepType classes must be either in the first
     * package either in the second one and their name must be the capitalized
     * name of their type. For example, the class for "get" is at
     * com.sebuilder.interpreter.steptype.Get.
     * <p>
     * Assert/Verify/WaitFor/Store steps use "Getter" objects that encapsulate
     * how to get the value they are about. Getters should be named e.g "Title"
     * for "verifyTitle" and also be in the com.sebuilder.interpreter.steptype
     * package.
     */
    private final HashMap<String, StepType> typesMap = new HashMap<String, StepType>();

    /**
     * @param o json object step load from
     * @throws JSONException If anything goes wrong with interpreting the JSON.
     */
    public ArrayList<Step> parseStep(JSONObject o) throws JSONException {
        JSONArray stepsA = o.getJSONArray("steps");
        ArrayList<Step> steps = new ArrayList<>();
        for (int i = 0; i < stepsA.length(); i++) {
            this.parseStep(steps, stepsA.getJSONObject(i));
        }
        return steps;
    }

    /**
     * @param name
     * @return a stepType instance for a given name
     */
    public StepType getStepTypeOfName(String name) {
        try {
            if (!typesMap.containsKey(name)) {
                String className = name.substring(0, 1).toUpperCase() + name.substring(1);
                boolean rawStepType = true;
                if (name.startsWith("assert")) {
                    className = className.substring("assert".length());
                    rawStepType = false;
                }
                if (name.startsWith("verify")) {
                    className = className.substring("verify".length());
                    rawStepType = false;
                }
                if (name.startsWith("waitFor")) {
                    className = className.substring("waitFor".length());
                    rawStepType = false;
                }
                if (name.startsWith("store") && !name.equals("store")) {
                    className = className.substring("store".length());
                    rawStepType = false;
                }
                if (name.startsWith("print") && !name.equals("print")) {
                    className = className.substring("print".length());
                    rawStepType = false;
                }
                if (name.startsWith("if") && !name.equals("if")) {
                    className = className.substring("if".length());
                    rawStepType = false;
                }
                if (name.startsWith("retry") && !name.equals("retry")) {
                    className = className.substring("retry".length());
                    rawStepType = false;
                }
                if (name.equals("retry")) {
                    className = "Retry";
                    rawStepType = false;
                }
                if (name.equals("loop")) {
                    className = "Loop";
                    rawStepType = false;
                }
                if (name.equals("if")) {
                    className = "If";
                    rawStepType = false;
                }
                Class<?> c = null;
                try {
                    c = Class.forName(primaryPackage + "." + className);
                } catch (ClassNotFoundException cnfe) {
                    try {
                        if (secondaryPackage != null) {
                            c = Class.forName(secondaryPackage + "." + className);
                        }
                    } catch (ClassNotFoundException cnfe2) {
                        throw new RuntimeException("No implementation class for step type \"" + name + "\" could be found.", cnfe);
                    }
                }
                if (c != null) try {
                    Object o = c.getDeclaredConstructor().newInstance();
                    if (name.startsWith("assert")) {
                        typesMap.put(name, Getter.class.cast(o).toAssert());
                    } else if (name.startsWith("verify")) {
                        typesMap.put(name, Getter.class.cast(o).toVerify());
                    } else if (name.startsWith("waitFor")) {
                        typesMap.put(name, Getter.class.cast(o).toWaitFor());
                    } else if (name.startsWith("store") && !name.equals("store")) {
                        typesMap.put(name, Getter.class.cast(o).toStore());
                    } else if (name.startsWith("print") && !name.equals("print")) {
                        typesMap.put(name, Getter.class.cast(o).toPrint());
                    } else if (name.startsWith("if") && !name.equals("if")) {
                        typesMap.put(name, Getter.class.cast(o).toIf());
                    } else if (name.startsWith("retry") && !name.equals("retry")) {
                        typesMap.put(name, Getter.class.cast(o).toRetry());
                    } else {
                        typesMap.put(name, (StepType) o);
                    }
                } catch (InstantiationException | IllegalAccessException ie) {
                    throw new RuntimeException(c.getName() + " could not be instantiated.", ie);
                } catch (ClassCastException cce) {
                    throw new RuntimeException(c.getName() + " does not extend "
                            + (rawStepType ? "StepType" : "Getter") + ".", cce);
                }
            }
            return typesMap.get(name);
        } catch (Exception e) {
            throw new RuntimeException("Step type \"" + name + "\" is not implemented.", e);
        }
    }

    /**
     * @param stepO json object step load from
     * @throws JSONException If anything goes wrong with interpreting the JSON.
     */
    private void parseStep(ArrayList<Step> steps, JSONObject stepO) throws JSONException {
        Step step = this.createStep(stepO);
        steps.add(step);
        this.configureStep(steps, stepO, step);
    }

    /**
     * @param stepO json object step load from
     * @return A new instance of step
     * @throws JSONException If anything goes wrong with interpreting the JSON.
     */
    private Step createStep(JSONObject stepO) throws JSONException {
        StepType type = this.getStepTypeOfName(stepO.getString("type"));
        boolean isNegated = stepO.optBoolean("negated", false);
        String name = stepO.optString("step_name", null);
        return new Step(name, type, isNegated);
    }

    /**
     * @param stepO json object step configuration load from
     * @param step  step configuration to
     * @throws JSONException If anything goes wrong with interpreting the JSON.
     */
    private void configureStep(ArrayList<Step> steps, JSONObject stepO, Step step) throws JSONException {
        JSONArray keysA = stepO.names();
        for (int j = 0; j < keysA.length(); j++) {
            this.configureStep(steps, stepO, step, keysA.getString(j));
        }
    }

    /**
     * @param stepO json object step configuration load from
     * @param step  step configuration to
     * @param key   configuration key
     * @throws JSONException If anything goes wrong with interpreting the JSON.
     */
    private void configureStep(ArrayList<Step> steps, JSONObject stepO, Step step, String key) throws JSONException {
        if (key.equals("type") || key.equals("negated")) {
            return;
        }
        if (stepO.optJSONObject(key) != null && key.equals("locator")) {
            step.put(key, new Locator(stepO.getJSONObject(key).getString("type"), stepO.getJSONObject(key).getString("value")));
        } else {
            step.put(key, stepO.getString(key));
        }
    }
}