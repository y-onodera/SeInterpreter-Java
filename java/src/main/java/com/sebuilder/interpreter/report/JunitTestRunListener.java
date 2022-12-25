package com.sebuilder.interpreter.report;

import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.TestCase;
import org.apache.logging.log4j.Logger;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.optional.junit.AggregateTransformer;
import org.apache.tools.ant.taskdefs.optional.junit.DOMUtil;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import org.apache.tools.ant.taskdefs.optional.junit.XMLResultAggregator;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.URLResource;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.stream.Collectors;

public class JunitTestRunListener extends TestRunListenerImpl {

    private final JunitTestResultFormatter formatter;
    private JUnitTest suite;
    private ResultReportableTestCase test;
    private int runTest;
    private int error;
    private int failed;
    private int info;

    public JunitTestRunListener(final Logger aLog) {
        super(aLog);
        this.formatter = new JunitTestResultFormatter();
        this.suite = null;
        this.test = null;
        this.runTest = 0;
        this.error = 0;
        this.failed = 0;
        this.info = 0;
    }

    @Override
    public String getReportFileName() {
        return "junit-noframes.html";
    }

    @Override
    public boolean openTestSuite(final TestCase testCase, final String testRunName, final InputData aProperty) {
        final boolean result = super.openTestSuite(testCase, testRunName, aProperty);
        this.suite = new JUnitTest();
        this.suite.setName(this.suiteName);
        this.suite.setRunTime(new Date().getTime());
        this.suite.setProperties(new Hashtable<>(
                aProperty.entrySet()
                        .stream()
                        .collect(Collectors.toMap(
                                entry -> entry.getKey().replace("'", "\\'")
                                , Map.Entry::getValue)
                        )));
        this.test = null;
        this.runTest = 0;
        this.error = 0;
        this.failed = 0;
        this.info = 0;
        try {
            this.formatter.setOutput(new FileOutputStream(new File(this.resultDir, "TEST-SeBuilder-" + this.suite.getName() + "-result.xml")));
            this.formatter.startTestSuite(this.suite);
        } catch (final FileNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
        return result;
    }

    @Override
    public void startTest(final String testName) {
        super.startTest(testName);
        this.runTest++;
        this.test = new ResultReportableTestCase(testName);
        this.formatter.setClassname(this.suite.getName().replace("_", "."));
        this.formatter.startTest(this.test);
    }

    @Override
    public File addScreenshot(final String file) {
        final File result = super.addScreenshot(file);
        this.test.setScreenshotPath(this.resultDir.getAbsoluteFile().toPath()
                .relativize(result.getAbsoluteFile().toPath())
                .toString());
        return result;
    }

    @Override
    public File saveExpectScreenshot(final File file) {
        final File result = super.saveExpectScreenshot(file);
        this.test.setExpectScreenshotPath(this.resultDir.getAbsoluteFile().toPath()
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
        this.test.setDownloadPath(downloadPath);
        return result;
    }

    @Override
    public void addError(final Throwable throwable) {
        super.addError(throwable);
        this.error++;
        this.formatter.addError(this.test, throwable);
    }

    @Override
    public void addFailure(final String message) {
        super.addFailure(message);
        this.failed++;
        this.formatter.addFailure(this.test, new AssertionError(message));
    }

    @Override
    public void info(final String s) {
        this.info++;
        this.formatter.setSystemOutput("_info%d:%s".formatted(this.info, s));
    }

    @Override
    public void endTest() {
        super.endTest();
        this.formatter.endTest(this.test);
    }

    @Override
    public void closeTestSuite() {
        super.closeTestSuite();
        this.suite.setCounts(this.runTest, this.failed, this.error);
        this.suite.setRunTime(new Date().getTime() - this.suite.getRunTime());
        this.formatter.endTestSuite(this.suite);
    }

    @Override
    public void aggregateResult() {
        super.aggregateResult();
        try {
            Files.createFile(new File(this.resultDir, "TEST-SeBuilder-result.xml").toPath());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        final XMLResultAggregator aggregator = new XMLResultAggregator() {
            @Override
            public AggregateTransformer createReport() {
                final AggregateTransformer transformer = new AggregateTransformer(this) {
                    @Override
                    protected Resource getStylesheet() {
                        return new URLResource(this.getClass().getResource("/report/junit-noframes.xsl"));
                    }
                };
                this.transformers.add(transformer);
                return transformer;
            }

            @Override
            protected void addTestSuite(final Element root, final Element testsuite) {
                final Element copy = (Element) DOMUtil.importNode(root, testsuite);
                copy.setAttribute("name", testsuite.getAttribute("name"));
                copy.setAttribute("id", Integer.toString(this.generatedId));
            }

        };
        aggregator.setProject(this.project);
        aggregator.setTodir(this.resultDir);
        aggregator.setTofile("TEST-SeBuilder-result.xml");
        final FileSet fs = new FileSet();
        fs.setDir(this.resultDir);
        fs.createInclude().setName("TEST-SeBuilder-*-result.xml");
        aggregator.addFileSet(fs);
        final AggregateTransformer transformer = aggregator.createReport();
        transformer.setTodir(this.resultDir);
        final AggregateTransformer.Format noFrame = new AggregateTransformer.Format();
        noFrame.setValue(AggregateTransformer.NOFRAMES);
        transformer.setFormat(noFrame);
        aggregator.execute();
        final Delete delete = new Delete();
        delete.setProject(this.project);
        delete.setFile(new File(this.resultDir, "TEST-SeBuilder-result.xml"));
        delete.execute();
    }

    static class ResultReportableTestCase extends junit.framework.TestCase {
        private String downloadPath = "";
        private String screenshotPath = "";
        private String expectScreenshotPath = "";

        public ResultReportableTestCase(final String testName) {
            super(testName);
        }

        public String getScreenshotPath() {
            return this.screenshotPath;
        }

        public void setScreenshotPath(final String screenshotPath) {
            this.screenshotPath = screenshotPath;
        }

        public String getExpectScreenshotPath() {
            return this.expectScreenshotPath;
        }

        public void setExpectScreenshotPath(final String expectScreenshotPath) {
            this.expectScreenshotPath = expectScreenshotPath;
        }

        public String getDownloadPath() {
            return this.downloadPath;
        }

        public void setDownloadPath(final String downloadPath) {
            this.downloadPath = downloadPath;
        }
    }

}
