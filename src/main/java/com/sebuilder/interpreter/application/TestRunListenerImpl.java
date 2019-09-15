package com.sebuilder.interpreter.application;

import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.TestCase;
import com.sebuilder.interpreter.TestData;
import com.sebuilder.interpreter.TestRunListener;
import junit.framework.AssertionFailedError;
import org.apache.logging.log4j.Logger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.Mkdir;
import org.apache.tools.ant.taskdefs.optional.junit.AggregateTransformer;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import org.apache.tools.ant.taskdefs.optional.junit.XMLResultAggregator;
import org.apache.tools.ant.types.FileSet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;
import java.util.stream.Collectors;

public class TestRunListenerImpl implements TestRunListener {
    private final TestResultFormatter formatter;
    private Project project;
    private File resultDir;
    private File downloadDirectory;
    private File screenShotOutputDirectory;
    private File templateOutputDirectory;
    private JUnitTest suite;
    private junit.framework.TestCase test;
    private int stepNo;
    private int runTest;
    private int error;
    private int failed;
    private Logger log;

    public TestRunListenerImpl(Logger aLog) {
        this.log = aLog;
        this.project = new Project();
        this.project.setName("se-interpreter");
        this.project.setBaseDir(new File("."));
        this.project.setProperty("java.io.tmpdir", System.getProperty("java.io.tmpdir"));
        this.formatter = new TestResultFormatter();
        this.suite = null;
        this.test = null;
        this.runTest = 0;
        this.error = 0;
        this.failed = 0;
        this.stepNo = 0;
        this.resultDir = null;
        this.downloadDirectory = null;
        this.screenShotOutputDirectory = null;
        this.templateOutputDirectory = null;
    }

    public TestRunListenerImpl(TestRunListener extendFrom) {
        this.log = extendFrom.getLog();
        this.project = new Project();
        this.project.setName("se-interpreter");
        this.project.setBaseDir(new File("."));
        this.project.setProperty("java.io.tmpdir", System.getProperty("java.io.tmpdir"));
        this.formatter = new TestResultFormatter();
        this.suite = null;
        this.test = null;
        this.runTest = 0;
        this.error = 0;
        this.failed = 0;
        this.stepNo = 0;
        this.resultDir = extendFrom.getResultDir();
        this.downloadDirectory = extendFrom.getDownloadDirectory();
        this.screenShotOutputDirectory = extendFrom.getScreenShotOutputDirectory();
        this.templateOutputDirectory = extendFrom.getTemplateOutputDirectory();
    }

    @Override
    public TestRunListener copy() {
        return new TestRunListenerImpl(this);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public File getResultDir() {
        return resultDir;
    }

    @Override
    public File getDownloadDirectory() {
        return downloadDirectory;
    }

    @Override
    public File getScreenShotOutputDirectory() {
        return screenShotOutputDirectory;
    }

    @Override
    public File getTemplateOutputDirectory() {
        return templateOutputDirectory;
    }

    @Override
    public void cleanResult() {
        this.cleanResult(Context.getInstance().getResultOutputDirectory());
    }

    @Override
    public void cleanResult(File dest) {
        this.cleanDir(dest);
        this.setUpDir(dest);
    }

    @Override
    public void cleanDir() {
        this.cleanDir(Context.getInstance().getResultOutputDirectory());
    }

    @Override
    public void cleanDir(File dest) {
        this.log.info("clean up directory:" + dest.getName());
        // delete old result
        Delete delete = new Delete();
        delete.setProject(this.project);
        delete.setDir(dest);
        delete.execute();
    }

    @Override
    public void setUpDir(File dest) {
        // create directory result save in
        this.resultDir = dest;
        Mkdir mkdir = new Mkdir();
        mkdir.setProject(this.project);
        mkdir.setDir(this.resultDir);
        mkdir.execute();
        this.downloadDirectory = new File(resultDir, Context.getInstance().getDownloadDirectory()).getAbsoluteFile();
        mkdir.setDir(this.downloadDirectory);
        mkdir.execute();
        this.screenShotOutputDirectory = new File(resultDir, Context.getInstance().getScreenShotOutputDirectory()).getAbsoluteFile();
        mkdir.setDir(this.screenShotOutputDirectory);
        mkdir.execute();
        this.templateOutputDirectory = new File(resultDir, Context.getInstance().getTemplateOutputDirectory()).getAbsoluteFile();
        mkdir.setDir(this.templateOutputDirectory);
        mkdir.execute();
    }

    @Override
    public boolean openTestSuite(TestCase testCase, String testRunName, TestData aProperty) {
        String baseName = testRunName;
        String testName = baseName.replace("\\", ".").replace("/", ".").replaceAll("^\\.+", "");
        this.log.info("open suite:" + testName);
        this.suite = new JUnitTest();
        this.suite.setName(testName);
        this.suite.setProperties(new Hashtable<>(
                aProperty.entrySet()
                        .stream()
                        .collect(Collectors.toMap(
                                entry -> entry.getKey().replace("'", "\\'")
                                , entry -> entry.getValue())
                        )));
        this.suite.setRunTime(new Date().getTime());
        this.test = null;
        this.runTest = 0;
        this.error = 0;
        this.failed = 0;
        this.stepNo = 0;
        try {
            this.formatter.setOutput(new FileOutputStream(new File(this.resultDir, "TEST-SeBuilder-" + suite.getName() + "-result.xml")));
            this.formatter.startTestSuite(suite);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
        return true;
    }

    @Override
    public void startTest(String testName) {
        this.log.info("start test:" + testName);
        this.runTest++;
        this.test = new junit.framework.TestCase(testName) {
        };
        this.formatter.setClassname(this.suite.getName().replace("_", "."));
        this.formatter.startTest(this.test);
    }

    @Override
    public void skipTestIndex(int count) {
        this.stepNo = this.stepNo + count;
    }

    @Override
    public int getStepNo() {
        return this.stepNo;
    }

    @Override
    public void addError(Throwable throwable) {
        this.log.info("result error:" + this.test.getName());
        this.log.error(throwable);
        this.error++;
        this.formatter.addError(this.test, throwable);
    }

    @Override
    public void addFailure(String message) {
        this.log.info("result failure:" + this.test.getName());
        this.log.info("cause :" + message);
        this.failed++;
        this.formatter.addFailure(this.test, new AssertionFailedError(message));
    }

    @Override
    public void endTest() {
        this.log.info("result success:" + this.test.getName());
        this.formatter.endTest(this.test);
    }

    @Override
    public void closeTestSuite() {
        this.log.info("close suite:" + this.suite.getName());
        this.suite.setCounts(this.runTest, this.failed, this.error);
        this.suite.setRunTime(new Date().getTime() - this.suite.getRunTime());
        this.formatter.endTestSuite(this.suite);
    }

    @Override
    public void aggregateResult() {
        this.log.info("aggregate test result");
        try {
            new File(this.resultDir, "TEST-SeBuilder-result.xml").createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        XMLResultAggregator aggregator = new XMLResultAggregator();
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

}
