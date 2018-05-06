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

import com.sebuilder.interpreter.Script;
import com.sebuilder.interpreter.SeInterpreterTestListener;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.webdriverfactory.WebDriverFactory;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Factory to create a TestRun objects from a script.
 *
 * @author jkowalczyk
 */
public class TestRunFactory {
    private Long implicitlyWaitDriverTimeout = Long.valueOf(-1);
    private Long pageLoadDriverTimeout = Long.valueOf(-1);

    public void setImplicitlyWaitDriverTimeout(Long implicitlyWaitDriverTimeout) {
        this.implicitlyWaitDriverTimeout = implicitlyWaitDriverTimeout;
    }

    public void setPageLoadDriverTimeout(Long pageLoadDriverTimeout) {
        this.pageLoadDriverTimeout = pageLoadDriverTimeout;
    }

    /**
     * @param script
     * @param log
     * @param webDriverFactory
     * @param webDriverConfig
     * @param initialVars
     * @param previousRun
     * @param seInterpreterTestListener
     * @return A new instance of TestRun, using the previous run's driver and vars if available.
     */
    public TestRun createTestRun(Script script, Log log, WebDriverFactory webDriverFactory, HashMap<String, String> webDriverConfig, Map<String, String> initialVars, TestRun previousRun, SeInterpreterTestListener seInterpreterTestListener) {
        if (script.usePreviousDriverAndVars && previousRun != null && previousRun.driver() != null) {
            return new TestRun(script, log, previousRun.driver(), implicitlyWaitDriverTimeout, pageLoadDriverTimeout, initialVars, seInterpreterTestListener);
        }
        return new TestRun(script, log, webDriverFactory, webDriverConfig, implicitlyWaitDriverTimeout, pageLoadDriverTimeout, initialVars, seInterpreterTestListener);
    }
}