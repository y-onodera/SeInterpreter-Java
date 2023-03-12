package com.sebuilder.interpreter.javafx.application;

import com.google.common.io.Files;
import com.sebuilder.interpreter.*;
import com.sebuilder.interpreter.application.CommandLineArgument;
import com.sebuilder.interpreter.application.SeInterpreterREPL;
import com.sebuilder.interpreter.step.type.ExportTemplate;
import com.sebuilder.interpreter.step.type.HighLightElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

public class SeInterpreterRunner {

    private int exportCount;

    private int screenshotCount;
    private SeInterpreterREPL repl;

    private final Logger log = LogManager.getLogger(SeInterpreterRunner.class);

    private final TestRunListener globalListener;

    public SeInterpreterRunner(final List<String> raw) {
        this.repl = new SeInterpreterREPL(raw.toArray(new String[0]), this.log);
        this.repl.setUpREPL();
        this.globalListener = Context.getTestListener(this.log);
        this.globalListener.setUpDir(Context.getResultOutputDirectory());
    }

    public Logger getLog() {
        return this.log;
    }

    public TestRunListener getGlobalListener() {
        return this.globalListener;
    }

    public File getDataSourceDirectory() {
        return Context.getDataSourceDirectory();
    }

    public File getTemplateOutputDirectory() {
        return this.globalListener.getTemplateOutputDirectory();
    }

    public void reloadSetting(final String browserName, final String driverPath, final String binaryPath) {
        if (this.isOpen()) {
            this.close();
        } else {
            this.setUp();
        }
        this.repl.reloadBrowserSetting(browserName, driverPath, binaryPath);
    }

    public boolean isOpen() {
        return this.repl != null;
    }

    public void run(final TestCase testCase) {
        final TestRunListener listener = Context.getTestListener(this.log);
        this.repl.execute(testCase, listener);
    }

    public void highlightElement(final String locatorType, final String value) {
        final Step highLightElement = new HighLightElement()
                .toStep()
                .locator(new Locator(locatorType, value))
                .build();
        final TestRunListener listener = Context.getTestListener(this.log);
        this.repl.copy().execute(highLightElement.toTestCase().map(it -> it.isPreventContextAspect(true)), listener);
    }

    public TestCase exportTemplate(final Locator locator, final List<String> targetTags, final boolean withDataSource) {
        if (!this.isOpen()) {
            this.setUp();
        }
        final String fileName = "Template" + this.exportCount + ".json";
        final StepBuilder export = new ExportTemplate()
                .toStep()
                .locator(locator)
                .put("file", fileName)
                .put("filterTag", "true");
        for (final String targetTag : targetTags) {
            export.put(targetTag, "true");
        }
        final String dataSourceName = "Template" + this.exportCount + ".csv";
        if (withDataSource) {
            export.put("datasource", dataSourceName);
        }
        final TestCase get = export.build()
                .toTestCase()
                .map(it -> it.isPreventContextAspect(true));
        final TestRunListener listener = Context.getTestListener(this.log);
        this.repl.copy().execute(get, listener);
        final File exported = new File(listener.getTemplateOutputDirectory(), fileName);
        if (!exported.exists()) {
            return new TestCaseBuilder().build();
        }
        this.exportCount++;
        if (withDataSource) {
            final File exportedDataSource = new File(listener.getTemplateOutputDirectory(), dataSourceName);
            if (exportedDataSource.exists()) {
                try {
                    Files.copy(exportedDataSource, new File(this.globalListener.getTemplateOutputDirectory(), dataSourceName));
                } catch (final IOException e) {
                    this.log.error(e);
                }
            }
        }
        final TestCase result = this.repl.loadScript(exported.getAbsolutePath());
        return result.builder()
                .associateWith(null)
                .setName(result.name())
                .build();
    }

    public File screenShot(final StepBuilder stepBuilder) {
        final String result = String.format("%04d", this.screenshotCount++) + "_" + stepBuilder.getStringParams().get("file");
        final TestCase saveScreenshot = stepBuilder.put("file", result)
                .put("addPrefix", "false")
                .build()
                .toTestCase().map(it -> it.isPreventContextAspect(true));
        final TestRunListener listener = Context.getTestListener(this.log);
        this.repl.copy().execute(saveScreenshot, listener);
        final File saved = new File(listener.getScreenShotOutputDirectory(), result);
        final File copyTo = new File(this.globalListener.getScreenShotOutputDirectory(), result);
        try {
            Files.copy(saved, copyTo);
        } catch (final IOException e) {
            this.log.error(e);
        }
        return copyTo;
    }

    public SeInterpreterRunTask createRunScriptTask(final TestCase currentDisplay, final Debugger debugger, final Function<Logger, TestRunListener> listenerFactory) {
        return this.createBackgroundTask(currentDisplay, debugger, listenerFactory.apply(this.log));
    }

    public void stopRunning() {
        this.repl.stopRunning();
    }

    public void close() {
        if (!this.isOpen()) {
            return;
        }
        this.stopRunning();
        this.repl.tearDownREPL();
    }

    private void setUp() {
        if (this.repl == null) {
            final String[] args = new String[]{CommandLineArgument.DRIVER.createArgument(Context.getBrowser())};
            this.repl = new SeInterpreterREPL(args, this.log);
            this.repl.setUpREPL();
        }
    }

    private SeInterpreterRunTask createBackgroundTask(final TestCase testCase, final Debugger debugger, final TestRunListener listener) {
        if (!this.isOpen()) {
            this.setUp();
        }
        return new SeInterpreterRunTask(this.log, listener, this.repl, testCase, debugger);
    }

}
