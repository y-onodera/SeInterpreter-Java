package com.sebuilder.interpreter.javafx.application;

import com.sebuilder.interpreter.TestCase;
import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.TestRunListener;
import com.sebuilder.interpreter.TestRunListenerWrapper;
import com.sebuilder.interpreter.application.SeInterpreterREPL;
import javafx.concurrent.Task;
import org.apache.logging.log4j.Logger;

public class SeInterpreterRunTask extends Task<String> {

    private final Logger log;
    private final TestRunListener listener;
    private final SeInterpreterREPL repl;
    private final TestCase target;

    public SeInterpreterRunTask(Logger log, TestRunListener listener, SeInterpreterREPL repl, TestCase target) {
        this.log = log;
        this.listener = listener;
        this.repl = repl;
        this.target = target;
    }

    @Override
    protected String call() {
        Boolean result = true;
        try {
            this.log.info("operation recieve");
            updateMessage("setup running....");
            this.repl.execute(this.target, new TestRunListenerWrapper(this.listener) {
                private int currentScriptSteps;

                @Override
                public boolean openTestSuite(TestCase testCase, String testRunName, InputData aProperty) {
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
