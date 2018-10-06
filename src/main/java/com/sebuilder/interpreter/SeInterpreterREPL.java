package com.sebuilder.interpreter;

import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class SeInterpreterREPL extends CommandLineRunner {
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
        try {
            this.setupREPL();
            this.runningREPL();
        } finally {
            this.tearDownREPL();
        }
    }

    public void setupREPL() {
        this.seInterpreterTestListener.cleanResult();
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
                Script script = this.toScript(input.toString());
                input = null;
                if (script != null) {
                    this.execute(script);
                }
            } else if (commandInput) {
                input.append(line);
            }
        }
    }

    public void tearDownREPL() {
        if (this.driver != null) {
            this.driver.quit();
        }
        this.seInterpreterTestListener.aggregateResult();
    }

    public Iterable<Script> loadScript(String file) {
        Iterable<Script> result = Lists.newArrayList();
        try {
            result = this.sf.parse(new File(file));
        } catch (Throwable e) {
            this.log.error(e);
        }
        return result;
    }

    public Script toScript(String cmdInput) {
        this.log.info("start parse input");
        Script result = null;
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

    public void execute(Script script) {
        this.execute(script, this.seInterpreterTestListener);
    }

    public void execute(Script script, SeInterpreterTestListener seInterpreterTestListener) {
        String suiteName = "com.sebuilder.interpreter.REPL_EXEC" + this.execCount++;
        script.stateTakeOver(new HashMap<>());
        int i = 1;
        for (Map<String, String> data : script.dataRows) {
            seInterpreterTestListener.openTestSuite(suiteName + "_row_" + i, data);
            data.put(DataSource.ROW_NUMBER, String.valueOf(i));
            this.execute(script, data, seInterpreterTestListener);
            seInterpreterTestListener.closeTestSuite();
            i++;
        }
    }

    private void execute(Script script, Map<String, String> data, SeInterpreterTestListener seInterpreterTestListener) {
        this.log.info("start execute script");
        this.lastRun = script.createTestRun(this.log, this.wdf, this.driverConfig, data, this.lastRun, seInterpreterTestListener);
        while (this.lastRun.hasNext()) {
            try {
                this.lastRun.next();
            } catch (AssertionError error) {
                // nothing to do;
            }
        }
        if (this.driver == null) {
            this.driver = this.lastRun.driver();
        }
        this.log.info("finish execute script");
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
