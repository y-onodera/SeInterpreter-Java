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
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Log4J2LoggerFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
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

    public SeInterpreter(final String[] args, final Logger log) {
        super(args, log);
    }

    public static void main(final String[] args) {
        InternalLoggerFactory.setDefaultFactory(Log4J2LoggerFactory.INSTANCE);
        final Logger log = LogManager.getLogger(SeInterpreter.class);
        final SeInterpreter interpreter = new SeInterpreter(args, log);
        if (interpreter.paths.isEmpty()) {
            log.error("Configuration successful but no paths to testCases specified. Exiting.");
            System.exit(1);
        }
        try {
            if (!interpreter.runScripts()) {
                log.error("test failed.");
                System.exit(1);
            }
        } catch (final Exception e) {
            log.fatal("Run error.", e);
            System.exit(2);
        }
        log.info("test success.");
        System.exit(0);
    }

    public boolean runScripts() {
        this.testRunListener.cleanResult();
        try {
            boolean success = false;
            for (final String path : this.paths) {
                success = this.loadTestCase(path).run(this, this.testRunListener);
            }
            return success;
        } finally {
            if (this.lastRun != null && this.closeDriver) {
                this.lastRun.driver().quit();
            }
            this.testRunListener.aggregateResult();
        }
    }

    @Override
    public STATUS execute(final TestRunBuilder testRunBuilder, final InputData data, final TestRunListener aTestRunListener) {
        boolean success = false;
        try {
            this.lastRun = this.getTestRun(testRunBuilder, data, aTestRunListener);
            if (this.lastRun.finish()) {
                success = true;
                this.log.info(testRunBuilder.getScriptName() + " succeeded");
            } else {
                this.log.info(testRunBuilder.getScriptName() + " failed");
            }
        } catch (final AssertionError e) {
            this.log.info(testRunBuilder.getScriptName() + " failed", e);
        } finally {
            this.closeDriver = true;
        }
        final boolean stopped = this.lastRun.isStopped();
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

    protected TestCase loadTestCase(final String path) {
        return Context.getScriptParser().load(new File(path));
    }

    @Override
    protected void preSetUp() {
        this.paths = new ArrayList<>();
    }

    @Override
    protected void setScripts(final Set<String> scripts) {
        this.paths.addAll(scripts);
    }

}
