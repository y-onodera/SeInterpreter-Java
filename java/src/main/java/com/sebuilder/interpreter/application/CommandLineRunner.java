package com.sebuilder.interpreter.application;

import com.sebuilder.interpreter.*;
import com.sebuilder.interpreter.datasource.DataSourceFactoryImpl;
import com.sebuilder.interpreter.script.Sebuilder;
import com.sebuilder.interpreter.script.SebuilderToStringConverter;
import com.sebuilder.interpreter.script.seleniumide.SeleniumIDE;
import com.sebuilder.interpreter.step.StepTypeFactoryImpl;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.Set;

public abstract class CommandLineRunner {
    protected TestRun lastRun;
    protected TestRunListener testRunListener;
    protected Logger log;

    protected CommandLineRunner(final Logger log) {
        this.log = log;
    }

    protected CommandLineRunner(final String[] args, final Logger log) {
        System.setProperty("webdriver.http.factory", "jdk-http-client");
        if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
            System.setProperty("javax.net.ssl.trustStore", "NUL");
            System.setProperty("javax.net.ssl.trustStoreType", "WINDOWS-ROOT");
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (this.lastRun != null) {
                this.lastRun.driver().quit();
            }
        }));
        this.log = log;
        Context.getInstance()
                .setDefaultScriptParser(new Sebuilder())
                .addScriptParser(new SeleniumIDE())
                .setDataSourceFactory(new DataSourceFactoryImpl())
                .setStepTypeFactory(new StepTypeFactoryImpl())
                .setTestCaseConverter(new SebuilderToStringConverter())
        ;
        this.setUp(args);
    }

    public void setTestRunListener(final TestRunListener testRunListener) {
        this.testRunListener = testRunListener;
    }

    public void reloadBrowserSetting(final String browserName, final String browserVersion, final String driverPath, final String binaryPath) {
        Context.getInstance().setBrowser(browserName, browserVersion, driverPath, binaryPath);
    }

    public void setUp(final String[] args) {
        this.log.info("setUp start");
        if (!this.validateArgs(args)) {
            this.log.info("Usage: [--driver=<drivername] [--driver.<configkey>=<configvalue>...] [--implicitlyWait=<ms>] [--pageLoadTimeout=<ms>] [--stepTypePackage=<package name>] <script path>...");
            System.exit(0);
        }
        this.preSetUp();
        this.lastRun = null;
        final CommandLineOption option = new CommandLineOption();
        try {
            option.parse(args);
            Context.getInstance()
                    .setImplicitlyWaitTime(option.getImplicitlyWait())
                    .setPageLoadWaitTime(option.getPageLoadTimeout())
                    .setWaitForMaxMs(option.getWaitForMaxMs())
                    .setWaitForIntervalMs(option.getWaitForIntervalMs())
                    .setBrowser(option.getDriver())
                    .setDriverConfig(option.getDriverConfig())
                    .ifMatch(!this.isNullOrEmpty(option.getBrowserVersion())
                            , it -> it.setBrowserVersion(option.getBrowserVersion())
                    )
                    .ifMatch(!Context.isRemote()
                            , it -> it.setWebDriverPath(option.getDriverPath())
                    )
                    .setDataSourceEncoding(option.getDatasourceEncoding())
                    .setDataSourceDirectory(option.getDatasourceDirectory())
                    .setScreenShotOutputDirectory(option.getScreenshotoutput())
                    .setExpectScreenShotDirectory(option.getExpectScreenshotDirectory())
                    .setTemplateOutputDirectory(option.getTemplateoutput())
                    .setResultOutputDirectory(option.getResultoutput())
                    .setReportPrefix(option.getJunitReportPrefix())
                    .setTestRunListenerFactory(option.getReportFormat())
                    .setDownloadDirectory(option.getDownloadoutput())
                    .ifMatch(!this.isNullOrEmpty(option.getAspectFile())
                            , it -> it.setAspect(option.getAspectFile())
                    )
                    .ifMatch(!this.isNullOrEmpty(option.getEnvironmentProperties())
                            , it -> it.setEnvironmentProperties(option.getEnvironmentProperties())
                    )
                    .setEnvironmentProperty(option.getEnvVar())
                    .setLocale(option.getLocale())
                    .setLocaleConfDir(option.getLocaleConf())
            ;
            this.setScripts(option.getScripts());
        } catch (final Exception e) {
            this.log.error("error argument parse:", e);
            System.exit(1);
        }
        this.setTestRunListener(Context.getTestListener(this.log));
        this.log.info("setUp finish");
    }

    public TestRun getTestRun(final TestRunBuilder script, final InputData data, final TestRunListener testRunListener) {
        return script.createTestRun(testRunListener.getLog()
                , Context.getWebDriverFactory()
                , Context.getDriverConfig()
                , Context.getImplicitlyWaitTime()
                , Context.getPageLoadWaitTime()
                , data
                , this.lastRun
                , testRunListener);
    }

    protected boolean validateArgs(final String[] args) {
        return args.length > 0;
    }

    protected void preSetUp() {
    }

    protected void setScripts(final Set<String> scripts) {
    }

    protected boolean isNullOrEmpty(final String target) {
        return Optional.ofNullable(target).filter(it -> !it.isEmpty()).isEmpty();
    }
}
