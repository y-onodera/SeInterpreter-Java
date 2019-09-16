package com.sebuilder.interpreter;

import org.apache.logging.log4j.Logger;

import java.io.File;

public class TestRunListenerWrapper implements TestRunListener {
    private final TestRunListener delegate;

    public TestRunListenerWrapper(TestRunListener delegate) {
        this.delegate = delegate;
    }

    @Override
    public TestRunListener copy() {
        return new TestRunListenerWrapper(delegate.copy());
    }

    @Override
    public Logger getLog() {
        return delegate.getLog();
    }

    @Override
    public File getResultDir() {
        return delegate.getResultDir();
    }

    @Override
    public File getDownloadDirectory() {
        return delegate.getDownloadDirectory();
    }

    @Override
    public File getScreenShotOutputDirectory() {
        return delegate.getScreenShotOutputDirectory();
    }

    @Override
    public File getTemplateOutputDirectory() {
        return delegate.getTemplateOutputDirectory();
    }

    @Override
    public void cleanResult() {
        delegate.cleanResult();
    }

    @Override
    public void cleanResult(File dest) {
        delegate.cleanResult(dest);
    }

    @Override
    public void cleanDir() {
        delegate.cleanDir();
    }

    @Override
    public void cleanDir(File dest) {
        delegate.cleanDir(dest);
    }

    @Override
    public void setUpDir(File dest) {
        delegate.setUpDir(dest);
    }

    @Override
    public boolean openTestSuite(TestCase testCase, String testRunName, TestData aProperty) {
        return delegate.openTestSuite(testCase, testRunName, aProperty);
    }

    @Override
    public void startTest(String testName) {
        delegate.startTest(testName);
    }

    @Override
    public void skipTestIndex(int count) {
        delegate.skipTestIndex(count);
    }

    @Override
    public int getStepNo() {
        return delegate.getStepNo();
    }

    @Override
    public File addScreenshot(String file) {
        return this.delegate.addScreenshot(file);
    }

    @Override
    public void addError(Throwable throwable) {
        delegate.addError(throwable);
    }

    @Override
    public void addFailure(String message) {
        delegate.addFailure(message);
    }

    @Override
    public void endTest() {
        delegate.endTest();
    }

    @Override
    public void closeTestSuite() {
        delegate.closeTestSuite();
    }

    @Override
    public void aggregateResult() {
        delegate.aggregateResult();
    }
}
