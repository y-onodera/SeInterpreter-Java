package com.sebuilder.interpreter;

import com.sebuilder.interpreter.factory.ScriptFactory;
import com.sebuilder.interpreter.factory.StepTypeFactory;
import com.sebuilder.interpreter.factory.TestRunFactory;
import com.sebuilder.interpreter.webdriverfactory.Firefox;
import com.sebuilder.interpreter.webdriverfactory.WebDriverFactory;
import org.apache.commons.logging.Log;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public abstract class CommandLineRunner {
    protected static WebDriverFactory DEFAULT_DRIVER_FACTORY = new Firefox();
    protected ScriptFactory sf = new ScriptFactory();
    protected StepTypeFactory stf = new StepTypeFactory();
    protected TestRunFactory trf = new TestRunFactory();
    protected HashMap<String, String> driverConfig = new HashMap<>();
    protected WebDriverFactory wdf = DEFAULT_DRIVER_FACTORY;
    protected TestRun lastRun = null;
    protected SeInterpreterTestListener seInterpreterTestListener = new SeInterpreterTestListener();
    protected Log log;

    protected CommandLineRunner(String[] args, Log log) {
        this.log = log;
        if (args.length == 0) {
            log.info("Usage: [--driver=<drivername] [--driver.<configkey>=<configvalue>...] [--implicitlyWait=<ms>] [--pageLoadTimeout=<ms>] [--stepTypePackage=<package name>] <script path>...");
            System.exit(0);
        }
        preSetUp();
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
                    trf.setImplicitlyWaitDriverTimeout(Long.valueOf(kv[1]));
                } else if (s.startsWith("--pageLoadTimeout")) {
                    trf.setPageLoadDriverTimeout(Long.valueOf(kv[1]));
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
                    Context.getInstance().setScreenShotOutputDirectory(kv[1]);
                } else if (s.startsWith("--templateoutput")) {
                    Context.getInstance().setTemplateOutputDirectory(kv[1]);
                } else if (s.startsWith("--resultoutput")) {
                    Context.getInstance().setResultOutputDirectory(kv[1]);
                } else {
                    configureOption(s, kv);
                }
            } else {
                configureOption(s);
            }
        }
    }

    protected void preSetUp() {
    }

    protected void configureOption(String s, String[] kv) {
    }

    protected void configureOption(String s) {
    }
}
