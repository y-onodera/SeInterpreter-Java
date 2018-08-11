package com.sebuilder.interpreter.javafx.application;

import com.google.common.base.Charsets;
import com.google.common.eventbus.Subscribe;
import com.google.common.io.Files;
import com.sebuilder.interpreter.CommandLineArgument;
import com.sebuilder.interpreter.Script;
import com.sebuilder.interpreter.SeInterpreterREPL;
import com.sebuilder.interpreter.javafx.EventBus;
import com.sebuilder.interpreter.javafx.event.ReportErrorEvent;
import com.sebuilder.interpreter.javafx.event.browser.BrowserCloseEvent;
import com.sebuilder.interpreter.javafx.event.browser.BrowserOpenEvent;
import com.sebuilder.interpreter.javafx.event.browser.BrowserRunScriptEvent;
import com.sebuilder.interpreter.javafx.event.script.ScriptReloadEvent;
import com.sebuilder.interpreter.javafx.event.script.ScriptSaveEvent;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;

public class SeInterpreterApplication extends Application {

    private Script currentDisplay;

    private Queue<Consumer<SeInterpreterRunner>> queue = new LinkedBlockingDeque<>();

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("SeInterpreter");
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/seleniumbuilder.fxml")));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
        EventBus.registSubscriber(this);
        new Thread(new SeInterpreterRunner(this.queue)).start();
    }

    @Override
    public void stop() throws Exception {
        this.queue.add((SeInterpreterRunner runner) -> runner.quit());
        super.stop();
    }

    @Subscribe
    public void scriptSave(ScriptSaveEvent event) {
        ReportErrorEvent.publishIfExecuteThrowsException(() -> {
            File target = event.getFile();
            if (!target.exists()) {
                target.createNewFile();
            }
            String content = this.currentDisplay.toJSON().toString(4);
            Files.asCharSink(target, Charsets.UTF_8).write(content);
        });
    }

    @Subscribe
    public void scriptReload(ScriptReloadEvent event) {
        this.currentDisplay = event.getScript();
    }

    @Subscribe
    public void browserOpern(BrowserOpenEvent event) {
        this.queue.add((SeInterpreterRunner runner) -> runner.browserOpern());
    }

    @Subscribe
    public void browserRunScript(BrowserRunScriptEvent event) {
        this.queue.add((SeInterpreterRunner runner) -> runner.browserRunScript(this.currentDisplay));
    }

    @Subscribe
    public void browserClose(BrowserCloseEvent event) {
        this.queue.add((SeInterpreterRunner runner) -> runner.browserClose());
    }


    public static class SeInterpreterRunner implements Runnable {

        private boolean stop;

        private SeInterpreterREPL repl;

        private Queue<Consumer<SeInterpreterRunner>> queue;

        private Logger log = LogManager.getLogger(SeInterpreterRunner.class);

        public SeInterpreterRunner(Queue<Consumer<SeInterpreterRunner>> queue) {
            this.queue = queue;
        }

        @Override
        public void run() {
            log.info("start running");
            while (!stop) {
                Consumer<SeInterpreterRunner> operation = queue.poll();
                if (operation != null) {
                    log.info("operation recieve");
                    operation.accept(this);
                }
            }
            log.info("stop running");
        }

        public void browserOpern() {
            setUp();
            Script get = this.repl.toScript("{\"steps\":[" + "{\"type\":\"get\",\"url\":\"https://www.google.com\"}" + "]}");
            this.repl.execute(get);
        }

        public void browserRunScript(Script currentDisplay) {
            if (this.repl == null) {
                setUp();
            }
            this.repl.execute(currentDisplay);
        }

        public void browserClose() {
            this.repl.tearDownREPL();
            this.repl = null;
        }

        private void setUp() {
            String[] args = new String[]{CommandLineArgument.DRIVER.getArgument("Chrome")};
            this.repl = new SeInterpreterREPL(args, log);
            this.repl.setSeInterpreterTestListener(new SeInterpreterTestGUIListener(log));
            this.repl.setupREPL();
        }

        public void quit() {
            this.stop = true;
        }
    }

}
