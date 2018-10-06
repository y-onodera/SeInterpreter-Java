package com.sebuilder.interpreter.javafx.application;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.google.common.io.Files;
import com.sebuilder.interpreter.Script;
import com.sebuilder.interpreter.SeInterpreterTestListener;
import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.Suite;
import com.sebuilder.interpreter.factory.ScriptFactory;
import com.sebuilder.interpreter.javafx.EventBus;
import com.sebuilder.interpreter.javafx.event.ReportErrorEvent;
import com.sebuilder.interpreter.javafx.event.browser.BrowserCloseEvent;
import com.sebuilder.interpreter.javafx.event.browser.BrowserOpenEvent;
import com.sebuilder.interpreter.javafx.event.file.FileLoadEvent;
import com.sebuilder.interpreter.javafx.event.file.FileSaveAsEvent;
import com.sebuilder.interpreter.javafx.event.file.FileSaveEvent;
import com.sebuilder.interpreter.javafx.event.replay.*;
import com.sebuilder.interpreter.javafx.event.script.*;
import com.sebuilder.interpreter.javafx.event.view.OpenScriptSaveChooserEvent;
import com.sebuilder.interpreter.javafx.event.view.RefreshScriptViewEvent;
import com.sebuilder.interpreter.javafx.event.view.RefreshStepEditViewEvent;
import com.sebuilder.interpreter.javafx.event.view.RefreshStepViewEvent;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;

public class SeInterpreterApplication extends Application {

    private Suite suite;

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
        this.queue.add((runner) -> runner.quit());
        super.stop();
    }

    @Subscribe
    public void reset(ScriptResetEvent event) {
        ReportErrorEvent.publishIfExecuteThrowsException(() -> {
            this.suite = new Suite(Lists.newArrayList(templateScript()), true);
            this.resetSuite(this.suite);
        });
    }

    @Subscribe
    public void scriptReLoad(FileLoadEvent event) {
        File file = event.getFile();
        ReportErrorEvent.publishIfExecuteThrowsException(() -> {
            Suite newSuite = new Suite(file, getScriptFactory());
            this.resetSuite(newSuite);
        });

    }

    @Subscribe
    public void addScript(ScriptAddEvent event) throws IOException, JSONException {
        Script newScript = event.getScript();
        if (newScript == null) {
            newScript = this.templateScript();
        }
        this.suite.add(newScript);
        this.resetSuite(this.suite);
    }

    @Subscribe
    public void changeCurrentScript(ScriptSelectEvent event) {
        if (!event.getScriptName().equals(this.suite.getName())) {
            this.currentDisplay = this.suite.get(event.getScriptName());
            EventBus.publish(new RefreshStepViewEvent(this.currentDisplay));
        }
    }

    @Subscribe
    public void deleteStep(StepDeleteEvent event) {
        this.currentDisplay = this.currentDisplay.removeStep(event.getStepIndex());
        this.suite.replace(this.currentDisplay);
        EventBus.publish(new RefreshStepViewEvent(this.currentDisplay));
    }

    @Subscribe
    public void editStep(StepEditEvent event) throws JSONException, IOException {
        JSONObject json = new JSONObject();
        JSONArray steps = new JSONArray();
        steps.put(event.getStepSource());
        json.putOpt("steps", steps);
        Script script = getScriptFactory().parse(json, null).iterator().next();
        Step newStep = script.steps.get(0);
        if (event.getEditAction().equals("change")) {
            this.currentDisplay = this.currentDisplay.replaceStep(event.getStepIndex(), newStep);
        } else if (event.getEditAction().equals("insert")) {
            this.currentDisplay = this.currentDisplay.insertStep(event.getStepIndex(), newStep);
        } else {
            this.currentDisplay = this.currentDisplay.addStep(event.getStepIndex(), newStep);
        }
        this.suite.replace(this.currentDisplay);
        EventBus.publish(new RefreshStepViewEvent(this.currentDisplay));
    }

    @Subscribe
    public void createStep(StepAddEvent event) throws IOException, JSONException {
        Script templateScript = getScriptFactory().template(event.getStepType());
        EventBus.publish(RefreshStepEditViewEvent.add(templateScript.steps.get(0)));
    }

    @Subscribe
    public void loadStep(StepLoadEvent event) {
        Step step = this.currentDisplay.steps.get(event.getStepIndex());
        EventBus.publish(RefreshStepEditViewEvent.change(step));
    }

    @Subscribe
    public void scriptSave(FileSaveEvent event) {
        if (this.currentDisplay.path == null) {
            EventBus.publish(new OpenScriptSaveChooserEvent());
        } else {
            File target = new File(this.currentDisplay.path);
            this.saveCurrentDisplay(target);
        }
    }

    @Subscribe
    public void scriptSave(FileSaveAsEvent event) {
        File target = event.getFile();
        String oldName = this.currentDisplay.name;
        this.currentDisplay = this.currentDisplay.associateWith(target);
        this.suite.replace(oldName, this.currentDisplay);
        this.saveCurrentDisplay(target);
        this.resetSuite(this.suite);
    }

    @Subscribe
    public void browserOpern(BrowserOpenEvent event) throws IOException, JSONException {
        Script dummy = this.templateScript();
        this.queue.add((runner) -> runner.browserRunScript(dummy, log -> new SeInterpreterTestListener(log)));
    }

    @Subscribe
    public void browserExportScriptTemplate(TemplateLoadEvent event) {
        this.queue.add((runner) -> {
            Script export = runner.browserExportScriptTemplate();
            Platform.runLater(() -> EventBus.publish(new ScriptAddEvent(export)));
        });
    }

    @Subscribe
    public void highLightElement(ElementHighLightEvent event) throws IOException, JSONException {
        Script script = getScriptFactory().highLightElement(event.getLocator(), event.getValue());
        this.queue.add((runner) -> runner.browserRunScript(script, log -> new SeInterpreterTestListener(log)));
    }

    @Subscribe
    public void browserRunStep(RunStepEvent event) {
        this.queue.add((runner) -> {
            runner.browserRunScript(this.currentDisplay.removeStep(event.getFilter())
                    , log -> new SeInterpreterTestGUIListener(log) {
                        @Override
                        public int getStepNo() {
                            return event.getStepNoFunction().apply(super.getStepNo());
                        }
                    });
        });
    }

    @Subscribe
    public void browserRunScript(RunEvent event) {
        this.queue.add((runner) -> runner.browserRunScript(this.currentDisplay));
    }

    @Subscribe
    public void browserRunSuite(RunSuiteEvent event) {
        this.queue.add((runner) -> runner.browserRunSuite(this.suite));
    }

    @Subscribe
    public void browserClose(BrowserCloseEvent event) {
        this.queue.add((runner) -> runner.browserClose());
    }

    protected ScriptFactory getScriptFactory() {
        return new ScriptFactory();
    }

    private Script templateScript() throws IOException, JSONException {
        Script templateScript = getScriptFactory().open("https://www.google.com");
        templateScript.name = "new script";
        return templateScript;
    }

    private void resetSuite(Suite aSuite) {
        this.suite = aSuite;
        EventBus.publish(new RefreshScriptViewEvent(this.suite));
    }

    private void saveCurrentDisplay(File target) {
        ReportErrorEvent.publishIfExecuteThrowsException(() -> {
            if (!target.exists()) {
                target.createNewFile();
            }
            String content = this.currentDisplay.toJSON().toString(4);
            Files.asCharSink(target, Charsets.UTF_8).write(content);
        });
    }
}
