package com.sebuilder.interpreter.javafx.application;

import com.sebuilder.interpreter.*;
import com.sebuilder.interpreter.application.CommandLineArgument;
import com.sebuilder.interpreter.application.SeInterpreterREPL;
import com.sebuilder.interpreter.steptype.HighLightElement;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class SeInterpreterRunner {

    private int exportCount;

    private SeInterpreterREPL repl;

    private Logger log = LogManager.getLogger(SeInterpreterRunner.class);

    public SeInterpreterRunner(List<String> raw) {
        this.repl = new SeInterpreterREPL(raw.toArray(new String[raw.size()]), log);
        this.repl.setUpREPL();
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

    public Script exportScriptTemplate() {
        String locator = new Locator("css selector", "body").toString();
        return exportTemplate(locator);
    }

    public void highLightElement(String locatorType, String value) {
        Step highLightElement = new Step(new HighLightElement());
        highLightElement.put("locator", new Locator(locatorType, value));
        Script highLight = new ScriptBuilder()
                .addStep(highLightElement)
                .createScript();
        SeInterpreterTestListener listener = new SimpleSeInterpreterTestListener(this.log);
        this.repl.execute(highLight, listener);
    }

    public Locator handleFocusElement() {
        String locator = new Locator("focus", "").toString();
        return exportTemplate(locator)
                .steps
                .get(0)
                .getLocator("locator");
    }

    public Task createRunScriptTask(Script currentDisplay) {
        return this.createRunScriptTask(currentDisplay, log -> new SeInterpreterTestGUIListener(log));
    }

    public Task createRunScriptTask(Script currentDisplay, Function<Logger, SeInterpreterTestListener> listenerFactory) {
        return this.createBackgroundTask(currentDisplay, listenerFactory.apply(this.log));
    }

    public Task createRunSuiteTask(Suite suite) {
        return this.createBackgroundTask(suite, new SeInterpreterTestGUIListener(this.log));
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

    private Task createBackgroundTask(TestRunnable runnable, SeInterpreterTestListener listener) {
        if (!this.isOpen()) {
            this.setUp();
        }
        return new Task() {
            @Override
            protected Object call() {
                Boolean result = true;
                try {
                    log.info("operation recieve");
                    updateMessage("setup running....");
                    runnable.accept(repl, new SeInterpreterTestListenerWrapper(listener) {
                        private int currentScriptSteps;

                        @Override
                        public boolean openTestSuite(Script script, String testRunName, Map<String, String> aProperty) {
                            this.currentScriptSteps = script.steps.size();
                            updateMessage(testRunName);
                            updateProgress(0, this.currentScriptSteps);
                            return super.openTestSuite(script, testRunName, aProperty);
                        }

                        @Override
                        public void startTest(String testName) {
                            updateProgress(this.getStepNo(), this.currentScriptSteps);
                            super.startTest(testName);
                        }
                    });
                } catch (Throwable ex) {
                    result = false;
                    log.error(ex);
                }
                if (result) {
                    log.info("operation success");
                } else {
                    log.info("operation failed");
                }
                return result;
            }
        };
    }

    private Script exportTemplate(String locator) {
        if (!this.isOpen()) {
            this.setUp();
        }
        String fileName = "Template" + this.exportCount++ + ".json";
        Script get = this.repl.toScript("{\"steps\":[{\"type\":\"exportTemplate\",\"file\":\"" + fileName + "\",\"locator\":" + locator + "}]}");
        SeInterpreterTestListener listener = new SimpleSeInterpreterTestListener(this.log);
        this.repl.execute(get, listener);
        File exported = new File(listener.getTemplateOutputDirectory(), fileName);
        Script result = this.repl.loadScript(exported.getAbsolutePath()).iterator().next();
        return result.builder()
                .associateWith(null)
                .setName(result.name)
                .createScript();
    }
}
