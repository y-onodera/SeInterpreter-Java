package com.sebuilder.interpreter.javafx.application;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import com.google.common.io.Files;
import com.sebuilder.interpreter.*;
import com.sebuilder.interpreter.factory.ScriptFactory;
import com.sebuilder.interpreter.javafx.EventBus;
import com.sebuilder.interpreter.javafx.event.ReportErrorEvent;
import com.sebuilder.interpreter.javafx.event.browser.BrowserCloseEvent;
import com.sebuilder.interpreter.javafx.event.browser.BrowserOpenEvent;
import com.sebuilder.interpreter.javafx.event.browser.LoadTemplateEvent;
import com.sebuilder.interpreter.javafx.event.file.FileLoadEvent;
import com.sebuilder.interpreter.javafx.event.file.FileSaveEvent;
import com.sebuilder.interpreter.javafx.event.replay.HighLightTargetElementEvent;
import com.sebuilder.interpreter.javafx.event.replay.RunEvent;
import com.sebuilder.interpreter.javafx.event.script.*;
import com.sebuilder.interpreter.javafx.event.view.RefreshScriptViewEvent;
import com.sebuilder.interpreter.javafx.event.view.RefreshStepEditViewEvent;
import com.sebuilder.interpreter.javafx.event.view.RefreshStepViewEvent;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;

public class SeInterpreterApplication extends Application {

    private String suiteName;

    private LinkedHashMap<String, com.sebuilder.interpreter.Script> scripts = Maps.newLinkedHashMap();

    private com.sebuilder.interpreter.Script currentDisplay;

    private Queue<Consumer<SeInterpreterRunner>> queue = new LinkedBlockingDeque<>();

    @Override
    public void start(Stage stage) throws Exception {
        System.setProperty("log4j.configurationFile", "log4j2-gui.xml");
        stage.setTitle("SeInterpreter");
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/seleniumbuilder.fxml")));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
        EventBus.registSubscriber(this);
        EventBus.publish(new ScriptResetEvent());
        new Thread(new SeInterpreterRunner(this.queue)).start();
    }

    @Override
    public void stop() throws Exception {
        this.queue.add((SeInterpreterRunner runner) -> runner.quit());
        super.stop();
    }

    @Subscribe
    public void reset(ScriptResetEvent event) {
        ReportErrorEvent.publishIfExecuteThrowsException(() -> {
            String templateName = "new script";
            Script templateScript = templateScript();
            LinkedHashMap<String, Script> scripts = Maps.newLinkedHashMap();
            scripts.put("new script", templateScript);
            this.resetSuite(templateName, scripts);
        });
    }

    @Subscribe
    public void scriptSave(FileSaveEvent event) {
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
    public void scriptReLoad(FileLoadEvent event) {
        File file = event.getFile();
        ReportErrorEvent.publishIfExecuteThrowsException(() -> {
            String fileName = file.getName();
            LinkedHashMap<String, Script> scripts = Maps.newLinkedHashMap();
            List<Script> loaded = new ScriptFactory().parse(file);
            for (Script script : loaded) {
                scripts.put(script.name, script);
            }
            this.resetSuite(fileName, scripts);
        });

    }

    @Subscribe
    public void addScript(AddNewScriptEvent event) throws IOException, JSONException {
        Script newScript = event.getScript();
        if (newScript == null) {
            newScript = this.templateScript();
        }
        this.scripts.put(newScript.name, newScript);
        EventBus.publish(new RefreshScriptViewEvent(this.suiteName, this.scripts));
    }

    @Subscribe
    public void changeCurrentScript(SelectScriptEvent event) {
        this.currentDisplay = this.scripts.get(event.getScriptName());
        EventBus.publish(new RefreshStepViewEvent(this.currentDisplay));
    }

    @Subscribe
    public void deleteStep(StepDeleteEvent event) {
        this.currentDisplay = this.currentDisplay.removeStep(event.getStepIndex());
        this.scripts.replace(this.currentDisplay.name, this.currentDisplay);
        EventBus.publish(new RefreshStepViewEvent(this.currentDisplay));
    }

    @Subscribe
    public void editStep(StepEditEvent event) throws JSONException, IOException {
        JSONObject json = new JSONObject();
        JSONArray steps = new JSONArray();
        steps.put(event.getStepSource());
        json.putOpt("steps", steps);
        Script script = new ScriptFactory().parse(json, null).get(0);
        Step newStep = script.steps.get(0);
        if (event.getEditAction().equals("change")) {
            this.currentDisplay = this.currentDisplay.replaceStep(event.getStepIndex(), newStep);
        } else {
            this.currentDisplay = this.currentDisplay.addStep(event.getStepIndex(), newStep);
        }
        this.scripts.replace(this.currentDisplay.name, this.currentDisplay);
        EventBus.publish(new RefreshStepViewEvent(this.currentDisplay));
    }

    @Subscribe
    public void createStep(SelectNewStepEvent event) throws IOException, JSONException {
        Script templateScript = new ScriptFactory().parse("{\"steps\":[{\"type\":\"" + event.getStepType() + "\"}]}");
        EventBus.publish(RefreshStepEditViewEvent.add(templateScript.steps.get(0)));
    }

    @Subscribe
    public void loadStep(StepLoadEvent event) {
        Step step = this.currentDisplay.steps.get(event.getStepIndex() - 1);
        EventBus.publish(RefreshStepEditViewEvent.change(step));
    }

    @Subscribe
    public void browserOpern(BrowserOpenEvent event) {
        this.queue.add((SeInterpreterRunner runner) -> runner.browserOpern());
    }

    @Subscribe
    public void browserExportScriptTemplate(LoadTemplateEvent event) {
        this.queue.add((SeInterpreterRunner runner) -> {
            com.sebuilder.interpreter.Script export = runner.browserExportScriptTemplate();
            EventBus.publish(new AddNewScriptEvent(export));
        });
    }

    @Subscribe
    public void highLightElement(HighLightTargetElementEvent event) throws IOException, JSONException {
        JSONObject json = new JSONObject();
        JSONArray steps = new JSONArray();
        JSONObject step = new JSONObject();
        JSONObject locator = new JSONObject();
        locator.put("type", event.getLocator());
        locator.put("value", event.getValue());
        step.put("type", "highLightElement");
        step.putOpt("locator", locator);
        steps.put(step);
        json.putOpt("steps", steps);
        Script script = new ScriptFactory().parse(json, null).get(0);
        this.queue.add((SeInterpreterRunner runner) -> runner.browserRunScript(script));
    }

    @Subscribe
    public void browserRunScript(RunEvent event) {
        this.queue.add((SeInterpreterRunner runner) -> runner.browserRunScript(this.currentDisplay));
    }

    @Subscribe
    public void browserClose(BrowserCloseEvent event) {
        this.queue.add((SeInterpreterRunner runner) -> runner.browserClose());
    }

    private Script templateScript() throws IOException, JSONException {
        Script templateScript = new ScriptFactory().parse("{\"steps\":[" + "{\"type\":\"get\",\"url\":\"https://www.google.com\"}" + "]}");
        templateScript.name = "new script";
        return templateScript;
    }

    private void resetSuite(String suiteName, LinkedHashMap<String, Script> scripts) {
        this.suiteName = suiteName;
        this.scripts = scripts;
        EventBus.publish(new RefreshScriptViewEvent(this.suiteName, this.scripts));
    }

    public static class SeInterpreterRunner implements Runnable {

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
                        // prevent thread dead
                    }
                }
            }
            this.log.info("stop running");
        }

        public void browserOpern() {
            Script get = this.repl.toScript("{\"steps\":[" + "{\"type\":\"get\",\"url\":\"https://www.google.com\"}" + "]}");
            this.repl.execute(get);
        }

        public Script browserExportScriptTemplate() {
            String fileName = "exportedBrowserTemplate" + this.exportCount++ + ".json";
            Script get = this.repl.toScript("{\"steps\":[" + "{\"type\":\"exportTemplate\",\"file\":\"" + fileName + "\"}" + "]}");
            this.repl.execute(get);
            File exported = new File(Context.getInstance().getTemplateOutputDirectory(), fileName);
            return this.repl.loadScript(exported.getAbsolutePath()).get(0);
        }

        public void browserRunScript(com.sebuilder.interpreter.Script currentDisplay) {
            if (this.repl == null) {
                this.setUp();
            }
            this.repl.execute(currentDisplay);
        }

        public void browserClose() {
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

}
