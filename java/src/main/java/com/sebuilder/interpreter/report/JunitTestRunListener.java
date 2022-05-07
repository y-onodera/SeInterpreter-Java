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

    public JunitTestRunListener(Logger aLog) {
        super(aLog);
        this.formatter = new JunitTestResultFormatter();
        this.suite = null;
        this.test = null;
        this.runTest = 0;
        this.error = 0;
        this.failed = 0;
        this.info = 0;
    }

    public JunitTestRunListener(JunitTestRunListener extendFrom) {
        super(extendFrom);
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
    public boolean openTestSuite(TestCase testCase, String testRunName, InputData aProperty) {
        boolean result = super.openTestSuite(testCase, testRunName, aProperty);
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
            this.formatter.setOutput(new FileOutputStream(new File(this.resultDir, "TEST-SeBuilder-" + suite.getName() + "-result.xml")));
            this.formatter.startTestSuite(suite);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
        return result;
    }

    @Override
    public void startTest(String testName) {
        super.startTest(testName);
        this.runTest++;
        this.test = new ResultReportableTestCase(testName);
        this.formatter.setClassname(this.suite.getName().replace("_", "."));
        this.formatter.startTest(this.test);
    }

    @Override
    public File addScreenshot(String file) {
        File result = super.addScreenshot(file);
        this.test.setScreenshotPath(this.resultDir.getAbsoluteFile().toPath()
                .relativize(result.getAbsoluteFile().toPath())
                .toString());
        return result;
    }

    @Override
    public File saveExpectScreenshot(File file) {
        File result = super.saveExpectScreenshot(file);
        this.test.setExpectScreenshotPath(this.resultDir.getAbsoluteFile().toPath()
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
        this.test.setDownloadPath(downloadPath);
        return result;
    }

    @Override
    public void addError(Throwable throwable) {
        super.addError(throwable);
        this.error++;
        this.formatter.addError(this.test, throwable);
    }

    @Override
    public void addFailure(String message) {
        super.addFailure(message);
        this.failed++;
        this.formatter.addFailure(this.test, new AssertionError(message));
    }

    @Override
    public void info(String s) {
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
            new File(this.resultDir, "TEST-SeBuilder-result.xml").createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        XMLResultAggregator aggregator = new XMLResultAggregator() {
            @Override
            public AggregateTransformer createReport() {
                AggregateTransformer transformer = new AggregateTransformer(this) {
                    @Override
                    protected Resource getStylesheet() {
                        return new URLResource(this.getClass().getResource("/report/junit-noframes.xsl"));
                    }
                };
                this.transformers.add(transformer);
                return transformer;
            }

            @Override
            protected void addTestSuite(Element root, Element testsuite) {
                Element copy = (Element) DOMUtil.importNode(root, testsuite);
                copy.setAttribute("name", testsuite.getAttribute("name"));
                copy.setAttribute("id", Integer.toString(this.generatedId));
            }

        };
        aggregator.setProject(this.project);
        aggregator.setTodir(this.resultDir);
        aggregator.setTofile("TEST-SeBuilder-result.xml");
        FileSet fs = new FileSet();
        fs.setDir(this.resultDir);
        fs.createInclude().setName("TEST-SeBuilder-*-result.xml");
        aggregator.addFileSet(fs);
        AggregateTransformer transformer = aggregator.createReport();
        transformer.setTodir(this.resultDir);
        AggregateTransformer.Format noFrame = new AggregateTransformer.Format();
        noFrame.setValue(AggregateTransformer.NOFRAMES);
        transformer.setFormat(noFrame);
        aggregator.execute();
        Delete delete = new Delete();
        delete.setProject(this.project);
        delete.setFile(new File(this.resultDir, "TEST-SeBuilder-result.xml"));
        delete.execute();
    }

    static class ResultReportableTestCase extends junit.framework.TestCase {
        private String downloadPath = "";
        private String screenshotPath = "";
        private String expectScreenshotPath = "";

        public ResultReportableTestCase(String testName) {
            super(testName);
        }

        public String getScreenshotPath() {
            return screenshotPath;
        }

        public void setScreenshotPath(String screenshotPath) {
            this.screenshotPath = screenshotPath;
        }

        public String getExpectScreenshotPath() {
            return this.expectScreenshotPath;
        }

        public void setExpectScreenshotPath(String expectScreenshotPath) {
            this.expectScreenshotPath = expectScreenshotPath;
        }

        public String getDownloadPath() {
            return this.downloadPath;
        }

        public void setDownloadPath(String downloadPath) {
            this.downloadPath = downloadPath;
        }
    }

}
