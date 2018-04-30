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

import com.sebuilder.interpreter.factory.ScriptFactory;
import com.sebuilder.interpreter.factory.StepTypeFactory;
import com.sebuilder.interpreter.factory.TestRunFactory;
import com.sebuilder.interpreter.webdriverfactory.Firefox;
import com.sebuilder.interpreter.webdriverfactory.WebDriverFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * An interpreter for Builder JSON tests. Given one or more JSON script files, it plays them back
 * using the Java WebDriver bindings.
 *
 * @author zarkonnen
 */
public class SeInterpreter {
    public static WebDriverFactory DEFAULT_DRIVER_FACTORY = new Firefox();
    private ScriptFactory sf = new ScriptFactory();
    private StepTypeFactory stf = new StepTypeFactory();
    private TestRunFactory trf = new TestRunFactory();
    private ArrayList<String> paths = new ArrayList<>();
    private HashMap<String, String> driverConfig = new HashMap<>();
    private WebDriverFactory wdf = DEFAULT_DRIVER_FACTORY;
    private TestRun lastRun = null;
    private SeInterpreterTestListener seInterpreterTestListener = new SeInterpreterTestListener();

    public SeInterpreter(String[] args, Log log) {
        sf.setStepTypeFactory(stf);
        sf.setTestRunFactory(trf);
        for (String s : args) {
            if (s.startsWith("--")) {
                String[] kv = s.split("=", 2);
                if (kv.length < 2) {
                    log.fatal("Driver configuration option \"" + s + "\" is not of the form \"--driver=<name>\" or \"--driver.<key>=<value\".");
                    System.exit(1);
                }
                if (s.startsWith("--implicitlyWait")) {
                    trf.setImplicitlyWaitDriverTimeout(Integer.parseInt(kv[1]));
                } else if (s.startsWith("--pageLoadTimeout")) {
                    trf.setPageLoadDriverTimeout(Integer.parseInt(kv[1]));
                } else if (s.startsWith("--stepTypePackage")) {
                    stf.setPrimaryPackage(kv[1]);
                } else if (s.startsWith("--driver.")) {
                    driverConfig.put(kv[0].substring("--driver.".length()), kv[1]);
                } else if (s.startsWith("--driver")) {
                    try {
                        wdf = (WebDriverFactory) Class.forName("com.sebuilder.interpreter.webdriverfactory." + kv[1]).getDeclaredConstructor().newInstance();
                    } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
                        log.fatal("Unknown WebDriverFactory: " + "com.sebuilder.interpreter.webdriverfactory." + kv[1], e);
                    } catch (InstantiationException | IllegalAccessException e) {
                        log.fatal("Could not instantiate WebDriverFactory " + "com.sebuilder.interpreter.webdriverfactory." + kv[1], e);
                    }
                } else if (s.startsWith("--datasource.encoding")) {
                    Context.getInstance().setDataSourceEncording(kv[1]);
                } else if (s.startsWith("--datasource.directory")) {
                    Context.getInstance().setDataSourceDirectory(kv[1]);
                } else if (s.startsWith("--screenshotoutput")) {
                    Context.getInstance().setScreenShotOutput(kv[1]);
                } else {
                    paths.add(s);
                }
            } else {
                paths.add(s);
            }
        }
    }

    public static void main(String[] args) {
        Log log = LogFactory.getFactory().getInstance(SeInterpreter.class);
        if (args.length == 0) {
            log.info("Usage: [--driver=<drivername] [--driver.<configkey>=<configvalue>...] [--implicitlyWait=<ms>] [--pageLoadTimeout=<ms>] [--stepTypePackage=<package name>] <script path>...");
            System.exit(0);
        }
        SeInterpreter interpreter = new SeInterpreter(args, log);
        if (interpreter.paths.isEmpty()) {
            log.info("Configuration successful but no paths to scripts specified. Exiting.");
            System.exit(0);
        }
        try {
            interpreter.runScripts(log);
        } catch (Exception e) {
            log.fatal("Run error.", e);
            System.exit(1);
        }
    }

    private void runScripts(Log log) throws IOException, JSONException {
        this.seInterpreterTestListener.cleanResult();
        for (String path : this.paths) {
            this.runScripts(log, path);
        }
        this.seInterpreterTestListener.aggregateResult();
    }


    private void runScripts(Log log, String path) throws IOException, JSONException {
        for (Script script : this.sf.parse(new File(path))) {
            this.runScript(log, script);
        }
    }

    private void runScript(Log log, Script script) {
        int i = 1;
        for (Map<String, String> data : script.dataRows) {
            seInterpreterTestListener.openTestSuite(script.name.replace(".json", "") + ".row" + String.valueOf(i), new Hashtable<>(data));
            this.runScript(log, script, data, seInterpreterTestListener);
            seInterpreterTestListener.closeTestSuite();
            i++;
        }
    }

    private void runScript(Log log, Script script, Map<String, String> data, SeInterpreterTestListener seInterpreterTestListener) {
        try {
            lastRun = script.createTestRun(log, this.wdf, this.driverConfig, data, lastRun, seInterpreterTestListener);
            if (lastRun.finish()) {
                log.info(script.name + " succeeded");
            } else {
                log.info(script.name + " failed");
            }
        } catch (Exception e) {
            log.info(script.name + " failed", e);
        }
    }
}
