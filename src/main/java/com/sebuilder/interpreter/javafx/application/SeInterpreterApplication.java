package com.sebuilder.interpreter.javafx.application;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.google.common.io.Files;
import com.sebuilder.interpreter.*;
import com.sebuilder.interpreter.factory.ScriptConverter;
import com.sebuilder.interpreter.factory.TestCaseFactory;
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
import com.sebuilder.interpreter.steptype.Get;
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

    private TestCaseFactory testCaseFactory = new TestCaseFactory();

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
            Suite newSuite = getTestcaseFactory().parse(new File(unnamed.get(0)));
            this.resetSuite(newSuite, newSuite.iterator().next());
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
            Suite newSuite = new SuiteBuilder(this.templateScript()).createSuite();
            this.resetSuite(newSuite, newSuite.iterator().next());
        });
    }

    @Subscribe
    public void scriptReLoad(FileLoadEvent event) {
        File file = event.getFile();
        ReportErrorEvent.publishIfExecuteThrowsException(() -> {
            Suite newSuite = getTestcaseFactory().parse(file);
            this.resetSuite(newSuite, newSuite.iterator().next());
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
        Suite newSuite = this.suite.insert(this.currentDisplay, newTestCase);
        int index = newSuite.getIndex(this.currentDisplay);
        this.resetSuite(newSuite, newSuite.get(index - 1));
    }

    @Subscribe
    public void exportTemplate(TemplateLoadEvent event) {
        TestCase exported = this.runner.exportTemplate(event.getParentLocator(), event.getTargetTag(), event.isWithDataSource());
        this.addScript(exported);
    }

    @Subscribe
    public void scriptImport(ScriptImportEvent event) {
        File file = event.getFile();
        ReportErrorEvent.publishIfExecuteThrowsException(() -> {
            TestCase testCase = getTestcaseFactory().parse(file).get(0);
            addScript(testCase);
        });

    }

    @Subscribe
    public void deleteScript(ScriptDeleteEvent event) {
        Suite newSuite = this.suite.delete(this.currentDisplay);
        this.resetSuite(newSuite, newSuite.iterator().next());
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
            TestCase newTestCase = getTestcaseFactory().parse(event.getScript());
            this.currentDisplay = newTestCase.builder()
                    .associateWith(new File(this.currentDisplay.path()))
                    .setName(this.currentDisplay.name())
                    .build();
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
        Step step = this.currentDisplay.steps().get(event.getFrom());
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
    public void editStep(StepEditEvent event) {
        Step newStep = event.getStepSource();
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
    public void createStep(StepAddEvent event) {
        TestCase templateTestCase = getTestcaseFactory()
                .getStepTypeFactory()
                .getStepTypeOfName(event.getStepType())
                .toStep()
                .build()
                .toTestCase();
        EventBus.publish(RefreshStepEditViewEvent.add(templateTestCase.steps().get(0)));
    }

    @Subscribe
    public void loadStep(StepLoadEvent event) {
        Step step = this.currentDisplay.steps().get(event.getStepIndex());
        EventBus.publish(RefreshStepEditViewEvent.change(step));
    }

    @Subscribe
    public void saveScript(FileSaveEvent event) {
        if (Strings.isNullOrEmpty(this.currentDisplay.path())) {
            EventBus.publish(new OpenScriptSaveChooserEvent());
        } else {
            File target = new File(this.currentDisplay.path());
            this.saveContents(target, new ScriptConverter().toString(this.currentDisplay));
        }
    }

    @Subscribe
    public void saveScript(FileSaveAsEvent event) {
        File target = event.getFile();
        String oldName = this.currentDisplay.name();
        String oldPath = this.currentDisplay.path();
        TestCase save = this.saveContents(target
                , new TestCaseBuilder(this.currentDisplay)
                        .associateWith(target)
                        .build()
                , oldPath);
        this.resetSuite(this.suite.replace(oldName, save), save);
    }

    @Subscribe
    public void saveSuite(FileSaveSuiteAsEvent event) {
        if (Strings.isNullOrEmpty(this.suite.getPath())) {
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
    public void browserSetting(BrowserSettingEvent event) {
        String browserName = event.getSelectedBrowser();
        String driverPath = event.getDriverPath();
        this.runner.reloadSetting(browserName, driverPath);
        this.open(new BrowserOpenEvent());
    }

    @Subscribe
    public void open(BrowserOpenEvent event) {
        Task task = this.runner.createRunScriptTask(this.templateScript(), logger -> {
            return new SeInterpreterTestListenerImpl(logger);
        });
        this.executeTask(task);
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

    protected TestCaseFactory getTestcaseFactory() {
        return this.testCaseFactory;
    }

    private TestCase templateScript() {
        return new Get().toStep().put("url", "https://www.google.com").build().toTestCase();
    }

    private void addScript(TestCase newTestCase) {
        Suite newSuite = this.suite.add(this.currentDisplay, newTestCase);
        int index = newSuite.getIndex(this.currentDisplay);
        this.resetSuite(newSuite, newSuite.get(index + 1));
    }

    private void resetSuite(Suite aSuite, TestCase toSelect) {
        this.suite = aSuite;
        EventBus.publish(new RefreshScriptViewEvent(this.suite, toSelect.name()));
    }

    private void saveSuite() {
        File target = new File(this.suite.getPath());
        List<TestCase> notAssociateFile = Lists.newArrayList();
        this.suite.forEach(it -> {
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
            this.suite = this.suite.replace(oldName, save);
            if (it == this.currentDisplay) {
                this.currentDisplay = this.suite.get(newName);
            }
        });
        this.saveContents(target, new ScriptConverter().toString(this.suite));
        this.resetSuite(this.suite, this.currentDisplay);
    }

    private TestCase saveContents(File target, TestCase exportTo, String oldPath) {
        TestCase save = exportTo;
        if (Strings.isNullOrEmpty(oldPath)) {
            save = this.copyDataSourceTemplate(exportTo);
        }
        this.saveContents(target, new ScriptConverter().toString(save));
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
        if (it.dataSourceConfig().containsKey("path")) {
            File src = new File(this.runner.getTemplateOutputDirectory(), it.dataSourceConfig().get("path"));
            final String newDataSourceName = it.name().replace(".json", ".csv");
            File dest = new File(this.runner.getDataSourceDirectory(), newDataSourceName);
            if (src.exists() && !dest.exists()) {
                ReportErrorEvent.publishIfExecuteThrowsException(() -> Files.copy(src, dest));
                return it.changeDataSourceConfig("path", newDataSourceName);
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
