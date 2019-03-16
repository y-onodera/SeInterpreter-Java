package com.sebuilder.interpreter.application;

import com.google.common.collect.Lists;
import com.sebuilder.interpreter.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Scanner;

public class SeInterpreterREPL extends CommandLineRunner implements TestRunner {
    private int execCount = 1;

    public SeInterpreterREPL(String[] args, Logger log) {
        super(args, log);
    }

    public static void main(String[] args) {
        Logger log = LogManager.getLogger(SeInterpreterREPL.class);
        SeInterpreterREPL interpreter = new SeInterpreterREPL(args, log);
        try {
            interpreter.run();
        } catch (Throwable e) {
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
        this.seInterpreterTestListener.cleanDir();
    }

    public void runningREPL() {
        final Scanner scanner = new Scanner(System.in);
        StringBuilder input = null;
        boolean commandInput = false;
        while (scanner.hasNext()) {
            String line = scanner.nextLine().trim();
            if (line.equals("exit")) {
                break;
            } else if (!commandInput && line.startsWith("@")) {
                this.loadScript(line.substring(1))
                        .forEach(script -> execute(script));
            } else if (!commandInput && line.startsWith("{")) {
                input = this.startScript();
                input.append(line);
                commandInput = true;
            } else if (commandInput && line.equals("/")) {
                this.closeScript(input);
                commandInput = false;
                TestCase testCase = this.toTestCase(input.toString());
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
        if (this.driver != null) {
            this.driver.quit();
            this.driver = null;
            this.lastRun = null;
        }
    }

    public Iterable<TestCase> loadScript(String file) {
        Iterable<TestCase> result = Lists.newArrayList();
        try {
            result = this.sf.parse(new File(file));
        } catch (Throwable e) {
            this.log.error(e);
        }
        return result;
    }

    public TestCase toTestCase(String cmdInput) {
        this.log.info("start parse input");
        TestCase result = null;
        try {
            result = this.sf.parse(cmdInput);
        } catch (Throwable e) {
            this.log.error(e);
        }
        if (result == null) {
            this.log.error("invalid input:" + cmdInput);
        }
        this.log.info("finish parse input");
        return result;
    }

    public void execute(TestCase testCase) {
        this.execute(createTestRunBuilder(testCase), this.seInterpreterTestListener);
    }

    @Override
    public void execute(Suite suite, SeInterpreterTestListener seInterpreterTestListener) {
        seInterpreterTestListener.cleanResult(new File(Context.getInstance().getResultOutputDirectory(), String.valueOf(execCount++)));
        try {
            for (TestRunBuilder builder : suite.getTestRuns()) {
                boolean stop = this.execute(builder, seInterpreterTestListener);
                if (stop) {
                    break;
                }
            }
        } catch (Throwable t) {
            log.error(t);
        } finally {
            seInterpreterTestListener.aggregateResult();
        }
    }

    @Override
    public void execute(TestCase testCase, SeInterpreterTestListener seInterpreterTestListener) {
        seInterpreterTestListener.cleanResult(new File(Context.getInstance().getResultOutputDirectory(), String.valueOf(execCount++)));
        try {
            this.execute(createTestRunBuilder(testCase), seInterpreterTestListener);
        } catch (Throwable t) {
            log.error(t);
        } finally {
            seInterpreterTestListener.aggregateResult();
        }
    }

    public void stopRunning() {
        if (this.lastRun != null) {
            this.log.info("stop execute test");
            this.lastRun.stop();
        }
    }

    @Override
    protected TestRunBuilder createTestRunBuilder(TestCase testCase) {
        return super.createTestRunBuilder(testCase.usePreviousDriverAndVars(true));
    }

    private boolean execute(TestRunBuilder testRunBuilder, SeInterpreterTestListener seInterpreterTestListener) {
        for (TestData data : testRunBuilder.loadData()) {
            boolean stop = this.execute(testRunBuilder, data, seInterpreterTestListener);
            if (stop) {
                return true;
            }
        }
        return false;
    }

    private boolean execute(TestRunBuilder testRunBuilder, TestData data, SeInterpreterTestListener seInterpreterTestListener) {
        this.lastRun = this.getTestRun(testRunBuilder, data, seInterpreterTestListener);
        this.log.info("start execute test");
        this.lastRun.finish();
        this.log.info("finish execute test");
        if (this.driver == null && lastRun != null) {
            this.driver = this.lastRun.driver();
        }
        return this.lastRun.isStopped();
    }

    private StringBuilder startScript() {
        this.log.info("start input accept");
        return new StringBuilder().append("{\"steps\":[");
    }

    private void closeScript(StringBuilder script) {
        script.append("]}");
        this.log.info("finish input accept");
    }

}