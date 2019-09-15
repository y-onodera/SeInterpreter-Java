package com.sebuilder.interpreter.application;

import com.sebuilder.interpreter.*;
import com.sebuilder.interpreter.datasource.DataSourceFactoryImpl;
import com.sebuilder.interpreter.parser.Sebuilder;
import com.sebuilder.interpreter.step.StepTypeFactoryImpl;
import com.sebuilder.interpreter.webdriverfactory.Chrome;
import com.sebuilder.interpreter.webdriverfactory.WebDriverFactory;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public abstract class CommandLineRunner {
    protected static WebDriverFactory DEFAULT_DRIVER_FACTORY = new Chrome();
    protected WebDriverFactory wdf;
    protected HashMap<String, String> driverConfig;
    protected TestRun lastRun;
    protected TestRunListener testRunListener;
    protected Logger log;
    protected RemoteWebDriver driver;
    protected Long implicitlyWaitTime;
    protected Long pageLoadWaitTime;

    protected CommandLineRunner(String[] args, Logger log) {
        this.log = log;
        Context.getInstance()
                .setDefaultScriptParser(new Sebuilder())
                .setDataSourceFactory(new DataSourceFactoryImpl())
                .setStepTypeFactory(new StepTypeFactoryImpl());
        this.driverConfig = new HashMap<>();
        this.wdf = DEFAULT_DRIVER_FACTORY;
        this.implicitlyWaitTime = Long.valueOf(-1);
        this.pageLoadWaitTime = Long.valueOf(-1);
        setUp(args);
    }

    public Long getImplicitlyWaitTime() {
        return this.implicitlyWaitTime;
    }

    public Long getPageLoadWaitTime() {
        return this.pageLoadWaitTime;
    }

    public void setTestRunListener(TestRunListener testRunListener) {
        this.testRunListener = testRunListener;
    }

    public void reloadBrowserSetting(String browserName, String driverPath) {
        this.resetDriverFactory(browserName);
        this.wdf.setDriverPath(driverPath);
    }

    public void setUp(String[] args) {
        this.log.info("setUp start");
        if (args.length == 0) {
            log.info("Usage: [--driver=<drivername] [--driver.<configkey>=<configvalue>...] [--implicitlyWait=<ms>] [--pageLoadTimeout=<ms>] [--stepTypePackage=<package name>] <script path>...");
            System.exit(0);
        }
        preSetUp();
        this.lastRun = null;
        String aspectFileName = null;
        for (String s : args) {
            if (s.startsWith("--")) {
                String[] kv = s.split("=", 2);
                if (kv.length < 2) {
                    this.log.fatal("Driver configuration option \"" + s + "\" is not of the form \"--driver=<name>\" or \"--driver.<key>=<value\".");
                    System.exit(1);
                }
                if (s.startsWith(CommandLineArgument.IMPLICITLY_WAIT.key())) {
                    this.implicitlyWaitTime = Long.valueOf(kv[1]);
                } else if (s.startsWith(CommandLineArgument.PAGE_LOAD_TIMEOUT.key())) {
                    this.pageLoadWaitTime = Long.valueOf(kv[1]);
                } else if (s.startsWith(CommandLineArgument.STEP_TYPE_PACKAGE.key())) {
                    Context.getStepTypeFactory().setPrimaryPackage(kv[1]);
                } else if (s.startsWith(CommandLineArgument.STEP_TYPE_PACKAGE2.key())) {
                    Context.getStepTypeFactory().setSecondaryPackage(kv[1]);
                } else if (s.startsWith(CommandLineArgument.DRIVER_CONFIG_PREFIX.key())) {
                    this.driverConfig.put(kv[0].substring(CommandLineArgument.DRIVER_CONFIG_PREFIX.key().length()), kv[1]);
                } else if (s.startsWith(CommandLineArgument.DRIVER.key())) {
                    resetDriverFactory(kv[1]);
                } else if (s.startsWith(CommandLineArgument.DATASOURCE_PACKAGE.key())) {
                    Context.getDataSourceFactory().setCustomDataSourcePackage(kv[1]);
                } else if (s.startsWith(CommandLineArgument.DATASOURCE_ENCODING.key())) {
                    Context.getInstance().setDataSourceEncoding(kv[1]);
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
                } else if (s.startsWith(CommandLineArgument.ASPECT.key())) {
                    aspectFileName = kv[1];
                } else {
                    configureOption(s, kv);
                }
            } else {
                configureOption(s);
            }
        }
        if (Strings.isNotEmpty(aspectFileName)) {
            try {
                this.setAspect(new File(Context.getBaseDirectory(), aspectFileName));
            } catch (IOException e) {
                this.log.fatal(e);
                System.exit(1);
            }
        }
        this.testRunListener = new TestRunListenerImpl(this.log);
        this.log.info("setUp finish");
    }

    protected TestRun getTestRun(TestRunBuilder script, TestData data, TestRunListener seInterpreterTestListener) {
        return script.createTestRun(this.log
                , this.wdf
                , this.driverConfig
                , this.getImplicitlyWaitTime()
                , this.getPageLoadWaitTime()
                , data
                , this.lastRun
                , seInterpreterTestListener);
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

    protected void setAspect(File aSource) throws IOException {
        Context.getInstance().setAspect(Context.getScriptParser().loadAspect(aSource));
    }

    protected TestRunBuilder createTestRunBuilder(TestCase testCase) {
        return new TestRunBuilder(testCase);
    }

}
