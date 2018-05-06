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
    private XMLJUnitResultFormatter formatter = new XMLJUnitResultFormatter();
    private JUnitTest suite;
    private Test test = null;
    private int runTest = 0;
    private int error = 0;
    private int failed = 0;
    private File resultDir = new File("result");
    private Project project = new Project();

    public SeInterpreterTestListener() {
        project.setName("se-interpreter");
        project.setBaseDir(new File("."));
        project.setProperty("java.io.tmpdir", System.getProperty("java.io.tmpdir"));
    }

    public void cleanResult() {
        Delete delete = new Delete();
        delete.setProject(project);
        delete.setDir(resultDir);
        delete.execute();
        Mkdir mkdir = new Mkdir();
        mkdir.setProject(project);
        mkdir.setDir(resultDir);
        mkdir.execute();
    }

    public boolean openTestSuite(String name, Hashtable<?, ?> property) {
        suite = new JUnitTest();
        suite.setName(name.replace("\\",".").replace("/",".").replaceAll("^\\.+",""));
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
