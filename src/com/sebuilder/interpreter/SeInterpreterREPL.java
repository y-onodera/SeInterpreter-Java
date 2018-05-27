package com.sebuilder.interpreter;

import com.google.common.collect.Lists;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SeInterpreterREPL extends CommandLineRunner {
    private RemoteWebDriver driver;

    public SeInterpreterREPL(String[] args, Log log) {
        super(args, log);
    }

    public static void main(String[] args) {
        Log log = LogFactory.getFactory().getInstance(SeInterpreterREPL.class);
        SeInterpreterREPL interpreter = new SeInterpreterREPL(args, log);
        try {
            interpreter.run();
        } catch (Exception e) {
            log.fatal("Run error.", e);
            System.exit(1);
        }
    }

    private void run() {
        setupREPL();
        runningREPL();
        tearDownREPL();
    }

    private void setupREPL() {
        this.seInterpreterTestListener.cleanResult();
        this.seInterpreterTestListener.openTestSuite("REPL", new Hashtable<>());
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
                List<Script> scripts = loadScript(line.substring(1));
                scripts.forEach(script -> execute(script));
            } else if (!commandInput && line.startsWith("{")) {
                input = startScript();
                input.append(line);
                commandInput = true;
            } else if (commandInput && line.equals("/")) {
                closeScript(input);
                commandInput = false;
                Script script = toScript(input.toString());
                input = null;
                if (script != null) {
                    execute(script);
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
        this.seInterpreterTestListener.closeTestSuite();
        this.seInterpreterTestListener.aggregateResult();
    }

    private void execute(Script script) {
        script.stateTakeOver();
        int i = 1;
        for (Map<String, String> data : script.dataRows) {
            data.put(DataSource.ROW_NUMBER, String.valueOf(i));
            execute(script, data);
            i++;
        }
    }

    private void execute(Script script, Map<String, String> data) {
        lastRun = script.createTestRun(this.log, wdf, driverConfig, data, lastRun, seInterpreterTestListener);
        if (this.driver == null) {
            this.driver = lastRun.driver();
        }
        while (lastRun.hasNext()) {
            try {
                if (lastRun.next()) {
                    this.log.info(lastRun.currentStep().toPrettyString() + " succeeded");
                } else {
                    this.log.info(lastRun.currentStep().toPrettyString() + " failed");
                }
            } catch (AssertionError e) {
                this.log.error("error " + lastRun.currentStep().toPrettyString(), e);
            }
        }
    }

    private List<Script> loadScript(String file) {
        List<Script> result = Lists.newArrayList();
        try {
            result = sf.parse(new File(file));
        } catch (IOException | JSONException | RuntimeException e) {
            this.log.error(e);
        }
        return result;
    }

    private Script toScript(String cmdInput) {
        Script result = null;
        try {
            result = sf.parse(cmdInput);
        } catch (IOException | JSONException e) {
            this.log.error(e);
        }
        if (result == null) {
            this.log.error("invalid input:" + cmdInput);
        }
        return result;
    }

    private StringBuilder startScript() {
        return new StringBuilder().append("{\"steps\":[");
    }

    private void closeScript(StringBuilder script) {
        script.append("]}");
    }

}
