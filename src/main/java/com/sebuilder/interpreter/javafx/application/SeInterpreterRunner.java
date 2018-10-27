package com.sebuilder.interpreter.javafx.application;

import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.Script;
import com.sebuilder.interpreter.SeInterpreterTestListener;
import com.sebuilder.interpreter.Suite;
import com.sebuilder.interpreter.application.CommandLineArgument;
import com.sebuilder.interpreter.application.SeInterpreterREPL;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;

public class SeInterpreterRunner {

    private int exportCount;

    private SeInterpreterREPL repl;

    private Logger log = LogManager.getLogger(SeInterpreterRunner.class);

    public void reloadSetting(String browserName, String driverPath) {
        if (this.isOpen()) {
            this.close();
        }
        this.repl.reloadBrowserSetting(browserName, driverPath);
    }

    public boolean isOpen() {
        return this.repl != null;
    }

    public Script exportScriptTemplate() {
        if (!this.isOpen()) {
            this.setUp();
        }
        String fileName = "Template" + this.exportCount++ + ".json";
        Script get = this.repl.toScript("{\"steps\":[" + "{\"type\":\"exportTemplate\",\"file\":\"" + fileName + "\"}" + "]}");
        SeInterpreterTestListener listener = new SeInterpreterTestListener(this.log);
        this.repl.execute(get, listener);
        File exported = new File(listener.getTemplateOutputDirectory(), fileName);
        return this.repl.loadScript(exported.getAbsolutePath()).iterator().next();
    }

    public void runScript(Script currentDisplay) {
        this.runScript(currentDisplay, log -> new SeInterpreterTestGUIListener(log));
    }

    public void runScript(Script currentDisplay, Function<Logger, SeInterpreterTestListener> listenerFactory) {
        this.backgroundTaskRunning(() -> {
            if (!this.isOpen()) {
                this.setUp();
            }
            this.repl.execute(currentDisplay, listenerFactory.apply(this.log));
            return true;
        });
    }

    public void runSuite(Suite suite) {
        this.backgroundTaskRunning(() -> {
            if (!this.isOpen()) {
                this.setUp();
            }
            this.repl.execute(suite, new SeInterpreterTestGUIListener(this.log));
            return true;
        });
    }

    public void stopRunning() {
        repl.stopRunning();
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

    private void backgroundTaskRunning(Supplier<Boolean> s) {
        Task task = new Task() {
            @Override
            protected Object call() {
                Boolean result = false;
                try {
                    log.info("operation recieve");
                    result = s.get();
                } catch (Throwable ex) {
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
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(task);
    }

}
