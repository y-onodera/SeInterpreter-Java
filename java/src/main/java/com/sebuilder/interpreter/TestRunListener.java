package com.sebuilder.interpreter;

import org.apache.logging.log4j.Logger;

import java.io.File;

public interface TestRunListener {

    String getReportFileName();

    String getReportPrefix();

    Logger getLog();

    File getResultDir();

    File getDownloadDirectory();

    File getScreenShotOutputDirectory();

    File getTemplateOutputDirectory();

    void cleanResult();

    void cleanResult(File dest);

    void cleanDir();

    void cleanDir(File dest);

    void setUpDir(File dest);

    boolean openTestSuite(TestCase testCase, String testRunName, InputData aProperty);

    void startTest(String testName);

    void setStepIndex(int count);

    int getStepIndex();

    File addScreenshot(String file);

    File saveExpectScreenshot(File file);

    File addDownloadFile(String file);

    void addError(Throwable throwable);

    void addFailure(String message);

    void endTest();

    void closeTestSuite();

    void aggregateResult();

    void reportError(String name, Throwable e);

    void info(String s);

    void isAspectRunning(boolean aspectRunning);

    boolean isAspectRunning();

    interface Factory {
        TestRunListener create(Logger log);
    }
}
