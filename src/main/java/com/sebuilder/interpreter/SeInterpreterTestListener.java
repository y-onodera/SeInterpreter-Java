package com.sebuilder.interpreter;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.apache.logging.log4j.Logger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.Mkdir;
import org.apache.tools.ant.taskdefs.optional.junit.AggregateTransformer;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import org.apache.tools.ant.taskdefs.optional.junit.XMLJUnitResultFormatter;
import org.apache.tools.ant.taskdefs.optional.junit.XMLResultAggregator;
import org.apache.tools.ant.types.FileSet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.stream.Collectors;

public class SeInterpreterTestListener {
    private final XMLJUnitResultFormatter formatter;
    private Project project;
    private File resultDir;
    private File downloadDirectory;
    private File screenShotOutputDirectory;
    private File templateOutputDirectory;
    private JUnitTest suite;
    private TestCase test;
    private int stepNo;
    private int runTest;
    private int error;
    private int failed;
    private Logger log;

    public SeInterpreterTestListener(Logger aLog) {
        this.log = aLog;
        this.project = new Project();
        this.project.setName("se-interpreter");
        this.project.setBaseDir(new File("."));
        this.project.setProperty("java.io.tmpdir", System.getProperty("java.io.tmpdir"));
        this.formatter = new XMLJUnitResultFormatter();
        this.suite = null;
        this.test = null;
        this.runTest = 0;
        this.error = 0;
        this.failed = 0;
        this.stepNo = 0;
    }

    public void cleanResult() {
        this.cleanResult(Context.getInstance().getResultOutputDirectory());
    }

    public void cleanResult(File dest) {
        this.cleanDir(dest);
        this.setUpDir(dest);
    }

    public void cleanDir() {
        this.cleanDir(Context.getInstance().getResultOutputDirectory());
    }

    public void cleanDir(File dest) {
        this.log.info("clean up directory:" + dest.getName());
        // delete old result
        Delete delete = new Delete();
        delete.setProject(this.project);
        delete.setDir(dest);
        delete.execute();
    }

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

    public File getResultDir() {
        return resultDir;
    }

    public File getDownloadDirectory() {
        return downloadDirectory;
    }

    public File getScreenShotOutputDirectory() {
        return screenShotOutputDirectory;
    }

    public File getTemplateOutputDirectory() {
        return templateOutputDirectory;
    }

    public boolean openTestSuite(String scriptName, String testRunName, Map<String, String> aProperty) {
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

    public void startTest(String testName) {
        this.log.info("start test:" + testName);
        this.runTest++;
        this.test = new TestCase(testName) {
        };
        this.formatter.startTest(this.test);
    }

    public void skipTestIndex(int count) {
        this.stepNo = this.stepNo + count;
    }

    public int getStepNo() {
        return this.stepNo;
    }

    public void addError(Throwable throwable) {
        this.log.info("result error:" + this.test.getName());
        this.log.error(throwable);
        this.error++;
        this.formatter.addError(this.test, throwable);
    }

    public void addFailure(String message) {
        this.log.info("result failure:" + this.test.getName());
        this.log.info("cause :" + message);
        this.failed++;
        this.formatter.addFailure(this.test, new AssertionFailedError(message));
    }

    public void endTest() {
        this.log.info("result success:" + this.test.getName());
        this.formatter.endTest(this.test);
    }

    public void closeTestSuite() {
        this.log.info("close suite:" + this.suite.getName());
        this.suite.setCounts(this.runTest, this.failed, this.error);
        this.suite.setRunTime(new Date().getTime() - this.suite.getRunTime());
        this.formatter.endTestSuite(this.suite);
    }

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
        delete.addFileset(fs);
        delete.execute();
    }

}
