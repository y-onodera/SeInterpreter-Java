package com.sebuilder.interpreter;

import com.google.common.base.Strings;
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
    private final Project project;
    private final File resultDir;
    private final XMLJUnitResultFormatter formatter;
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
        this.resultDir = Context.getInstance().getResultOutputDirectory();
        this.formatter = new XMLJUnitResultFormatter();
        this.suite = null;
        this.test = null;
        this.runTest = 0;
        this.error = 0;
        this.failed = 0;
        this.stepNo = 0;
    }

    public void cleanResult() {
        this.log.info("clean up result folder");
        // delete old result
        Delete delete = new Delete();
        delete.setProject(project);
        delete.setDir(resultDir);
        delete.execute();
        // create directory result save in
        Mkdir mkdir = new Mkdir();
        mkdir.setProject(project);
        mkdir.setDir(resultDir);
        mkdir.execute();
        // create directory screenshot output under the resultDir
        mkdir.setDir(Context.getInstance().getScreenShotOutputDirectory());
        mkdir.execute();
    }

    public boolean openTestSuite(String name, Map<String, String> aProperty) {
        String baseName = name;
        if (!Strings.isNullOrEmpty(aProperty.get(DataSource.ROW_NUMBER))) {
            baseName = name + "_rowNumber" + aProperty.get(DataSource.ROW_NUMBER);
        }
        String testName = baseName.replace("\\", ".").replace("/", ".").replaceAll("^\\.+", "");
        this.log.info("open suite:" + testName);
        this.suite = new JUnitTest();
        this.suite.setName(testName);
        this.suite.setProperties(new Hashtable<String, String>(
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

    public String testName() {
        return this.test.getName();
    }

    public String suiteName() {
        return this.suite.getName();
    }

    public void skipTestIndex(int count) {
        this.stepNo = this.stepNo + count;
    }

    public int getStepNo() {
        return this.stepNo;
    }

    public int getRunTest() {
        return this.runTest;
    }

}
