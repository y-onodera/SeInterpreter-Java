package com.sebuilder.interpreter;

import com.sebuilder.interpreter.factory.ScriptFactory;
import com.sebuilder.interpreter.factory.StepTypeFactory;
import com.sebuilder.interpreter.factory.TestRunFactory;
import com.sebuilder.interpreter.webdriverfactory.Firefox;
import com.sebuilder.interpreter.webdriverfactory.WebDriverFactory;
import org.apache.commons.logging.Log;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public abstract class CommandLineRunner {
    protected static WebDriverFactory DEFAULT_DRIVER_FACTORY = new Firefox();
    protected ScriptFactory sf;
    protected StepTypeFactory stf;
    protected TestRunFactory trf;
    protected HashMap<String, String> driverConfig;
    protected WebDriverFactory wdf;
    protected TestRun lastRun;
    protected SeInterpreterTestListener seInterpreterTestListener;
    protected Log log;
    protected RemoteWebDriver driver;

    protected CommandLineRunner(String[] args, Log log) {
        this.log = log;
        this.sf = new ScriptFactory();
        this.stf = new StepTypeFactory();
        this.trf = new TestRunFactory();
        this.driverConfig = new HashMap<>();
        this.wdf = DEFAULT_DRIVER_FACTORY;
        this.seInterpreterTestListener = new SeInterpreterTestListener(this.log);
        setUp(args);
    }

    protected void setUp(String[] args) {
        this.log.info("setUp start");
        if (args.length == 0) {
            log.info("Usage: [--driver=<drivername] [--driver.<configkey>=<configvalue>...] [--implicitlyWait=<ms>] [--pageLoadTimeout=<ms>] [--stepTypePackage=<package name>] <script path>...");
            System.exit(0);
        }
        preSetUp();
        this.lastRun = null;
        this.sf.setStepTypeFactory(this.stf);
        this.sf.setTestRunFactory(this.trf);
        for (String s : args) {
            if (s.startsWith("--")) {
                String[] kv = s.split("=", 2);
                if (kv.length < 2) {
                    this.log.fatal("Driver configuration option \"" + s + "\" is not of the form \"--driver=<name>\" or \"--driver.<key>=<value\".");
                    System.exit(1);
                }
                if (s.startsWith("--implicitlyWait")) {
                    this.trf.setImplicitlyWaitDriverTimeout(Long.valueOf(kv[1]));
                } else if (s.startsWith("--pageLoadTimeout")) {
                    this.trf.setPageLoadDriverTimeout(Long.valueOf(kv[1]));
                } else if (s.startsWith("--stepTypePackage")) {
                    this.stf.setPrimaryPackage(kv[1]);
                } else if (s.startsWith("--driver.")) {
                    this.driverConfig.put(kv[0].substring("--driver.".length()), kv[1]);
                } else if (s.startsWith("--driver")) {
                    try {
                        this.wdf = (WebDriverFactory) Class.forName("com.sebuilder.interpreter.webdriverfactory." + kv[1]).getDeclaredConstructor().newInstance();
                    } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
                        this.log.fatal("Unknown WebDriverFactory: " + "com.sebuilder.interpreter.webdriverfactory." + kv[1], e);
                    } catch (InstantiationException | IllegalAccessException e) {
                        this.log.fatal("Could not instantiate WebDriverFactory " + "com.sebuilder.interpreter.webdriverfactory." + kv[1], e);
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
        this.log.info("setUp finish");
    }

    protected void preSetUp() {
    }

    protected void configureOption(String s, String[] kv) {
    }

    protected void configureOption(String s) {
    }
}
