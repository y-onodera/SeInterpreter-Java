package com.sebuilder.interpreter;

import com.google.common.collect.Lists;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.*;

public class SeInterpreterREPL extends CommandLineRunner {
    private int execCount = 1;

    public SeInterpreterREPL(String[] args, Log log) {
        super(args, log);
    }

    public static void main(String[] args) {
        Log log = LogFactory.getFactory().getInstance(SeInterpreterREPL.class);
        SeInterpreterREPL interpreter = new SeInterpreterREPL(args, log);
        try {
            interpreter.run();
        } catch (Throwable e) {
            log.fatal("Run error.", e);
            System.exit(1);
        }
    }

    private void run() {
        try {
            this.setupREPL();
            this.runningREPL();
        } finally {
            this.tearDownREPL();
        }
    }

    private void setupREPL() {
        this.seInterpreterTestListener.cleanResult();
    }

    private void runningREPL() {
        final Scanner scanner = new Scanner(System.in);
        StringBuilder input = null;
        boolean commandInput = false;
        while (scanner.hasNext()) {
            String line = scanner.nextLine().trim();
            if (line.equals("exit")) {
                break;
            } else if (!commandInput && line.startsWith("@")) {
                List<Script> scripts = this.loadScript(line.substring(1));
                scripts.forEach(script -> execute(script));
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

    private void tearDownREPL() {
        if (this.driver != null) {
            this.driver.quit();
        }
        this.seInterpreterTestListener.aggregateResult();
    }

    private void execute(Script script) {
        String suiteName = "com.sebuilder.interpreter.REPL_EXEC" + this.execCount++;
        script.stateTakeOver(new HashMap<>());
        int i = 1;
        for (Map<String, String> data : script.dataRows) {
            this.seInterpreterTestListener.openTestSuite(suiteName + "_row_" + i, data);
            data.put(DataSource.ROW_NUMBER, String.valueOf(i));
            this.execute(script, data);
            this.seInterpreterTestListener.closeTestSuite();
            i++;
        }
    }

    private void execute(Script script, Map<String, String> data) {
        this.log.info("start execute script");
        this.lastRun = script.createTestRun(this.log, this.wdf, this.driverConfig, data, this.lastRun, this.seInterpreterTestListener);
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

    private List<Script> loadScript(String file) {
        List<Script> result = Lists.newArrayList();
        try {
            result = this.sf.parse(new File(file));
        } catch (Throwable e) {
            this.log.error(e);
        }
        return result;
    }

    private Script toScript(String cmdInput) {
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

    private StringBuilder startScript() {
        this.log.info("start input accept");
        return new StringBuilder().append("{\"steps\":[");
    }

    private void closeScript(StringBuilder script) {
        script.append("]}");
        this.log.info("finish input accept");
    }

}
