package com.sebuilder.interpreter.javafx.model;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.sebuilder.interpreter.*;
import com.sebuilder.interpreter.step.type.Get;
import com.sebuilder.interpreter.step.type.SaveScreenshot;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.WorkerStateEvent;
import javafx.util.Pair;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public record SeInterpreter(
        ObjectProperty<Suite> suite
        , ObjectProperty<TestCase> displayTestCase
        , ObjectProperty<ViewType> scriptViewType
        , BiConsumer<File, String> saveContents
        , SeInterpreterRunner runner
        , Consumer<SeInterpreterRunTask> taskHandler
        , ObjectProperty<Pair<Integer, Result>> replayStatus
        , Debugger debugger
        , Map<String, Step> takeScreenshotTemplate
        , Consumer<ThrowableAction> errorHandler
) {
    public SeInterpreter(final List<String> parameters, final BiConsumer<File, String> saveContents, final Consumer<SeInterpreterRunTask> taskHandler, final Consumer<ThrowableAction> errorHandler) {
        this(new SimpleObjectProperty<>()
                , new SimpleObjectProperty<>()
                , new SimpleObjectProperty<>()
                , saveContents
                , new SeInterpreterRunner(parameters)
                , taskHandler
                , new SimpleObjectProperty<>()
                , new Debugger()
                , new LinkedHashMap<>()
                , errorHandler
        );
    }

    public Suite getSuite() {
        return this.suite().getValue();
    }

    public TestCase getDisplayTestCase() {
        return this.displayTestCase().getValue();
    }

    public String getCurrentDisplayAsJson() {
        return Context.toString(this.getDisplayTestCase());
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

    public String getReportFileName() {
        return this.runner().getTestListener().getReportFileName();
    }

    public void changeScriptViewType(final ViewType viewType) {
        this.scriptViewType().setValue(viewType);
    }

    public void reset() {
        this.resetSuite(this.templateScript().toSuite());
    }

    public void resetSuite(final File file) {
        this.resetSuite(Context.load(file).toSuite());
    }

    public void resetSuite(final Suite newSuite) {
        this.resetScript(newSuite, newSuite.head());
    }

    public void scriptReLoad(final File file) {
        this.scriptReLoad(file, Context.getDefaultScript());
    }

    public void scriptReLoad(final File file, final String scriptType) {
        this.resetSuite(Context.loadWithScriptType(scriptType, file).toSuite());
    }

    public void selectScript(final String newValue) {
        this.displayTestCase().setValue(this.getSuite().get(newValue));
    }

    public void replaceScript(final String text) {
        final TestCase replaced = Context.load(text, this.getDisplayTestCase().scriptFile().toFile())
                .map(it -> it.setName(this.getDisplayTestCase().name())
                        .mapWhen(builder -> this.getDisplayTestCase().scriptFile().type() == ScriptFile.Type.TEST
                                , builder -> builder.setOverrideSetting(this.getDisplayTestCase())
                                        .setChains(this.getDisplayTestCase().chains())
                        )
                );
        this.replaceDisplayCase(replaced);
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

    public void importScript(final File file) {
        this.addScript(Context.load(file));
    }

    public void removeScript() {
        this.resetSuite(this.getSuite().map(it -> it.remove(this.getDisplayTestCase())));
    }

    public void removeScriptFromChain(final String chainHeadName, final String targetName) {
        final TestCase target = this.findTestCase(chainHeadName, targetName);
        this.resetSuite(this.getSuite().map(it -> it.remove(target)));
    }

    public void saveSuite(final File file) {
        this.suite().setValue(this.getSuite().map(builder -> builder.associateWith(file)));
        this.saveSuite();
    }

    public void saveSuite() {
        final File target = new File(this.getSuite().path());
        final List<TestCase> notAssociateFile = Lists.newArrayList();
        this.getSuite().getChains().forEach(it -> {
            if (Strings.isNullOrEmpty(it.path())) {
                notAssociateFile.add(it);
            }
        });
        final File scriptSaveTo = new File(target.getParentFile(), "script");
        if (notAssociateFile.size() > 0 && !scriptSaveTo.exists()) {
            this.errorHandler().accept(() -> Files.createDirectories(scriptSaveTo.toPath()));
        }
        notAssociateFile.forEach(it -> this.errorHandler().accept(() -> {
            final String oldName = it.name();
            String newName = oldName;
            if (!oldName.endsWith(".json")) {
                newName = newName + ".json";
            }
            final File saveTo = new File(scriptSaveTo, newName);
            final TestCase save = this.changeAssociateFile(it.builder().associateWith(saveTo).build(), "");
            this.saveContents().accept(saveTo, Context.toString(save));
            final Suite newSuite = this.getSuite().replace(it, save);
            if (it == this.getDisplayTestCase()) {
                this.resetScript(newSuite, save);
            } else {
                this.resetScript(newSuite, this.getDisplayTestCase());
            }
        }));
        this.saveContents().accept(target, Context.toString(this.getSuite()));
    }

    public void highLightElement(final String locatorType, final String value) {
        this.runner().highlightElement(locatorType, value);
    }

    public void replaceDisplayCase(final TestCase newCase) {
        this.resetScript(this.getSuite().replace(this.getDisplayTestCase(), newCase), newCase);
    }

    public void saveTestCase(final File target) {
        this.errorHandler().accept(() -> {
            final TestCase save = this.changeAssociateFile(
                    this.getDisplayTestCase().map(builder -> builder.associateWith(target))
                    , this.getDisplayTestCase().path());
            this.saveContents().accept(target, Context.toString(save));
            this.replaceDisplayCase(save);
        });
    }

    public void saveTestCase() {
        this.saveContents().accept(new File(this.getDisplayTestCase().path()), Context.toString(this.getDisplayTestCase()));
    }

    public void browserSetting(final String selectedBrowser, final String remoteUrl, final String driverPath, final String binaryPath) {
        this.runner().reloadSetting(selectedBrowser, driverPath, binaryPath);
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
        this.runner().close();
    }

    public TestCase exportTemplate(final Locator locator, final List<String> targetTags, final boolean withDataSource) {
        return this.runner().exportTemplate(locator, targetTags, withDataSource);
    }

    public void runSuite() {
        this.executeTask(this.getSuite().head(), this.listener());
    }

    public void runScript(final ReplayOption replayOption) {
        this.errorHandler().accept(() -> {
            final InputData inputData = this.currentDisplayShareInput(replayOption);
            this.executeTask(this.getDisplayTestCase()
                            .map(builder -> builder.setShareInput(inputData).map(replayOption::apply))
                    , this.listener());
        });
    }

    public void runStep(final ReplayOption replayOption, final Pointcut filter, final boolean isChainTakeOver) throws IOException {
        final InputData inputData = this.currentDisplayShareInput(replayOption);
        this.executeTask(this.getDisplayTestCase()
                        .map(builder -> builder.setIncludeTestRun(filter)
                                .setShareInput(inputData)
                                .map(replayOption::apply)
                                .mapWhen(it -> !isChainTakeOver, it -> it.setChains(new TestCaseChains())))
                , log -> new GUITestRunListener(Context.getTestListener(log), this));
    }

    public void stopReplay() {
        this.runner().stopRunning();
    }

    public void updateReplayStatus(final int stepNo, final Result result) {
        this.replayStatus().setValue(new Pair<>(stepNo, result));
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

    public File takeScreenShot(final StepBuilder stepBuilder) {
        return this.runner().screenShot(stepBuilder);
    }

    public void saveScreenshotTemplate(final File file) {
        this.saveContents().accept(file, Context.toString(new TestCaseBuilder()
                .addSteps(new ArrayList<>(this.takeScreenshotTemplate().values()
                        .stream()
                        .toList()
                        .subList(1, this.takeScreenshotTemplate().size())))
                .build()));
    }

    public void addScreenshotTemplates(final Step step) {
        if (step.type() instanceof SaveScreenshot) {
            if (step.containsParam("displayName")) {
                this.takeScreenshotTemplate().put(step.getParam("displayName"), step.withAllParam());
            }
            this.takeScreenshotTemplate().put(String.format("has no displayName#%s", this.takeScreenshotTemplate()
                    .values()
                    .stream()
                    .filter(it -> !it.containsParam("displayName"))
                    .count()), step.withAllParam());
        }
    }

    public void reloadScreenshotTemplate(final File takeScreenshotTemplate1) {
        this.takeScreenshotTemplate().clear();
        this.takeScreenshotTemplate().put("", Context.createStep("saveScreenshot").withAllParam());
        if (takeScreenshotTemplate1 != null) {
            for (final Step step : Context.load(takeScreenshotTemplate1)
                    .steps()) {
                this.addScreenshotTemplates(step);
            }
        }
    }

    public void removeScreenshotTemplate(final String displayName) {
        this.takeScreenshotTemplate().remove(displayName);
    }

    private void resetScript(final Suite aSuite, final TestCase toSelect) {
        this.suite().setValue(aSuite);
        this.selectScript(toSelect.name());
    }

    private TestCase templateScript() {
        final TestCase result = new Get().toStep().put("url", "https://www.google.com").build().toTestCase();
        if (this.getSuite() == null) {
            return result;
        }
        final long no = this.getSuite()
                .getChains()
                .flattenTestCases()
                .filter(it -> Strings.isNullOrEmpty(it.scriptFile().path()))
                .count();
        if (no > 0) {
            return result.map(it -> it.setName(result.name() + "(" + no + ")"));
        }
        return result;
    }

    private TestCase changeAssociateFile(final TestCase exportTo, final String oldPath) throws IOException {
        if (Strings.isNullOrEmpty(oldPath)) {
            return this.copyDataSourceTemplate(exportTo);
        }
        return exportTo;
    }

    private TestCase copyDataSourceTemplate(final TestCase it) throws IOException {
        if (it.dataSourceLoader().dataSourceConfig().containsKey("path")) {
            final File src = new File(this.runner().getTemplateOutputDirectory(), it.dataSourceLoader()
                    .dataSourceConfig()
                    .get("path"));
            if (src.exists()) {
                final String newDataSourceName = it.name().replace(".json", "");
                File newDataSource = new File(this.runner().getDataSourceDirectory(), newDataSourceName + ".csv");
                int suffix = 1;
                while (newDataSource.exists()) {
                    newDataSource = new File(this.runner().getDataSourceDirectory(), newDataSourceName + suffix + ".csv");
                    suffix++;
                }
                final File dest = newDataSource;
                Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return it.map(builder -> builder.addDataSourceConfig("path", dest.getName()));
            }
        }
        return it;
    }

    private InputData currentDisplayShareInput(final ReplayOption replayOption) throws IOException {
        return replayOption.reduceShareInput(this.replayShareInput()
                , this.getDisplayScriptDataSources(it -> it.include(this.getDisplayTestCase()) && !it.equals(this.getDisplayTestCase())));
    }

    private Function<Logger, TestRunListener> listener() {
        return log -> new GUITestRunListener(Context.getTestListener(log), this);
    }

    private void executeTask(final TestCase replayCase, final Function<Logger, TestRunListener> listenerFactory) {
        final SeInterpreterRunTask task = this.runner.createRunScriptTask(replayCase, this.debugger.reset(), listenerFactory);
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, wse -> executor.shutdown());
        executor.submit(task);
        this.taskHandler().accept(task);
    }

    public interface ThrowableAction {
        void execute() throws Exception;
    }

}
