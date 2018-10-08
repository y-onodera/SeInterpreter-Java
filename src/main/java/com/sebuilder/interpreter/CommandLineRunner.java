package com.sebuilder.interpreter;

import com.sebuilder.interpreter.factory.ScriptFactory;
import com.sebuilder.interpreter.factory.StepTypeFactory;
import com.sebuilder.interpreter.factory.TestRunFactory;
import com.sebuilder.interpreter.webdriverfactory.Firefox;
import com.sebuilder.interpreter.webdriverfactory.WebDriverFactory;
import org.apache.logging.log4j.Logger;
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
    protected Logger log;
    protected RemoteWebDriver driver;

    protected CommandLineRunner(String[] args, Logger log) {
        this.log = log;
        this.sf = new ScriptFactory();
        this.stf = new StepTypeFactory();
        this.trf = new TestRunFactory();
        this.driverConfig = new HashMap<>();
        this.wdf = DEFAULT_DRIVER_FACTORY;
        this.seInterpreterTestListener = new SeInterpreterTestListener(this.log);
        setUp(args);
    }

    public void setSeInterpreterTestListener(SeInterpreterTestListener seInterpreterTestListener) {
        this.seInterpreterTestListener = seInterpreterTestListener;
    }

    public void reloadBrowserSetting(String browserName, String driverPath) {
        this.resetDriverFactory(browserName);
        this.wdf.setDriverPath(driverPath);
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
        for (String s : args) {
            if (s.startsWith("--")) {
                String[] kv = s.split("=", 2);
                if (kv.length < 2) {
                    this.log.fatal("Driver configuration option \"" + s + "\" is not of the form \"--driver=<name>\" or \"--driver.<key>=<value\".");
                    System.exit(1);
                }
                if (s.startsWith(CommandLineArgument.IMPLICITLY_WAIT.key())) {
                    this.trf.setImplicitlyWaitDriverTimeout(Long.valueOf(kv[1]));
                } else if (s.startsWith(CommandLineArgument.PAGE_LOAD_TIMEOUT.key())) {
                    this.trf.setPageLoadDriverTimeout(Long.valueOf(kv[1]));
                } else if (s.startsWith(CommandLineArgument.STEP_TYPE_PACKAGE.key())) {
                    this.stf.setPrimaryPackage(kv[1]);
                } else if (s.startsWith(CommandLineArgument.DRIVER_CONFIG_PREFIX.key())) {
                    this.driverConfig.put(kv[0].substring(CommandLineArgument.DRIVER_CONFIG_PREFIX.key().length()), kv[1]);
                } else if (s.startsWith(CommandLineArgument.DRIVER.key())) {
                    resetDriverFactory(kv[1]);
                } else if (s.startsWith(CommandLineArgument.DATASOURCE_ENCODING.key())) {
                    Context.getInstance().setDataSourceEncording(kv[1]);
                } else if (s.startsWith(CommandLineArgument.DATASOURCE_DIRECTORY.key())) {
                    Context.getInstance().setDataSourceDirectory(kv[1]);
                } else if (s.startsWith(CommandLineArgument.SCREENSHOT_OUTPUT.key())) {
                    Context.getInstance().setScreenShotOutputDirectory(kv[1]);
                } else if (s.startsWith(CommandLineArgument.TEMPLATE_OUTPUT.key())) {
                    Context.getInstance().setTemplateOutputDirectory(kv[1]);
                } else if (s.startsWith(CommandLineArgument.RESULT_OUTPUT.key())) {
                    Context.getInstance().setResultOutputDirectory(kv[1]);
                } else if (s.startsWith(CommandLineArgument.DOWNLOAD_OUTPUT.key())) {
                    Context.getInstance().setDownloadDirectory(kv[1]);
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

    protected void resetDriverFactory(String s) {
        Context.getInstance().setBrowser(s);
        try {
            this.wdf = (WebDriverFactory) Class.forName("com.sebuilder.interpreter.webdriverfactory." + s).getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
            this.log.fatal("Unknown WebDriverFactory: " + "com.sebuilder.interpreter.webdriverfactory." + s, e);
        } catch (InstantiationException | IllegalAccessException e) {
            this.log.fatal("Could not instantiate WebDriverFactory " + "com.sebuilder.interpreter.webdriverfactory." + s, e);
        }
    }

}
