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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

/**
 * An interpreter for Builder JSON tests. Given one or more JSON script files, it plays them back
 * using the Java WebDriver bindings.
 *
 * @author zarkonnen
 */
public class SeInterpreter extends CommandLineRunner {
    private ArrayList<String> paths;
    private boolean closeDriver;

    public SeInterpreter(String[] args, Log log) {
        super(args, log);
    }

    public static void main(String[] args) {
        Log log = LogFactory.getFactory().getInstance(SeInterpreter.class);
        SeInterpreter interpreter = new SeInterpreter(args, log);
        if (interpreter.paths.isEmpty()) {
            log.info("Configuration successful but no paths to scripts specified. Exiting.");
            System.exit(0);
        }
        try {
            interpreter.runScripts();
        } catch (Exception e) {
            log.fatal("Run error.", e);
            System.exit(1);
        }
    }

    @Override
    protected void preSetUp() {
        paths = new ArrayList<>();
    }

    @Override
    protected void configureOption(String s) {
        this.paths.add(s);
    }

    private void runScripts() throws IOException, JSONException {
        this.seInterpreterTestListener.cleanResult();
        try {
            for (String path : this.paths) {
                this.runScripts(path);
            }
        } finally {
            if (driver != null && closeDriver) {
                driver.quit();
            }
            this.seInterpreterTestListener.aggregateResult();
        }
    }

    private void runScripts(String path) throws IOException, JSONException {
        for (Script script : this.sf.parse(new File(path))) {
            this.runScript(script);
        }
    }

    private void runScript(Script script) {
        int i = 1;
        for (Map<String, String> data : script.dataRows) {
            data.put(DataSource.ROW_NUMBER, String.valueOf(i));
            Path currentDir = Paths.get(".").toAbsolutePath();
            Path executeScript = Paths.get(script.path);
            String normalizePath = currentDir.relativize(executeScript).normalize().toString();
            this.seInterpreterTestListener.openTestSuite(normalizePath.replace(".json", "") + "_rowNumber" + String.valueOf(i), data);
            this.runScript(script, data, this.seInterpreterTestListener);
            this.seInterpreterTestListener.closeTestSuite();
            i++;
        }
    }

    private void runScript(Script script, Map<String, String> data, SeInterpreterTestListener seInterpreterTestListener) {
        try {
            this.lastRun = script.createTestRun(this.log, this.wdf, this.driverConfig, data, this.lastRun, seInterpreterTestListener);
            if (this.lastRun.finish()) {
                this.log.info(script.name + " succeeded");
            } else {
                this.log.info(script.name + " failed");
            }
        } catch (AssertionError e) {
            this.log.info(script.name + " failed", e);
        }
        if (!script.closeDriver) {
            if (lastRun != null) {
                this.driver = lastRun.driver();
            }
            this.closeDriver = true;
        } else {
            this.lastRun = null;
            this.driver = null;
            this.closeDriver = false;
        }
    }
}
