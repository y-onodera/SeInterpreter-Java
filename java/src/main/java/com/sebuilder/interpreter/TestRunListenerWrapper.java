package com.sebuilder.interpreter;

import org.apache.logging.log4j.Logger;

import java.io.File;

public class TestRunListenerWrapper implements TestRunListener {
    private final TestRunListener delegate;

    public TestRunListenerWrapper(final TestRunListener delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getReportFileName() {
        return this.delegate.getReportFileName();
    }

    @Override
    public Logger getLog() {
        return this.delegate.getLog();
    }

    @Override
    public String getReportPrefix() {
        return this.delegate.getReportPrefix();
    }

    @Override
    public File getResultDir() {
        return this.delegate.getResultDir();
    }

    @Override
    public File getDownloadDirectory() {
        return this.delegate.getDownloadDirectory();
    }

    @Override
    public File getScreenShotOutputDirectory() {
        return this.delegate.getScreenShotOutputDirectory();
    }

    @Override
    public File getTemplateOutputDirectory() {
        return this.delegate.getTemplateOutputDirectory();
    }

    @Override
    public void cleanResult() {
        this.delegate.cleanResult();
    }

    @Override
    public void cleanResult(final File dest) {
        this.delegate.cleanResult(dest);
    }

    @Override
    public void cleanDir() {
        this.delegate.cleanDir();
    }

    @Override
    public void cleanDir(final File dest) {
        this.delegate.cleanDir(dest);
    }

    @Override
    public void setUpDir(final File dest) {
        this.delegate.setUpDir(dest);
    }

    @Override
    public boolean openTestSuite(final TestCase testCase, final String testRunName, final InputData aProperty) {
        return this.delegate.openTestSuite(testCase, testRunName, aProperty);
    }

    @Override
    public void startTest(final String testName) {
        this.delegate.startTest(testName);
    }

    @Override
    public void skipTestIndex(final int count) {
        this.delegate.skipTestIndex(count);
    }

    @Override
    public int getStepNo() {
        return this.delegate.getStepNo();
    }

    @Override
    public File addScreenshot(final String file) {
        return this.delegate.addScreenshot(file);
    }

    @Override
    public File saveExpectScreenshot(final File file) {
        return this.delegate.saveExpectScreenshot(file);
    }

    @Override
    public File addDownloadFile(final String file) {
        return this.delegate.addDownloadFile(file);
    }

    @Override
    public void addError(final Throwable throwable) {
        this.delegate.addError(throwable);
    }

    @Override
    public void addFailure(final String message) {
        this.delegate.addFailure(message);
    }

    @Override
    public void endTest() {
        this.delegate.endTest();
    }

    @Override
    public void closeTestSuite() {
        this.delegate.closeTestSuite();
    }

    @Override
    public void aggregateResult() {
        this.delegate.aggregateResult();
    }

    @Override
    public void reportError(final String name, final Throwable e) {
        this.delegate.reportError(name, e);
    }

    @Override
    public void info(final String s) {
        this.delegate.info(s);
    }
}
