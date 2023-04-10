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
import com.sebuilder.interpreter.step.type.SaveScreenshot;
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
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Predicate;

public class SeInterpreterApplication extends Application {

    private SeInterpreterRunner runner;

    private final Map<String, Step> takeScreenshotTemplate = new LinkedHashMap<>();

    private int takeScreenshotTemplateNoName = 0;

    private final Debugger debugger = new Debugger();

    private ErrorDialog errorDialog;

    private final ObjectProperty<Suite> suite = new SimpleObjectProperty<>();

    private final ObjectProperty<TestCase> displayTestCase = new SimpleObjectProperty<>();

    private final ObjectProperty<ViewType> scriptViewType = new SimpleObjectProperty<>();

    private final ObjectProperty<Pair<Integer, Result>> replayStatus = new SimpleObjectProperty<>();

    private MainView mainView;

    public static void main(final String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage stage) throws Exception {
        System.setProperty("log4j.configurationFile", "log4j2-gui.xml");
        InternalLoggerFactory.setDefaultFactory(Log4J2LoggerFactory.INSTANCE);
        Injector.setModelOrService(SeInterpreterApplication.class, this);
        final Parameters parameters = this.getParameters();
        this.runner = new SeInterpreterRunner(parameters.getRaw());
        this.errorDialog = new ErrorDialog(this.runner.getLog());
        final List<String> unnamed = parameters.getUnnamed();
        if (unnamed.size() > 0) {
            this.resetSuite(this.getScriptParser()
                    .load(new File(unnamed.get(0)))
                    .toSuite());
        } else {
            this.reset();
        }
        this.mainView = new MainView();
        this.mainView.open(stage);
        this.reloadScreenshotTemplate(Optional.ofNullable(parameters.getNamed().get("takeScreenshotTemplate"))
                .map(File::new)
                .orElse(null));
    }

    public void reloadScreenshotTemplate(final File takeScreenshotTemplate1) {
        this.takeScreenshotTemplate.clear();
        this.takeScreenshotTemplate.put("", this.createStep("saveScreenshot").withAllParam());
        if (takeScreenshotTemplate1 != null) {
            this.takeScreenshotTemplateNoName = 0;
            for (final Step step : this.getScriptParser()
                    .load(takeScreenshotTemplate1)
                    .steps()) {
                this.addScreenshotTemplates(step);
            }
        }
    }

    public void saveScreenshotTemplate(final File file) {
        this.saveContents(file, this.getTestCaseConverter().toString(new TestCaseBuilder()
                .addSteps(new ArrayList<>(this.takeScreenshotTemplate.values()
                        .stream()
                        .toList()
                        .subList(1, this.takeScreenshotTemplate.size())))
                .build()));
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

    public DataSourceLoader[] getDisplayScriptDataSources(final Predicate<TestCase> predicate) {
        return this.getSuite().dataSources(predicate);
    }

    public void executeAndLoggingCaseWhenThrowException(final ThrowableAction action) {
        try {
            action.execute();
        } catch (final Throwable th) {
            this.errorDialog.show(th.getMessage(), th);
        }
    }

    public void changeScriptViewType(final ViewType viewType) {
        this.scriptViewType.setValue(viewType);
    }

    public void reset() {
        this.resetSuite(this.templateScript().toSuite());
    }

    public void selectScript(final String newValue) {
        this.displayTestCase.setValue(this.getSuite().get(newValue));
    }

    public void replaceScript(final String text) {
        this.executeAndLoggingCaseWhenThrowException(() -> {
            final TestCase replaced = Context.getScriptParser()
                    .load(text, this.getDisplayTestCase().scriptFile().toFile())
                    .map(it -> it.setName(this.getDisplayTestCase().name()));
            this.replaceDisplayCase(replaced);
            SuccessDialog.show("commit succeed");
        });
    }

    public void insertScript() {
        final TestCase newTestCase = this.templateScript();
        this.resetScript(this.getSuite().map(it -> it.insertTest(this.getDisplayTestCase(), newTestCase)), newTestCase);
    }

    public void addScript() {
        this.addScript(this.templateScript());
    }

    public void addScript(final TestCase newTestCase) {
        this.resetScript(this.getSuite().map(it -> it.addChain(this.getDisplayTestCase(), newTestCase)), newTestCase);
    }

    public void addScript(final String chainHeadName, final int i, final TestCase dragged) {
        final TestCase before = this.findChainHead(chainHeadName);
        final TestCase after = before.map(it -> it.addChain(dragged, i));
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

    public void removeScriptFromChain(final String chainHeadName, final String targetName) {
        final TestCase target = this.findTestCase(chainHeadName, targetName);
        this.resetSuite(this.getSuite().map(it -> it.remove(target)));
    }

    public TestCase findTestCase(final String chainHeadName, final String targetName) {
        return this.findChainHead(chainHeadName)
                .chains()
                .get(targetName);
    }

    public TestCase findChainHead(final String chainHeadName) {
        return TestCaseSelector.builder()
                .setHeadName(chainHeadName)
                .build()
                .findChainHead(this.getSuite());
    }

    public void resetSuite(final Suite newSuite) {
        this.resetScript(newSuite, newSuite.head());
    }

    public void scriptReLoad(final File file) {
        this.scriptReLoad(file, Context.getDefaultScript());
    }

    public void scriptReLoad(final File file, final String scriptType) {
        this.executeAndLoggingCaseWhenThrowException(() -> this.resetSuite(this.getScriptParser(scriptType).load(file).toSuite()));
    }

    public void importScript(final File file) {
        this.executeAndLoggingCaseWhenThrowException(() -> this.addScript(this.getScriptParser().load(file)));
    }

    public void saveSuite(final File file) {
        this.suite.setValue(this.getSuite().map(builder -> builder.associateWith(file)));
        this.saveSuite();
    }

    public void saveSuite() {
        this.executeAndLoggingCaseWhenThrowException(() -> {
            final File target = new File(this.getSuite().path());
            final List<TestCase> notAssociateFile = Lists.newArrayList();
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
                final String oldName = it.name();
                String newName = oldName;
                if (!oldName.endsWith(".json")) {
                    newName = newName + ".json";
                }
                final File saveTo = new File(scriptSaveTo, newName);
                final TestCase save = this.changeAssociateFile(it.builder().associateWith(saveTo).build(), "");
                this.saveContents(saveTo, this.getTestCaseConverter().toString(save));
                final Suite newSuite = this.getSuite().replace(it, save);
                if (it == this.getDisplayTestCase()) {
                    this.resetScript(newSuite, save);
                } else {
                    this.resetScript(newSuite, this.getDisplayTestCase());
                }
            });
            this.saveContents(target, this.getTestCaseConverter().toString(this.getSuite()));
        });
    }

    public StepType getStepTypeOfName(final String stepType) {
        return Context
                .getStepTypeFactory()
                .getStepTypeOfName(stepType);
    }

    public Step createStep(final String stepType) {
        return this.getStepTypeOfName(stepType)
                .toStep()
                .build()
                .toTestCase()
                .steps().get(0);
    }

    public void replaceDisplayCase(final TestCase newCase) {
        this.resetScript(this.getSuite().replace(this.getDisplayTestCase(), newCase), newCase);
    }

    public void saveTestCase(final File target) {
        final TestCase save = this.changeAssociateFile(
                this.getDisplayTestCase().map(builder -> builder.associateWith(target))
                , this.getDisplayTestCase().path());
        this.saveContents(target, this.getTestCaseConverter().toString(save));
        this.replaceDisplayCase(save);
    }

    public void saveTestCase() {
        this.saveContents(new File(this.getDisplayTestCase().path()), this.getTestCaseConverter().toString(this.getDisplayTestCase()));
    }

    public void browserSetting(final String selectedBrowser, final String remoteUrl, final String driverPath, final String binaryPath) {
        this.runner.reloadSetting(selectedBrowser, driverPath, binaryPath);
        if (!Strings.isNullOrEmpty(remoteUrl)) {
            Context.getInstance().setRemoteUrl(remoteUrl);
        } else {
            Context.getInstance().setRemoteUrl(null);
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

    public void highLightElement(final String locatorType, final String value) {
        this.runner.highlightElement(locatorType, value);
    }

    public TestCase exportTemplate(final Locator locator, final List<String> targetTags, final boolean withDataSource) {
        return this.runner.exportTemplate(locator, targetTags, withDataSource);
    }

    public Map<String, Step> takeScreenshotTemplates() {
        return this.takeScreenshotTemplate;
    }

    public void addScreenshotTemplates(final Step step) {
        if (step.type() instanceof SaveScreenshot) {
            if (step.containsParam("displayName")) {
                this.takeScreenshotTemplate.put(step.getParam("displayName"), step.withAllParam());
            } else {
                this.takeScreenshotTemplate.put(String.format("has no displayName#%s", this.takeScreenshotTemplateNoName++), step.withAllParam());
            }
        }
    }

    public void removeScreenshotTemplate(final String displayName) {
        this.takeScreenshotTemplate.remove(displayName);
    }

    public File takeScreenShot(final StepBuilder stepBuilder) {
        return this.runner.screenShot(stepBuilder);
    }

    public void runSuite() {
        this.executeTask(this.getSuite().head(), this.listener());
    }

    public void runScript(final ReplayOption replayOption) {
        this.executeAndLoggingCaseWhenThrowException(() -> {
            final InputData inputData = this.currentDisplayShareInput(replayOption);
            this.executeTask(this.getDisplayTestCase()
                            .map(builder -> builder.setShareInput(inputData).map(replayOption::apply))
                    , this.listener());
        });
    }

    public void runStep(final ReplayOption replayOption, final Pointcut filter, final boolean isChainTakeOver) {
        this.executeAndLoggingCaseWhenThrowException(() -> {
            final InputData inputData = this.currentDisplayShareInput(replayOption);
            this.executeTask(this.getDisplayTestCase()
                            .map(builder -> builder.setIncludeTestRun(filter)
                                    .setShareInput(inputData)
                                    .map(replayOption::apply)
                                    .mapWhen(it -> !isChainTakeOver, it -> it.setChains(new TestCaseChains())))
                    , log -> new GUITestRunListener(Context.getTestListener(log), this));
        });
    }

    public void stopReplay() {
        this.runner.stopRunning();
    }

    public void updateReplayStatus(final int stepNo, final Result result) {
        this.replayStatus.setValue(new Pair<>(stepNo, result));
    }

    public void addBreakPoint(final int stepIndex, final Pointcut pointcut) {
        final BreakPoint breakPoint = BreakPoint.findFrom(this.getDisplayTestCase().aspect())
                .orElseGet(() -> new BreakPoint(new HashMap<>(), this.debugger))
                .addCondition(stepIndex, pointcut);
        this.replaceDisplayCase(this.getDisplayTestCase().map(it ->
                it.filterAspect(BreakPoint.typeMatch().negate())
                        .insertAspect(breakPoint.toAspect())));
    }

    public void removeBreakPoint(final int stepIndex) {
        BreakPoint.findFrom(this.getDisplayTestCase().aspect()).ifPresent(current -> {
            final BreakPoint breakPoint = current.removeCondition(stepIndex);
            if (breakPoint.condition().size() == 0) {
                this.replaceDisplayCase(this.getDisplayTestCase().map(it ->
                        it.filterAspect(BreakPoint.typeMatch().negate())));
            } else {
                this.replaceDisplayCase(this.getDisplayTestCase().map(it ->
                        it.filterAspect(BreakPoint.typeMatch().negate())
                                .insertAspect(breakPoint.toAspect())));
            }
        });
    }

    public String getReportFileName() {
        return this.runner.getTestListener().getReportFileName();
    }

    protected ScriptParser getScriptParser() {
        return this.getScriptParser(Context.getDefaultScript());
    }

    protected ScriptParser getScriptParser(final String scriptType) {
        return Context.getScriptParser(scriptType);
    }

    protected TestCaseConverter getTestCaseConverter() {
        return Context.getTestCaseConverter();
    }

    protected void resetScript(final Suite aSuite, final TestCase toSelect) {
        this.suite.setValue(aSuite);
        this.selectScript(toSelect.name());
    }

    protected TestCase templateScript() {
        final TestCase result = new Get().toStep().put("url", "https://www.google.com").build().toTestCase();
        if (this.suite.get() == null) {
            return result;
        }
        final long no = this.suite.get()
                .getChains()
                .flattenTestCases()
                .filter(it -> Strings.isNullOrEmpty(it.scriptFile().path()))
                .count();
        if (no > 0) {
            return result.map(it -> it.setName(result.name() + "(" + no + ")"));
        }
        return result;
    }

    protected TestCase changeAssociateFile(final TestCase exportTo, final String oldPath) {
        if (Strings.isNullOrEmpty(oldPath)) {
            return this.copyDataSourceTemplate(exportTo);
        }
        return exportTo;
    }

    protected void saveContents(final File target, final String content) {
        this.executeAndLoggingCaseWhenThrowException(() -> {
            if (!target.exists()) {
                Files.createFile(target.toPath());
            }
            Files.writeString(target.toPath(), content, Charsets.UTF_8);
            SuccessDialog.show("save succeed:" + target.getAbsolutePath());
        });
    }

    protected TestCase copyDataSourceTemplate(final TestCase it) {
        if (it.dataSourceLoader().dataSourceConfig().containsKey("path")) {
            final File src = new File(this.runner.getTemplateOutputDirectory(), it.dataSourceLoader().dataSourceConfig().get("path"));
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

    protected InputData currentDisplayShareInput(final ReplayOption replayOption) throws IOException {
        return replayOption.reduceShareInput(this.replayShareInput()
                , this.getDisplayScriptDataSources(it -> it.include(this.getDisplayTestCase()) && !it.equals(this.getDisplayTestCase())));
    }

    protected Function<Logger, TestRunListener> listener() {
        return log -> new GUITestRunListener(Context.getTestListener(log), this);
    }

    protected void executeTask(final TestCase replayCase, final Function<Logger, TestRunListener> listenerFactory) {
        final SeInterpreterRunTask task = this.runner.createRunScriptTask(replayCase, this.debugger.reset(), listenerFactory);
        this.executeAndLoggingCaseWhenThrowException(() -> {
            final ExecutorService executor = Executors.newSingleThreadExecutor();
            this.showProgressbar(task);
            task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, wse -> executor.shutdown());
            executor.submit(task);
        });
    }

    protected void showProgressbar(final SeInterpreterRunTask task) {
        new ReplayView().open(this.mainView.getMainWindow(), task);
    }

    public interface ThrowableAction {
        void execute() throws Exception;
    }
}
