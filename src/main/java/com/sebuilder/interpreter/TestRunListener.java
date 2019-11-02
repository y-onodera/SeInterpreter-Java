package com.sebuilder.interpreter;

import org.apache.logging.log4j.Logger;

import java.io.File;

public interface TestRunListener {

    String getStartTime();

    TestRunListener copy();

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

    boolean openTestSuite(TestCase testCase, String testRunName, TestData aProperty);

    void startTest(String testName);

    void skipTestIndex(int count);

    int getStepNo();

    File addScreenshot(String file);

    File saveExpectScreenshot();

    File addDownloadFile(String file);

    void addError(Throwable throwable);

    void addFailure(String message);

    void endTest();

    void closeTestSuite();

    void aggregateResult();
}
