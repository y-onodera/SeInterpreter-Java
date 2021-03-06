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
import java.util.Set;

/**
 * An interpreter for Builder JSON tests. Given one or more JSON script files, it plays them back
 * using the Java WebDriver bindings.
 *
 * @author zarkonnen
 */
public class SeInterpreter extends CommandLineRunner implements TestRunner {

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

    public void runScripts() {
        this.testRunListener.cleanResult();
        try {
            for (String path : this.paths) {
                this.loadTestCase(path).run(this, this.testRunListener);
            }
        } finally {
            if (this.lastRun != null && this.closeDriver) {
                this.lastRun.driver().quit();
            }
            this.testRunListener.aggregateResult();
        }
    }

    @Override
    public STATUS execute(TestRunBuilder testRunBuilder, InputData data, TestRunListener aTestRunListener) {
        boolean success = false;
        try {
            this.lastRun = getTestRun(testRunBuilder, data, aTestRunListener);
            if (this.lastRun.finish()) {
                success = true;
                this.log.info(testRunBuilder.getScriptName() + " succeeded");
            } else {
                this.log.info(testRunBuilder.getScriptName() + " failed");
            }
        } catch (AssertionError e) {
            this.log.info(testRunBuilder.getScriptName() + " failed", e);
        } finally {
            this.closeDriver = true;
        }
        boolean stopped = this.lastRun.isStopped();
        if (this.lastRun.isCloseDriver()) {
            this.lastRun = null;
            this.closeDriver = false;
        }
        if (stopped) {
            return STATUS.STOPPED;
        } else if (!success) {
            return STATUS.FAILED;
        }
        return STATUS.SUCCESS;
    }

    protected TestCase loadTestCase(String path) {
        try {
            return Context.getScriptParser().load(new File(path), this.testRunListener);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    protected void preSetUp() {
        paths = new ArrayList<>();
    }

    @Override
    protected void setScripts(Set<String> scripts) {
        this.paths.addAll(scripts);
    }

}
