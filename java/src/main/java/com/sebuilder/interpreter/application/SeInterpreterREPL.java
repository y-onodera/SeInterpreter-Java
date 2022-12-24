package com.sebuilder.interpreter.application;

import com.google.common.base.Throwables;
import com.sebuilder.interpreter.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Scanner;

public class SeInterpreterREPL extends CommandLineRunner implements TestRunner {
    private int execCount = 1;

    public SeInterpreterREPL(final String[] args, final Logger log) {
        super(args, log);
    }

    public static void main(final String[] args) {
        final Logger log = LogManager.getLogger(SeInterpreterREPL.class);
        final SeInterpreterREPL interpreter = new SeInterpreterREPL(args, log);
        try {
            interpreter.run();
        } catch (final Throwable e) {
            log.fatal("Run error.", e);
            System.exit(1);
        }
    }

    public void run() {
        this.setUpREPL();
        try {
            this.runningREPL();
        } finally {
            this.tearDownREPL();
        }
    }

    public void setUpREPL() {
        this.testRunListener.cleanDir();
    }

    public void runningREPL() {
        final Scanner scanner = new Scanner(System.in);
        StringBuilder input = null;
        boolean commandInput = false;
        while (scanner.hasNext()) {
            final String line = scanner.nextLine().trim();
            if (line.equals("exit")) {
                break;
            } else if (!commandInput && line.startsWith("@")) {
                this.execute(this.loadScript(line.substring(1)), this.testRunListener);
            } else if (!commandInput && line.startsWith("{")) {
                input = this.startScript();
                input.append(line);
                commandInput = true;
            } else if (commandInput && line.equals("/")) {
                this.closeScript(input);
                commandInput = false;
                final TestCase testCase = this.toTestCase(input.toString());
                input = null;
                if (testCase != null) {
                    this.execute(testCase);
                }
            } else if (commandInput) {
                input.append(line);
            }
        }
    }

    public void tearDownREPL() {
        if (this.lastRun != null) {
            this.lastRun.driver().quit();
            this.lastRun = null;
        }
    }

    public TestCase loadScript(final String file) {
        try {
            return Context.getScriptParser().load(new File(file));
        } catch (final Throwable e) {
            this.log.error(e);
        }
        return new TestCaseBuilder().build();
    }

    public void execute(final TestCase target, final TestRunListener seInterpreterTestListener) {
        seInterpreterTestListener.cleanResult(new File(Context.getResultOutputDirectory(), String.valueOf(this.execCount++)));
        try {
            target.map(it -> it.isShareState(true))
                    .run(this, seInterpreterTestListener);
        } catch (final Throwable t) {
            this.log.error(Throwables.getStackTraceAsString(t));
        } finally {
            seInterpreterTestListener.aggregateResult();
        }
    }

    @Override
    public STATUS execute(final TestRunBuilder testRunBuilder, final InputData data, final TestRunListener seInterpreterTestListener) {
        this.lastRun = this.getTestRun(testRunBuilder, data, seInterpreterTestListener);
        this.log.info("start execute test");
        final boolean result = this.lastRun.finish();
        this.log.info("finish execute test");
        if (this.lastRun.isStopped()) {
            return STATUS.STOPPED;
        } else if (!result) {
            return STATUS.FAILED;
        }
        return STATUS.SUCCESS;
    }

    public void stopRunning() {
        if (this.lastRun != null) {
            this.log.info("stop execute test");
            this.lastRun.stop();
        }
    }

    @Override
    protected boolean validateArgs(final String[] args) {
        return true;
    }

    private TestCase toTestCase(final String cmdInput) {
        this.log.info("start load input");
        TestCase result = null;
        try {
            result = Context.getScriptParser().load(cmdInput);
        } catch (final Throwable e) {
            this.log.error(e);
        }
        if (result == null) {
            this.log.error("invalid input:" + cmdInput);
        }
        this.log.info("finish load input");
        return result;
    }

    private void execute(final TestCase testCase) {
        this.execute(testCase, this.testRunListener);
    }

    private StringBuilder startScript() {
        this.log.info("start input finish");
        return new StringBuilder().append("{\"steps\":[");
    }

    private void closeScript(final StringBuilder script) {
        script.append("]}");
        this.log.info("finish input finish");
    }

}