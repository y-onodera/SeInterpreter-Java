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
        if (this.isOpen()) {
            this.close();
            ;
        }
        this.log.info("stop running");
    }

    public void reloadSetting(String browserName, String driverPath) {
        if (this.isOpen()) {
            this.close();
        }
        this.setUp();
        this.repl.reloadBrowserSetting(browserName, driverPath);
    }

    public boolean isOpen() {
        return this.repl != null;
    }

    public Script exportScriptTemplate() {
        if (!this.isOpen()) {
            this.setUp();
        }
        String fileName = "exportedBrowserTemplate" + this.exportCount++ + ".json";
        Script get = this.repl.toScript("{\"steps\":[" + "{\"type\":\"exportTemplate\",\"file\":\"" + fileName + "\"}" + "]}");
        this.repl.execute(get, new SeInterpreterTestListener(this.log));
        File exported = new File(Context.getInstance().getTemplateOutputDirectory(), fileName);
        return this.repl.loadScript(exported.getAbsolutePath()).iterator().next();
    }

    public void runScript(Script currentDisplay) {
        this.runScript(currentDisplay, log -> new SeInterpreterTestGUIListener(log));
    }

    public void runScript(Script currentDisplay, Function<Logger, SeInterpreterTestListener> listenerFactory) {
        if (!this.isOpen()) {
            this.setUp();
        }
        this.repl.execute(currentDisplay, listenerFactory.apply(this.log));
    }

    public void runSuite(Suite suite) {
        suite.forEach(it -> {
            EventBus.publish(new ScriptSelectEvent(it.name));
            this.runScript(it);
        });
    }

    public void close() {
        if (!this.isOpen()) {
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
