package com.sebuilder.interpreter.application;

import com.google.common.base.Strings;
import com.sebuilder.interpreter.*;
import com.sebuilder.interpreter.datasource.DataSourceFactoryImpl;
import com.sebuilder.interpreter.script.Sebuilder;
import com.sebuilder.interpreter.script.SebuilderToStringConverter;
import com.sebuilder.interpreter.script.seleniumide.SeleniumIDE;
import com.sebuilder.interpreter.step.StepTypeFactoryImpl;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.manager.SeleniumManager;

import java.util.Set;

public abstract class CommandLineRunner {
    protected TestRun lastRun;
    protected TestRunListener testRunListener;
    protected Logger log;

    protected CommandLineRunner(final String[] args, final Logger log) {
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

    public void reloadBrowserSetting(final String browserName, final String driverPath, final String binaryPath) {
        Context.getInstance().setBrowser(browserName, driverPath, binaryPath);
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
                    .setBrowser(option.getDriver())
                    .ifMatch(!Strings.isNullOrEmpty(option.getDriverPath())
                            , it -> it.setWebDriverPath(option.getDriverPath())
                    )
                    .setDriverConfig(option.getDriverConfig())
                    .ifMatch(!Context.isRemote() && Strings.isNullOrEmpty(Context.getWebDriverFactory().getDriverPath())
                            , it -> it.setWebDriverPath(SeleniumManager.getInstance().getDriverPath(Context.getWebDriverFactory().getDriverName())))
                    .setDataSourceEncoding(option.getDatasourceEncoding())
                    .setDataSourceDirectory(option.getDatasourceDirectory())
                    .setScreenShotOutputDirectory(option.getScreenshotoutput())
                    .setTemplateOutputDirectory(option.getTemplateoutput())
                    .setResultOutputDirectory(option.getResultoutput())
                    .setJunitReportPrefix(option.getJunitReportPrefix())
                    .setTestRunListenerFactory(option.getReportFormat())
                    .setDownloadDirectory(option.getDownloadoutput())
                    .ifMatch(!Strings.isNullOrEmpty(option.getAspectFile())
                            , it -> it.setAspect(option.getAspectFile())
                    )
                    .ifMatch(!Strings.isNullOrEmpty(option.getEnvironmentProperties())
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

    protected TestRun getTestRun(final TestRunBuilder script, final InputData data, final TestRunListener testRunListener) {
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
}
