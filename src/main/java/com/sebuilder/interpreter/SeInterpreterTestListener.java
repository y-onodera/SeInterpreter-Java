package com.sebuilder.interpreter;

import java.io.File;
import java.util.Map;

public interface SeInterpreterTestListener {
    void cleanResult();

    void cleanResult(File dest);

    void cleanDir();

    void cleanDir(File dest);

    void setUpDir(File dest);

    File getResultDir();

    File getDownloadDirectory();

    File getScreenShotOutputDirectory();

    File getTemplateOutputDirectory();

    boolean openTestSuite(Script script, String testRunName, Map<String, String> aProperty);

    void startTest(String testName);

    void skipTestIndex(int count);

    int getStepNo();

    void addError(Throwable throwable);

    void addFailure(String message);

    void endTest();

    void closeTestSuite();

    void aggregateResult();
}
