package com.sebuilder.interpreter.javafx.application;

import com.airhacks.afterburner.injection.Injector;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.sebuilder.interpreter.*;
import com.sebuilder.interpreter.application.TestRunListenerImpl;
import com.sebuilder.interpreter.datasource.Manual;
import com.sebuilder.interpreter.javafx.view.replay.ReplayPresenter;
import com.sebuilder.interpreter.javafx.view.replay.ReplayView;
import com.sebuilder.interpreter.step.type.Get;
import com.sebuilder.interpreter.step.type.HighLightElement;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SeInterpreterApplication extends Application {

    private SeInterpreterRunner runner;

    private ObjectProperty<Suite> suite = new SimpleObjectProperty<>();

    private ObjectProperty<TestCase> displayTestCase = new SimpleObjectProperty<>();

    private ObjectProperty<ViewType> scriptViewType = new SimpleObjectProperty<>();

    private ObjectProperty<Pair<Integer, Result>> replayStatus = new SimpleObjectProperty<>();

    private Scene scene;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Injector.setModelOrService(SeInterpreterApplication.class, this);
        System.setProperty("log4j.configurationFile", "log4j2-gui.xml");
        final Parameters parameters = getParameters();
        this.runner = new SeInterpreterRunner(parameters.getRaw());
        final List<String> unnamed = parameters.getUnnamed();
        if (unnamed.size() > 0) {
            this.resetSuite(getScriptParser().load(new File(unnamed.get(0))).toSuite());
        } else {
            this.reset();
        }
        final MainView mainView = new MainView();
        this.scene = new Scene(mainView.getView());
        stage.setTitle("SeInterpreter");
        stage.setScene(this.scene);
        stage.setResizable(true);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        this.runner.close();
        super.stop();
    }

    public ObjectProperty<Suite> suiteProperty() {
        return suite;
    }

    public ObjectProperty<TestCase> displayTestCaseProperty() {
        return displayTestCase;
    }

    public Suite getSuite() {
        return this.suite.getValue();
    }

    public TestCase getDisplayTestCase() {
        return this.displayTestCase.getValue();
    }

    public String getCurrentDisplayAsJson() {
        return Context.getScriptParser().toString(this.getDisplayTestCase());
    }

    public ObjectProperty<ViewType> scriptViewTypeProperty() {
        return this.scriptViewType;
    }

    public ObjectProperty<Pair<Integer, Result>> replayStatusProperty() {
        return replayStatus;
    }

    public InputData replayShareInput() {
        return Context.settings();
    }

    public DataSourceLoader getDisplayTestCaseDataSource() {
        return this.getDisplayTestCase().runtimeDataSet();
    }

    public DataSourceLoader[] getDisplayTestCaseDataSources() {
        return this.getSuite().dataSources(this.getDisplayTestCase());
    }

    public void executeAndLoggingCaseWhenThrowException(ThrowableAction action) {
        try {
            action.execute();
        } catch (Throwable th) {
            this.runner.getLog().error(Throwables.getStackTraceAsString(th));
        }
    }

    public void changeScriptViewType(ViewType viewType) {
        this.scriptViewType.setValue(viewType);
    }

    public void reset() {
        this.resetSuite(this.templateScript().toSuite());
    }

    public void selectScript(String newValue) {
        this.displayTestCase.setValue(this.getSuite().get(newValue));
    }

    public void replaceScriptJson(String text) throws IOException {
        TestCase replaced = Context.getScriptParser().load(text, this.getDisplayTestCase().getScriptFile().toFile())
                .map(it -> it.setName(this.getDisplayTestCase().name()));
        replaceDisplayCase(replaced);
    }

    public void insertScript() {
        TestCase newTestCase = this.templateScript();
        this.resetScript(this.getSuite().map(it -> it.insertTest(this.getDisplayTestCase(), newTestCase)), newTestCase);
    }

    public void addScript() {
        this.addScript(this.templateScript());
    }

    public void addScript(TestCase newTestCase) {
        this.resetScript(this.getSuite().map(it -> it.addChain(this.getDisplayTestCase(), newTestCase)), newTestCase);
    }

    public void removeScript() {
        this.resetSuite(this.getSuite().map(it -> it.remove(this.getDisplayTestCase())));
    }

    public void resetSuite(Suite newSuite) {
        this.resetScript(newSuite, newSuite.head());
    }

    public void scriptReLoad(File file) {
        this.executeAndLoggingCaseWhenThrowException(() -> {
            this.resetSuite(getScriptParser().load(file).toSuite());
        });
    }

    public void importScript(File file) {
        this.executeAndLoggingCaseWhenThrowException(() -> {
            addScript(getScriptParser().load(file));
        });
    }

    public void saveSuite(File file) {
        this.suite.setValue(this.getSuite().map(builder -> builder.associateWith(file)));
        this.saveSuite();
    }

    public void saveSuite() {
        File target = new File(this.getSuite().path());
        List<TestCase> notAssociateFile = Lists.newArrayList();
        this.getSuite().getChains().forEach(it -> {
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
            Suite newSuite = this.getSuite().replace(it, save);
            if (it == this.getDisplayTestCase()) {
                this.resetScript(newSuite, save);
            } else {
                this.resetScript(newSuite, this.getDisplayTestCase());
            }
        });
        this.saveContents(target, this.getScriptParser().toString(this.getSuite()));
    }

    public StepType getStepTypeOfName(String stepType) {
        return Context
                .getStepTypeFactory()
                .getStepTypeOfName(stepType);
    }

    public Step createStep(String stepType) {
        return this.getStepTypeOfName(stepType)
                .toStep()
                .build()
                .toTestCase()
                .steps().get(0);
    }

    public void replaceDisplayCase(TestCase newCase) {
        this.resetScript(this.getSuite().replace(this.getDisplayTestCase(), newCase), newCase);
    }

    public void saveTestCase(File target) {
        TestCase save = this.saveContents(target
                , this.getDisplayTestCase().map(builder -> builder.associateWith(target))
                , this.getDisplayTestCase().path());
        this.replaceDisplayCase(save);
    }

    public void saveTestCase() {
        this.saveContents(new File(this.getDisplayTestCase().path()), this.getScriptParser().toString(this.getDisplayTestCase()));
    }

    public void browserSetting(String selectedBrowser, String driverPath) {
        this.runner.reloadSetting(selectedBrowser, driverPath);
        this.browserOpen();
    }

    public void browserOpen() {
        this.executeTask(this.runner.createRunScriptTask(this.templateScript(), logger -> new TestRunListenerImpl(logger)));
    }

    public void browserClose() {
        this.runner.close();
    }

    public void highLightElement(String locatorType, String value) {
        Step highLightElement = new HighLightElement()
                .toStep()
                .locator(new Locator(locatorType, value))
                .build();
        this.runner.run(highLightElement.toTestCase());
    }

    public TestCase exportTemplate(Locator locator, List<String> targetTags, boolean withDataSource) {
        return this.runner.exportTemplate(locator, targetTags, withDataSource);
    }

    public void runSuite() {
        this.executeTask(this.runner.createRunScriptTask(this.getSuite().head(), log -> new GUITestRunListener(log, this)));
    }

    public void runScript(Map<String, Integer> shareInputs) {
        InputData shareInput = getShareInput(shareInputs);
        final TestCase target = this.getDisplayTestCase().map(builder -> builder.setShareInput(shareInput));
        if (shareInputs.get(target.runtimeDataSet().name()) != null) {
            this.executeTask(this.runner.createRunScriptTask(
                    target.map(builder -> builder.setOverrideTestDataSet(new Manual()
                            , target.loadData()
                                    .get(shareInputs.get(target.runtimeDataSet().name()) - 1)
                                    .input()
                                    .stream()
                                    .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()))))
                    , log -> new GUITestRunListener(log, this)));
        } else {
            this.executeTask(this.runner.createRunScriptTask(target, log -> new GUITestRunListener(log, this)));
        }
    }


    public void runStep(Predicate<Number> filter, Function<Integer, Integer> function) {
        this.executeTask(this.runner.createRunScriptTask(this.getDisplayTestCase().removeStep(filter)
                , log -> new GUITestRunListener(log, this) {
                    @Override
                    public int getStepNo() {
                        return function.apply(super.getStepNo());
                    }
                }));
    }

    public void stopReplay() {
        this.runner.stopRunning();
    }

    public void updateReplayStatus(int stepNo, Result result) {
        this.replayStatus.setValue(new Pair<>(stepNo, result));
    }

    protected ScriptParser getScriptParser() {
        return Context.getScriptParser();
    }

    private InputData getShareInput(Map<String, Integer> shareInputs) {
        InputData shareInput = this.replayShareInput();
        for (DataSourceLoader loader : this.getDisplayTestCaseDataSources()) {
            DataSourceLoader withShareInput = loader.shareInput(shareInput);
            if (withShareInput.isLoadable()) {
                shareInput = shareInput.add(withShareInput
                        .loadData()
                        .get(shareInputs.getOrDefault(withShareInput.name(), 1) - 1));
            }
        }
        return shareInput;
    }

    private void resetScript(Suite aSuite, TestCase toSelect) {
        this.suite.setValue(aSuite);
        this.selectScript(toSelect.name());
    }

    private TestCase templateScript() {
        return new Get().toStep().put("url", "https://www.google.com").build().toTestCase();
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
        this.executeAndLoggingCaseWhenThrowException(() -> {
            if (!target.exists()) {
                target.createNewFile();
            }
            Files.asCharSink(target, Charsets.UTF_8).write(content.toString());
        });
    }

    private TestCase copyDataSourceTemplate(TestCase it) {
        if (it.getDataSourceLoader().getDataSourceConfig().containsKey("path")) {
            File src = new File(this.runner.getTemplateOutputDirectory(), it.getDataSourceLoader().getDataSourceConfig().get("path"));
            if (src.exists()) {
                final String newDataSourceName = it.name().replace(".json", "");
                File newDataSource = new File(this.runner.getDataSourceDirectory(), newDataSourceName + ".csv");
                int suffix = 1;
                while (newDataSource.exists()) {
                    newDataSource = new File(this.runner.getDataSourceDirectory(), newDataSourceName + suffix + ".csv");
                    suffix++;
                }
                final File dest = newDataSource;
                this.executeAndLoggingCaseWhenThrowException(() -> Files.copy(src, dest));
                return it.map(builder -> builder.addDataSourceConfig("path", dest.getName()));
            }
        }
        return it;
    }

    private void executeTask(Task task) {
        this.executeAndLoggingCaseWhenThrowException(() -> {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            this.showProgressbar(this.scene.getWindow(), task);
            task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, wse -> {
                executor.shutdown();
            });
            executor.submit(task);
        });
    }

    private void showProgressbar(Window window, Task task) {
        ReplayView replayView = new ReplayView();
        Scene scene = new Scene(replayView.getView());
        Stage runProgressDialog = new Stage();
        runProgressDialog.setScene(scene);
        runProgressDialog.initOwner(window);
        runProgressDialog.initModality(Modality.WINDOW_MODAL);
        runProgressDialog.setTitle("run progress");
        ReplayPresenter.class.cast(replayView.getPresenter()).bind(task);
        runProgressDialog.setResizable(false);
        runProgressDialog.show();
    }

    public interface ThrowableAction {
        void execute() throws Exception;
    }
}
