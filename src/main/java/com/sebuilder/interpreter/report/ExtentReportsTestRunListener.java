package com.sebuilder.interpreter.report;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.ViewName;
import freemarker.template.Configuration;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Map;
import java.util.stream.Collectors;

public class ExtentReportsTestRunListener extends TestRunListenerImpl {

    private ExtentReports reports;
    private ExtentTest extentTest;

    public ExtentReportsTestRunListener(Logger aLog) {
        super(aLog);
        this.reports = null;
    }

    public ExtentReportsTestRunListener(ExtentReportsTestRunListener extendFrom) {
        super(extendFrom);
        this.reports = extendFrom.getExtentReports();
    }

    @Override
    public ExtentReportsTestRunListener copy() {
        return new ExtentReportsTestRunListener(this);
    }

    @Override
    public String getReportFileName() {
        return "ExtentReport.html";
    }

    public ExtentReports getExtentReports() {
        return this.reports;
    }

    @Override
    public void setUpDir(File dest) {
        super.setUpDir(dest);
        this.reports = new ExtentReports();
        ExtentSparkReporter spark = new ExtentSparkReporter(new File(this.resultDir, this.getReportFileName())) {
            @Override
            protected Configuration createFreemarkerConfig(String templatePath, String encoding) {
                return super.createFreemarkerConfig("custom/" + templatePath, encoding);
            }
        }.viewConfigurer()
                .viewOrder()
                .as(new ViewName[]{
                        ViewName.DASHBOARD,
                        ViewName.CATEGORY,
                        ViewName.TEST,
                        ViewName.AUTHOR,
                        ViewName.DEVICE,
                        ViewName.EXCEPTION,
                        ViewName.LOG
                })
                .apply();
        this.reports.attachReporter(spark);
    }

    @Override
    public void startTest(String testName) {
        super.startTest(testName);
        this.extentTest = this.reports.createTest(testName)
                .assignCategory(this.suiteName);
        this.extentTest.log(Status.INFO, this.inputData.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().replace("'", "\\'")
                        , Map.Entry::getValue)
                ).toString());
    }

    @Override
    public File addScreenshot(String file) {
        File result = super.addScreenshot(file);
        this.extentTest.addScreenCaptureFromPath(this.resultDir.getAbsoluteFile().toPath()
                .relativize(result.getAbsoluteFile().toPath())
                .toString());
        return result;
    }

    @Override
    public File saveExpectScreenshot(File file) {
        File result = super.saveExpectScreenshot(file);
        this.extentTest.addScreenCaptureFromPath(this.resultDir.getAbsoluteFile().toPath()
                .relativize(result.getAbsoluteFile().toPath())
                .toString());
        return result;
    }

    @Override
    public File addDownloadFile(String file) {
        File result = super.addDownloadFile(file);
        String downloadPath = this.resultDir.getAbsoluteFile().toPath()
                .relativize(result.getAbsoluteFile().toPath())
                .toString();
        this.extentTest.log(Status.INFO, "<a href=\"" + downloadPath + "\">" + downloadPath + "</a>");
        return result;
    }

    @Override
    public void addError(Throwable throwable) {
        super.addError(throwable);
        this.extentTest.fail(throwable);
    }

    @Override
    public void addFailure(String message) {
        super.addFailure(message);
        this.extentTest.warning(message);
    }

    @Override
    public void endTest() {
        super.endTest();
        this.extentTest.pass("success:" + this.testName);
    }

    @Override
    public void aggregateResult() {
        super.aggregateResult();
        this.reports.flush();
    }
}
