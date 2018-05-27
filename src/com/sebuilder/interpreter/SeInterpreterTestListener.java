package com.sebuilder.interpreter;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
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

public class SeInterpreterTestListener {
    private final Project project;
    private final File resultDir;
    private final XMLJUnitResultFormatter formatter;
    private JUnitTest suite;
    private Test test;
    private int runTest;
    private int error;
    private int failed;

    public SeInterpreterTestListener() {
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
    }

    public void cleanResult() {
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

    public boolean openTestSuite(String name, Hashtable<?, ?> property) {
        suite = new JUnitTest();
        suite.setName(name.replace("\\", ".").replace("/", ".").replaceAll("^\\.+", ""));
        suite.setProperties(property);
        suite.setRunTime(new Date().getTime());
        test = null;
        runTest = 0;
        error = 0;
        failed = 0;
        try {
            this.formatter.setOutput(new FileOutputStream(new File(this.resultDir, "TEST-SeBuilder-" + suite.getName() + "-result.xml")));
            this.formatter.startTestSuite(suite);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
        return true;
    }

    public void startTest(String testName) {
        runTest++;
        test = new TestCase(testName) {
        };
        formatter.startTest(test);
    }

    public void addError(Throwable throwable) {
        error++;
        formatter.addFailure(test, throwable);

    }

    public void addFailure(String message) {
        failed++;
        formatter.addFailure(test, new AssertionFailedError(message));
    }

    public void endTest() {
        formatter.endTest(test);
    }

    public void closeTestSuite() {
        suite.setCounts(runTest, failed, error);
        suite.setRunTime(new Date().getTime() - suite.getRunTime());
        this.formatter.endTestSuite(suite);
    }

    public void aggregateResult() {
        try {
            new File(this.resultDir, "TEST-SeBuilder-result.xml").createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final File outputHtml = new File(resultDir, "html");
        XMLResultAggregator aggregator = new XMLResultAggregator();
        aggregator.setProject(project);
        aggregator.setTodir(resultDir);
        aggregator.setTofile("TEST-SeBuilder-result.xml");
        FileSet fs = new FileSet();
        fs.setDir(this.resultDir);
        fs.createInclude().setName("TEST-SeBuilder-*-result.xml");
        aggregator.addFileSet(fs);
        AggregateTransformer transformer = aggregator.createReport();
        transformer.setTodir(outputHtml);
        AggregateTransformer.Format noFrame = new AggregateTransformer.Format();
        noFrame.setValue(AggregateTransformer.NOFRAMES);
        transformer.setFormat(noFrame);
        aggregator.execute();
    }
}
