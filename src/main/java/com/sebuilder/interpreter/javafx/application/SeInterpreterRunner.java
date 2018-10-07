package com.sebuilder.interpreter.javafx.application;

import com.sebuilder.interpreter.*;
import com.sebuilder.interpreter.javafx.EventBus;
import com.sebuilder.interpreter.javafx.event.script.ScriptSelectEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Function;

public class SeInterpreterRunner implements Runnable {

    private int exportCount;

    private boolean stop;

    private SeInterpreterREPL repl;

    private Queue<Consumer<SeInterpreterRunner>> queue;

    private Logger log = LogManager.getLogger(SeInterpreterRunner.class);

    public SeInterpreterRunner(Queue<Consumer<SeInterpreterRunner>> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        this.log.info("start running");
        while (!this.stop) {
            Consumer<SeInterpreterRunner> operation = this.queue.poll();
            if (operation != null) {
                try {
                    this.log.info("operation recieve");
                    operation.accept(this);
                } catch (Throwable ex) {
                    this.log.error(ex);
                }
            }
        }
        this.log.info("stop running");
    }

    public void reloadBrowserSetting(String browserName, String driverPath) {
        if (this.isBrowserOpen()) {
            this.browserClose();
        }
        this.setUp();
        this.repl.reloadBrowserSetting(browserName, driverPath);
    }

    public boolean isBrowserOpen() {
        return this.repl != null;
    }

    public Script browserExportScriptTemplate() {
        if (!this.isBrowserOpen()) {
            this.setUp();
        }
        String fileName = "exportedBrowserTemplate" + this.exportCount++ + ".json";
        Script get = this.repl.toScript("{\"steps\":[" + "{\"type\":\"exportTemplate\",\"file\":\"" + fileName + "\"}" + "]}");
        this.repl.execute(get, new SeInterpreterTestListener(this.log));
        File exported = new File(Context.getInstance().getTemplateOutputDirectory(), fileName);
        return this.repl.loadScript(exported.getAbsolutePath()).iterator().next();
    }

    public void browserRunScript(Script currentDisplay) {
        this.browserRunScript(currentDisplay, log -> new SeInterpreterTestGUIListener(log));
    }

    public void browserRunScript(Script currentDisplay, Function<Logger, SeInterpreterTestListener> listenerFactory) {
        if (!this.isBrowserOpen()) {
            this.setUp();
        }
        this.repl.execute(currentDisplay, listenerFactory.apply(this.log));
    }

    public void browserRunSuite(Suite suite) {
        suite.forEach(it -> {
            EventBus.publish(new ScriptSelectEvent(it.name));
            this.browserRunScript(it);
        });
    }

    public void browserClose() {
        if (!this.isBrowserOpen()) {
            return;
        }
        this.repl.tearDownREPL();
        this.repl = null;
    }

    public void quit() {
        this.stop = true;
    }

    private void setUp() {
        String[] args = new String[]{CommandLineArgument.DRIVER.getArgument("Chrome")};
        this.repl = new SeInterpreterREPL(args, log);
        this.repl.setSeInterpreterTestListener(new SeInterpreterTestGUIListener(log));
        this.repl.setupREPL();
    }

}
