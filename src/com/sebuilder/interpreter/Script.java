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

import com.sebuilder.interpreter.factory.TestRunFactory;
import com.sebuilder.interpreter.webdriverfactory.WebDriverFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A Selenium 2 script. To create and run a test, instantiate a Script object,
 * add some Script.Steps to its steps, then invoke "run". If you want to be able
 * to run the script step by step, invoke "start", which will return a TestRun
 * object.
 *
 * @author zarkonnen
 */
public class Script {
    public ArrayList<Step> steps = new ArrayList<>();
    public TestRunFactory testRunFactory = new TestRunFactory();
    public List<Map<String, String>> dataRows;
    public String name = "Script";
    public boolean usePreviousDriverAndVars = false;
    public boolean closeDriver = true;
    public File relativePath;

    public Script() {
        // By default there is one empty data row.
        dataRows = new ArrayList<>(1);
        dataRows.add(new HashMap<>());
    }

    public TestRun createTestRun(Log log, WebDriverFactory wdf, HashMap<String, String> driverConfig, Map<String, String> data, TestRun lastRun, SeInterpreterTestListener seInterpreterTestListener) {
        return testRunFactory.createTestRun(this, log, wdf, driverConfig, data, lastRun, seInterpreterTestListener);
    }

    /**
     * @param scripts
     */
    public void stateTakeOver() {
        this.closeDriver = false;
        this.usePreviousDriverAndVars = true;
    }

    @Override
    public String toString() {
        try {
            return toJSON().toString(4);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject o = new JSONObject();
        JSONArray stepsA = new JSONArray();
        for (Step s : steps) {
            stepsA.put(s.toJSON());
        }
        o.put("steps", stepsA);
        return o;
    }

}