package com.sebuilder.interpreter.report;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.ViewName;
import com.aventstack.extentreports.templating.FreemarkerTemplate;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.stream.Collectors;

public class ExtentReportsTestRunListener extends TestRunListenerImpl {

    private ExtentReports reports;

    private ExtentTest extentTest;

    public ExtentReportsTestRunListener(final Logger aLog) {
        super(aLog);
        this.reports = null;
    }

    @Override
    public String getReportFileName() {
        return "ExtentReport.html";
    }

    @Override
    public void setUpDir(final File dest) {
        super.setUpDir(dest);
        this.reports = new ExtentReports();
        final ExtentSparkReporter spark = new ExtentSparkReporter(new File(this.resultDir, this.getReportFileName())) {
            @Override
            protected Configuration createFreemarkerConfig(final String templatePath, final String encoding) {
                return super.createFreemarkerConfig("custom/" + templatePath, encoding);
            }

            @Override
            protected void processTemplate(final Template template, final File outputFile) throws TemplateException, IOException {
                final String encoding = this.getFreemarkerConfig().getDefaultEncoding();
                final FreemarkerTemplate freemarkerTemplate = new FreemarkerTemplate(this.getFreemarkerConfig()) {
                    @Override
                    public void writeTemplate(final Template template, final Map<String, Object> templateMap, final File outputFile) throws TemplateException, IOException {
                        final String source = this.getSource(template, templateMap);
                        try (final BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, Charset.forName(encoding)))) {
                            writer.write(source);
                        }
                    }
                };
                freemarkerTemplate.writeTemplate(template, this.getTemplateModel(), outputFile);
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
    public void startTest(final String testName) {
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
    public File addScreenshot(final String file) {
        final File result = super.addScreenshot(file);
        this.extentTest.addScreenCaptureFromPath(this.resultDir.getAbsoluteFile().toPath()
                .relativize(result.getAbsoluteFile().toPath())
                .toString());
        return result;
    }

    @Override
    public File saveExpectScreenshot(final File file) {
        final File result = super.saveExpectScreenshot(file);
        this.extentTest.addScreenCaptureFromPath(this.resultDir.getAbsoluteFile().toPath()
                .relativize(result.getAbsoluteFile().toPath())
                .toString());
        return result;
    }

    @Override
    public File addDownloadFile(final String file) {
        final File result = super.addDownloadFile(file);
        final String downloadPath = this.resultDir.getAbsoluteFile().toPath()
                .relativize(result.getAbsoluteFile().toPath())
                .toString();
        this.extentTest.log(Status.INFO, "<a href=\"" + downloadPath + "\">" + downloadPath + "</a>");
        return result;
    }

    @Override
    public void addError(final Throwable throwable) {
        super.addError(throwable);
        this.extentTest.fail(throwable);
        this.extentTest = null;
    }

    @Override
    public void addFailure(final String message) {
        super.addFailure(message);
        this.extentTest.warning(message);
        this.extentTest = null;
    }

    @Override
    public void info(final String s) {
        if (this.extentTest != null) {
            this.extentTest.info(s);
        } else {
            this.log.info(s);
        }
    }

    @Override
    public void endTest() {
        super.endTest();
        this.extentTest.pass("success:" + this.testName);
        this.extentTest = null;
    }

    @Override
    public void aggregateResult() {
        super.aggregateResult();
        this.reports.flush();
    }

}
