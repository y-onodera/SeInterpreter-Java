package com.sebuilder.interpreter.report;

import com.sebuilder.interpreter.*;
import org.apache.logging.log4j.Logger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.Mkdir;

import java.io.File;

public abstract class TestRunListenerImpl implements TestRunListener {

    protected final Project project;
    protected final Logger log;
    protected String reportPrefix;
    protected File resultDir;
    protected File downloadDirectory;
    protected File screenShotOutputDirectory;
    protected File templateOutputDirectory;
    protected String suiteName;
    protected InputData inputData;
    protected String testName;
    private int stepIndex;
    private boolean aspectRunning;

    public TestRunListenerImpl(final Logger aLog) {
        this.project = new Project();
        this.project.setName("se-interpreter");
        this.project.setBaseDir(new File("."));
        this.project.setProperty("java.io.tmpdir", System.getProperty("java.io.tmpdir"));
        this.log = aLog;
        this.resultDir = null;
        this.downloadDirectory = null;
        this.screenShotOutputDirectory = null;
        this.templateOutputDirectory = null;
        this.stepIndex = 0;
    }

    protected TestRunListenerImpl(final TestRunListener extendFrom) {
        this.project = new Project();
        this.project.setName("se-interpreter");
        this.project.setBaseDir(new File("."));
        this.project.setProperty("java.io.tmpdir", System.getProperty("java.io.tmpdir"));
        this.log = extendFrom.getLog();
        this.reportPrefix = extendFrom.getReportPrefix();
        this.resultDir = extendFrom.getResultDir();
        this.downloadDirectory = extendFrom.getDownloadDirectory();
        this.screenShotOutputDirectory = extendFrom.getScreenShotOutputDirectory();
        this.templateOutputDirectory = extendFrom.getTemplateOutputDirectory();
        this.stepIndex = 0;
    }

    @Override
    public Logger getLog() {
        return this.log;
    }

    @Override
    public String getReportPrefix() {
        return this.reportPrefix;
    }


    @Override
    public File getResultDir() {
        return this.resultDir;
    }

    @Override
    public File getDownloadDirectory() {
        return this.downloadDirectory;
    }

    @Override
    public File getScreenShotOutputDirectory() {
        return this.screenShotOutputDirectory;
    }

    @Override
    public File getTemplateOutputDirectory() {
        return this.templateOutputDirectory;
    }

    @Override
    public void cleanResult() {
        this.cleanResult(Context.getResultOutputDirectory());
    }

    @Override
    public void cleanResult(final File dest) {
        this.cleanDir(dest);
        this.setUpDir(dest);
    }

    @Override
    public void cleanDir() {
        this.cleanDir(Context.getResultOutputDirectory());
    }

    @Override
    public void cleanDir(final File dest) {
        this.log.info("clean up directory:" + dest.getName());
        final Delete delete = new Delete();
        delete.setProject(this.project);
        delete.setDir(dest);
        delete.execute();
    }

    @Override
    public void setUpDir(final File dest) {
        this.reportPrefix = Context.getReportPrefixValue();
        // create directory result save in
        this.resultDir = dest;
        final Mkdir mkdir = new Mkdir();
        mkdir.setProject(this.project);
        mkdir.setDir(this.resultDir);
        mkdir.execute();
        this.downloadDirectory = new File(this.resultDir, Context.getDownloadDirectory()).getAbsoluteFile();
        mkdir.setDir(this.downloadDirectory);
        mkdir.execute();
        this.screenShotOutputDirectory = new File(this.resultDir, Context.getScreenShotOutputDirectory()).getAbsoluteFile();
        mkdir.setDir(this.screenShotOutputDirectory);
        mkdir.execute();
        this.templateOutputDirectory = new File(this.resultDir, Context.getTemplateOutputDirectory()).getAbsoluteFile();
        mkdir.setDir(this.templateOutputDirectory);
        mkdir.execute();

    }

    @Override
    public boolean openTestSuite(final TestCase testCase, final String testRunName, final InputData aProperty) {
        this.suiteName = this.reportPrefix + testRunName.replace("_", ".");
        this.log.info("open suite:" + this.suiteName);
        this.stepIndex = 0;
        this.inputData = aProperty;
        return true;
    }

    @Override
    public void startTest(final String testName) {
        this.testName = testName;
        this.log.info("start test:" + testName);
    }

    @Override
    public void setStepIndex(final int index) {
        this.stepIndex = index;
    }

    @Override
    public int getStepIndex() {
        return this.stepIndex;
    }

    @Override
    public void isAspectRunning(final boolean aspectRunning) {
        this.aspectRunning = aspectRunning;
    }

    @Override
    public boolean isAspectRunning() {
        return this.aspectRunning;
    }

    @Override
    public File addScreenshot(final String file) {
        return new File(this.getScreenShotOutputDirectory(), file);
    }

    @Override
    public File saveExpectScreenshot(final File file) {
        return new File(file.getPath().replaceAll("\\.png$", "") + "_expect.png");
    }

    @Override
    public File addDownloadFile(final String file) {
        return new File(this.getDownloadDirectory(), file);
    }

    @Override
    public void addError(final Throwable throwable) {
        this.log.info("result error:" + this.testName);
        this.log.error("cause :", throwable);
    }

    @Override
    public void addFailure(final String message) {
        this.log.info("result failure:" + this.testName);
        this.log.info("cause :" + message);
    }

    @Override
    public void endTest() {
        this.log.info("result success:" + this.testName);
    }

    @Override
    public void closeTestSuite() {
        this.log.info("close suite:" + this.suiteName);
    }

    @Override
    public void aggregateResult() {
        this.log.info("aggregate test result");
    }

    @Override
    public void reportError(final String testCaseName, final Throwable toBeReport) {
        this.openTestSuite(new TestCaseBuilder().build(), testCaseName, Context.settings());
        this.startTest(testCaseName);
        this.addError(toBeReport);
        this.closeTestSuite();
    }

}
