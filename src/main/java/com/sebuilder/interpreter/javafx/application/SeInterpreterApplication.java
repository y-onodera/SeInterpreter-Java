package com.sebuilder.interpreter.javafx.application;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.google.common.io.Files;
import com.sebuilder.interpreter.*;
import com.sebuilder.interpreter.factory.ScriptFactory;
import com.sebuilder.interpreter.javafx.EventBus;
import com.sebuilder.interpreter.javafx.controller.RunningProgressController;
import com.sebuilder.interpreter.javafx.event.ReportErrorEvent;
import com.sebuilder.interpreter.javafx.event.ViewType;
import com.sebuilder.interpreter.javafx.event.browser.BrowserCloseEvent;
import com.sebuilder.interpreter.javafx.event.browser.BrowserOpenEvent;
import com.sebuilder.interpreter.javafx.event.browser.BrowserSettingEvent;
import com.sebuilder.interpreter.javafx.event.file.*;
import com.sebuilder.interpreter.javafx.event.replay.*;
import com.sebuilder.interpreter.javafx.event.script.*;
import com.sebuilder.interpreter.javafx.event.view.*;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SeInterpreterApplication extends Application {

    private Suite suite;

    private Script currentDisplay;

    private ViewType currentMainView;

    private SeInterpreterRunner runner;

    private Scene scene;

    private Stage runProgressDialog;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        System.setProperty("log4j.configurationFile", "log4j2-gui.xml");
        stage.setTitle("SeInterpreter");

        final URL resource = this.getClass().getResource("/fxml/seleniumbuilder.fxml");
        Parent root = FXMLLoader.load(Objects.requireNonNull(resource));
        this.scene = new Scene(root);
        stage.setScene(this.scene);
        stage.setResizable(true);
        stage.show();
        EventBus.registSubscriber(this);
        EventBus.publish(new ScriptResetEvent());
        this.currentMainView = ViewType.TABLE;
        final Parameters parameters = getParameters();
        this.runner = new SeInterpreterRunner(parameters.getRaw());
        final List<String> unnamed = parameters.getUnnamed();
        if (unnamed.size() > 0) {
            Suite newSuite = getScriptFactory().parse(new File(unnamed.get(0)));
            this.resetSuite(newSuite);
        }
    }

    @Override
    public void stop() throws Exception {
        this.runner.close();
        super.stop();
    }

    @Subscribe
    public void reset(ScriptResetEvent event) {
        ReportErrorEvent.publishIfExecuteThrowsException(() -> {
            this.suite = new SuiteBuilder(this.templateScript()).createSuite();
            this.resetSuite(this.suite);
        });
    }

    @Subscribe
    public void scriptReLoad(FileLoadEvent event) {
        File file = event.getFile();
        ReportErrorEvent.publishIfExecuteThrowsException(() -> {
            Suite newSuite = getScriptFactory().parse(file);
            this.resetSuite(newSuite);
        });

    }

    @Subscribe
    public void addScript(ScriptAddEvent event) throws IOException, JSONException {
        Script newScript = event.getScript();
        if (newScript == null) {
            newScript = this.templateScript();
        }
        this.suite = this.suite.add(newScript);
        this.resetSuite(this.suite);
    }

    @Subscribe
    public void insertScript(ScriptInsertEvent event) throws IOException, JSONException {
        Script newScript = this.templateScript();
        this.suite = this.suite.insert(this.currentDisplay, newScript);
        this.resetSuite(this.suite);
    }

    @Subscribe
    public void deleteScript(ScriptDeleteEvent event) {
        this.suite = this.suite.delete(this.currentDisplay);
        this.resetSuite(this.suite);
    }

    @Subscribe
    public void changeCurrentScript(ScriptSelectEvent event) {
        if (!event.getScriptName().equals(this.suite.getName())) {
            this.currentDisplay = this.suite.get(event.getScriptName());
            this.refreshMainView();
        }
    }

    @Subscribe
    public void replaceScript(ScriptReplaceEvent event) {
        ReportErrorEvent.publishIfExecuteThrowsException(() -> {
            Script newScript = getScriptFactory().parse(event.getScript());
            this.currentDisplay = newScript.builder().associateWith(new File(this.currentDisplay.path))
                    .setName(this.currentDisplay.name())
                    .createScript();
            this.suite = this.suite.replace(this.currentDisplay);
            this.refreshMainView();
        });
    }

    @Subscribe
    public void handleViewTypeChange(ScriptViewChangeEvent event) {
        this.currentMainView = event.getViewType();
        this.refreshMainView();
    }

    @Subscribe
    public void deleteStep(StepDeleteEvent event) {
        this.currentDisplay = this.currentDisplay.removeStep(event.getStepIndex());
        this.suite = this.suite.replace(this.currentDisplay);
        this.refreshMainView();
    }

    @Subscribe
    public void moveStep(StepMoveEvent event) {
        Step step = this.currentDisplay.steps.get(event.getFrom());
        int indexTo = event.getTo();
        if (event.getTo() > event.getFrom()) {
            this.currentDisplay = this.currentDisplay.addStep(indexTo, step)
                    .removeStep(event.getFrom());
        } else {
            this.currentDisplay = this.currentDisplay.insertStep(indexTo, step)
                    .removeStep(event.getFrom() + 1);
        }
        this.suite = this.suite.replace(this.currentDisplay);
        this.refreshMainView();
    }

    @Subscribe
    public void editStep(StepEditEvent event) throws JSONException, IOException {
        JSONObject json = new JSONObject();
        JSONArray steps = new JSONArray();
        steps.put(event.getStepSource());
        json.putOpt("steps", steps);
        Script script = getScriptFactory().parse(json);
        Step newStep = script.steps.get(0);
        if (event.getEditAction().equals("change")) {
            this.currentDisplay = this.currentDisplay.replaceStep(event.getStepIndex(), newStep);
        } else if (event.getEditAction().equals("insert")) {
            this.currentDisplay = this.currentDisplay.insertStep(event.getStepIndex(), newStep);
        } else {
            this.currentDisplay = this.currentDisplay.addStep(event.getStepIndex(), newStep);
        }
        this.suite = this.suite.replace(this.currentDisplay);
        this.refreshMainView();
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
    public void saveScript(FileSaveEvent event) {
        if (this.currentDisplay.path == null) {
            EventBus.publish(new OpenScriptSaveChooserEvent());
        } else {
            File target = new File(this.currentDisplay.path);
            this.saveContents(target, this.currentDisplay);
        }
    }

    @Subscribe
    public void saveScript(FileSaveAsEvent event) {
        File target = event.getFile();
        String oldName = this.currentDisplay.name;
        this.currentDisplay = new ScriptBuilder(this.currentDisplay)
                .associateWith(target)
                .createScript();
        this.suite = this.suite.replace(oldName, this.currentDisplay);
        this.saveContents(target, this.currentDisplay);
        this.resetSuite(this.suite);
    }

    @Subscribe
    public void saveSuite(FileSaveSuiteAsEvent event) {
        if (this.suite.getPath() == null) {
            EventBus.publish(new OpenSuiteSaveChooserEvent());
        } else {
            this.saveSuite();
        }
    }


    @Subscribe
    public void saveSuite(FileSaveSuiteEvent event) {
        File target = event.getFile();
        this.suite = this.suite.builder()
                .associateWith(target)
                .createSuite();
        this.saveSuite();
    }

    @Subscribe
    public void browserSetting(BrowserSettingEvent event) throws IOException, JSONException {
        String browserName = event.getSelectedBrowser();
        String driverPath = event.getDriverPath();
        this.runner.reloadSetting(browserName, driverPath);
        this.open(new BrowserOpenEvent());
    }

    @Subscribe
    public void open(BrowserOpenEvent event) throws IOException, JSONException {
        Task task = this.runner.createRunScriptTask(this.templateScript(), logger -> {
            return new SimpleSeInterpreterTestListener(logger);
        });
        this.executeTask(task);
    }

    @Subscribe
    public void exportTemplate(TemplateLoadEvent event) {
        Script export = this.runner.exportTemplate(event.getParentLocator(), event.getTargetTag());
        this.currentDisplay = this.currentDisplay.addStep(export);
        this.suite = this.suite.replace(this.currentDisplay);
        this.refreshMainView();
    }

    @Subscribe
    public void highLightElement(ElementHighLightEvent event) {
        this.runner.highLightElement(event.getLocator(), event.getValue());
    }

    @Subscribe
    public void runStep(RunStepEvent event) {
        Task task = this.runner.createRunScriptTask(this.currentDisplay.removeStep(event.getFilter())
                , log -> new SeInterpreterTestGUIListener(log) {
                    @Override
                    public int getStepNo() {
                        return event.getStepNoFunction().apply(super.getStepNo());
                    }
                });
        this.executeTask(task);
    }

    @Subscribe
    public void runScript(RunEvent event) {
        Task task = this.runner.createRunScriptTask(this.currentDisplay);
        this.executeTask(task);
    }

    @Subscribe
    public void runSuite(RunSuiteEvent event) {
        Task task = this.runner.createRunSuiteTask(this.suite);
        this.executeTask(task);
    }

    @Subscribe
    public void runStop(StopEvent event) {
        this.runner.stopRunning();
    }

    @Subscribe
    public void close(BrowserCloseEvent event) {
        this.runner.close();
    }

    protected ScriptFactory getScriptFactory() {
        return new ScriptFactory();
    }

    private Script templateScript() throws IOException, JSONException {
        return getScriptFactory().open("https://www.google.com");
    }

    private void resetSuite(Suite aSuite) {
        this.suite = aSuite;
        EventBus.publish(new RefreshScriptViewEvent(this.suite));
    }

    private void saveSuite() {
        File target = new File(this.suite.getPath());
        List<Script> notAssociateFile = Lists.newArrayList();
        this.suite.forEach(it -> {
            if (it.path == null) {
                notAssociateFile.add(it);
            }
        });
        final File scriptSaveTo = new File(target.getParentFile(), "script");
        if (notAssociateFile.size() > 0 && !scriptSaveTo.exists()) {
            scriptSaveTo.mkdirs();
        }
        notAssociateFile.forEach(it -> {
            String oldName = it.name;
            String newName = oldName;
            if (!oldName.endsWith(".json")) {
                newName = newName + ".json";
            }
            File saveTo = new File(scriptSaveTo, newName);
            this.suite = this.suite.replace(oldName, it.builder().associateWith(saveTo).createScript());
                }
        );
        this.suite.forEach(it -> {
            this.saveContents(new File(it.path), it);
        });
        this.saveContents(target, this.suite);
        this.resetSuite(this.suite);
    }

    private void saveContents(File target, Object content) {
        ReportErrorEvent.publishIfExecuteThrowsException(() -> {
            if (!target.exists()) {
                target.createNewFile();
            }
            Files.asCharSink(target, Charsets.UTF_8).write(content.toString());
        });
    }

    private void refreshMainView() {
        if (this.currentMainView == ViewType.TABLE) {
            EventBus.publish(new RefreshStepTableViewEvent(this.currentDisplay));
        } else if (this.currentMainView == ViewType.TEXT) {
            EventBus.publish(new RefreshStepTextViewEvent(this.currentDisplay));
        }
    }

    private void executeTask(Task task) {
        ReportErrorEvent.publishIfExecuteThrowsException(() -> {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(task);
            this.initScriptRunProgressDialog(task);
            task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, wse -> {
                executor.shutdown();
            });
        });
    }

    private void initScriptRunProgressDialog(Task task) throws IOException {
        if (this.runProgressDialog == null || !this.runProgressDialog.isShowing()) {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(this.getClass().getResource("/fxml/runprogress.fxml")));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            this.runProgressDialog = new Stage();
            this.runProgressDialog.setScene(scene);
            this.runProgressDialog.initOwner(this.scene.getWindow());
            this.runProgressDialog.initModality(Modality.WINDOW_MODAL);
            this.runProgressDialog.setTitle("run progress");
            RunningProgressController controller = loader.getController();
            controller.bind(task);
            this.runProgressDialog.setResizable(false);
            this.runProgressDialog.show();
        }
    }
}
