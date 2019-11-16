package com.sebuilder.interpreter.javafx.application;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.google.common.io.Files;
import com.sebuilder.interpreter.*;
import com.sebuilder.interpreter.application.TestRunListenerImpl;
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
import com.sebuilder.interpreter.step.type.Get;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SeInterpreterApplication extends Application {

    private Suite suite;

    private TestCase currentDisplay;

    private ViewType currentMainView;

    private SeInterpreterRunner runner;

    private Scene scene;

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
            this.resetSuite(getScriptParser().load(new File(unnamed.get(0))).toSuite());
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
            this.resetSuite(this.templateScript().toSuite());
        });
    }

    @Subscribe
    public void scriptReLoad(FileLoadEvent event) {
        ReportErrorEvent.publishIfExecuteThrowsException(() -> {
            this.resetSuite(getScriptParser().load(event.getFile()).toSuite());
        });

    }

    @Subscribe
    public void addScript(ScriptAddEvent event) {
        TestCase newTestCase = event.getTestCase();
        if (newTestCase == null) {
            newTestCase = this.templateScript();
        }
        addScript(newTestCase);
    }

    @Subscribe
    public void insertScript(ScriptInsertEvent event) {
        TestCase newTestCase = this.templateScript();
        this.resetScript(this.suite.map(it -> it.insertTest(this.currentDisplay, newTestCase)), newTestCase);
    }

    @Subscribe
    public void exportTemplate(TemplateLoadEvent event) {
        this.addScript(this.runner.exportTemplate(event.getParentLocator(), event.getTargetTag(), event.isWithDataSource()));
    }

    @Subscribe
    public void scriptImport(ScriptImportEvent event) {
        ReportErrorEvent.publishIfExecuteThrowsException(() -> {
            addScript(getScriptParser().load(event.getFile()));
        });

    }

    @Subscribe
    public void deleteScript(ScriptDeleteEvent event) {
        this.resetSuite(this.suite.map(it -> it.remove(this.currentDisplay)));
    }

    @Subscribe
    public void changeCurrentScript(ScriptSelectEvent event) {
        this.currentDisplay = this.suite.get(event.getScriptName());
        this.refreshMainView();
    }

    @Subscribe
    public void replaceScript(ScriptReplaceEvent event) {
        ReportErrorEvent.publishIfExecuteThrowsException(() -> {
            TestCase newTestCase = getScriptParser().load(event.getJsonString(), new File(this.currentDisplay.path()))
                    .map(builder -> builder.setName(this.currentDisplay.name()));
            this.resetScript(this.suite.replace(this.currentDisplay, newTestCase), newTestCase);
        });
    }

    @Subscribe
    public void handleViewTypeChange(ScriptViewChangeEvent event) {
        this.currentMainView = event.getViewType();
        this.refreshMainView();
    }

    @Subscribe
    public void deleteStep(StepDeleteEvent event) {
        TestCase newCase = this.currentDisplay.removeStep(event.getStepIndex());
        this.resetScript(this.suite.replace(this.currentDisplay, newCase), newCase);
    }

    @Subscribe
    public void moveStep(StepMoveEvent event) {
        Step step = this.currentDisplay.steps().get(event.getFrom());
        int indexTo = event.getTo();
        TestCase newCase;
        if (event.getTo() > event.getFrom()) {
            newCase = this.currentDisplay.addStep(indexTo, step)
                    .removeStep(event.getFrom());
        } else {
            newCase = this.currentDisplay.insertStep(indexTo, step)
                    .removeStep(event.getFrom() + 1);
        }
        this.resetScript(this.suite.replace(this.currentDisplay, newCase), newCase);
    }

    @Subscribe
    public void editStep(StepEditEvent event) {
        Step newStep = event.getStepSource();
        TestCase newCase;
        if (event.getEditAction().equals("change")) {
            newCase = this.currentDisplay.setSteps(event.getStepIndex(), newStep);
        } else if (event.getEditAction().equals("insert")) {
            newCase = this.currentDisplay.insertStep(event.getStepIndex(), newStep);
        } else {
            newCase = this.currentDisplay.addStep(event.getStepIndex(), newStep);
        }
        this.resetScript(this.suite.replace(this.currentDisplay, newCase), newCase);
    }

    @Subscribe
    public void createStep(StepAddEvent event) {
        TestCase templateTestCase = Context
                .getStepTypeFactory()
                .getStepTypeOfName(event.getStepType())
                .toStep()
                .build()
                .toTestCase();
        EventBus.publish(RefreshStepEditViewEvent.add(templateTestCase.steps().get(0)));
    }

    @Subscribe
    public void loadStep(StepLoadEvent event) {
        EventBus.publish(RefreshStepEditViewEvent.change(this.currentDisplay.steps().get(event.getStepIndex())));
    }

    @Subscribe
    public void saveScript(FileSaveEvent event) {
        if (Strings.isNullOrEmpty(this.currentDisplay.path())) {
            EventBus.publish(new OpenScriptSaveChooserEvent());
        } else {
            this.saveContents(new File(this.currentDisplay.path()), this.getScriptParser().toString(this.currentDisplay));
        }
    }

    @Subscribe
    public void saveScript(FileSaveAsEvent event) {
        File target = event.getFile();
        TestCase save = this.saveContents(target
                , this.currentDisplay.builder()
                        .associateWith(target)
                        .build()
                , this.currentDisplay.path());
        this.resetScript(this.suite.replace(this.currentDisplay, save), save);
    }

    @Subscribe
    public void saveSuite(FileSaveSuiteAsEvent event) {
        if (Strings.isNullOrEmpty(this.suite.path())) {
            EventBus.publish(new OpenSuiteSaveChooserEvent());
        } else {
            this.saveSuite();
        }
    }


    @Subscribe
    public void saveSuite(FileSaveSuiteEvent event) {
        this.suite = this.suite.builder()
                .associateWith(event.getFile())
                .build()
                .toSuite();
        this.saveSuite();
    }

    @Subscribe
    public void browserSetting(BrowserSettingEvent event) {
        this.runner.reloadSetting(event.getSelectedBrowser(), event.getDriverPath());
        this.open(new BrowserOpenEvent());
    }

    @Subscribe
    public void open(BrowserOpenEvent event) {
        this.executeTask(this.runner.createRunScriptTask(this.templateScript(), logger -> {
            return new TestRunListenerImpl(logger);
        }));
    }

    @Subscribe
    public void highLightElement(ElementHighLightEvent event) {
        this.runner.highLightElement(event.getLocator(), event.getValue());
    }

    @Subscribe
    public void runStep(RunStepEvent event) {
        this.executeTask(this.runner.createRunScriptTask(this.currentDisplay.removeStep(event.getFilter())
                , log -> new GUITestRunListener(log) {
                    @Override
                    public int getStepNo() {
                        return event.getStepNoFunction().apply(super.getStepNo());
                    }
                }));
    }

    @Subscribe
    public void runScript(RunEvent event) {
        this.executeTask(this.runner.createRunScriptTask(this.currentDisplay));
    }

    @Subscribe
    public void runSuite(RunSuiteEvent event) {
        this.executeTask(this.runner.createRunScriptTask(this.suite.head()));
    }

    @Subscribe
    public void runStop(StopEvent event) {
        this.runner.stopRunning();
    }

    @Subscribe
    public void close(BrowserCloseEvent event) {
        this.runner.close();
    }

    protected ScriptParser getScriptParser() {
        return Context.getScriptParser();
    }

    private TestCase templateScript() {
        return new Get().toStep().put("url", "https://www.google.com").build().toTestCase();
    }

    private void addScript(TestCase newTestCase) {
        this.resetScript(this.suite.map(it -> it.addChain(this.currentDisplay, newTestCase)), newTestCase);
    }

    private void resetSuite(Suite newSuite) {
        resetScript(newSuite, newSuite.head());
    }

    private void resetScript(Suite aSuite, TestCase toSelect) {
        this.suite = aSuite;
        if (this.currentDisplay != null && Objects.equals(this.currentDisplay.path(), toSelect.path())) {
            this.currentDisplay = this.suite.get(this.currentDisplay.name());
        } else {
            this.currentDisplay = this.suite.get(toSelect.name());
        }
        EventBus.publish(new RefreshScriptViewEvent(this.suite, toSelect.name()));
    }

    private void saveSuite() {
        File target = new File(this.suite.path());
        List<TestCase> notAssociateFile = Lists.newArrayList();
        this.suite.getChains().forEach(it -> {
            if (Strings.isNullOrEmpty(it.path())) {
                notAssociateFile.add(it);
            }
        });
        final File scriptSaveTo = new File(target.getParentFile(), "script");
        if (notAssociateFile.size() > 0 && !scriptSaveTo.exists()) {
            scriptSaveTo.mkdirs();
        }
        notAssociateFile.forEach(it -> {
            String oldName = it.name();
            String newName = oldName;
            if (!oldName.endsWith(".json")) {
                newName = newName + ".json";
            }
            File saveTo = new File(scriptSaveTo, newName);
            TestCase save = this.saveContents(saveTo, it.builder().associateWith(saveTo).build(), "");
            Suite newSuite = this.suite.replace(it, save);
            if (it == this.currentDisplay) {
                this.resetScript(newSuite, save);
            } else {
                this.resetScript(newSuite, this.currentDisplay);
            }
        });
        this.saveContents(target, this.getScriptParser().toString(this.suite));
    }

    private TestCase saveContents(File target, TestCase exportTo, String oldPath) {
        TestCase save = exportTo;
        if (Strings.isNullOrEmpty(oldPath)) {
            save = this.copyDataSourceTemplate(exportTo);
        }
        this.saveContents(target, this.getScriptParser().toString(save));
        return save;
    }

    private void saveContents(File target, String content) {
        ReportErrorEvent.publishIfExecuteThrowsException(() -> {
            if (!target.exists()) {
                target.createNewFile();
            }
            Files.asCharSink(target, Charsets.UTF_8).write(content.toString());
        });
    }

    private TestCase copyDataSourceTemplate(TestCase it) {
        if (it.getTestDataSet().getDataSourceConfig().containsKey("path")) {
            File src = new File(this.runner.getTemplateOutputDirectory(), it.getTestDataSet().getDataSourceConfig().get("path"));
            final String newDataSourceName = it.name().replace(".json", ".csv");
            File dest = new File(this.runner.getDataSourceDirectory(), newDataSourceName);
            if (src.exists() && !dest.exists()) {
                ReportErrorEvent.publishIfExecuteThrowsException(() -> Files.copy(src, dest));
                return it.map(builder -> builder.addDataSourceConfig("path", newDataSourceName));
            }
        }
        return it;
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
            RunningProgressController.init(this.scene.getWindow(), task);
            task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, wse -> {
                executor.shutdown();
            });
            executor.submit(task);
        });
    }
}
