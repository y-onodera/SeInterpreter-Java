package com.sebuilder.interpreter.javafx.application;

import com.google.common.io.Files;
import com.sebuilder.interpreter.*;
import com.sebuilder.interpreter.application.CommandLineArgument;
import com.sebuilder.interpreter.application.SeInterpreterREPL;
import com.sebuilder.interpreter.step.type.ExportTemplate;
import com.sebuilder.interpreter.step.type.HighLightElement;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

public class SeInterpreterRunner {

    private int exportCount;

    private SeInterpreterREPL repl;

    private Logger log = LogManager.getLogger(SeInterpreterRunner.class);

    private TestRunListener globalListener;

    public SeInterpreterRunner(List<String> raw) {
        this.repl = new SeInterpreterREPL(raw.toArray(new String[raw.size()]), log);
        this.repl.setUpREPL();
        this.globalListener = new TestRunListenerImpl(this.log);
        this.globalListener.setUpDir(Context.getInstance().getResultOutputDirectory());
    }

    public File getDataSourceDirectory() {
        return Context.getInstance().getDataSourceDirectory();
    }

    public File getTemplateOutputDirectory() {
        return this.globalListener.getTemplateOutputDirectory();
    }

    public void reloadSetting(String browserName, String driverPath) {
        if (this.isOpen()) {
            this.close();
        } else {
            this.setUp();
        }
        this.repl.reloadBrowserSetting(browserName, driverPath);
    }

    public boolean isOpen() {
        return this.repl != null;
    }

    public TestCase exportTemplate(Locator locator, List<String> targetTags, boolean withDataSource) {
        if (!this.isOpen()) {
            this.setUp();
        }
        String fileName = "Template" + this.exportCount + ".json";
        StepBuilder export = new ExportTemplate()
                .toStep()
                .locator(locator)
                .put("file", fileName)
                .put("filterTag", "true");
        for (String targetTag : targetTags) {
            export.put(targetTag, "true");
        }
        final String dataSourceName = "Template" + this.exportCount + ".csv";
        if (withDataSource) {
            export.put("datasource", dataSourceName);
        }
        TestCase get = export.build().toTestCase();
        TestRunListener listener = new TestRunListenerImpl(this.log);
        this.repl.execute(get, listener);
        File exported = new File(listener.getTemplateOutputDirectory(), fileName);
        if (!exported.exists()) {
            return new TestCaseBuilder().build();
        }
        this.exportCount++;
        if (withDataSource) {
            File exportedDataSource = new File(listener.getTemplateOutputDirectory(), dataSourceName);
            if (exportedDataSource.exists()) {
                try {
                    Files.copy(exportedDataSource, new File(this.globalListener.getTemplateOutputDirectory(), dataSourceName));
                } catch (IOException e) {
                    this.log.error(e);
                }
            }
        }
        TestCase result = this.repl.loadScript(exported.getAbsolutePath()).iterator().next();
        return result.builder()
                .associateWith(null)
                .setName(result.name())
                .build();
    }

    public void highLightElement(String locatorType, String value) {
        Step highLightElement = new HighLightElement()
                .toStep()
                .locator(new Locator(locatorType, value))
                .build();
        TestCase highLight = highLightElement.toTestCase();
        TestRunListener listener = new TestRunListenerImpl(this.log);
        this.repl.execute(highLight, listener);
    }

    public Task createRunScriptTask(TestCase currentDisplay) {
        return this.createRunScriptTask(currentDisplay, log -> new GUITestRunListener(log));
    }

    public Task createRunScriptTask(TestCase currentDisplay, Function<Logger, TestRunListener> listenerFactory) {
        return this.createBackgroundTask(currentDisplay, listenerFactory.apply(this.log));
    }

    public Task createRunSuiteTask(Suite suite) {
        return this.createBackgroundTask(suite, new GUITestRunListener(this.log));
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
            String[] args = new String[]{CommandLineArgument.DRIVER.getArgument(Context.getInstance().getBrowser())};
            this.repl = new SeInterpreterREPL(args, log);
            this.repl.setUpREPL();
        }
    }

    private Task createBackgroundTask(TestRunnable runnable, TestRunListener listener) {
        if (!this.isOpen()) {
            this.setUp();
        }
        return new SeInterpreterRunTask(this.log, listener, this.repl, runnable);
    }
}
