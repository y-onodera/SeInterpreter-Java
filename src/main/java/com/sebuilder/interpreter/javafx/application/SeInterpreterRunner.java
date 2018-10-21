package com.sebuilder.interpreter.javafx.application;

import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.Script;
import com.sebuilder.interpreter.SeInterpreterTestListener;
import com.sebuilder.interpreter.Suite;
import com.sebuilder.interpreter.application.CommandLineArgument;
import com.sebuilder.interpreter.application.SeInterpreterREPL;
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
                    this.log.info("operation recieve:" + operation);
                    operation.accept(this);
                } catch (Throwable ex) {
                    this.log.error(ex);
                }
            }
        }
        if (this.isOpen()) {
            this.close();
        }
        this.log.info("stop running");
    }

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

    public void runSuite(Suite suite) {
        if (!this.isOpen()) {
            this.setUp();
        }
        this.repl.execute(suite, new SeInterpreterTestGUIListener(this.log));
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

    public void close() {
        if (!this.isOpen()) {
            return;
        }
        this.repl.tearDownREPL();
    }

    public void quit() {
        this.stop = true;
    }

    private void setUp() {
        if (this.repl == null) {
            String[] args = new String[]{CommandLineArgument.DRIVER.getArgument(Context.getInstance().getBrowser())};
            this.repl = new SeInterpreterREPL(args, log);
            this.repl.setUpREPL();
        }
    }

}
