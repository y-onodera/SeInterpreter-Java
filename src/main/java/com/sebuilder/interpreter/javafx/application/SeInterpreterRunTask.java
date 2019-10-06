package com.sebuilder.interpreter.javafx.application;

import com.sebuilder.interpreter.*;
import com.sebuilder.interpreter.application.SeInterpreterREPL;
import javafx.concurrent.Task;
import org.apache.logging.log4j.Logger;

public class SeInterpreterRunTask extends Task<String> {

    private final Logger log;
    private final TestRunListener listener;
    private final SeInterpreterREPL repl;
    private final TestRunnable runnable;

    public SeInterpreterRunTask(Logger log, TestRunListener listener, SeInterpreterREPL repl, TestRunnable runnable) {
        this.log = log;
        this.listener = listener;
        this.repl = repl;
        this.runnable = runnable;
    }

    @Override
    protected String call() {
        Boolean result = true;
        try {
            this.log.info("operation recieve");
            updateMessage("setup running....");
            this.repl.execute(this.runnable, new TestRunListenerWrapper(this.listener) {
                private int currentScriptSteps;

                @Override
                public boolean openTestSuite(TestCase testCase, String testRunName, TestData aProperty) {
                    this.currentScriptSteps = testCase.steps().size();
                    updateMessage(testRunName);
                    updateProgress(0, this.currentScriptSteps);
                    return super.openTestSuite(testCase, testRunName, aProperty);
                }

                @Override
                public void startTest(String testName) {
                    updateProgress(this.getStepNo(), this.currentScriptSteps);
                    super.startTest(testName);
                }

                @Override
                public void aggregateResult() {
                    super.aggregateResult();
                    updateValue(getResultDir().getAbsolutePath());
                }
            });
        } catch (Throwable ex) {
            result = false;
            this.log.error(ex);
        }
        if (result) {
            this.log.info("operation success");
        } else {
            this.log.info("operation failed");
        }
        return this.listener.getResultDir().getAbsolutePath();
    }
}
