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

package com.sebuilder.interpreter.application;

import com.sebuilder.interpreter.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * An interpreter for Builder JSON tests. Given one or more JSON script files, it plays them back
 * using the Java WebDriver bindings.
 *
 * @author zarkonnen
 */
public class SeInterpreter extends CommandLineRunner {

    private ArrayList<String> paths;

    private boolean closeDriver;

    public SeInterpreter(String[] args, Logger log) {
        super(args, log);
    }

    public static void main(String[] args) {
        Logger log = LogManager.getLogger(SeInterpreter.class);
        SeInterpreter interpreter = new SeInterpreter(args, log);
        if (interpreter.paths.isEmpty()) {
            log.info("Configuration successful but no paths to testCases specified. Exiting.");
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

    private void runScripts() throws IOException {
        this.testRunListener.cleanResult();
        try {
            for (String path : this.paths) {
                this.runScripts(path);
            }
        } finally {
            if (this.driver != null && this.closeDriver) {
                this.driver.quit();
            }
            this.testRunListener.aggregateResult();
        }
    }

    private void runScripts(String path) throws IOException {
        Suite suite = Context.getScriptParser().load(new File(path));
        for (TestRunBuilder script : suite.createTestRunBuilder()) {
            this.runScript(script);
        }
    }

    private void runScript(TestRunBuilder testRunBuilder) {
        for (TestData data : testRunBuilder.loadData()) {
            this.runScript(testRunBuilder, data, this.testRunListener);
        }
    }

    private void runScript(TestRunBuilder testRunBuilder, TestData data, TestRunListener seInterpreterTestListener) {
        try {
            this.lastRun = getTestRun(testRunBuilder, data, seInterpreterTestListener);
            if (this.lastRun.finish()) {
                this.log.info(testRunBuilder.getScriptName() + " succeeded");
            } else {
                this.log.info(testRunBuilder.getScriptName() + " failed");
            }
        } catch (AssertionError e) {
            this.log.info(testRunBuilder.getScriptName() + " failed", e);
        }
        if (!testRunBuilder.closeDriver()) {
            if (this.lastRun != null) {
                this.driver = this.lastRun.driver();
            }
            this.closeDriver = true;
        } else {
            this.lastRun = null;
            this.driver = null;
            this.closeDriver = false;
        }
    }

}
