package com.sebuilder.interpreter.javafx.application;

import com.airhacks.afterburner.injection.Injector;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.sebuilder.interpreter.*;
import com.sebuilder.interpreter.javafx.view.SuccessDialog;
import com.sebuilder.interpreter.javafx.view.main.ErrorDialog;
import com.sebuilder.interpreter.javafx.view.main.MainView;
import com.sebuilder.interpreter.javafx.view.replay.ReplayView;
import com.sebuilder.interpreter.step.type.Get;
import com.sebuilder.interpreter.step.type.HighLightElement;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Log4J2LoggerFactory;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.WorkerStateEvent;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Predicate;

public class SeInterpreterApplication extends Application {

    private SeInterpreterRunner runner;

    private ErrorDialog errorDialog;

    private final ObjectProperty<Suite> suite = new SimpleObjectProperty<>();

    private final ObjectProperty<TestCase> displayTestCase = new SimpleObjectProperty<>();

    private final ObjectProperty<ViewType> scriptViewType = new SimpleObjectProperty<>();

    private final ObjectProperty<Pair<Integer, Result>> replayStatus = new SimpleObjectProperty<>();

    private MainView mainView;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        System.setProperty("log4j.configurationFile", "log4j2-gui.xml");
        InternalLoggerFactory.setDefaultFactory(Log4J2LoggerFactory.INSTANCE);
        Injector.setModelOrService(SeInterpreterApplication.class, this);
        final Parameters parameters = getParameters();
        this.runner = new SeInterpreterRunner(parameters.getRaw());
        this.errorDialog = new ErrorDialog(this.runner.getLog());
        final List<String> unnamed = parameters.getUnnamed();
        if (unnamed.size() > 0) {
            this.resetSuite(getScriptParser()
                    .load(new File(unnamed.get(0)), this.runner.getGlobalListener())
                    .toSuite());
        } else {
            this.reset();
        }
        this.mainView = new MainView();
        this.mainView.open(stage);
    }

    @Override
    public void stop() throws Exception {
        this.runner.close();
        super.stop();
    }

    public ObjectProperty<Suite> suiteProperty() {
        return this.suite;
    }

    public ObjectProperty<TestCase> displayTestCaseProperty() {
        return this.displayTestCase;
    }

    public Suite getSuite() {
        return this.suite.getValue();
    }

    public TestCase getDisplayTestCase() {
        return this.displayTestCase.getValue();
    }

    public String getCurrentDisplayAsJson() {
        return Context.getTestCaseConverter().toString(this.getDisplayTestCase());
    }

    public ObjectProperty<ViewType> scriptViewTypeProperty() {
        return this.scriptViewType;
    }

    public ObjectProperty<Pair<Integer, Result>> replayStatusProperty() {
        return this.replayStatus;
    }

    public InputData replayShareInput() {
        return Context.settings();
    }

    public DataSourceLoader getDisplayTestCaseDataSource() {
        return this.getDisplayTestCase().runtimeDataSet();
    }

    public DataSourceLoader[] getDisplayTestCaseDataSources() {
        return this.getDisplayScriptDataSources(it -> it.include(this.getDisplayTestCase()));
    }

    public DataSourceLoader[] getDisplayScriptDataSources(Predicate<TestCase> predicate) {
        return this.getSuite().dataSources(predicate);
    }

    public void executeAndLoggingCaseWhenThrowException(ThrowableAction action) {
        try {
            action.execute();
        } catch (Throwable th) {
            this.errorDialog.show(th.getMessage(), th);
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

    public void replaceScript(String text) {
        this.executeAndLoggingCaseWhenThrowException(() -> {
            TestCase replaced = Context.getScriptParser()
                    .load(text, this.getDisplayTestCase().getScriptFile().toFile(), this.runner.getGlobalListener())
                    .map(it -> it.setName(this.getDisplayTestCase().name()));
            replaceDisplayCase(replaced);
            SuccessDialog.show("commit succeed");
        });
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

    public void addScript(String chainHeadName, int i, TestCase dragged) {
        TestCase before = this.findChainHead(chainHeadName);
        TestCase after = before.map(it -> it.addChain(dragged, i));
        this.resetScript(this.getSuite().replace(before, after)
                , TestCaseSelector.builder()
                        .setHeadName(after.name())
                        .setTestCaseName(dragged.name())
                        .build()
                        .findTestCase(after));
    }

    public void removeScript() {
        this.resetSuite(this.getSuite().map(it -> it.remove(this.getDisplayTestCase())));
    }

    public void removeScriptFromChain(String chainHeadName, String targetName) {
        TestCase target = this.findTestCase(chainHeadName, targetName);
        this.resetSuite(this.getSuite().map(it -> it.remove(target)));
    }

    public TestCase findTestCase(String chainHeadName, String targetName) {
        return this.findChainHead(chainHeadName)
                .getChains()
                .get(targetName);
    }

    public TestCase findChainHead(String chainHeadName) {
        return TestCaseSelector.builder()
                .setHeadName(chainHeadName)
                .build()
                .findChainHead(this.getSuite());
    }

    public void resetSuite(Suite newSuite) {
        this.resetScript(newSuite, newSuite.head());
    }

    public void scriptReLoad(File file) {
        this.scriptReLoad(file, Context.getDefaultScript());
    }

    public void scriptReLoad(File file, String scriptType) {
        this.executeAndLoggingCaseWhenThrowException(() -> this.resetSuite(getScriptParser(scriptType).load(file, this.runner.getGlobalListener()).toSuite()));
    }

    public void importScript(File file) {
        this.executeAndLoggingCaseWhenThrowException(() -> addScript(getScriptParser().load(file, this.runner.getGlobalListener())));
    }

    public void saveSuite(File file) {
        this.suite.setValue(this.getSuite().map(builder -> builder.associateWith(file)));
        this.saveSuite();
    }

    public void saveSuite() {
        this.executeAndLoggingCaseWhenThrowException(() -> {
            File target = new File(this.getSuite().path());
            List<TestCase> notAssociateFile = Lists.newArrayList();
            this.getSuite().getChains().forEach(it -> {
                if (Strings.isNullOrEmpty(it.path())) {
                    notAssociateFile.add(it);
                }
            });
            final File scriptSaveTo = new File(target.getParentFile(), "script");
            if (notAssociateFile.size() > 0 && !scriptSaveTo.exists()) {
                Files.createDirectories(scriptSaveTo.toPath());
            }
            notAssociateFile.forEach(it -> {
                String oldName = it.name();
                String newName = oldName;
                if (!oldName.endsWith(".json")) {
                    newName = newName + ".json";
                }
                File saveTo = new File(scriptSaveTo, newName);
                TestCase save = this.changeAssociateFile(it.builder().associateWith(saveTo).build(), "");
                this.saveContents(saveTo, this.getTestCaseConverter().toString(save));
                Suite newSuite = this.getSuite().replace(it, save);
                if (it == this.getDisplayTestCase()) {
                    this.resetScript(newSuite, save);
                } else {
                    this.resetScript(newSuite, this.getDisplayTestCase());
                }
            });
            this.saveContents(target, this.getTestCaseConverter().toString(this.getSuite()));
        });
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
        TestCase save = this.changeAssociateFile(
                this.getDisplayTestCase().map(builder -> builder.associateWith(target))
                , this.getDisplayTestCase().path());
        this.saveContents(target, this.getTestCaseConverter().toString(save));
        this.replaceDisplayCase(save);
    }

    public void saveTestCase() {
        this.saveContents(new File(this.getDisplayTestCase().path()), this.getTestCaseConverter().toString(this.getDisplayTestCase()));
    }

    public void browserSetting(String selectedBrowser, String remoteUrl, String driverPath, String binaryPath) {
        this.runner.reloadSetting(selectedBrowser, driverPath, binaryPath);
        if (!Strings.isNullOrEmpty(remoteUrl)) {
            Context.getInstance().setRemoteUrl(remoteUrl);
        }
        this.browserOpen();
    }

    public void browserOpen() {
        this.executeTask(this.templateScript().map(it -> it.isPreventContextAspect(true))
                , Context::getTestListener);
    }

    public void browserClose() {
        this.runner.close();
    }

    public void highLightElement(String locatorType, String value) {
        Step highLightElement = new HighLightElement()
                .toStep()
                .locator(new Locator(locatorType, value))
                .build();
        this.runner.run(highLightElement.toTestCase()
                .map(it -> it.isPreventContextAspect(true)));
    }

    public TestCase exportTemplate(Locator locator, List<String> targetTags, boolean withDataSource) {
        return this.runner.exportTemplate(locator, targetTags, withDataSource);
    }

    public void runSuite() {
        this.executeTask(this.getSuite().head(), this.listener());
    }

    public void runScript(ReplayOption replayOption) {
        this.executeAndLoggingCaseWhenThrowException(() -> {
            InputData inputData = this.currentDisplayShareInput(replayOption);
            this.executeTask(this.getDisplayTestCase()
                            .map(builder -> builder.setShareInput(inputData).map(replayOption::apply))
                    , this.listener());
        });
    }

    public void runStep(ReplayOption replayOption, Predicate<Number> filter, Function<Integer, Integer> function) {
        this.executeAndLoggingCaseWhenThrowException(() -> {
            InputData inputData = this.currentDisplayShareInput(replayOption);
            this.executeTask(this.getDisplayTestCase()
                            .removeStep(filter)
                            .map(builder -> builder.setShareInput(inputData).map(replayOption::apply))
                    , log -> new GUITestRunListener(Context.getTestListener(log), this) {
                        @Override
                        public int getStepNo() {
                            return function.apply(super.getStepNo());
                        }
                    });
        });
    }

    public void stopReplay() {
        this.runner.stopRunning();
    }

    public void updateReplayStatus(int stepNo, Result result) {
        this.replayStatus.setValue(new Pair<>(stepNo, result));
    }

    public String getReportFileName() {
        return this.runner.getGlobalListener().getReportFileName();
    }

    protected ScriptParser getScriptParser() {
        return this.getScriptParser(Context.getDefaultScript());
    }

    protected ScriptParser getScriptParser(String scriptType) {
        return Context.getScriptParser(scriptType);
    }

    protected TestCaseConverter getTestCaseConverter() {
        return Context.getTestCaseConverter();
    }

    protected void resetScript(Suite aSuite, TestCase toSelect) {
        this.suite.setValue(aSuite);
        this.selectScript(toSelect.name());
    }

    protected TestCase templateScript() {
        TestCase result = new Get().toStep().put("url", "https://www.google.com").build().toTestCase();
        if (this.suite.get() == null) {
            return result;
        }
        long no = this.suite.get()
                .getChains()
                .flattenTestCases()
                .filter(it -> Strings.isNullOrEmpty(it.getScriptFile().path()))
                .count();
        if (no > 0) {
            return result.map(it -> it.setName(result.name() + "(" + no + ")"));
        }
        return result;
    }

    protected TestCase changeAssociateFile(TestCase exportTo, String oldPath) {
        if (Strings.isNullOrEmpty(oldPath)) {
            return this.copyDataSourceTemplate(exportTo);
        }
        return exportTo;
    }

    protected void saveContents(File target, String content) {
        this.executeAndLoggingCaseWhenThrowException(() -> {
            if (!target.exists()) {
                Files.createFile(target.toPath());
            }
            Files.writeString(target.toPath(), content, Charsets.UTF_8);
            SuccessDialog.show("save succeed:" + target.getAbsolutePath());
        });
    }

    protected TestCase copyDataSourceTemplate(TestCase it) {
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
                this.executeAndLoggingCaseWhenThrowException(() -> Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING));
                return it.map(builder -> builder.addDataSourceConfig("path", dest.getName()));
            }
        }
        return it;
    }

    protected InputData currentDisplayShareInput(ReplayOption replayOption) throws IOException {
        return replayOption.reduceShareInput(this.replayShareInput()
                , this.getDisplayScriptDataSources(it -> it.include(getDisplayTestCase()) && !it.equals(getDisplayTestCase())));
    }

    protected Function<Logger, TestRunListener> listener() {
        return log -> new GUITestRunListener(Context.getTestListener(log), this);
    }

    protected void executeTask(TestCase replayCase, Function<Logger, TestRunListener> listenerFactory) {
        SeInterpreterRunTask task = this.runner.createRunScriptTask(replayCase, listenerFactory);
        this.executeAndLoggingCaseWhenThrowException(() -> {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            this.showProgressbar(task);
            task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, wse -> executor.shutdown());
            executor.submit(task);
        });
    }

    protected void showProgressbar(SeInterpreterRunTask task) {
        new ReplayView().open(this.mainView.getMainWindow(), task);
    }

    public interface ThrowableAction {
        void execute() throws Exception;
    }
}
