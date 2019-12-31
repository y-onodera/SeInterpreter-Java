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
package com.sebuilder.interpreter.step;

import com.sebuilder.interpreter.StepType;

import java.util.HashMap;

/**
 * Factory to create a StepType object from the step's name. Each step can be
 * loaded from a settable primary package or a secondary package. Thanks to this
 * mechanism, steps can be easily overridden when needed.
 *
 * @author jkowalczyk
 */
public class StepTypeFactoryImpl implements com.sebuilder.interpreter.StepTypeFactory {

    public static final String DEFAULT_PACKAGE = "com.sebuilder.interpreter.step";

    /**
     * Mapping of the names of step types to their implementing classes, lazily
     * loaded through reflection. StepType classes must be either in the first
     * package either in the second one and their name must be the capitalized
     * name of their type. For example, the class for "getChainTo" is at
     * com.sebuilder.interpreter.steptype.Get.
     * <p>
     * Assert/Verify/WaitFor/Store steps use "Getter" objects that encapsulate
     * how to getChainTo the value they are about. Getters should be named e.g "Title"
     * for "verifyTitle" and also be in the com.sebuilder.interpreter.steptype
     * package.
     */
    private final HashMap<String, StepType> typesMap = new HashMap<String, StepType>();

    /**
     * @param name
     * @return a stepType instance for a given name
     */
    @Override
    public StepType getStepTypeOfName(String name) {
        try {
            if (!this.typesMap.containsKey(name)) {
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
                if (name.startsWith("if")) {
                    className = className.substring("if".length());
                    rawStepType = false;
                }
                if (name.startsWith("retry")) {
                    className = className.substring("retry".length());
                    rawStepType = false;
                }
                Class<?> c;
                if (className.equals("Loop")) {
                    c = Loop.class;
                } else if (rawStepType) {
                    c = newStepType(name, className);
                } else {
                    c = newGetter(name, className);
                }
                try {
                    Object o = c.getDeclaredConstructor().newInstance();
                    if (name.startsWith("assert")) {
                        this.typesMap.put(name, Getter.class.cast(o).toAssert());
                    } else if (name.startsWith("verify")) {
                        this.typesMap.put(name, Getter.class.cast(o).toVerify());
                    } else if (name.startsWith("waitFor")) {
                        this.typesMap.put(name, Getter.class.cast(o).toWaitFor());
                    } else if (name.startsWith("store") && !name.equals("store")) {
                        this.typesMap.put(name, Getter.class.cast(o).toStore());
                    } else if (name.startsWith("print") && !name.equals("print")) {
                        this.typesMap.put(name, Getter.class.cast(o).toPrint());
                    } else if (name.startsWith("if") && !name.equals("if")) {
                        this.typesMap.put(name, Getter.class.cast(o).toIf());
                    } else if (name.startsWith("retry") && !name.equals("retry")) {
                        this.typesMap.put(name, Getter.class.cast(o).toRetry());
                    } else {
                        this.typesMap.put(name, (StepType) o);
                    }
                } catch (InstantiationException | IllegalAccessException ie) {
                    throw new RuntimeException(c.getName() + " could not be instantiated.", ie);
                } catch (ClassCastException cce) {
                    throw new RuntimeException(c.getName() + " does not extend "
                            + (rawStepType ? "StepType" : "Getter") + ".", cce);
                }
            }
            return this.typesMap.get(name);
        } catch (Exception e) {
            throw new RuntimeException("Step type \"" + name + "\" is not implemented.", e);
        }
    }

    protected Class<?> newGetter(String name, String className) {
        final String errorMessage = "No implementation class for getter \"" + name + "\" could be found.";
        final String subPackage = ".getter.";
        return newInstance(className, errorMessage, subPackage);
    }

    protected Class<?> newStepType(String name, String className) {
        final String errorMessage = "No implementation class for step type \"" + name + "\" could be found.";
        final String subPackage = ".type.";
        return newInstance(className, errorMessage, subPackage);
    }

    private Class<?> newInstance(String className, String errorMessage, String subPackage) {
        Class<?> c = null;
        try {
            c = Class.forName(DEFAULT_PACKAGE + subPackage + className);
        } catch (ClassNotFoundException cnfe) {
            throw new RuntimeException(errorMessage, cnfe);
        }
        return c;
    }

}