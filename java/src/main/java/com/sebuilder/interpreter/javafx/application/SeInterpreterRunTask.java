package com.sebuilder.interpreter.javafx.application;

import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.TestCase;
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
    private final Debugger debugger;

    public SeInterpreterRunTask(final Logger log, final TestRunListener listener, final SeInterpreterREPL repl, final TestCase target, final Debugger debugger) {
        this.log = log;
        this.listener = listener;
        this.repl = repl;
        this.debugger = debugger;
        this.target = target.map(this.debugger.append())
                .map(it -> it.mapChains(this.debugger.append(), testCase -> true));
    }

    public Debugger getDebugger() {
        return this.debugger;
    }

    @Override
    protected String call() {
        boolean result = true;
        try {
            this.log.info("operation recieve");
            this.updateMessage("setup running....");
            this.repl.execute(this.target, new TestRunListenerWrapper(this.listener) {
                private int currentScriptSteps;

                @Override
                public boolean openTestSuite(final TestCase testCase, final String testRunName, final InputData aProperty) {
                    this.currentScriptSteps = testCase.steps().size();
                    SeInterpreterRunTask.this.updateMessage(testRunName);
                    SeInterpreterRunTask.this.updateProgress(0, this.currentScriptSteps);
                    return super.openTestSuite(testCase, testRunName, aProperty);
                }

                @Override
                public void startTest(final String testName) {
                    SeInterpreterRunTask.this.updateProgress(this.getStepIndex() + 1, this.currentScriptSteps);
                    super.startTest(testName);
                }

                @Override
                public void aggregateResult() {
                    super.aggregateResult();
                    SeInterpreterRunTask.this.updateValue(this.getResultDir().getAbsolutePath());
                }
            });
        } catch (final Throwable ex) {
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
