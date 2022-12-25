package com.sebuilder.interpreter.report;

import junit.framework.Test;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitVersionHelper;
import org.apache.tools.ant.taskdefs.optional.junit.XMLJUnitResultFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.lang.reflect.Field;
import java.util.Hashtable;
import java.util.Map;

public class JunitTestResultFormatter extends XMLJUnitResultFormatter {

    private String classname;

    @Override
    public void endTest(final Test test) {
        final String testDescription = this.createDescription(test);
        if (!this.getTestStarts().containsKey(testDescription)) {
            this.startTest(test);
        }

        final Element currentTest;
        if (!this.getFailedTests().containsKey(test) && !this.getSkippedTests().containsKey(testDescription) && !this.getIgnoredTests().containsKey(testDescription)) {
            currentTest = this.getDoc().createElement("testcase");
            final String n = JUnitVersionHelper.getTestCaseName(test);
            currentTest.setAttribute("name", n == null ? "unknown" : n);
            currentTest.setAttribute("classname", this.getClassname());
            this.getRootElement().appendChild(currentTest);
            this.getTestElements().put(this.createDescription(test), currentTest);
        } else {
            currentTest = this.getTestElements().get(testDescription);
        }

        final Long l = this.getTestStarts().get(this.createDescription(test));
        currentTest.setAttribute("time", Double.toString((double) (System.currentTimeMillis() - l) / 1000.0D));
        String downloadPath = "";
        String screenshotPath = "";
        String expectScreenshotPath = "";
        if (test instanceof JunitTestRunListener.ResultReportableTestCase) {
            screenshotPath = ((JunitTestRunListener.ResultReportableTestCase) test).getScreenshotPath();
            expectScreenshotPath = ((JunitTestRunListener.ResultReportableTestCase) test).getExpectScreenshotPath();
            downloadPath = ((JunitTestRunListener.ResultReportableTestCase) test).getDownloadPath();
        }
        currentTest.setAttribute("screenshot", screenshotPath);
        currentTest.setAttribute("screenshotExpect", expectScreenshotPath);
        currentTest.setAttribute("download", downloadPath);
    }

    public String getClassname() {
        return this.classname;
    }

    public void setClassname(final String classname) {
        this.classname = classname;
    }

    private String createDescription(final Test test) throws BuildException {
        return JUnitVersionHelper.getTestCaseName(test) + "(" + JUnitVersionHelper.getTestCaseClassName(test) + ")";
    }

    private Element getRootElement() {
        return this.getProperty("rootElement");
    }

    private Document getDoc() {
        return this.getProperty("doc");
    }

    private Hashtable<String, Element> getTestElements() {
        return this.getProperty("testElements");
    }

    private Map<String, Test> getIgnoredTests() {
        return this.getProperty("ignoredTests");
    }

    private Map<String, Test> getSkippedTests() {
        return this.getProperty("skippedTests");
    }

    private Map<Test, Test> getFailedTests() {
        return this.getProperty("failedTests");
    }

    private Map<String, Long> getTestStarts() {
        return this.getProperty("testStarts");
    }

    private <T> T getProperty(final String name) {
        try {
            final Field f = XMLJUnitResultFormatter.class.getDeclaredField(name);
            f.setAccessible(true);
            return (T) f.get(this);
        } catch (final IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
